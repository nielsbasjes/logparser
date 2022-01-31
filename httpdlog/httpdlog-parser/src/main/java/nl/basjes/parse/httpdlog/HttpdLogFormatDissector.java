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

package nl.basjes.parse.httpdlog;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenFormatDissector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.basjes.parse.core.Casts.NO_CASTS;

public class HttpdLogFormatDissector extends Dissector {

    private static final Logger LOG = LoggerFactory.getLogger(HttpdLogFormatDissector.class);

    // This value MUST be the same for all formats this dissector can wrap
    public static final String INPUT_TYPE = "HTTPLOGLINE";

    private final List<String> registeredLogFormats;
    private final List<TokenFormatDissector> dissectors;
    private TokenFormatDissector activeDissector;

    public HttpdLogFormatDissector() {
        registeredLogFormats = new ArrayList<>(16);
        dissectors = new ArrayList<>(16);
        activeDissector = null;
    }

    public HttpdLogFormatDissector(final String multiLineLogFormat) {
        this();
        addMultipleLogFormats(multiLineLogFormat);

        if (enableJettyFix) {
            addAdditionalLogFormatsToHandleJettyUseragentProblem();
        }
    }

    // Jetty has two (historical) problems:
    // 1) An empty user field was logged in the past as " - " instead of "-"
    //    See: https://github.com/eclipse/jetty.project/commit/2332b4f
    // 2) An empty useragent field was logged with an extra trailing space.
    // This boolean enables a hack to parse these files.
    private boolean enableJettyFix = false;

    private void addAdditionalLogFormatsToHandleJettyUseragentProblem() {
        for (String logFormat : getAllLogFormats()) {
            if (logFormat.contains("\"%{User-Agent}i\"")) {
                LOG.info("Creating extra logformat to handle Jetty useragent problem.");
                String patchedLogFormat = logFormat.replace("\"%{User-Agent}i\"", "\"%{User-Agent}i\" ");
                addLogFormat(patchedLogFormat);
            }
        }

        // Jetty has suffered from this historical problem:
        // An empty user field was logged in the past as " - " instead of "-"
        // See: https://github.com/eclipse/jetty.project/commit/2332b4f
        for (String logFormat : getAllLogFormats()) {
            if (logFormat.contains("%u")) {
                LOG.info("Creating extra logformat to handle Jetty userfield problem.");
                String patchedLogFormat = logFormat.replace("%u", " %u ");
                addLogFormat(patchedLogFormat);
            }
        }
    }

    public HttpdLogFormatDissector enableJettyFix() {
        enableJettyFix = true;
        return this;
    }

    public HttpdLogFormatDissector addMultipleLogFormats(final String multiLineLogFormat) {
        return addLogFormat(Arrays.asList(multiLineLogFormat.split("\\r?\\n")));
    }

    public HttpdLogFormatDissector addLogFormat(final List<String> logFormats) {
        for (String logFormat : logFormats) {
            addLogFormat(logFormat);
        }
        return this;
    }

    public HttpdLogFormatDissector addLogFormat(final String logFormat) {
        if (logFormat == null || logFormat.trim().isEmpty()) {
            return this; // Skip this one
        }

        if (logFormat.toUpperCase().trim().equals("ENABLE JETTY FIX")) {
            return enableJettyFix();
        }

        if (registeredLogFormats.contains(logFormat)) {
            LOG.info("Skipping duplicate LogFormat: >>{}<<", logFormat);
            return this; // We already have this one
        }

        registeredLogFormats.add(logFormat);

        switch (determineMostLikelyLogFormat(logFormat)) {
            case APACHE:
                LOG.info("Registering APACHE HTTPD LogFormat[{}]= >>{}<<", dissectors.size(), logFormat);
                dissectors.add(new ApacheHttpdLogFormatDissector(logFormat));
                break;
            case NGINX:
                LOG.info("Registering NGINX LogFormat[{}]= >>{}<<", dissectors.size(), logFormat);
                dissectors.add(new NginxHttpdLogFormatDissector(logFormat));
                break;
            default:
                LOG.error("Unable to determine if this is an APACHE or a NGINX LogFormat= >>{}<<", logFormat);
                break;
        }
        return this;
    }

    private enum LogFormatType {
        APACHE,
        NGINX,
        UNKNOWN
    }

    private LogFormatType determineMostLikelyLogFormat(final String logFormat) {
        if (ApacheHttpdLogFormatDissector.looksLikeApacheFormat(logFormat)) {
            return LogFormatType.APACHE;
        }
        if (NginxHttpdLogFormatDissector.looksLikeNginxFormat(logFormat)) {
            return LogFormatType.NGINX;
        }
        // We do not know
        return LogFormatType.UNKNOWN;
    }


    @Override
    public boolean initializeFromSettingsParameter(String multiLineLogFormat) {
        addMultipleLogFormats(multiLineLogFormat);
        return true;
    }

    @Override
    public <RECORD> void createAdditionalDissectors(Parser<RECORD> parser) {
        for (Dissector dissector : dissectors) {
            dissector.createAdditionalDissectors(parser);
        }
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        if (dissectors.isEmpty()) {
            throw new DissectionFailure("We need one or more logformats before we can dissect.");
        }

        // Initial: We must determine the right dissector
        if (activeDissector == null) {
            activeDissector = dissectors.get(0);
            LOG.info("At start we use LogFormat[0]= >>{}<<", activeDissector.getLogFormat());
        }

        try {
            activeDissector.dissect(parsable, inputname);
        } catch (DissectionFailure df) {
            if (dissectors.size() > 1) {
                int index = 0;
                for (TokenFormatDissector dissector : dissectors) {
                    try {
                        dissector.dissect(parsable, inputname);
                        LOG.info("Switched to LogFormat[{}]= >>{}<<", index, activeDissector.getLogFormat());
                        activeDissector = dissector;
                        return;
                    } catch (DissectionFailure e) {
                        index++;
                        // We ignore the error and try the next one.
                    }
                }
            }
            throw df;
        }
    }

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    @Override
    public List<String> getPossibleOutput() {
        if (dissectors.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> result = new HashSet<>(32); // Go via a Set to deduplicate the fields
        for (Dissector dissector : dissectors) {
            result.addAll(dissector.getPossibleOutput());
        }

        return new ArrayList<>(result);
    }

    @Override
    public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
        if (dissectors.isEmpty()) {
            return NO_CASTS;
        }

        EnumSet<Casts> result = EnumSet.noneOf(Casts.class); // Start empty
        for (Dissector dissector : dissectors) {
            result.addAll(dissector.prepareForDissect(inputname, outputname));
        }
        return result;
    }

    @Override
    public void prepareForRun() throws InvalidDissectorException {
        if (dissectors.isEmpty()) {
            throw new InvalidDissectorException("Cannot run without logformats");
        }

        for (Dissector dissector : dissectors) {
            if (!INPUT_TYPE.equals(dissector.getInputType())) {
                throw new InvalidDissectorException("All dissectors controlled by " + this.getClass().getCanonicalName()
                    + " MUST have \"" + INPUT_TYPE + "\" as their inputtype.");
            }
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
        if (dissectors.isEmpty()) {
            return;
        }

        if (newInstance instanceof HttpdLogFormatDissector) {
            ((HttpdLogFormatDissector) newInstance).addLogFormat(getAllLogFormats());

            if (enableJettyFix) {
                ((HttpdLogFormatDissector) newInstance).enableJettyFix();
            }

        } else {
            LOG.error("============================== WTF == {}", newInstance.getClass().getCanonicalName());
        }

    }
}
