package nl.basjes.pig.input.apachehttpdlog;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Disector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DisectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDisectorException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ScreenResolutionDisector extends Disector {

  public static final String SCREENRESOLUTION = "SCREENRESOLUTION";
  private String separator;
  private boolean wantWidth = false;
  private boolean wantHeight = false;

  public ScreenResolutionDisector() {
    this("x");
  }

  public ScreenResolutionDisector(String separator) {
    this.separator = separator;
  }

  @Override
  public void disect(Parsable<?> parsable, String inputname) throws DisectionFailure {
    final ParsedField field = parsable.getParsableField(SCREENRESOLUTION, inputname);

    final String fieldValue = field.getValue();
    if (fieldValue == null || fieldValue.isEmpty()) {
      return; // Nothing to do here
    }

    if (fieldValue.contains("x")) {
      String[] parts = fieldValue.split("x");
      if (wantWidth) {
        parsable.addDisection(inputname, "SCREENWIDTH", "width", parts[0]);
      }
      if (wantHeight) {
        parsable.addDisection(inputname, "SCREENHEIGHT", "height", parts[1]);
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
  public EnumSet<Casts> prepareForDisect(String inputname, String outputname) {
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
  public void prepareForRun() throws InvalidDisectorException {
    // Nothing to do
  }

  @Override
  protected void initializeNewInstance(Disector newInstance) {
    // Nothing to do
  }
}
