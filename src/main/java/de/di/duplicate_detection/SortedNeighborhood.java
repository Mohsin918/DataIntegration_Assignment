package de.di.duplicate_detection;

import de.di.Relation;
import de.di.duplicate_detection.structures.AttrSimWeight;
import de.di.duplicate_detection.structures.Duplicate;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.Levenshtein;
import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

/**
 * Implements the Sorted Neighborhood Method for duplicate detection in a dataset.
 */
public class SortedNeighborhood {

    /**
     * Internal class to represent a record with its original index.
     */
    @Data
    @AllArgsConstructor
    private static class Record {
        private int originalIndex;
        private String[] attributeValues;
    }

    /**
     * Detects duplicates in the provided relation using the Sorted Neighborhood Method.
     * The method sorts records based on different sorting keys, slides a window over the sorted records,
     * and compares records within the window.
     *
     * @param relation The relation to search for duplicates.
     * @param sortingKeys Attribute indices used for sorting.
     * @param windowSize Size of the window for comparing records.
     * @param recordComparator Comparator to evaluate record similarity.
     * @return A set of detected duplicate pairs.
     */
    public Set<Duplicate> detectDuplicates(Relation relation, int[] sortingKeys, int windowSize, RecordComparator recordComparator) {
        Set<Duplicate> detectedDuplicates = new HashSet<>();

        // Create an array of records with their original indices
        Record[] recordsArray = new Record[relation.getRecords().length];
        for (int i = 0; i < relation.getRecords().length; i++) {
            recordsArray[i] = new Record(i, relation.getRecords()[i]);
        }

        // For each sorting key, sort the records and slide the window to find duplicates
        for (int sortingKey : sortingKeys) {
            Arrays.sort(recordsArray, Comparator.comparing(record -> record.getAttributeValues()[sortingKey]));

            for (int i = 0; i < recordsArray.length - 1; i++) {
                for (int j = i + 1; j < Math.min(recordsArray.length, i + windowSize); j++) {
                    double similarityScore = recordComparator.compare(recordsArray[i].getAttributeValues(), recordsArray[j].getAttributeValues());
                    if (recordComparator.isDuplicate(similarityScore)) {
                        detectedDuplicates.add(new Duplicate(recordsArray[i].getOriginalIndex(), recordsArray[j].getOriginalIndex(), similarityScore, relation));
                    }
                }
            }
        }

        return detectedDuplicates;
    }

    /**
     * Suggests a RecordComparator configured for the provided relation.
     *
     * @param relation The relation for which to suggest a RecordComparator.
     * @return A configured RecordComparator.
     */
    public static RecordComparator suggestRecordComparatorFor(Relation relation) {
        List<AttrSimWeight> similarityWeights = new ArrayList<>();
        double threshold = 0.5; // Adjust this threshold based on expected similarity

        // Define similarity measures and weights for each attribute
        for (int i = 0; i < relation.getAttributes().length; i++) {
            String attributeName = relation.getAttributes()[i];
            AttrSimWeight attributeSimilarityWeight;
            if (attributeName.equalsIgnoreCase("name") || attributeName.equalsIgnoreCase("title")) {
                attributeSimilarityWeight = new AttrSimWeight(i, new Levenshtein(true), 0.3);
            } else if (attributeName.equalsIgnoreCase("description")) {
                attributeSimilarityWeight = new AttrSimWeight(i, new Jaccard(new Tokenizer(2, false), false), 0.2);
            } else {
                attributeSimilarityWeight = new AttrSimWeight(i, new Levenshtein(false), 0.1);
            }
            similarityWeights.add(attributeSimilarityWeight);
        }

        return new RecordComparator(similarityWeights, threshold);
    }
}
