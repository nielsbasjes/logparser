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
package nl.basjes.parse.httpdlog.dissectors.tokenformat;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Token implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(Token.class);

    private final List<TokenOutputField> outputFields = new ArrayList<>();
    private final String regex;
    private final int startPos;
    private final int length;
    private final int prio;
    protected String warningMessageWhenUsed = null;

    // In some cases a token needs a custom dissector.
    private Dissector customDissector = null;

    public Token(
        final String nRegex,
        final int nStartPos,
        final int nLength,
        final int nPrio) {
        regex = nRegex;
        startPos = nStartPos;
        length = nLength;
        prio = nPrio;
    }

    public Token addOutputField(String type, String name, EnumSet<Casts> casts) {
        outputFields.add(new TokenOutputField(type, name, casts));
        return this;
    }

    public Token addOutputFields(List<TokenOutputField> nOutputFields) {
        this.outputFields.addAll(nOutputFields);
        return this;
    }

    public List<TokenOutputField> getOutputFields() {
        return outputFields;
    }

    public boolean canProduceADesiredFieldName(Set<String> desiredNames) {
        for (TokenOutputField tokenOutputField: outputFields) {
            if (desiredNames.contains(tokenOutputField.getName())) {
                return true;
            }
        }
        return false;
    }

    public void setCustomDissector(Dissector dissector) {
        customDissector = dissector;
    }

    public Dissector getCustomDissector() {
        return customDissector;
    }

    public String getRegex() {
        return regex;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getLength() {
        return length;
    }


    public int getPrio() {
        return prio;
    }

    public void setWarningMessageWhenUsed(String message) {
        warningMessageWhenUsed = message;
    }

    public void tokenWasUsed() {
        if (warningMessageWhenUsed != null) {
            LOG.warn("------------------------------------------------------------------------");
            LOG.warn(warningMessageWhenUsed, outputFields);
            LOG.warn("------------------------------------------------------------------------");
        }
    }

    // This is used by your favorite debugger.
    @Override
    public String toString() {
        return "{" + outputFields + " (" + startPos + "+" + length + ");Prio=" + prio + "}";
    }

}
