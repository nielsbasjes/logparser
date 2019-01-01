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
package nl.basjes.parse.core;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.FatalErrorDuringCallOfSetterMethod;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.InvalidFieldMethodSignature;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static nl.basjes.parse.core.Parser.SetterPolicy.ALWAYS;
import static nl.basjes.parse.core.Parser.SetterPolicy.NOT_EMPTY;
import static nl.basjes.parse.core.Parser.SetterPolicy.NOT_NULL;

public class Parser<RECORD> implements Serializable {

    public enum SetterPolicy {
        /** Call the setter for all values: Normal, Empty and NULL */
        ALWAYS,

        /** Call the setter for values: Normal and Empty, but not for NULL values */
        NOT_NULL,

        /** Call the setter for values: Normal, but not for Empty and NULL values */
        NOT_EMPTY
    }

    private static class DissectorPhase implements Serializable {
        DissectorPhase(final String inputType, final String outputType, final String name, final Dissector instance) {
            this.inputType  = inputType;
            this.outputType = outputType;
            this.name       = name;
            this.instance   = instance;
        }

        private final String   inputType;
        private final String   outputType;
        private final String   name;
        private final Dissector instance;
    }

    // --------------------------------------------

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    private final Class<RECORD> recordClass;

    private final Set<DissectorPhase> availableDissectors = new HashSet<>();
    private final Set<Dissector> allDissectors = new HashSet<>();

    // Key = "request.time.hour"
    // Value = the set of dissectors that must all be started once we have this value
    private Map<String, Set<DissectorPhase>> compiledDissectors = null;
    private Set<String> usefulIntermediateFields = null;
    private String rootType;

    // NOTE: The Method is NOT serializable. So after deserialization the 'assembled' is false
    //       and we 're-find' all methods using their names and parameter lists.

    // The target methods in the record class that will want to receive the values
    private transient Map<String, Set<Pair<Method, SetterPolicy>>> targets = new TreeMap<>();
    // Each method is a list of String: method name followed by the class names of each parameter.
    private final Map<String, Set<Pair<List<String>, SetterPolicy>>> targetsMethodNames = new TreeMap<>();
    private transient boolean assembled = false;

    private final Map<String, EnumSet<Casts>> castsOfTargets = new TreeMap<>();


    // --------------------------------------------

    public Set<String> getNeeded() {
        return targets.keySet();
    }

    /**
     * Returns the casts possible for the specified path.
     * Before you call 'getCasts' the actual parser needs to be constructed.
     * Simply calling getPossiblePaths does not build the actual parser.
     * If you want to get the casts for all possible paths the code looks something like this:
     * <pre>{@code
     * Parser<Object> dummyParser= new HttpdLoglineParser<>(Object.class, logformat);
     * List<String> possiblePaths = dummyParser.getPossiblePaths();
     * // Use a random method that has the right signature
     * dummyParser.addParseTarget(String.class.getMethod("indexOf", String.class), possiblePaths);
     * for (String path : possiblePaths) {
     *     LOG.info("{}     {}", path, dummyParser.getCasts(path));
     * }
     * }</pre>
     * @param name The name of the path for which you want the casts
     * @return The set of casts that are valid for this name. Null if this name is unknown.
     */
    public EnumSet<Casts> getCasts(String name) throws MissingDissectorsException, InvalidDissectorException {
        assembleDissectors();
        return castsOfTargets.get(name);
    }

    public Map<String, EnumSet<Casts>> getAllCasts() throws MissingDissectorsException, InvalidDissectorException {
        assembleDissectors();
        return castsOfTargets;
    }

    // --------------------------------------------

    Set<String> getUsefulIntermediateFields() {
        return usefulIntermediateFields;
    }

    // --------------------------------------------

    public final Parser<RECORD> addDissectors(final List<Dissector> dissectors) {
        assembled = false;
        if (dissectors != null) {
            allDissectors.addAll(dissectors);
        }
        return this;
    }

    // --------------------------------------------

    public final Parser<RECORD> addDissector(final Dissector dissector) {
        assembled = false;
        if (dissector != null) {
            allDissectors.add(dissector);
        }
        return this;
    }

    // --------------------------------------------

