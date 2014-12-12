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

import nl.basjes.parse.core.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class Parser<RECORD> {

    private static class DissectorPhase {
        public DissectorPhase(final String inputType, final String outputType, final String name, final Dissector instance) {
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
    private String rootName;

    // The target methods in the record class that will want to receive the values
    private final Map<String, Set<Method>> targets = new TreeMap<>();
    private final Map<String, EnumSet<Casts>> castsOfTargets = new TreeMap<>();

    private final Set<String> locatedTargets = new HashSet<>();

    private boolean usable = false;

    // --------------------------------------------

    public Set<String> getNeeded() {
        return targets.keySet();
    }

    public EnumSet<Casts> getCasts(String name) {
        try {
            assembleDissectors();
        } catch (MissingDissectorsException
                |InvalidDissectorException e) {
            e.printStackTrace();
        }
        return castsOfTargets.get(name);
    }

    public Map<String, EnumSet<Casts>> getAllCasts() {
        try {
            assembleDissectors();
        } catch (MissingDissectorsException
                |InvalidDissectorException e) {
            e.printStackTrace();
        }
        return castsOfTargets;
    }

    // --------------------------------------------

    Set<String> getUsefulIntermediateFields() {
        return usefulIntermediateFields;
    }

    // --------------------------------------------

    public final void addDissectors(final List<Dissector> dissectors) {
        if (compiledDissectors != null) {
            throw new CannotChangeDissectorsAfterConstructionException();
        }

        if (dissectors != null) {
            for (Dissector dissector : dissectors) {
                allDissectors.add(dissector);
            }
        }
    }

    // --------------------------------------------

    public final void addDissector(final Dissector dissector) {
        if (compiledDissectors != null) {
            throw new CannotChangeDissectorsAfterConstructionException();
        }

        if (dissector != null) {
            allDissectors.add(dissector);
        }
    }

    // --------------------------------------------

    public final void dropDissector(Class<? extends Dissector> dissectorClassToDrop) {
        if (compiledDissectors != null) {
            throw new CannotChangeDissectorsAfterConstructionException();
        }

        Set<Dissector> removeDissector = new HashSet<>();
        for (final Dissector dissector : allDissectors) {
            if (dissector.getClass().equals(dissectorClassToDrop)) {
                removeDissector.add(dissector);
            }
        }
        allDissectors.removeAll(removeDissector);
    }

    // --------------------------------------------

    protected void setRootType(final String newRootType) {
        compiledDissectors = null;

        rootType = newRootType;
        rootName = "rootinputline";
    }

    // --------------------------------------------
    private void assembleDissectorPhases() throws InvalidDissectorException {
        if (compiledDissectors != null) {
            return; // nothing to do.
        }

        for (final Dissector dissector : allDissectors) {
            final String inputType = dissector.getInputType();
            final List<String> outputs = dissector.getPossibleOutput();

            if (outputs == null || outputs.size() == 0) {
                throw new InvalidDissectorException("Dissector cannot create any outputs: ["+ dissector.getClass().getCanonicalName()+"]");
            }

            // Create all dissector phases
            for (final String output : outputs) {
                final int colonPos = output.indexOf(':');
                final String outputType = output.substring(0, colonPos);
                final String name = output.substring(colonPos + 1);
                availableDissectors.add(new DissectorPhase(inputType, outputType, name, dissector));
            }
        }
    }
    // --------------------------------------------

    private void assembleDissectors() throws MissingDissectorsException, InvalidDissectorException {
        if (compiledDissectors != null) {
            return; // nothing to do.
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
        needed.add(rootType + ':' + rootName);
        LOG.debug("Root: >>>{}:{}<<<", rootType, rootName);

        Set<String> allPossibleSubtargets = new HashSet<>();
        for (String need : needed) {
            String neededName = need.substring(need.indexOf(':') + 1);
            LOG.debug("Needed  : >>>{}<<<", neededName);
            String[] needs = neededName.split("\\.");
            StringBuilder sb = new StringBuilder(need.length());

            for (String part : needs) {
                if (sb.length() == 0) {
                    sb.append(part);
                } else {
                    sb.append('.').append(part);
                }
                allPossibleSubtargets.add(sb.toString());
                LOG.debug("Possible: >>>{}<<<", sb.toString());
            }
        }

        // Step 2: From the root we explore all possibly useful trees (recursively)
        compiledDissectors = new HashMap<>();
        usefulIntermediateFields = new HashSet<>();
        findUsefulDissectorsFromField(allPossibleSubtargets, rootType, rootName, true);

        // Step 3: Inform all dissectors to prepare for the run
        for (Set<DissectorPhase> dissectorPhases : compiledDissectors.values()) {
            for (DissectorPhase dissectorPhase : dissectorPhases) {
                dissectorPhase.instance.prepareForRun();
            }
        }
        // Step 4: As a final step we verify that every required input can be found
        Set<String> missingDissectors = getTheMissingFields();
        if (missingDissectors != null && !missingDissectors.isEmpty()) {
            StringBuilder allMissing = new StringBuilder(missingDissectors.size()*64);
            for (String missing:missingDissectors){
                allMissing.append(missing).append(' ');
            }
            throw new MissingDissectorsException(allMissing.toString());
        }

        usable = true;
    }

    // --------------------------------------------

    private void findUsefulDissectorsFromField(
            final Set<String> possibleTargets,
            final String subRootType, final String subRootName,
            final boolean thisIsTheRoot) {

        String subRootId = subRootType + ':' + subRootName;

        LOG.debug("findUsefulDissectors:\"" + subRootType + "\" \"" + subRootName + "\"");

        // When we reach this point we have dissectors to get here.
        // So we store this to later validate if we have everything.
        locatedTargets.add(subRootId);

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
                        LOG.debug("Informing : (" + dissector.inputType + ")" + subRootName
                                + " --> " + dissector.instance.getClass().getName()
                                + " --> (" + dissector.outputType + ")" + checkField);
                    }
                    castsOfTargets.put(dissector.outputType + ':' + checkField,
                            dissectorPhaseInstance.instance.prepareForDissect(subRootName, checkField));

                    // Recurse from this point down
                    findUsefulDissectorsFromField(possibleTargets, dissector.outputType, checkField, false);
                }
            }
        }

        Set<String> mappings = typeRemappings.get(subRootName);
        if (mappings != null) {
            for (String mappedType : mappings) {
                if (!compiledDissectors.containsKey(mappedType + ':' + subRootName)) {
                    // Retyped targets are ALWAYS String ONLY.
                    castsOfTargets.put(mappedType + ':' + subRootName, Casts.STRING_ONLY);
                    findUsefulDissectorsFromField(possibleTargets, mappedType, subRootName, false);
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

    private Set<String> getTheMissingFields() {
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
                addParseTarget(method, Arrays.asList(field.value()));
            }
        }
    }

    // --------------------------------------------

    /*
     * When there is a need to add a target callback manually use this method. */
    public void addParseTarget(final Method method, final List<String> fieldValues) {
        if (method == null || fieldValues == null) {
            return; // Nothing to do here
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
                String cleanedFieldValue = cleanupFieldValue(fieldValue);
                if (!fieldValue.equals(cleanedFieldValue)) {
                    LOG.warn("The requested \"" + fieldValue + "\" was converted into \"" + cleanedFieldValue + "\" ");
                }

                // We have 1 real target
                Set<Method> fieldTargets = targets.get(cleanedFieldValue);
                if (fieldTargets == null) {
                    fieldTargets = new HashSet<>();
                }
                fieldTargets.add(method);
                targets.put(cleanedFieldValue, fieldTargets);
            }
        } else {
            throw new InvalidFieldMethodSignature(method);
        }

        compiledDissectors = null;
    }

    // --------------------------------------------

    private Map<String,Set<String>> typeRemappings = new HashMap<>(16);

    public void setTypeRemappings(Map<String, Set<String>> typeRemappings) {
        if (typeRemappings == null) {
            this.typeRemappings.clear();
        } else {
            this.typeRemappings = typeRemappings;
        }
    }

    public void addTypeRemappings(Map<String, Set<String>> additionalTypeRemappings) {
        for (Map.Entry<String,Set<String>> entry: additionalTypeRemappings.entrySet()){
            String input = entry.getKey();
            for (String newType: entry.getValue() ) {
                addTypeRemapping(input, newType, Casts.STRING_ONLY);
            }
        }
    }

    public void addTypeRemapping(String input, String newType) {
        addTypeRemapping(input, newType, Casts.STRING_ONLY);
    }

    public void addTypeRemapping(String input, String newType, EnumSet<Casts> newCasts) {
        if (compiledDissectors != null) {
            throw new CannotChangeDissectorsAfterConstructionException();
        }

        String theInput = input.trim().toLowerCase(Locale.ENGLISH);
        String theType = newType.trim().toUpperCase(Locale.ENGLISH);
        
        Set<String> mappingsForInput = typeRemappings.get(theInput); 
        if (mappingsForInput == null) {
            mappingsForInput = new HashSet<>();
            typeRemappings.put(theInput,mappingsForInput);
        }

        if (!mappingsForInput.contains(theType)) {
            mappingsForInput.add(theType);
            castsOfTargets.put(theType+':'+theInput,newCasts);
        }
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
        parsable.setRootDissection(rootType, rootName, value);
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
        parsable.setRootDissection(rootType, rootName, value);
        return parse(parsable).getRecord();
    }

    // --------------------------------------------

    Parsable<RECORD> parse(final Parsable<RECORD> parsable)
        throws DissectionFailure, InvalidDissectorException, MissingDissectorsException {
        assembleDissectors();

        if (!usable) {
            return null;
        }

        // Values look like "TYPE:foo.bar"
        Set<ParsedField> toBeParsed = new HashSet<>(parsable.getToBeParsed());

        while (toBeParsed.size() > 0) {
            for (ParsedField fieldThatNeedsToBeParsed : toBeParsed) {
                parsable.setAsParsed(fieldThatNeedsToBeParsed);
                Set<DissectorPhase> dissectorSet = compiledDissectors.get(fieldThatNeedsToBeParsed.getId());
                if (dissectorSet != null) {
                    for (DissectorPhase dissector : dissectorSet) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Dissect " + fieldThatNeedsToBeParsed + " with " + dissector.instance.getClass().getName());
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

    void store(final RECORD record, final String key, final String name, final String value) {
        boolean calledASetter = false;
        
        final Set<Method> methods = targets.get(key);
        if (methods == null) {
            LOG.error("NO methods for \"" + key + "\"");
            return;
        }

        EnumSet<Casts> castsTo = castsOfTargets.get(key);
        if (castsTo == null) {
            castsTo = castsOfTargets.get(name);
            if (castsTo == null) {
                LOG.error("NO casts for \"" + name + "\"");
                return;
            }
        }

        for (Method method : methods) {
            if (method != null) {
                try {
                    Class<?>[] parameters = method.getParameterTypes();
                    Class<?> valueClass = parameters[parameters.length - 1]; // Always the last one

                    if (valueClass == String.class) {
                        if (castsTo.contains(Casts.STRING)) {
                            if (parameters.length == 2) {
                                method.invoke(record, name, value);
                            } else {
                                method.invoke(record, value);
                            }
                            calledASetter = true;
                        }
                        continue;
                    } 
                    
                    if (valueClass == Long.class) {
                        if (castsTo.contains(Casts.LONG)) {
                            Long longValue;
                            try {
                                longValue = (value == null ? null : Long.parseLong(value));
                            } catch (NumberFormatException e) {
                                longValue = null;
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
                            Double doubleValue;
                            try {
                                doubleValue = (value == null ? null : Double.parseDouble(value));
                            } catch (NumberFormatException e) {
                                doubleValue = null;
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
                            " castsTo = \"" + castsTo + "\"");
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
            LOG.error("Unable to create instance: " + e.toString());
            return null;
        }
        return createParsable(record);
    }

    // --------------------------------------------

    /**
     * This method is for use by the developer to query the parser about
     * the possible paths that may be extracted.
     * @return A list of all possible paths that could be determined automatically.
     * @throws nl.basjes.parse.core.exceptions.InvalidDissectorException
     * @throws nl.basjes.parse.core.exceptions.MissingDissectorsException
     */
    public List<String> getPossiblePaths() throws MissingDissectorsException, InvalidDissectorException {
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
            return null; // nothing to do.
        }

        List<String> paths = new ArrayList<>();

        Map<String, List<String>> pathNodes = new HashMap<>();

        for (Dissector dissector : allDissectors) {
            final String inputType = dissector.getInputType();
            final List<String> outputs = dissector.getPossibleOutput();
            pathNodes.put(inputType, outputs);
        }

        findAdditionalPossiblePaths(pathNodes, paths, "", rootType, maxDepth);

        for (Map.Entry<String,Set<String>> typeRemappingSet: typeRemappings.entrySet()) {
            for (String typeRemapping: typeRemappingSet.getValue()) {

                String remappedPath = typeRemapping + ':' + typeRemappingSet.getKey();
                LOG.debug("Adding remapped path: {}", remappedPath);
                paths.add(remappedPath);
                findAdditionalPossiblePaths(pathNodes, paths, typeRemappingSet.getKey(), typeRemapping, maxDepth - 1);
            }
        }

        return paths;
    }

    /**
     * Add all child paths in respect to the base (which is already present in the result set)
     */
    private void findAdditionalPossiblePaths(Map<String, List<String>> pathNodes, List<String> paths, String base, String baseType,
            int maxDepth) {
        if (maxDepth == 0) {
            return;
        }

        if (pathNodes.containsKey(baseType)) {
            List<String> childPaths = pathNodes.get(baseType);
            for (String childPath : childPaths) {
                final int colonPos = childPath.indexOf(':');
                final String childType = childPath.substring(0, colonPos);
                final String childName = childPath.substring(colonPos + 1);

                String childBase;
                if (base.isEmpty()) {
                    childBase = childName;
                } else {
                    childBase = base + '.' + childName;
                }
                paths.add(childType+':'+childBase);

                findAdditionalPossiblePaths(pathNodes, paths, childBase, childType, maxDepth - 1);

            }
        }
    }

    // --------------------------------------------

}
