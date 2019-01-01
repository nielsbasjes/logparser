/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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
package nl.basjes.parse.core.test.expectfailure;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// Based upon https://stackoverflow.com/questions/4055022/mark-unit-test-as-an-expected-failure-in-junit

public class ExpectedFailure implements TestRule {

    private static final Logger LOG = LoggerFactory.getLogger(ExpectedFailure.class);

    public Statement apply(Statement base, Description description) {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                TestShouldFail testShouldFail = description.getAnnotation(TestShouldFail.class);
                if (testShouldFail == null) {
                    base.evaluate();
                } else {
                    try {
                        base.evaluate();
                    } catch (Throwable e) {
                        LOG.info("The test failed as it should.");

                        String message = e.getMessage();
                        LOG.info("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
                            message +
                            "\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");

                        for (String expectedSubstring: testShouldFail.value()) {
                            assertTrue("Message does not contain expected substring: \""+expectedSubstring+"\"",
                                message.contains(expectedSubstring));
                        }

                        return;
                    }
                    fail("This test "+ description.getClassName() + "::" + description.getMethodName() + " should have failed... but it didn't");
                }
            }
        };
    }
}