    public final Parser<RECORD> dropDissector(Class<? extends Dissector> dissectorClassToDrop) {
        assembled = false;
        Set<Dissector> removeDissector = new HashSet<>();
        for (final Dissector dissector : allDissectors) {
            if (dissector.getClass().equals(dissectorClassToDrop)) {
                removeDissector.add(dissector);
            }
        }
        allDissectors.removeAll(removeDissector);
        return this;
    }

    // --------------------------------------------

    public final Set<Dissector> getAllDissectors() {
        return allDissectors;
    }

    // --------------------------------------------

    public Parser<RECORD> setRootType(final String newRootType) {
        assembled = false;
        rootType = newRootType;
        return this;
    }

    // --------------------------------------------
    private void assembleDissectorPhases() throws InvalidDissectorException {
        for (final Dissector dissector : allDissectors) {
            final String inputType = dissector.getInputType();
            if (inputType == null) {
                throw new InvalidDissectorException("Dissector returns null on getInputType(): ["+ dissector.getClass().getCanonicalName()+"]");
            }

            final List<String> outputs = dissector.getPossibleOutput();
            if (outputs == null || outputs.isEmpty()) {
                throw new InvalidDissectorException("Dissector cannot create any outputs: ["+ dissector.getClass().getCanonicalName()+"]");
            }

            // Create all dissector phases
            for (final String output: outputs) {
                final int colonPos = output.indexOf(':');
                final String outputType = output.substring(0, colonPos);
                final String name = output.substring(colonPos + 1);
                availableDissectors.add(new DissectorPhase(inputType, outputType, name, dissector));
            }
        }
    }

    // --------------------------------------------

    private boolean failOnMissingDissectors = true;

    /**
     * This method sets the flag to ignore the missing dissectors situation.
     * This must be called before parsing the first line.
     * The effect is that those fields that would have been classified as 'missing'
     * will result in a null value (or better: the setter is never called) for all records.
     */
    public Parser<RECORD> ignoreMissingDissectors() {
        failOnMissingDissectors = false;
        return this;
    }

    /**
     * Reset back to the default of failing on missing dissectors.
     */
    public Parser<RECORD> failOnMissingDissectors() {
        failOnMissingDissectors = true;
        return this;
    }


