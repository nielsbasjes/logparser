/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2021 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basjes.parse.core;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.EnumSet;
import java.util.List;

/**
 * <p>A Dissector is a class capable of chopping a String into multiple values of specific types.</p>
 * <p>The lifecycle:</p>
 * <p><b>Parser setup</b></p>
 * <ol>
 * <li>First instance is constructed</li>
 * <li>First instance is added to the {@link nl.basjes.parse.core.Parser} using
 * {@link nl.basjes.parse.core.Parser#addDissector(Dissector)} </li>
 * <li>The {@link nl.basjes.parse.core.Parser} calls {@link #getInputType()} and {@link #getPossibleOutput()} to know
 * what this dissector can deliver.</li>
 * </ol>
 * <p>The parser now constructs a tree based on the available {@link Dissector}s
 * and what was requested.</p>
 * <p><b>Dissectors setup</b></p>
 * <p>For each node in the tree a new instance of the required dissector is created by calling {@link #getNewInstance()}
 * which calls {@link #initializeNewInstance(Dissector)}. Note that only {@link Dissector}s
 * that are actually needed will be in the parse tree.</p>
 * <p>For each of those instances in the tree:</p>
 * <ol>
 * <li>For each of the actually needed input+output combinations {@link #prepareForDissect(String, String)} is called.
 * This can be used to avoid needless CPU cycles during the actual run.</li>
 * <li>As a final step a call to {@link #prepareForRun()} is done as an indication that all preparation input has been
 * provided. A Dissector can use this to finalize the runtime data structures so doing the actual dissecting faster.</li>
 * </ol>
 * <p><b>Dissecting</b></p>
 * <ul>
 * <li>During a run the instance will be called with {@link #dissect} many times.</li>
 * <li>In the {@link #dissect(Parsable, String)} the actual value to be worked on must be retrieved using
 * {@link nl.basjes.parse.core.Parsable#getParsableField(String, String)}</li>
 * <li>The result(s) of the dissection must be put back using
 * {@link nl.basjes.parse.core.Parsable#addDissection(String, String, String, String)}</li>
 * </ul>
 */
public abstract class Dissector implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Dissector.class);

    // --------------------------------------------

    /**
     * If a Dissector is loaded through an external language then this is the
     * method that is called to set all the parameters.
     * There is exactly one String as input so it is up to the specific
     * Dissector implementation to parse and handle this input.
     * @return true if everything went right. false otherwise.
     */
    public boolean initializeFromSettingsParameter(String settings) {
        // Default behaviour is do nothing.
        return true;
    }

    // --------------------------------------------

    /**
     * This method must dissect the provided field from the parsable into 'smaller' pieces.
     */
    public abstract void dissect(Parsable<?> parsable, String inputname)
        throws DissectionFailure;

    // --------------------------------------------

    /**
     * @return The required typename of the input
     */
    public abstract String getInputType();

    // --------------------------------------------

    /**
     * What are all possible outputs that can be provided.
     * @return array of "type:name" values that indicates all the possible outputs. Never a null!
     */
    public abstract List<String> getPossibleOutput();

    // --------------------------------------------

    /**
     * This tells the dissector that it should prepare that we will call it soon
     * with 'inputname' and expect to get 'inputname.outputname' because
     * inputname is of the type returned by getInputType and outputname
     * was part of the answer from getPossibleOutput.
     * This can be used by the dissector implementation to optimize the internal parsing
     * algorithms and lookup tables and such.
     * The dissector must return the types to which this value can be mapped later on during the run.
     * @return The EnumSet of all allowed casts. Returns an empty EnumSet if nothing is allowed. Never a null !
     */
    public abstract EnumSet<Casts> prepareForDissect(String inputname, String outputname);

    // --------------------------------------------

    /**
     * The framework will tell the dissector that it should get ready to run.
     * I.e. finalize the bootstrapping.
     */
    public void prepareForRun() throws InvalidDissectorException {
        // Default behaviour is do nothing.
    }

    // --------------------------------------------

    /**
     * Create an additional instance of this dissector.
     * This is needed because in the parse tree we may need the same dissector multiple times.
     * In order to optimize per node we need separate instances.
     * @return New instance of this Dissector
     */
    public Dissector getNewInstance() {
        try {
            Constructor<? extends Dissector> co = this.getClass().getConstructor();
            Dissector newInstance = co.newInstance();
            initializeNewInstance(newInstance);
            return newInstance;
        } catch (Exception e) {
            LOG.error("Unable to create instance of {}: {}", this.getClass().getCanonicalName(), e);
        }
        return null;
    }

    public String extractFieldName(final String inputname, final String outputname){
        String fieldName = outputname;

        if (inputname.equals(outputname)) {
            return "";
        }
        if (!inputname.equals("")) {
            fieldName = outputname.substring(inputname.length() + 1);
        }
        return fieldName;
    }

    /**
     * This is called after instantiating the class that is actually in the parsetree.
     * @param newInstance The new instances of this class that must be initialized
     */
    protected void initializeNewInstance(Dissector newInstance) throws InvalidDissectorException {
        // Default behaviour is do nothing.
    }

    /**
     * If a dissector really needs to add an additional dissector to the set this method is the
     * place to do so.
     * @param parser The instance of the parser where the extra dissector is to be added to.
     * @param <RECORD> The type of the record.
     */
    public <RECORD> void createAdditionalDissectors(Parser<RECORD> parser) {
        // Default behaviour is do nothing.
    }

    public void setInputType(String s) throws InvalidDissectorException {
        // Usually only implemented in very dynamic dissectors (like custom timestamp format)
        throw new InvalidDissectorException("The InputType of " + this.getClass().getCanonicalName() + " cannot be changed");
    }

    @Override
    public String toString() {
        return "{ " + this.getClass().getSimpleName() + " : " + getInputType() + " --> " + getPossibleOutput() + " }";
    }
}
