package nl.basjes.parse.core.reference;

import nl.basjes.parse.core.Parser;

public class FooSpecialDissector extends FooDissector {
    @Override
    public <RECORD> void createAdditionalDissectors(Parser<RECORD> parser) {
        parser.addDissector(new BarDissector());
        parser.addTypeRemapping("foostring", "BARINPUT");
    }
}
