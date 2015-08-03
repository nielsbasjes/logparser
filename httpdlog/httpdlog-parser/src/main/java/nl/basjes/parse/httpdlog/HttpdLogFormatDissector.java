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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HttpdLogFormatDissector extends Dissector {

  private static final Logger LOG = LoggerFactory.getLogger(HttpdLogFormatDissector.class);

  private List<Dissector> dissectors;
  private Dissector activeDissector;

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
    if (logFormat == null || logFormat.isEmpty()) {
      return; // Skip this one
    }
    Dissector newDissector = new ApacheHttpdLogFormatDissector(logFormat);
    dissectors.add(newDissector);
    if (activeDissector == null) {
      activeDissector = newDissector;
    }
  }

  @Override
  public boolean initializeFromSettingsParameter(String multiLineLogFormat) {
    addMultipleLogFormats(multiLineLogFormat);
    return true;
  }

  @Override
  public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
    if (activeDissector == null) {
      throw new DissectionFailure("We need one or more logformats before we can dissect.");
    }

    try {
      activeDissector.dissect(parsable, inputname);
      return;
    }
    catch (DissectionFailure df) {
      int index = 0;
      for (Dissector dissector : dissectors) {
        try {
          dissector.dissect(parsable, inputname);
          activeDissector = dissector;
          return;
        } catch (DissectionFailure e) {
          index ++;
          // We ignore the error and try the next one.
        }
      }
      throw df;
    }
  }

  @Override
  public String getInputType() {
    if (activeDissector == null) {
      return null;
    }

    // FIXME: Assert that all dissectors use the same input type!!
    return dissectors.get(0).getInputType(); // We can only return one .. so we pick the first
  }

  @Override
  public List<String> getPossibleOutput() {
    if (activeDissector == null) {
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
    if (activeDissector == null) {
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
    if (activeDissector == null) {
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
    if (activeDissector == null) {
      return;
    }

    if (newInstance instanceof HttpdLogFormatDissector) {
      ((HttpdLogFormatDissector) newInstance).addLogFormat(getAllLogFormats());
    } else {
      LOG.error("============================== WTF == " + newInstance.getClass().getCanonicalName());
    }

  }
}
