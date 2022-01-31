/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2021 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static nl.basjes.parse.core.Casts.NO_CASTS;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG;

/**
 * The documentation of mod_unique_id clearly states:
 * http://httpd.apache.org/docs/current/mod/mod_unique_id.html
 * ... it should be emphasized that applications should not dissect the encoding. ...
 * Applications should treat the entire encoded UNIQUE_ID as an opaque token,
 * which can be compared against other UNIQUE_IDs for equality only.
 *
 * Yet being able to peek inside is sometimes very useful...
 */
public class ModUniqueIdDissector extends Dissector {
    // --------------------------------------------

    private static final String INPUT_TYPE = "MOD_UNIQUE_ID";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();

        result.add("TIME.EPOCH:epoch");
        result.add("IP:ip");
        result.add("PROCESSID:processid");
        result.add("COUNTER:counter");
        result.add("THREAD_INDEX:threadindex");
        return result;
    }

    // --------------------------------------------

    private boolean wantTime = false;
    private boolean wantIp = false;
    private boolean wantProcessId = false;
    private boolean wantCounter = false;
    private boolean wantThreadIndex = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = extractFieldName(inputname, outputname);
        if ("epoch".equals(name)) {
            wantTime = true;
            return STRING_OR_LONG;
        }
        if ("ip".equals(name)) {
            wantIp = true;
            return STRING_OR_LONG;
        }
        if ("processid".equals(name)) {
            wantProcessId = true;
            return STRING_OR_LONG;
        }
        if ("counter".equals(name)) {
            wantCounter = true;
            return STRING_OR_LONG;
        }
        if ("threadindex".equals(name)) {
            wantThreadIndex = true;
            return STRING_OR_LONG;
        }
        return NO_CASTS;
    }

    // --------------------------------------------

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        String fieldValue = field.getValue().getString();
        if (fieldValue == null || fieldValue.isEmpty()) {
            return; // Nothing to do here
        }

        UniqueIdRec record = decode(fieldValue);
        if (record == null) {
            return;
        }

        if (wantTime) {
            parsable.addDissection(inputname, "TIME.EPOCH",   "epoch",       record.timestamp);
        }
        if (wantIp) {
            parsable.addDissection(inputname, "IP",           "ip",          record.ipaddrStr);
        }
        if (wantProcessId) {
            parsable.addDissection(inputname, "PROCESSID",    "processid",   record.pid);
        }
        if (wantCounter) {
            parsable.addDissection(inputname, "COUNTER",      "counter",     record.counter);
        }
        if (wantThreadIndex) {
            parsable.addDissection(inputname, "THREAD_INDEX", "threadindex", record.threadIndex);
        }
    }
    // --------------------------------------------

    private static class UniqueIdRec {
        long timestamp;
        long ipaddr;
        String ipaddrStr;
        long pid;
        long counter;
        long threadIndex;
    }

    // 1 letter = 6 bits of data = 2^6 = 64 letters needed to do the mapping
    // 4 letters = 4*6 = 24 = 3*8 = 3 bytes
    // So 24 letters = 24*6 = 144 bits = 18 bytes
    public static final Charset CHARSET_UTF_8 = StandardCharsets.UTF_8;

    private static final byte[] EMPTY = {};

    private byte[] decodeToBytes(String modUniqueIdString) {
        if (modUniqueIdString.length() != 24) {
            return EMPTY;
        }

        // http://httpd.apache.org/docs/current/mod/mod_unique_id.html
        // The UNIQUE_ID environment variable is constructed by encoding the 144-bit
        // (32-bit IP address, 32 bit pid, 32 bit time stamp, 16 bit counter, 32 bit thread index)
        // quadruple using the alphabet [A-Za-z0-9@-] in a manner similar to MIME base64 encoding,
        // producing 24 characters.

        // This implementation is based on the observation that the encoding used by mod-unique-id is
        // the same as Base64 except that the last two letters are different.
        // So by simply replacing the occurrences of these letters in the source we reuse and existing
        // Base64 decode implementation.

        byte[] modUniqueIdBytes = modUniqueIdString.getBytes(CHARSET_UTF_8);

        byte[] modUniqueIdBase64Bytes = new byte[modUniqueIdBytes.length];

        for (int i = 0; i < modUniqueIdBytes.length; i++) {
            byte nextByte = modUniqueIdBytes[i];
            switch (nextByte) {
                case '+':
                case '/':
                    modUniqueIdBase64Bytes[i] = '@';
                    break;

                default:
                    modUniqueIdBase64Bytes[i] = nextByte;
                    break;
            }
        }

        try {
            return Base64.decodeBase64(modUniqueIdBase64Bytes);
        } catch (IllegalArgumentException iae) {
            return EMPTY;
        }
    }

    private UniqueIdRec decode(String modUniqueIdString) {
        byte[] bytes = decodeToBytes(modUniqueIdString);
        if (bytes == EMPTY) {
            return null;
        }

        // Is the decoded output the right length?
        if (bytes.length != 18) {
            return null;
        }

        UniqueIdRec result = new UniqueIdRec();

//        http://httpd.apache.org/docs/current/mod/mod_unique_id.html

//         we will use a Unix timestamp (seconds since January 1, 1970 UTC)
//      (32-bit IP address, 32 bit pid, 32 bit time stamp, 16 bit counter, 32 bit thread index)
//      The actual ordering of the encoding is: time stamp, IP address, pid, counter.

        result.timestamp    =                               (bytes[0] & 0xFF);
        result.timestamp    = (result.timestamp    * 256) + (bytes[1] & 0xFF);
        result.timestamp    = (result.timestamp    * 256) + (bytes[2] & 0xFF);
        result.timestamp    = (result.timestamp    * 256) + (bytes[3] & 0xFF);
        // Quote: The timestamp has only one second granularity
        result.timestamp   *= 1000; // This is to convert the time into milliseconds

        // NOTE: In case of IPv6 the value will be related to the lower bits of the address.
        result.ipaddr       =                               (bytes[4] & 0xFF);
        result.ipaddr       = (result.ipaddr       * 256) + (bytes[5] & 0xFF);
        result.ipaddr       = (result.ipaddr       * 256) + (bytes[6] & 0xFF);
        result.ipaddr       = (result.ipaddr       * 256) + (bytes[7] & 0xFF);
        result.ipaddrStr    = ""  + (bytes[4] & 0xFF) +
                              '.' + (bytes[5] & 0xFF) +
                              '.' + (bytes[6] & 0xFF) +
                              '.' + (bytes[7] & 0xFF);

        result.pid          =                                (bytes[8] & 0xFF);
        result.pid          = (result.pid          * 256) +  (bytes[9] & 0xFF);
        result.pid          = (result.pid          * 256) +  (bytes[10] & 0xFF);
        result.pid          = (result.pid          * 256) +  (bytes[11] & 0xFF);

        result.counter      =                                (bytes[12] & 0xFF);
        result.counter      = (result.counter      * 256) +  (bytes[13] & 0xFF);

        result.threadIndex  =                                (bytes[14] & 0xFF);
        result.threadIndex  = (result.threadIndex  * 256) +  (bytes[15] & 0xFF);
        result.threadIndex  = (result.threadIndex  * 256) +  (bytes[16] & 0xFF);
        result.threadIndex  = (result.threadIndex  * 256) +  (bytes[17] & 0xFF);

        return result;
    }
}
