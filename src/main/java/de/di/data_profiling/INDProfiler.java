package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.IND;
import de.di.data_profiling.structures.AttributeList;

import java.util.*;

public class INDProfiler {

    /**
     * Discovers all non-trivial unary inclusion dependencies in the provided relations using a hash-based approach.
     * @param relations The list of relations to be profiled for inclusion dependencies.
     * @param discoverNary Whether to discover n-ary INDs (currently not supported).
     * @return A list of all discovered non-trivial unary inclusion dependencies.
     */
    public List<IND> profile(List<Relation> relations, boolean discoverNary) {
        List<IND> inclusionDependencies = new ArrayList<>();

        Map<String, Map<String, Set<Integer>>> columnHashes = new HashMap<>();
        for (Relation relation : relations) {
            String relationName = relation.getName();
            columnHashes.put(relationName, new HashMap<>());
            String[][] columns = relation.getColumns();

            for (int colIndex = 0; colIndex < columns.length; colIndex++) {
                String attributeName = relation.getAttributes()[colIndex];
                Set<Integer> hashedValues = new HashSet<>();

                for (String value : columns[colIndex]) {
                    int hashValue = hashValue(normalizeValue(value));
                    hashedValues.add(hashValue);
                }

                columnHashes.get(relationName).put(attributeName, hashedValues);
            }
        }

        for (Relation relation : relations) {
            String relationName = relation.getName();
            for (int colIndex = 0; colIndex < relation.getAttributes().length; colIndex++) {
                String attributeName = relation.getAttributes()[colIndex];
                Set<Integer> baseSet = columnHashes.get(relationName).get(attributeName);

                for (Relation otherRelation : relations) {
                    String otherRelationName = otherRelation.getName();
                    for (int otherColIndex = 0; otherColIndex < otherRelation.getAttributes().length; otherColIndex++) {
                        if (relation == otherRelation && colIndex == otherColIndex) continue; // Skip trivial dependencies

                        String otherAttributeName = otherRelation.getAttributes()[otherColIndex];
                        Set<Integer> compareSet = columnHashes.get(otherRelationName).get(otherAttributeName);

                        if (compareSet.containsAll(baseSet)) {
                            inclusionDependencies.add(new IND(relation, colIndex, otherRelation, otherColIndex));
                        }
                    }
                }
            }
        }

        if (discoverNary) {
            throw new UnsupportedOperationException("N-ary IND discovery is not supported.");
        }

        return inclusionDependencies;
    }

    private String normalizeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("^\"|\"$", "");
    }

    private int hashValue(String value) {
        return value.hashCode();
    }
}
