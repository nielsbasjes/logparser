package nl.basjes.parse.core.reference;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.nl.basjes.parse.core.test.DissectorTester;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * These tests validate what happens if two dissectors want the SAME input to work on
 */
public class ReferenceTestDouble {

    @Test
    public void verifyRemap() {
        DissectorTester.create()
            .withDissector(new RemapInputDissector())
            .withInput("Doesn't matter")
            .expect("INPUT:",  "42")
            .checkExpectations();
    }


    @Test
    public void verifyFooInput() {
        DissectorTester.create()
            .withDissector(new FooInputDissector())
            .withInput("Doesn't matter")
            .expect("STRING:foostring",  "42")
            .expect("STRING:foostring",  42L)
            .expect("STRING:foostring",  42D)
            .expect("LONG:foolong",      "42")
            .expect("LONG:foolong",      42L)
            .expect("LONG:foolong",      42D)
            .expect("DOUBLE:foodouble",  "42.0")
            .expect("DOUBLE:foodouble",  42L)
            .expect("DOUBLE:foodouble",  42D)
            .verbose()
            .checkExpectations();
    }

    @Test
    public void verifyBarInput() {
        DissectorTester.create()
            .withDissector(new BarInputDissector())
            .withInput("Doesn't matter")
            .expect("STRING:barstring",  "42")
            .expect("STRING:barstring",  42L)
            .expect("STRING:barstring",  42D)
            .expect("LONG:barlong",      "42")
            .expect("LONG:barlong",      42L)
            .expect("LONG:barlong",      42D)
            .expect("DOUBLE:bardouble",  "42.0")
            .expect("DOUBLE:bardouble",  42L)
            .expect("DOUBLE:bardouble",  42D)
            .verbose()
            .checkExpectations();
    }

    @Test
    public void runDoubleDissectors(){
        DissectorTester.create()
            .verbose()

            .withDissector(new InputCreatingDissector())
            .withDissector(new RemapInputDissector())
            .withDissector(new FooInputDissector())
            .withDissector(new BarInputDissector())
            .withInput("Doesn't matter")

            .expect("STRING:something.foostring",     "42")
            .expect("STRING:something.foostring",     42L)
            .expect("STRING:something.foostring",     42D)
            .expect("LONG:something.foolong",         "42")
            .expect("LONG:something.foolong",         42L)
            .expect("LONG:something.foolong",         42D)
            .expect("DOUBLE:something.foodouble",     "42.0")
            .expect("DOUBLE:something.foodouble",     42L)
            .expect("DOUBLE:something.foodouble",     42D)

            .expect("STRING:something.barstring",     "42")
            .expect("STRING:something.barstring",     42L)
            .expect("STRING:something.barstring",     42D)
            .expect("LONG:something.barlong",         "42")
            .expect("LONG:something.barlong",         42L)
            .expect("LONG:something.barlong",         42D)
            .expect("DOUBLE:something.bardouble",     "42.0")
            .expect("DOUBLE:something.bardouble",     42L)
            .expect("DOUBLE:something.bardouble",     42D)

            .printPossible()
            .printDissectors()
            .checkExpectations();
    }


    public static class InputCreatingDissector extends Dissector {
        @Override
        public boolean initializeFromSettingsParameter(String settings) {
            return true;
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
            parsable.addDissection(inputname, "BASEINPUT", "something", "42");
        }

        @Override
        public String getInputType() {
            return "SOME_LINE";
        }

        @Override
        public List<String> getPossibleOutput() {
            return Collections.singletonList("BASEINPUT:something");
        }

        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return Casts.STRING_ONLY;
        }

        @Override
        public void prepareForRun() throws InvalidDissectorException {
        }

        @Override
        protected void initializeNewInstance(Dissector newInstance) throws InvalidDissectorException {
        }

    }


    public static class RemapInputDissector extends Dissector {
        @Override
        public boolean initializeFromSettingsParameter(String settings) {
            return true;
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
            parsable.addDissection(inputname, "INPUT", "", "42");
        }

        @Override
        public String getInputType() {
            return "BASEINPUT";
        }

        @Override
        public List<String> getPossibleOutput() {
            return Collections.singletonList("INPUT:");
        }

        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return Casts.STRING_ONLY;
        }

        @Override
        public void prepareForRun() throws InvalidDissectorException {
        }

        @Override
        protected void initializeNewInstance(Dissector newInstance) throws InvalidDissectorException {
        }

    }



    public static class FooInputDissector extends FooDissector {
        @Override
        public String getInputType() {
            return "INPUT";
        }
    }

    public static class BarInputDissector extends BarDissector {
        @Override
        public String getInputType() {
            return "INPUT";
        }
    }

}
