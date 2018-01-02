/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2018 Niels Basjes
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
package nl.basjes.parse.core;

public class ParserNormalRecordTest {

    public ParserNormalRecordTest() {
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
