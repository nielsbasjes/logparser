/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2013 Niels Basjes
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

import nl.basjes.parse.core.exceptions.DisectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDisectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * <p>A Disector is a class capable of chopping a String into multiple values of specific types.</p>
 * <p>The lifecycle:</p>
 * <p><b>Parser setup</b></p>
 * <ol>
 * <li>First instance is constructed</li>
 * <li>First instance is added to the {@link nl.basjes.parse.core.Parser} using
 * {@link nl.basjes.parse.core.Parser#addDisector(Disector)} </li>
 * <li>The {@link nl.basjes.parse.core.Parser} calls {@link #getInputType()} and {@link #getPossibleOutput()} to know
 * what this disector can deliver.</li>
 * </ol>
 * <p>The parser now constructs a tree based on the available {@link nl.basjes.parse.core.Disector}s
 * and what was requested.</p>
 * <p><b>Disectors setup</b></p>
 * <p>For each node in the tree a new instance of the required disector is created by calling {@link #getNewInstance()}
 * which calls {@link #initializeNewInstance(Disector)}. Note that only {@link nl.basjes.parse.core.Disector}s
 * that are acually needed will be in the parse tree.</p>
 * <p>For each of those instances in the tree:</p>
 * <ol>
 * <li>For each of the actually needed input+output combinations {@link #prepareForDisect(String, String)} is called.
 * This can be used to avoid needless CPU cycles during the actual run.</li>
 * <li>As a final step a call to {@link #prepareForRun()} is done as an indication that all preparation input has been
 * provided. A Disector can use this to finalize the runtime data structures so doing the actual disecting faster.</li>
 * </ol>
 * <p><b>Disecting</b></p>
 * <ul>
 * <li>During a run the instance will be called with {@link #disect} many times.</li>
 * <li>In the {@link #disect(Parsable, String)} the actual value to be worked on must be retrieved using
 * {@link nl.basjes.parse.core.Parsable#getParsableField(String, String)}</li>
 * <li>The result(s) of the disection must be put back using
 * {@link nl.basjes.parse.core.Parsable#addDisection(String, String, String, String, java.util.EnumSet)}</li>
 * </ul>
 */
public abstract class Disector {

    private static final Logger LOG = LoggerFactory.getLogger(Disector.class);

    // --------------------------------------------

    /**
     * This method must disect the provided field from the parsable into 'smaller' pieces.
     */
    public abstract void disect(final Parsable<?> parsable, final String inputname)
        throws DisectionFailure;

    // --------------------------------------------

    /**
     * @return The required typename of the input
     */
    public abstract String getInputType();

    // --------------------------------------------

    /**
     * What are all possible outputs that can be provided.
     * @return array of "type:name" values that indicates all the possible outputs
     */
    public abstract List<String> getPossibleOutput();

    // --------------------------------------------

    /**
     * This tells the disector that it should prepare that we will call it soon
     * with 'inputname' and expect to get 'inputname.outputname' because
     * inputname is of the type returned by getInputType and outputname
     * was part of the answer from getPossibleOutput.
     * This can be used by the disector implementation to optimize the internal parsing
     * algorithms and lookup tables and such.
     */
    public abstract void prepareForDisect(final String inputname, final String outputname);

    // --------------------------------------------

    /**
     * The framework will tell the disector that it should get ready to run.
     * I.e. finalize the bootstrapping.
     */
    public abstract void prepareForRun() throws InvalidDisectorException;

    // --------------------------------------------

    /**
     * Create an additional instance of this disector.
     * This is needed because in the parse tree we may need the same disector multiple times.
     * In order to optimize per node we need separate instances.
     * @return New instance of this Disector
     */
    public Disector getNewInstance() {
        try {
            Constructor<? extends Disector> co = this.getClass().getConstructor();
            Disector newInstance = co.newInstance();
            initializeNewInstance(newInstance);
            return newInstance;
        } catch (Exception e) {
            LOG.error("Unable to create instance of " + this.getClass().getCanonicalName() + ": " + e.toString());
        }
        return null;
    }

    protected abstract void initializeNewInstance(Disector newInstance);

}
