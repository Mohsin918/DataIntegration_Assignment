package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.AttributeList;
import de.di.data_profiling.structures.PositionListIndex;
import de.di.data_profiling.structures.UCC;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UCCProfiler {

    /**
     * Discovers all minimal, non-trivial unique column combinations in the provided relation.
     *
     * @param relation The relation that should be profiled for unique column combinations.
     * @return The list of all minimal, non-trivial unique column combinations in the provided relation.
     */
    public List<UCC> profile(Relation relation) {
        int numAttributes = relation.getAttributes().length;
        List<UCC> uniques = new ArrayList<>();
        List<PositionListIndex> currentNonUniques = new ArrayList<>();

        // Calculate all unary UCCs and unary non-UCCs
        for (int attribute = 0; attribute < numAttributes; attribute++) {
            AttributeList attributes = new AttributeList(attribute);
            PositionListIndex pli = new PositionListIndex(attributes, relation.getColumns()[attribute]);
            if (pli.isUnique()) {
                uniques.add(new UCC(relation, attributes));
                System.out.println("Unary UCC: " + attributes);
            } else {
                currentNonUniques.add(pli);
                System.out.println("Unary Non-UCC: " + attributes);
            }
        }

        // Discover all UCCs of size n > 1
        int level = 1;
        while (!currentNonUniques.isEmpty()) {
            List<PositionListIndex> nextNonUniques = new ArrayList<>();
            Set<AttributeList> candidates = generateCandidates(currentNonUniques);

            for (AttributeList candidate : candidates) {
                String[] mergedColumns = mergeColumns(relation, candidate);
                PositionListIndex pli = new PositionListIndex(candidate, mergedColumns);
                if (pli.isUnique()) {
                    if (isMinimal(candidate, uniques)) {
                        uniques.add(new UCC(relation, candidate));
                        System.out.println("UCC of size " + candidate.size() + ": " + candidate);
                    } else {
                        System.out.println("Non-minimal UCC of size " + candidate.size() + ": " + candidate);
                    }
                } else {
                    nextNonUniques.add(pli);
                    System.out.println("Non-UCC of size " + candidate.size() + ": " + candidate);
                }
            }

            currentNonUniques = nextNonUniques;
            level++;
        }

        // Sort the unique column combinations before returning
        List<UCC> sortedUniques = uniques.stream()
                .sorted((ucc1, ucc2) -> {
                    int sizeDiff = ucc1.getAttributeList().size() - ucc2.getAttributeList().size();
                    if (sizeDiff != 0) {
                        return sizeDiff;
                    }
                    for (int i = 0; i < ucc1.getAttributeList().size(); i++) {
                        int attrDiff = ucc1.getAttributeList().getAttributes()[i] - ucc2.getAttributeList().getAttributes()[i];
                        if (attrDiff != 0) {
                            return attrDiff;
                        }
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        sortedUniques.forEach(ucc -> System.out.println("Sorted UCC: " + ucc.getAttributeList()));
        return sortedUniques;
    }

    /**
     * Generates candidate attribute lists for the next level.
     * @param currentNonUniques The current non-unique PLIs.
     * @return A set of candidate attribute lists for the next level.
     */
    private Set<AttributeList> generateCandidates(List<PositionListIndex> currentNonUniques) {
        Set<AttributeList> candidates = new HashSet<>();

        for (int i = 0; i < currentNonUniques.size(); i++) {
            for (int j = i + 1; j < currentNonUniques.size(); j++) {
                AttributeList attrList1 = currentNonUniques.get(i).getAttributes();
                AttributeList attrList2 = currentNonUniques.get(j).getAttributes();

                if (attrList1.samePrefixAs(attrList2)) {
                    AttributeList candidate = attrList1.union(attrList2);
                    if (isValidCandidate(candidate, currentNonUniques)) {
                        candidates.add(candidate);
                        System.out.println("Generated candidate: " + candidate);
                    } else {
                        System.out.println("Invalid candidate: " + candidate);
                    }
                }
            }
        }

        return candidates;
    }

    /**
     * Checks if a candidate is a valid candidate attribute list.
     * @param candidate The candidate attribute list.
     * @param currentNonUniques The current non-unique PLIs.
     * @return True if the candidate is valid, false otherwise.
     */
    private boolean isValidCandidate(AttributeList candidate, List<PositionListIndex> currentNonUniques) {
        for (PositionListIndex pli : currentNonUniques) {
            if (candidate.sublistOf(pli.getAttributes())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a candidate is a valid minimal unique column combination.
     * @param candidate The candidate attribute list.
     * @param uniques The current list of unique column combinations.
     * @return True if the candidate is minimal, false otherwise.
     */
    private boolean isMinimal(AttributeList candidate, List<UCC> uniques) {
        for (UCC ucc : uniques) {
            if (candidate.supersetOf(ucc.getAttributeList())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Merges multiple columns from the relation based on the attribute list.
     * @param relation The relation.
     * @param attributes The attribute list specifying the columns to merge.
     * @return The merged columns as a single string array.
     */
    private String[] mergeColumns(Relation relation, AttributeList attributes) {
        String[][] records = relation.getRecords();
        int[] attributeIndices = attributes.getAttributes();
        String[] mergedColumns = new String[records.length];

        for (int i = 0; i < records.length; i++) {
            StringBuilder mergedValue = new StringBuilder();
            for (int attributeIndex : attributeIndices) {
                // Check if the attribute index is within bounds
                if (attributeIndex >= 0 && attributeIndex < records[i].length) {
                    mergedValue.append(records[i][attributeIndex]).append(",");
                } else {
                    // Handle case where attribute index is out of bounds
                    // You can choose to append a placeholder value or handle it differently based on your requirements
                    mergedValue.append("N/A").append(",");
                }
            }
            // Remove the last comma from the merged value
            if (mergedValue.length() > 0) {
                mergedValue.deleteCharAt(mergedValue.length() - 1);
            }
            mergedColumns[i] = mergedValue.toString();
        }

        return mergedColumns;
    }
}