    private void assembleDissectors() throws MissingDissectorsException, InvalidDissectorException {
        if (assembled) {
            return; // nothing to do.
        }

        if (targets == null) {
            // This happens only AFTER deserialization.
            targets = new HashMap<>(targetsMethodNames.size());

            for (Entry<String, Set<Pair<List<String>, SetterPolicy>>> entry:targetsMethodNames.entrySet()) {

                String fieldName = entry.getKey();
                Set<Pair<List<String>, SetterPolicy>> methodSet = entry.getValue();

                Set<Pair<Method, SetterPolicy>> fieldTargets = targets.computeIfAbsent(fieldName, k -> new HashSet<>());

                for(Pair<List<String>, SetterPolicy> methodStringPair: methodSet) {
                    List<String> methodString = methodStringPair.getLeft();
                    SetterPolicy setterPolicy = methodStringPair.getRight();
                    Method method;
                    String methodName = methodString.get(0);
                    int numberOfParameters = methodString.size()-1;
                    Class<?>[] parameters = new Class[numberOfParameters];
                    try {
                        parameters[0] = Class.forName(methodString.get(1));
                        if (numberOfParameters == 2) {
                            parameters[1] = Class.forName(methodString.get(2));
                        }
                    } catch (ClassNotFoundException e) {
                        throw new InvalidDissectorException("Unable to locate class", e);
                    }
                    try {
                        method = recordClass.getMethod(methodName, parameters);
                        fieldTargets.add(Pair.of(method, setterPolicy));
                    } catch (NoSuchMethodException e) {
                        throw new InvalidDissectorException("Unable to locate method " + methodName, e);
                    }
                }
                targets.put(fieldName, fieldTargets);
            }
        }

        // In some cases a dissector may need to create a special 'extra' dissector.
        // Which in some cases this is a recursive problem
        Set<Dissector> doneDissectors = new HashSet<>(allDissectors.size() + 10);
        Set<Dissector> loopDissectors = new HashSet<>(allDissectors);

        while (!loopDissectors.isEmpty()) {
            for (final Dissector dissector : loopDissectors) {
                dissector.createAdditionalDissectors(this);
            }
            doneDissectors.addAll(loopDissectors);
            loopDissectors.clear();
            loopDissectors.addAll(allDissectors);
            loopDissectors.removeAll(doneDissectors);
        }

        // So
        // - we have a set of needed values (targets)
        // - we have a set of dissectors that can pick apart some input
        // - we know where to start from
        // - we need to know how to proceed

        assembleDissectorPhases();

        // Step 1: Acquire all potentially useful subtargets
        // We first build a set of all possible subtargets that may be useful
        // this way we can skip anything we know not to be useful
        Set<String> needed = new HashSet<>(getNeeded());
        needed.add(rootType + ':'); // The root name is an empty string
        LOG.debug("Root: >>>{}:<<<", rootType);

        Set<String> allPossibleSubtargets = new HashSet<>();
        for (String need : needed) {
            String neededName = need.substring(need.indexOf(':') + 1);
            LOG.debug("Needed  : >>>{}<<<", neededName);
            String[] needs = neededName.split("\\.");
            StringBuilder sb = new StringBuilder(need.length());

            for (String part : needs) {
                if (sb.length() == 0 || part.length() == 0) {
                    sb.append(part);
                } else {
                    sb.append('.').append(part);
                }
                allPossibleSubtargets.add(sb.toString());
                LOG.debug("Possible: >>>{}<<<", sb);
            }
        }

        // Step 2: From the root we explore all possibly useful trees (recursively)
        compiledDissectors = new HashMap<>();
        usefulIntermediateFields = new HashSet<>();
        Set<String> locatedTargets = new HashSet<>();
        findUsefulDissectorsFromField(allPossibleSubtargets, locatedTargets, rootType, "", true); // The root name is an empty string

        // Step 3: Inform all dissectors to prepare for the run
        for (Set<DissectorPhase> dissectorPhases : compiledDissectors.values()) {
            for (DissectorPhase dissectorPhase : dissectorPhases) {
                dissectorPhase.instance.prepareForRun();
            }
        }

        if (compiledDissectors == null || compiledDissectors.isEmpty()) {
            throw new MissingDissectorsException("There are no dissectors at all which makes this a completely useless parser.");
        }

        if (failOnMissingDissectors) {
            // Step 4: As a final step we verify that every required input can be found
            Set<String> missingDissectors = getTheMissingFields(locatedTargets);
            if (missingDissectors != null && !missingDissectors.isEmpty()) {
                StringBuilder allMissing = new StringBuilder(missingDissectors.size() * 64);
                for (String missing : missingDissectors) {
                    allMissing.append('\n').append(missing);
                }
                throw new MissingDissectorsException(allMissing.toString());
            }
        }
        assembled = true;
    }

    // --------------------------------------------

