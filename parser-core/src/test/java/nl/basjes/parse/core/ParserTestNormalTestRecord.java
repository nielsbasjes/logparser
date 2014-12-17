/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.basjes.parse.core;

public class ParserTestNormalTestRecord {

    public ParserTestNormalTestRecord() {
        // Empty but needed for instantiating
    }

    private String output1 = "xxx";

    @Field("SOMETYPE:output1")
    public void setValue1(String value) {
        output1 = "SOMETYPE1:SOMETYPE:output1:" + value;
    }

    public String getOutput1() {
        return output1;
    }

    private String output2 = "yyy";

    // @Field("OTHERTYPE:output") --> Set via direct method
    public void setValue2(String name, String value) {
        output2 = "OTHERTYPE2:" + name + ":" + value;
    }

    public String getOutput2() {
        return output2;
    }

    private String output3a = "xxx";
    private String output3b = "yyy";

    @Field({ "SOMETYPE:output1", "OTHERTYPE:output2" })
    public void setValue3(String name, String value) {
        if (name.startsWith("SOMETYPE:")) {
            output3a = "SOMETYPE3:" + name + ":" + value;
        } else {
            output3b = "OTHERTYPE3:" + name + ":" + value;
        }
    }

    public String getOutput3a() {
        return output3a;
    }

    public String getOutput3b() {
        return output3b;
    }

    private String output4a = "X";
    private String output4b = "Y";

    @Field({ "SOMETYPE:output1", "OTHERTYPE:output2", "SOMETYPE:output1", "OTHERTYPE:output2" })
    public void setValue4(String name, String value) {
        if (name.startsWith("SOMETYPE:")) {
            output4a = output4a + "=SOMETYPE:" + name + ":" + value;
        } else {
            output4b = output4b + "=OTHERTYPE:" + name + ":" + value;
        }
    }

    public String getOutput4a() {
        return output4a;
    }

    public String getOutput4b() {
        return output4b;
    }

    private String output5a = "X";
    private String output5b = "Y";

    @Field({ "SOMETYPE:output1", "OTHERTYPE:output2", "SOMETYPE:*", "OTHERTYPE:*" })
    public void setValue5(String name, String value) {
        if (name.startsWith("SOMETYPE:")) {
            output5a = output5a + "=SOMETYPE:" + name + ":" + value;
        } else {
            output5b = output5b + "=OTHERTYPE:" + name + ":" + value;
        }
    }

    public String getOutput5a() {
        return output5a;
    }

    public String getOutput5b() {
        return output5b;
    }

    private String output6 = "Z";

    @Field({ "FOO:output1.foo" })
    public void setValue6(String name, String value) {
        output6 = output6 + "=FOO:" + name + ":" + value;
    }

    public String getOutput6() {
        return output6;
    }

    private String output7 = "Z";

    @Field({ "BAR:output1.bar" })
    public void setValue7(String name, String value) {
        output7 = output7 + "=BAR:" + name + ":" + value;
    }

    public String getOutput7() {
        return output7;
    }

    private String output8 = "Z";

    @Field({ "WILD:output1.wild" })
    public void setValue8(String name, String value) {
        output8 = output8 + "=WILD:" + name + ":" + value;
    }

    public String getOutput8() {
        return output8;
    }

}
