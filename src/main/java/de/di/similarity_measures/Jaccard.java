package de.di.similarity_measures;
import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@AllArgsConstructor
public class Jaccard implements SimilarityMeasure {
    // The tokenizer that is used to transform string inputs into token lists.
    private final Tokenizer tokenizer;
    // A flag indicating whether the Jaccard algorithm should use set or bag semantics for the similarity calculation.
    private final boolean bagSemantics;
    /**
     * Calculates the Jaccard similarity of the two input strings. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String string1, String string2) {
        string1 = (string1 == null) ? "" : string1;
        string2 = (string2 == null) ? "" : string2;
        String[] strings1 = this.tokenizer.tokenize(string1);
        String[] strings2 = this.tokenizer.tokenize(string2);
        return this.calculate(strings1, strings2);
    }


    /**
     * Calculates the Jaccard similarity of the two string lists. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param strings1 The first string list argument for the similarity calculation.
     * @param strings2 The second string list argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */


    @Override
    public double calculate(String[] strings1, String[] strings2) {
        double jaccardSimilarity = 0;

        // Calculate Jaccard similarity based on bag semantics
        if (bagSemantics) {
            Map<String, Integer> multisets1 = createMultiset(strings1);
            Map<String, Integer> multisets2 = createMultiset(strings2);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //                                      DATA INTEGRATION ASSIGNMENT                                           //
            // Calculate the Jaccard similarity of the two String arrays. Note that the Jaccard similarity needs to be    //
            // calculated differently depending on the token semantics: set semantics remove duplicates while bag         //
            // semantics consider them during the calculation. The solution should be able to calculate the Jaccard       //
            // similarity either of the two semantics by respecting the inner bagSemantics flag.                          //
            // Calculate intersection size
            int intersectionSize = 0;
            for (String key : multisets1.keySet()) {
                if (multisets2.containsKey(key)) {
                    intersectionSize += Math.min(multisets1.get(key), multisets2.get(key));
                }
            }

            // Calculate union size
            int unionSize = 0;
            for (String key : multisets1.keySet()) {
                unionSize += multisets1.get(key);
            }
            for (String key : multisets2.keySet()) {
                unionSize += multisets2.get(key);
            }

            // Calculate Jaccard similarity
            jaccardSimilarity = (double) intersectionSize / unionSize;
        } else { // Calculate Jaccard similarity based on set semantics
            Set<String> set1 = new HashSet<>(Arrays.asList(strings1));
            Set<String> set2 = new HashSet<>(Arrays.asList(strings2));

            //                                                                                                            //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Calculate intersection size
            int intersectionSize = 0;
            for (String s : set1) {
                if (set2.contains(s)) {
                    intersectionSize++;
                }
            }

            // Calculate union size
            int unionSize = set1.size() + set2.size() - intersectionSize;

            // Calculate Jaccard similarity
            jaccardSimilarity = (double) intersectionSize / unionSize;
        }

        return jaccardSimilarity;
    }

    private Map<String, Integer> createMultiset(String[] strings) {
        Map<String, Integer> multisets = new HashMap<>();
        for (String str : strings) {
            multisets.put(str, multisets.getOrDefault(str, 0) + 1);
        }
        return multisets;
    }
}