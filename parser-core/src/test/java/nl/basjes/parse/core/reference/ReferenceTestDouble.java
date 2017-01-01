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
            .expect("ANY:fooany",        "42")
            .expect("ANY:fooany",        42L)
            .expect("ANY:fooany",        42D)
            .expect("STRING:foostring",  "42")
            .expect("STRING:foostring",  (Long)null)
            .expect("STRING:foostring",  (Double) null)
            .expect("LONG:foolong",      "42")
            .expect("LONG:foolong",      42L)
            .expect("LONG:foolong",      (Double) null)
            .expect("DOUBLE:foodouble",  "42.0")
            .expect("DOUBLE:foodouble",  (Long)null)
            .expect("DOUBLE:foodouble",  42D)
            .verbose()
            .checkExpectations();
    }

    @Test
    public void verifyBarInput() {
        DissectorTester.create()
            .withDissector(new BarInputDissector())
            .withInput("Doesn't matter")
            .expect("ANY:barany",        "42")
            .expect("ANY:barany",        42L)
            .expect("ANY:barany",        42D)
            .expect("STRING:barstring",  "42")
            .expect("STRING:barstring",  (Long)null)
            .expect("STRING:barstring",  (Double) null)
            .expect("LONG:barlong",      "42")
            .expect("LONG:barlong",      42L)
            .expect("LONG:barlong",      (Double) null)
            .expect("DOUBLE:bardouble",  "42.0")
            .expect("DOUBLE:bardouble",  (Long)null)
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

            .expect("ANY:something.fooany",           "42")
            .expect("ANY:something.fooany",           42L)
            .expect("ANY:something.fooany",           42D)
            .expect("STRING:something.foostring",     "42")
            .expect("STRING:something.foostring",     (Long)null)
            .expect("STRING:something.foostring",     (Double) null)
            .expect("LONG:something.foolong",         "42")
            .expect("LONG:something.foolong",         42L)
            .expect("LONG:something.foolong",         (Double) null)
            .expect("DOUBLE:something.foodouble",     "42.0")
            .expect("DOUBLE:something.foodouble",     (Long)null)
            .expect("DOUBLE:something.foodouble",     42D)

            .expect("ANY:something.barany",           "42")
            .expect("ANY:something.barany",           42L)
            .expect("ANY:something.barany",           42D)
            .expect("STRING:something.barstring",     "42")
            .expect("STRING:something.barstring",     (Long)null)
            .expect("STRING:something.barstring",     (Double) null)
            .expect("LONG:something.barlong",         "42")
            .expect("LONG:something.barlong",         42L)
            .expect("LONG:something.barlong",         (Double) null)
            .expect("DOUBLE:something.bardouble",     "42.0")
            .expect("DOUBLE:something.bardouble",     (Long)null)
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
