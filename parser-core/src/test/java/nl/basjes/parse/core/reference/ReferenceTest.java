package nl.basjes.parse.core.reference;

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.nl.basjes.parse.core.test.TestRecord;
import org.junit.Test;

public class ReferenceTest {

    @Test
    public void verifyFoo() {
        DissectorTester.create()
            .withDissector(new FooDissector())
            .withInput("Doesn't matter")
            .expect("ANY:fooany",        "42")
            .expect("ANY:fooany",        42L)
            .expect("ANY:fooany",        42D)
            .expect("STRING:foostring",  "42")
            .expect("STRING:foostring",  (Long)null)
            .expect("STRING:foostring",  (Double) null)
            .expect("INT:fooint",        "42")
            .expect("INT:fooint",        42L)
            .expect("INT:fooint",        (Double) null)
            .expect("LONG:foolong",      "42")
            .expect("LONG:foolong",      42L)
            .expect("LONG:foolong",      (Double) null)
            .expect("FLOAT:foofloat",    "42.0")
            .expect("FLOAT:foofloat",    (Long)null)
            .expect("FLOAT:foofloat",    42D)
            .expect("DOUBLE:foodouble",  "42.0")
            .expect("DOUBLE:foodouble",  (Long)null)
            .expect("DOUBLE:foodouble",  42D)
//            .verbose()
            .checkExpectations();
    }

    @Test
    public void verifyBar() {
        DissectorTester.create()
            .withDissector(new BarDissector())
            .withInput("Doesn't matter")
            .expect("ANY:barany",        "42")
            .expect("ANY:barany",        42L)
            .expect("ANY:barany",        42D)
            .expect("STRING:barstring",  "42")
            .expect("STRING:barstring",  (Long)null)
            .expect("STRING:barstring",  (Double) null)
            .expect("INT:barint",        "42")
            .expect("INT:barint",        42L)
            .expect("INT:barint",        (Double) null)
            .expect("LONG:barlong",      "42")
            .expect("LONG:barlong",      42L)
            .expect("LONG:barlong",      (Double) null)
            .expect("FLOAT:barfloat",    "42.0")
            .expect("FLOAT:barfloat",    (Long)null)
            .expect("FLOAT:barfloat",    42D)
            .expect("DOUBLE:bardouble",  "42.0")
            .expect("DOUBLE:bardouble",  (Long)null)
            .expect("DOUBLE:bardouble",  42D)
//            .verbose()
            .checkExpectations();
    }

    @Test
    public void runManuallyCombined(){
        Parser<TestRecord> parser = new Parser<>(TestRecord.class);
        parser.addDissector(new FooDissector());
        parser.addDissector(new BarDissector());
        parser.addTypeRemapping("foostring", "BARINPUT");
        parser.setRootType(new FooDissector().getInputType());

        DissectorTester.create()
            .withParser(parser)
            .withInput("BlaBlaBla")

            .expect("ANY:fooany",                   "42")
            .expect("ANY:fooany",                   42L)
            .expect("ANY:fooany",                   42D)
            .expect("STRING:foostring",             "42")
            .expect("STRING:foostring",             (Long)null)
            .expect("STRING:foostring",             (Double) null)
            .expect("INT:fooint",                   "42")
            .expect("INT:fooint",                   42L)
            .expect("INT:fooint",                   (Double) null)
            .expect("LONG:foolong",                 "42")
            .expect("LONG:foolong",                 42L)
            .expect("LONG:foolong",                 (Double) null)
            .expect("FLOAT:foofloat",               "42.0")
            .expect("FLOAT:foofloat",               (Long)null)
            .expect("FLOAT:foofloat",               42D)
            .expect("DOUBLE:foodouble",             "42.0")
            .expect("DOUBLE:foodouble",             (Long)null)
            .expect("DOUBLE:foodouble",             42D)

            .expect("ANY:foostring.barany",         "42")
            .expect("ANY:foostring.barany",         42L)
            .expect("ANY:foostring.barany",         42D)
            .expect("STRING:foostring.barstring",   "42")
            .expect("STRING:foostring.barstring",   (Long)null)
            .expect("STRING:foostring.barstring",   (Double) null)
            .expect("INT:foostring.barint",         "42")
            .expect("INT:foostring.barint",         42L)
            .expect("INT:foostring.barint",         (Double) null)
            .expect("LONG:foostring.barlong",       "42")
            .expect("LONG:foostring.barlong",       42L)
            .expect("LONG:foostring.barlong",       (Double) null)
            .expect("FLOAT:foostring.barfloat",     "42.0")
            .expect("FLOAT:foostring.barfloat",     (Long)null)
            .expect("FLOAT:foostring.barfloat",     42D)
            .expect("DOUBLE:foostring.bardouble",   "42.0")
            .expect("DOUBLE:foostring.bardouble",   (Long)null)
            .expect("DOUBLE:foostring.bardouble",   42D)

            .checkExpectations();
    }

    @Test
    public void runAutomaticallyAddedBar(){
        DissectorTester.create()
            .withDissector(new FooSpecialDissector())
            .withInput("BlaBlaBla")

            .expect("ANY:fooany",                   "42")
            .expect("ANY:fooany",                   42L)
            .expect("ANY:fooany",                   42D)
            .expect("STRING:foostring",             "42")
            .expect("STRING:foostring",             (Long)null)
            .expect("STRING:foostring",             (Double) null)
            .expect("INT:fooint",                 "42")
            .expect("INT:fooint",                 42L)
            .expect("INT:fooint",                 (Double) null)
            .expect("LONG:foolong",                 "42")
            .expect("LONG:foolong",                 42L)
            .expect("LONG:foolong",                 (Double) null)
            .expect("FLOAT:foofloat",             "42.0")
            .expect("FLOAT:foofloat",             (Long)null)
            .expect("FLOAT:foofloat",             42D)
            .expect("DOUBLE:foodouble",             "42.0")
            .expect("DOUBLE:foodouble",             (Long)null)
            .expect("DOUBLE:foodouble",             42D)

            .expect("ANY:foostring.barany",         "42")
            .expect("ANY:foostring.barany",         42L)
            .expect("ANY:foostring.barany",         42D)
            .expect("STRING:foostring.barstring",   "42")
            .expect("STRING:foostring.barstring",   (Long)null)
            .expect("STRING:foostring.barstring",   (Double) null)
            .expect("INT:foostring.barint",       "42")
            .expect("INT:foostring.barint",       42L)
            .expect("INT:foostring.barint",       (Double) null)
            .expect("LONG:foostring.barlong",       "42")
            .expect("LONG:foostring.barlong",       42L)
            .expect("LONG:foostring.barlong",       (Double) null)
            .expect("FLOAT:foostring.barfloat",   "42.0")
            .expect("FLOAT:foostring.barfloat",   (Long)null)
            .expect("FLOAT:foostring.barfloat",   42D)
            .expect("DOUBLE:foostring.bardouble",   "42.0")
            .expect("DOUBLE:foostring.bardouble",   (Long)null)
            .expect("DOUBLE:foostring.bardouble",   42D)

            .checkExpectations();
    }

}
