package de.di.similarity_measures;

import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;

import java.util.*;

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

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Calculate the Jaccard similarity of the two String arrays. Note that the Jaccard similarity needs to be    //
        // calculated differently depending on the token semantics: set semantics remove duplicates while bag         //
        // semantics consider them during the calculation. The solution should be able to calculate the Jaccard       //
        // similarity either of the two semantics by respecting the inner bagSemantics flag.                          //
        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        if(!bagSemantics) {
            //set semantic

            Set<String> setStrings1 = new HashSet<>();
            for(String str: strings1){
                setStrings1.add(str);
            }

            //TODO:    compare performance of each method

            Set<String> setStrings2 = new HashSet<>(Arrays.asList(strings2));

            //intersection
            Set<String> intersection = new HashSet<String>(setStrings1); // use the copy constructor
            intersection.retainAll(setStrings2);
            //TODO: was it shallow copy? did it change setStrings1?
            //answer: shallow copy; didn't change setStrings1

            jaccardSimilarity = (double)intersection.size()/(setStrings1.size()+setStrings2.size()-intersection.size());
        } else {
            //bag semantics or multi set semantics
            Map<String, Integer> mapStrings1 = new HashMap<>();
            Map<String, Integer> mapStrings2 = new HashMap<>();
            //converting string array to dictionary
            for(String str: strings1)
                if(!mapStrings1.containsKey(str))
                    mapStrings1.put(str, 1);
                else
                    mapStrings1.put(str, mapStrings1.get(str) + 1);

            for(String str2: strings2)
                if(!mapStrings2.containsKey(str2))
                    mapStrings2.put(str2, 1);
                else
                    mapStrings2.put(str2, mapStrings2.get(str2) + 1);

            //intersection
            Map<String, Integer> mapIntersection = new HashMap<>(mapStrings1);
            mapIntersection.keySet().retainAll(mapStrings2.keySet());
            for (String str: mapIntersection.keySet()){
                mapIntersection.put((str), Math.min(mapStrings1.get(str),mapStrings2.get(str)));
            }

            int intersectionSum =0;
            for(int i  :mapIntersection.values())
                intersectionSum += i;


            //sum of values in argument sets
            int mapStrings1Size =0;
            int mapStrings2Size =0;

            for(int i  :mapStrings1.values())
                mapStrings1Size += i;

            for (int i  :mapStrings2.values())
                mapStrings2Size += i;

            //jaccard similarity
            //bagging semantic union doesnt subtract intersection; sum of sets  //jaccardSimilarity = (double)intersectionSum/(mapStrings1Size+mapStrings2Size-intersectionSum);
            jaccardSimilarity = (double)intersectionSum/(mapStrings1Size+mapStrings2Size);

        }


        return jaccardSimilarity;
    }
}