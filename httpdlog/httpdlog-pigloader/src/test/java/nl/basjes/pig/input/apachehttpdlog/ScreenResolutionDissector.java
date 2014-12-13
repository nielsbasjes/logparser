package nl.basjes.pig.input.apachehttpdlog;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ScreenResolutionDissector extends Dissector {

  public static final String SCREENRESOLUTION = "SCREENRESOLUTION";
  private String separator;
  private boolean wantWidth = false;
  private boolean wantHeight = false;

  @Override
  public boolean initializeFromSettingsParameter(String settings) {
    this.separator = settings;
    return true;
  }

  @Override
  public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
    final ParsedField field = parsable.getParsableField(SCREENRESOLUTION, inputname);

    final String fieldValue = field.getValue();
    if (fieldValue == null || fieldValue.isEmpty()) {
      return; // Nothing to do here
    }

    if (fieldValue.contains("x")) {
      String[] parts = fieldValue.split("x");
      if (wantWidth) {
        parsable.addDissection(inputname, "SCREENWIDTH", "width", parts[0]);
      }
      if (wantHeight) {
        parsable.addDissection(inputname, "SCREENHEIGHT", "height", parts[1]);
      }
    }
  }

  @Override
  public String getInputType() {
    return SCREENRESOLUTION;
  }

  @Override
  public List<String> getPossibleOutput() {
    List<String> result = new ArrayList<>();
    result.add("SCREENWIDTH:width");
    result.add("SCREENHEIGHT:height");
    return result;
  }

  @Override
  public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
    String name = outputname.substring(inputname.length() + 1);
    if ("width".equals(name)) {
      wantWidth = true;
      return Casts.STRING_OR_LONG;
    }
    if ("height".equals(name)) {
      wantHeight = true;
      return Casts.STRING_OR_LONG;
    }
    return null;
  }

  @Override
  public void prepareForRun() throws InvalidDissectorException {
    // Nothing to do
  }

  @Override
  protected void initializeNewInstance(Dissector newInstance) {
    // Nothing to do
  }
}
