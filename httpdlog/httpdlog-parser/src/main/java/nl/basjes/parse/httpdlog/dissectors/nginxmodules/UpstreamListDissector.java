/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2018 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.httpdlog.dissectors.nginxmodules;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static nl.basjes.parse.core.Casts.NO_CASTS;

// Implement the tokens described here:
// http://nginx.org/en/docs/http/ngx_http_upstream_module.html#variables

// $upstream_addr
// keeps the IP address and port, or the path to the UNIX-domain socket of the upstream server.
// If several servers were contacted during request processing, their addresses are separated by commas,
// e.g. “192.168.1.1:80, 192.168.1.2:80, unix:/tmp/sock”.
//
// If an internal redirect from one server group to another happens, initiated by “X-Accel-Redirect”
// or error_page, then the server addresses from different groups are separated by colons,
// e.g. “192.168.1.1:80, 192.168.1.2:80, unix:/tmp/sock : 192.168.10.1:80, 192.168.10.2:80”.
//
// If a server cannot be selected, the variable keeps the name of the server group.

// This dissector tries to pick apart this list format.

public class UpstreamListDissector extends Dissector {

    private static final String OUTPUT_ORIGINAL_NAME   = ".value";
    private static final String OUTPUT_REDIRECTED_NAME = ".redirected";

    private String          inputType;
    private String          outputOriginalType;
    private EnumSet<Casts>  outputOriginalCasts;
    private String          outputRedirectedType;
    private EnumSet<Casts>  outputRedirectedCasts;

    public UpstreamListDissector() {
        inputType                   = null;
        outputOriginalType          = null;
        outputOriginalCasts         = null;
        outputRedirectedType        = null;
        outputRedirectedCasts       = null;
    }

    public UpstreamListDissector(String inputType,
                                 String outputOriginalType, EnumSet<Casts> outputOriginalCasts,
                                 String outputRedirectedType, EnumSet<Casts> outputRedirectedCasts) {
        this.inputType = inputType;
        this.outputOriginalType     = outputOriginalType;
        this.outputOriginalCasts    = outputOriginalCasts;
        this.outputRedirectedType   = outputRedirectedType;
        this.outputRedirectedCasts  = outputRedirectedCasts;
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(inputType, inputname);

        String fieldValue = field.getValue().getString();

        String[] servers = fieldValue.split(", ");
        int serverNr = 0;
        for (String server: servers) {
            String[] parts = server.split(": ");
            if (parts.length == 1) {
                parsable.addDissection(inputname,
                                       outputOriginalType,
                                       serverNr + OUTPUT_ORIGINAL_NAME,
                                       parts[0].trim());
                parsable.addDissection(inputname,
                                       outputRedirectedType,
                                       serverNr + OUTPUT_REDIRECTED_NAME,
                                       parts[0].trim());
            } else {
                parsable.addDissection(inputname,
                                       outputOriginalType,
                                       serverNr + OUTPUT_ORIGINAL_NAME,
                                       parts[0].trim());
                parsable.addDissection(inputname,
                                       outputRedirectedType,
                                       serverNr + OUTPUT_REDIRECTED_NAME,
                                       parts[1].trim());
            }
            serverNr++;
        }
    }

    @Override
    public String getInputType() {
        return inputType;
    }

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < 32; i++){
            result.add(outputOriginalType   + ":" + i + OUTPUT_ORIGINAL_NAME);
            result.add(outputRedirectedType + ":" + i + OUTPUT_REDIRECTED_NAME);
        }
        return result;
    }

    @Override
    public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
        String name = extractFieldName(inputname, outputname);

        if (name.endsWith(OUTPUT_ORIGINAL_NAME)) {
            return outputOriginalCasts;
        }
        if (name.endsWith(OUTPUT_REDIRECTED_NAME)) {
            return outputRedirectedCasts;
        }
        return NO_CASTS;
    }

    @Override
    protected void initializeNewInstance(Dissector newInstance) throws InvalidDissectorException {
        if (!(newInstance instanceof UpstreamListDissector)) {
            throw new InvalidDissectorException(
                "Called UpstreamListDissector::initializeNewInstance with a dissector of class " + newInstance.getClass().getCanonicalName());
        }
        UpstreamListDissector dissector  = (UpstreamListDissector)newInstance;
        dissector.inputType              = inputType;
        dissector.outputOriginalType     = outputOriginalType;
        dissector.outputOriginalCasts    = outputOriginalCasts;
        dissector.outputRedirectedType   = outputRedirectedType;
        dissector.outputRedirectedCasts  = outputRedirectedCasts;
    }
}
