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
            .check();
    }

    @Test
    public void verifyBar() {
        DissectorTester.create()
            .withDissector(new BarDissector())
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
            .check();
    }

    @Test
    public void runManuallyCombined(){
        Parser<TestRecord> parser = new Parser<>(TestRecord.class);
        parser.addTypeRemapping("fooString", "BARINPUT");
        parser.addDissector(new FooDissector());
        parser.addDissector(new BarDissector());
        parser.setRootType(new FooDissector().getInputType());

        for (String path : parser.getPossiblePaths()){
            System.out.println(path);
        }

        DissectorTester.create()
            .withParser(parser)
            .withInput("BlaBlaBla")
            .expect("STRING:foostring",             "42")
            .expect("STRING:foostring",             42L)
            .expect("STRING:foostring",             42D)
            .expect("LONG:foolong",                 "42")
            .expect("LONG:foolong",                 42L)
            .expect("LONG:foolong",                 42D)
            .expect("DOUBLE:foodouble",             "42.0")
            .expect("DOUBLE:foodouble",             42L)
            .expect("DOUBLE:foodouble",             42D)

            .expect("STRING:foostring.barstring",   "42")
            .expect("STRING:foostring.barstring",   42L)
            .expect("STRING:foostring.barstring",   42D)
            .expect("LONG:foostring.barlong",       "42")
            .expect("LONG:foostring.barlong",       42L)
            .expect("LONG:foostring.barlong",       42D)
            .expect("DOUBLE:foostring.bardouble",   "42.0")
            .expect("DOUBLE:foostring.bardouble",   42L)
            .expect("DOUBLE:foostring.bardouble",   42D)

            .check();
    }

    @Test
    public void runAutomaticallyAddedBar(){
        DissectorTester.create()
            .withDissector(new FooSpecialDissector())
            .withInput("BlaBlaBla")
            .expect("STRING:foostring",             "42")
            .expect("STRING:foostring",             42L)
            .expect("STRING:foostring",             42D)
            .expect("LONG:foolong",                 "42")
            .expect("LONG:foolong",                 42L)
            .expect("LONG:foolong",                 42D)
            .expect("DOUBLE:foodouble",             "42.0")
            .expect("DOUBLE:foodouble",             42L)
            .expect("DOUBLE:foodouble",             42D)

            .expect("STRING:foostring.barstring",   "42")
            .expect("STRING:foostring.barstring",   42L)
            .expect("STRING:foostring.barstring",   42D)
            .expect("LONG:foostring.barlong",       "42")
            .expect("LONG:foostring.barlong",       42L)
            .expect("LONG:foostring.barlong",       42D)
            .expect("DOUBLE:foostring.bardouble",   "42.0")
            .expect("DOUBLE:foostring.bardouble",   42L)
            .expect("DOUBLE:foostring.bardouble",   42D)

            .check();
    }


}