    private void findUsefulDissectorsFromField(
            final Set<String> possibleTargets,
            final Set<String> locatedTargets,
            final String subRootType, final String subRootName,
            final boolean thisIsTheRoot) {

        String subRootId = subRootType + ':' + subRootName;

        // When we reach this point we have dissectors to get here.
        // So we store this to later validate if we have everything.
        if (locatedTargets.contains(subRootId)) {
            // We already found this one.
            return; // Avoid infinite recursion
        }
        locatedTargets.add(subRootId);

        LOG.debug("findUsefulDissectors:\"{}\" \"{}\"", subRootType, subRootName);

        for (DissectorPhase dissector: availableDissectors) {

            if (!(dissector.inputType.equals(subRootType))) {
                continue; // Wrong type
            }

            // If it starts with a . it extends.
            // If it doesn't then it starts at the beginning
            Set<String> checkFields = new HashSet<>();

            // If true then this dissector can output any name instead of just one
            boolean isWildCardDissector = dissector.name.equals("*");

            if (isWildCardDissector) {
                // Ok, this is special
                // We need to see if any of the wanted types start with the
                // subRootName (it may have a '.' in the rest of the line !)
                String subRootNameMatch = subRootName + '.';
                for (String possibleTarget : possibleTargets) {
                    if (possibleTarget.startsWith(subRootNameMatch)) {
                        checkFields.add(possibleTarget);
                    }
                }
            } else if (thisIsTheRoot) {
                checkFields.add(dissector.name);
            } else if (dissector.name.isEmpty()) {
                checkFields.add(subRootName);
            } else {
                checkFields.add(subRootName + '.' + dissector.name);
            }

            for (String checkField: checkFields) {
                if (possibleTargets.contains(checkField)
                    && !compiledDissectors.containsKey(dissector.outputType + ":" + checkField)) {

                    Set<DissectorPhase> subRootPhases = compiledDissectors.get(subRootId);
                    if (subRootPhases == null) {
                        // New so we can simply add it.
                        subRootPhases = new HashSet<>();
                        compiledDissectors.put(subRootId, subRootPhases);
                        usefulIntermediateFields.add(subRootName);
                    }

                    Class<? extends Dissector> clazz = dissector.instance.getClass();
                    DissectorPhase dissectorPhaseInstance = findDissectorInstance(subRootPhases, clazz);

                    if (dissectorPhaseInstance == null) {
                        dissectorPhaseInstance =
                                new DissectorPhase(dissector.inputType, dissector.outputType,
                                        checkField, dissector.instance.getNewInstance());
                        subRootPhases.add(dissectorPhaseInstance);
                    }

                    // Tell the dissector instance what to expect
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Informing : ({}){} --> {} --> ({}){}",
                                dissector.inputType, subRootName,
                                dissector.instance.getClass().getName(),
                                dissector.outputType, checkField);
                    }
                    castsOfTargets.put(dissector.outputType + ':' + checkField,
                            dissectorPhaseInstance.instance.prepareForDissect(subRootName, checkField));

                    // Recurse from this point down
                    findUsefulDissectorsFromField(possibleTargets, locatedTargets, dissector.outputType, checkField, false);
                }
            }
        }

        Set<String> mappings = typeRemappings.get(subRootName);
        if (mappings != null) {
            for (String mappedType : mappings) {
                if (!compiledDissectors.containsKey(mappedType + ':' + subRootName)) {
                    // Retyped targets are ALWAYS String ONLY.
                    castsOfTargets.put(mappedType + ':' + subRootName, STRING_ONLY);
                    findUsefulDissectorsFromField(possibleTargets, locatedTargets, mappedType, subRootName, false);
                }
            }
        }

    }

    private DissectorPhase findDissectorInstance(Set<DissectorPhase> dissectorPhases,
                                                 Class<? extends Dissector> clazz) {
        for (DissectorPhase phase : dissectorPhases) {
            if (phase.instance.getClass() == clazz) {
                return phase;
            }
        }
        return null;
    }

    // --------------------------------------------

    private Set<String> getTheMissingFields(Set<String> locatedTargets) {
        Set<String> missing = new HashSet<>();
        for (String target : getNeeded()) {
            if (!locatedTargets.contains(target)) {
                // Handle wildcard targets differently
                if (target.endsWith("*")) {
                    if (target.endsWith(".*")) {
                        if (!locatedTargets.contains(target.substring(0, target.length() - 2))) {
                            missing.add(target);
                        }
                    }
                    // Else: it ends with :* and it is always "present".
                } else {
                    missing.add(target);
                }
            }
        }
        return missing;
    }

    // --------------------------------------------

    /*
     * The constructor tries to retrieve the desired fields from the annotations in the specified class. */
    public Parser(final Class<RECORD> clazz) {
        recordClass = clazz;

        // Get all methods of the correct signature that have been annotated
        // with Field
        for (final Method method : recordClass.getMethods()) {
            final Field field = method.getAnnotation(Field.class);
            if (field != null) {
                addParseTarget(method, field.setterPolicy(), Arrays.asList(field.value()));
            }
        }
    }

    // --------------------------------------------

    /*
     * When there is a need to add a target callback manually use this method. */
    public Parser<RECORD> addParseTarget(final String setterMethodName,
                               final String fieldValue) throws NoSuchMethodException {
        addParseTarget(setterMethodName, ALWAYS, fieldValue);
        return this;
    }

    /*
     * When there is a need to add a target callback manually use this method. */
    public Parser<RECORD> addParseTarget(final String setterMethodName,
                               final SetterPolicy setterPolicy,
                               final String fieldValue) throws NoSuchMethodException {
        Method method;

        try {
            method = recordClass.getMethod(setterMethodName, String.class);
        } catch (NoSuchMethodException a) {
            try {
                method = recordClass.getMethod(setterMethodName, String.class, String.class);
            } catch (NoSuchMethodException b) {
                try {
                    method = recordClass.getMethod(setterMethodName, String.class, Long.class);
                } catch (NoSuchMethodException c) {
                    try {
                        method = recordClass.getMethod(setterMethodName, String.class, Double.class);
                    } catch (NoSuchMethodException d) {
                        try {
                            method = recordClass.getMethod(setterMethodName, Long.class);
                        } catch (NoSuchMethodException e) {
                            try {
                                method = recordClass.getMethod(setterMethodName, Double.class);
                            } catch (NoSuchMethodException f) {
                                throw new NoSuchMethodException(
                                    "Unable to find any valid form of the method " + setterMethodName +
                                        " in the class " + recordClass.getCanonicalName());
                            }
                        }
                    }
                }
            }
        }

        addParseTarget(method, setterPolicy, Collections.singletonList(fieldValue));
        return this;
    }


    /*
     * When there is a need to add a target callback manually use this method. */
    public Parser<RECORD> addParseTarget(final Method method, final String fieldValue) {
        return addParseTarget(method, SetterPolicy.ALWAYS, Collections.singletonList(fieldValue));
    }

    /*
     * When there is a need to add a target callback manually use this method. */
    public Parser<RECORD> addParseTarget(final Method method,
                               final SetterPolicy setterPolicy,
                               final String fieldValue) {
        return addParseTarget(method, setterPolicy, Collections.singletonList(fieldValue));
    }

    /*
     * When there is a need to add a target callback manually use this method. */
    public Parser<RECORD> addParseTarget(final Method method, final List<String> fieldValues) {
        return addParseTarget(method, SetterPolicy.ALWAYS, fieldValues);
    }

    /*
     * When there is a need to add a target callback manually use this method. */
    public Parser<RECORD> addParseTarget(final Method method,
                               final SetterPolicy setterPolicy,
                               final List<String> fieldValues) {
        assembled = false;

        if (method == null || fieldValues == null) {
            return this; // Nothing to do here
        }

        final Class<?>[] parameters = method.getParameterTypes();
        if (
                // Setters that receive a String
                ((parameters.length == 1) && (parameters[0] == String.class)) ||
                ((parameters.length == 2) && (parameters[0] == String.class) && (parameters[1] == String.class)) ||

                // Setters that receive a Long
                ((parameters.length == 1) && (parameters[0] == Long.class)) ||
                ((parameters.length == 2) && (parameters[0] == String.class) && (parameters[1] == Long.class)) ||

                // Setters that receive a Double
                ((parameters.length == 1) && (parameters[0] == Double.class)) ||
                ((parameters.length == 2) && (parameters[0] == String.class) && (parameters[1] == Double.class))
        ) {
            for (final String fieldValue : fieldValues) {
                if (fieldValue == null) {
                    continue;
                }
                String cleanedFieldValue = cleanupFieldValue(fieldValue);
                if (!fieldValue.equals(cleanedFieldValue)) {
                    LOG.warn("The requested \"{}\" was converted into \"{}\"", fieldValue, cleanedFieldValue);
                }

                // We have 1 real target
                Set<Pair<Method, SetterPolicy>> fieldTargets = targets.computeIfAbsent(cleanedFieldValue, k -> new HashSet<>());
                fieldTargets.add(Pair.of(method, setterPolicy));
                targets.put(cleanedFieldValue, fieldTargets);

                // We have 1 real target
                Set<Pair<List<String>, SetterPolicy>> fieldTargetNames = targetsMethodNames.get(cleanedFieldValue);
                if (fieldTargetNames == null) {
                    fieldTargetNames = new HashSet<>();
                }
                List<String> methodList = new ArrayList<>();
                methodList.add(method.getName());
                for (Class<?> clazz: method.getParameterTypes()) {
                    methodList.add(clazz.getCanonicalName());
                }
                fieldTargetNames.add(Pair.of(methodList, setterPolicy));
                targetsMethodNames.put(cleanedFieldValue, fieldTargetNames);
            }
        } else {
            throw new InvalidFieldMethodSignature(method);
        }
        return this;
    }

    // --------------------------------------------

    private Map<String, Set<String>> typeRemappings = new HashMap<>(16);

    public Parser<RECORD> setTypeRemappings(Map<String, Set<String>> pTypeRemappings) {
        if (pTypeRemappings == null) {
            this.typeRemappings.clear();
        } else {
            this.typeRemappings = pTypeRemappings;
        }
        return this;
    }

    public Parser<RECORD> addTypeRemappings(Map<String, Set<String>> additionalTypeRemappings) {
        for (Entry<String, Set<String>> entry: additionalTypeRemappings.entrySet()){
            String input = entry.getKey();
            for (String newType: entry.getValue()) {
                addTypeRemapping(input, newType, STRING_ONLY);
            }
        }
        return this;
    }

    public Parser<RECORD> addTypeRemapping(String input, String newType) {
        return addTypeRemapping(input, newType, STRING_ONLY);
    }

    public Parser<RECORD> addTypeRemapping(String input, String newType, EnumSet<Casts> newCasts) {
        assembled = false;

        String theInput = input.trim().toLowerCase(Locale.ENGLISH);
        String theType = newType.trim().toUpperCase(Locale.ENGLISH);

        Set<String> mappingsForInput = typeRemappings.computeIfAbsent(theInput, k -> new HashSet<>());

        if (!mappingsForInput.contains(theType)) {
            mappingsForInput.add(theType);
            castsOfTargets.put(theType+':'+theInput, newCasts);
        }
        return this;
    }

    // --------------------------------------------

    public static String cleanupFieldValue(String fieldValue) {
        final int colonPos = fieldValue.indexOf(':');
        if (colonPos == -1) {
            return fieldValue.toLowerCase(Locale.ENGLISH);
        }

        final String fieldType = fieldValue.substring(0, colonPos);
        final String fieldName = fieldValue.substring(colonPos + 1);

        return fieldType.toUpperCase(Locale.ENGLISH)+':'+ fieldName.toLowerCase(Locale.ENGLISH);
    }

    // --------------------------------------------


    /**
     * Parse the value and return a new instance of RECORD.
     * For this method to work the RECORD class may NOT be an inner class.
     */
    public RECORD parse(final String value)
        throws DissectionFailure, InvalidDissectorException, MissingDissectorsException {
        assembleDissectors();
        final Parsable<RECORD> parsable = createParsable();
        if (parsable == null) {
            return null;
        }
        parsable.setRootDissection(rootType, value);
        return parse(parsable).getRecord();
    }

    // --------------------------------------------

    /**
     * Parse the value and call all configured setters in the provided instance of RECORD.
     */
    public RECORD parse(final RECORD record, final String value)
        throws DissectionFailure, InvalidDissectorException, MissingDissectorsException {
        assembleDissectors();
        final Parsable<RECORD> parsable = createParsable(record);
        parsable.setRootDissection(rootType, value);
        return parse(parsable).getRecord();
    }

    // --------------------------------------------

    Parsable<RECORD> parse(final Parsable<RECORD> parsable)
        throws DissectionFailure, InvalidDissectorException, MissingDissectorsException {
        assembleDissectors();

        if (!assembled) {
            return null;
        }

        // Values look like "TYPE:foo.bar"
        Set<ParsedField> toBeParsed = new HashSet<>(parsable.getToBeParsed());

        while (!toBeParsed.isEmpty()) {
            for (ParsedField fieldThatNeedsToBeParsed : toBeParsed) {
                parsable.setAsParsed(fieldThatNeedsToBeParsed);
                Set<DissectorPhase> dissectorSet = compiledDissectors.get(fieldThatNeedsToBeParsed.getId());
                if (dissectorSet != null) {
                    for (DissectorPhase dissector : dissectorSet) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Dissect {} with {}", fieldThatNeedsToBeParsed, dissector.instance.getClass().getName());
                        }
                        dissector.instance.dissect(parsable, fieldThatNeedsToBeParsed.getName());
                    }
                } else {
                    LOG.trace("NO DISSECTORS FOR \"{}\"", fieldThatNeedsToBeParsed);
                }
            }
            toBeParsed.clear();
            toBeParsed.addAll(parsable.getToBeParsed());
        }
        return parsable;
    }

    // --------------------------------------------

    void store(final RECORD record, final String key, final String name, final Value value) {
        boolean calledASetter = false;

        if (value == null) {
            LOG.error("Got a null value to store for key={}  name={}.", key, name);
            return; // Nothing to do
        }

        final Set<Pair<Method, SetterPolicy>> methodPairs = targets.get(key);
        if (methodPairs == null) {
            LOG.error("NO methods for key={}  name={}.", key, name);
            return;
        }

        EnumSet<Casts> castsTo = castsOfTargets.get(key);
        if (castsTo == null) {
            castsTo = castsOfTargets.get(name);
            if (castsTo == null) {
                LOG.error("NO casts for \"{}\"", name);
                return;
            }
        }

        for (Pair<Method, SetterPolicy> methodPair : methodPairs) {
            Method method = methodPair.getLeft();
            if (method != null) {
                SetterPolicy setterPolicy = methodPair.getRight();
                try {
                    Class<?>[] parameters = method.getParameterTypes();
                    Class<?> valueClass = parameters[parameters.length - 1]; // Always the last one

                    if (valueClass == String.class) {
                        if (castsTo.contains(Casts.STRING)) {
                            String stringValue = value.getString();
                            if (stringValue == null) {
                                if (setterPolicy == NOT_NULL || setterPolicy == NOT_EMPTY) {
                                    calledASetter = true;
                                    continue;
                                }
                            } else {
                                if (stringValue.isEmpty() && setterPolicy == NOT_EMPTY) {
                                    calledASetter = true;
                                    continue;
                                }
                            }
                            if (parameters.length == 2) {
                                method.invoke(record, name, stringValue);
                            } else {
                                method.invoke(record, stringValue);
                            }
                            calledASetter = true;
                        }
                        continue;
                    }

                    if (valueClass == Long.class) {
                        if (castsTo.contains(Casts.LONG)) {
                            Long longValue = value.getLong();
                            if (longValue == null &&
                                (setterPolicy == NOT_NULL || setterPolicy == NOT_EMPTY)) {
                                calledASetter = true;
                                continue;
                            }
                            if (parameters.length == 2) {
                                method.invoke(record, name, longValue);
                            } else {
                                method.invoke(record, longValue);
                            }
                            calledASetter = true;
                        }
                        continue;
                    }

                    if (valueClass == Double.class) {
                        if (castsTo.contains(Casts.DOUBLE)) {
                            Double doubleValue = value.getDouble();
                            if (doubleValue == null &&
                                (setterPolicy == NOT_NULL || setterPolicy == NOT_EMPTY)) {
                                calledASetter = true;
                                continue;
                            }
                            if (parameters.length == 2) {
                                method.invoke(record, name, doubleValue);
                            } else {
                                method.invoke(record, doubleValue);
                            }
                            calledASetter = true;
                        }
                        continue;
                    }

                    throw new FatalErrorDuringCallOfSetterMethod(
                            "Tried to call setter with unsupported class :" +
                            " key = \"" + key + "\" " +
                            " name = \"" + name + "\" " +
                            " value = \"" + value + "\"" +
                            " castsTo = \"" + castsTo + "\"");

                } catch (final Exception e) {
                    throw new FatalErrorDuringCallOfSetterMethod(e.getMessage() + " caused by \"" +
                            e.getCause() + "\" when calling \"" +
                            method.toGenericString() + "\" for " +
                            " key = \"" + key + "\" " +
                            " name = \"" + name + "\" " +
                            " value = \"" + value + "\"" +
                            " castsTo = \"" + castsTo + "\"", e);
                }
            }
        }

        if (!calledASetter) {
            throw new FatalErrorDuringCallOfSetterMethod("No setter called for " +
                    " key = \"" + key + "\" " +
                    " name = \"" + name + "\" " +
                    " value = \"" + value + "\"");
        }
    }

    // --------------------------------------------

    private Parsable<RECORD> createParsable(RECORD record) {
        return new Parsable<>(this, record, typeRemappings);
    }

    public Parsable<RECORD> createParsable() {
        RECORD record;

        try {
            Constructor<RECORD> co = recordClass.getConstructor();
            record = co.newInstance();
        } catch (Exception e) {
            LOG.error("Unable to create instance: {}", e);
            return null;
        }
        return createParsable(record);
    }

    // --------------------------------------------

    /**
     * This method is for use by the developer to query the parser about
     * the possible paths that may be extracted.
     * @return A list of all possible paths that could be determined automatically.
     */
    public List<String> getPossiblePaths() {
        return getPossiblePaths(15);
    }

    /**
     * This method is for use by the developer to query the parser about
     * the possible paths that may be extracted.
     * @param maxDepth The maximum recursion depth
     * @return A list of all possible paths that could be determined automatically.
     */
    public List<String> getPossiblePaths(int maxDepth) {
        if (allDissectors.isEmpty()) {
            return Collections.emptyList(); // nothing to do.
        }

        try {
            assembleDissectors();
        } catch (MissingDissectorsException | InvalidDissectorException e) {
            // Simply swallow this one
        }
        List<String> paths = new ArrayList<>();

        Map<String, List<String>> pathNodes = new HashMap<>();

        for (Dissector dissector : allDissectors) {
            final String inputType = dissector.getInputType();
            if (inputType == null) {
                LOG.error("Dissector returns null on getInputType(): [{}]", dissector.getClass().getCanonicalName());
                return Collections.emptyList();
            }

            final List<String> outputs = dissector.getPossibleOutput();

            if (LOG.isDebugEnabled()) {
                LOG.debug("------------------------------------");
                LOG.debug("Possible: Dissector IN {}", inputType);
                for (String output: outputs) {
                    LOG.debug("Possible:          --> {}", output);
                }
            }

            List<String> existingOutputs = pathNodes.get(inputType);
            if (existingOutputs != null) {
                outputs.addAll(existingOutputs);
            }
            pathNodes.put(inputType, outputs);
        }

        findAdditionalPossiblePaths(pathNodes, paths, "", rootType, maxDepth, "");

        for (Entry<String, Set<String>> typeRemappingSet: typeRemappings.entrySet()) {
            for (String typeRemapping: typeRemappingSet.getValue()) {

                String remappedPath = typeRemapping + ':' + typeRemappingSet.getKey();
                LOG.debug("Adding remapped path: {}", remappedPath);
                paths.add(remappedPath);
                findAdditionalPossiblePaths(pathNodes, paths, typeRemappingSet.getKey(), typeRemapping, maxDepth - 1, "");
            }
        }

        return paths;
    }

    /**
     * Add all child paths in respect to the base (which is already present in the result set)
     */
    private void findAdditionalPossiblePaths(Map<String, List<String>> pathNodes,
                                             List<String> paths,
                                             String base,
                                             String baseType,
                                             int maxDepth,
                                             String logPrefix) {
        if (maxDepth == 0) {
            return;
        }

        LOG.debug("Possible:{} > {}:{}", logPrefix, baseType, base);

        if (pathNodes.containsKey(baseType)) {
            List<String> childPaths = pathNodes.get(baseType);
            if (childPaths != null) {
                for (String childPath : childPaths) {
                    final int colonPos = childPath.indexOf(':');
                    final String childType = childPath.substring(0, colonPos);
                    final String childName = childPath.substring(colonPos + 1);

                    String childBase;
                    if (base.isEmpty()) {
                        childBase = childName;
                    } else {
                        if (childName.isEmpty()) {
                            childBase = base;
                        } else {
                            childBase = base + '.' + childName;
                        }
                    }

                    String newPath = childType + ':' + childBase;
                    if (!paths.contains(newPath)) {
                        LOG.debug("Possible:{} + {}:{}", logPrefix, childType, childBase);
                        paths.add(childType + ':' + childBase);

                        findAdditionalPossiblePaths(pathNodes, paths, childBase, childType, maxDepth - 1, logPrefix + "--");
                    }
                }
            }
        }
        LOG.debug("Possible:{} < {}:{}", logPrefix, baseType, base);
    }

    // --------------------------------------------

}
