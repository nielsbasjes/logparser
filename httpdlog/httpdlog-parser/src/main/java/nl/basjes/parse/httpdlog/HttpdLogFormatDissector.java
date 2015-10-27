/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
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

package nl.basjes.parse.httpdlog;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenFormatDissector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HttpdLogFormatDissector extends Dissector {

    private static final Logger LOG = LoggerFactory.getLogger(HttpdLogFormatDissector.class);

    // This value MUST be the same for all formats this dissector can wrap
    public static final String INPUT_TYPE = "HTTPLOGLINE";

    private List<TokenFormatDissector> dissectors;
    private TokenFormatDissector activeDissector;

    public HttpdLogFormatDissector() {
        dissectors = new ArrayList<>(16);
        activeDissector = null;
    }

    public HttpdLogFormatDissector(final String multiLineLogFormat) {
        this();
        addMultipleLogFormats(multiLineLogFormat);
    }


    public void addMultipleLogFormats(final String multiLineLogFormat) {
        for (String logFormat : multiLineLogFormat.split("\\r?\\n")) {
            addLogFormat(logFormat);
        }
    }

    public void addLogFormat(final List<String> logFormats) {
        for (String logFormat : logFormats) {
            addLogFormat(logFormat);
        }
    }

    public void addLogFormat(final String logFormat) {
        if (logFormat == null || logFormat.trim().isEmpty()) {
            return; // Skip this one
        }

        switch (determineMostLikelyLogFormat(logFormat)) {
            case APACHE:
                LOG.info("Registering APACHE HTTPD LogFormat[" + dissectors.size() + "]= >>" + logFormat + "<<");
                dissectors.add(new ApacheHttpdLogFormatDissector(logFormat));
                break;
            case NGINX:
                LOG.info("Registering NGINX LogFormat[" + dissectors.size() + "]= >>" + logFormat + "<<");
                dissectors.add(new NginxHttpdLogFormatDissector(logFormat));
                break;
            default:
                LOG.error("Unable to determine if this is an APACHE or a NGINX LogFormat= >>" + logFormat + "<<");
                break;
        }
    }

    private enum LogFormatType {
        APACHE,
        NGINX
    }

    // TODO: Actually implement pattern matching (OR make it explicit...).
    private LogFormatType determineMostLikelyLogFormat(final String logFormat) {
//    if (logFormat.indexOf('%') != -1) {
        return LogFormatType.APACHE;
//    }
//    if (logFormat.indexOf('$') != -1) {
//      return LogFormatType.NGINX;
//    }
//    // We do not know
//    return null;
    }


    @Override
    public boolean initializeFromSettingsParameter(String multiLineLogFormat) {
        addMultipleLogFormats(multiLineLogFormat);
        return true;
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        if (dissectors.isEmpty()) {
            throw new DissectionFailure("We need one or more logformats before we can dissect.");
        }

        // Initial: We must determine the right dissector
        if (activeDissector == null) {
            activeDissector = dissectors.get(0);
            LOG.info("At start we use LogFormat[0]= >>" + activeDissector.getLogFormat() + "<<");
        }

        try {
            activeDissector.dissect(parsable, inputname);
        } catch (DissectionFailure df) {
            int index = 0;
            for (TokenFormatDissector dissector : dissectors) {
                try {
                    dissector.dissect(parsable, inputname);
                    LOG.info("Switched to LogFormat[" + index + "]= >>" + activeDissector.getLogFormat() + "<<");
                    activeDissector = dissector;
                    return;
                } catch (DissectionFailure e) {
                    index++;
                    // We ignore the error and try the next one.
                }
            }
            throw df;
        }
    }

    @Override
    public String getInputType() {
        // FIXME: Assert that all dissectors use the same input type!!
        return INPUT_TYPE;
    }

    @Override
    public List<String> getPossibleOutput() {
        if (dissectors.size() == 0) {
            return null;
        }

        Set<String> result = new HashSet<>(32); // Go via a Set to deduplicate the fields
        for (Dissector dissector : dissectors) {
            result.addAll(dissector.getPossibleOutput());
        }

        return new ArrayList<>(result);
    }

    @Override
    public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
        if (dissectors.size() == 0) {
            return null;
        }

        EnumSet<Casts> result = EnumSet.noneOf(Casts.class); // Start empty
        for (Dissector dissector : dissectors) {
            result.addAll(dissector.prepareForDissect(inputname, outputname));
        }
        return result;
    }

    @Override
    public void prepareForRun() throws InvalidDissectorException {
        if (dissectors.size() == 0) {
            throw new InvalidDissectorException("Cannot run without logformats");
        }

        for (Dissector dissector : dissectors) {
            dissector.prepareForRun();
        }
    }


    private List<String> getAllLogFormats() {
        List<String> result = new ArrayList<>(dissectors.size());
        for (Dissector dissector : dissectors) {
            if (dissector instanceof TokenFormatDissector) {
                result.add(((TokenFormatDissector) dissector).getLogFormat());
            }
        }
        return result;
    }

    @Override
    protected void initializeNewInstance(Dissector newInstance) {
        if (dissectors.size() == 0) {
            return;
        }

        if (newInstance instanceof HttpdLogFormatDissector) {
            ((HttpdLogFormatDissector) newInstance).addLogFormat(getAllLogFormats());
        } else {
            LOG.error("============================== WTF == " + newInstance.getClass().getCanonicalName());
        }

    }
}
