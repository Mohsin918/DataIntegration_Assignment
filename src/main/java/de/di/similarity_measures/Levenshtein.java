package de.di.similarity_measures;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public class Levenshtein implements SimilarityMeasure {

    public static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    // The choice of whether Levenshtein or DamerauLevenshtein should be calculated.
    private final boolean withDamerau;

    /**
     * Calculates the Levenshtein similarity of the two input strings.
     * The Levenshtein similarity is defined as "1 - normalized Levenshtein distance".
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The (Damerau) Levenshtein similarity of the two arguments.
     */

    @Override
    public double calculate(String string1, String string2) {
        if (string1 == null || string2 == null) {
            return 0;
        }

        if (string1.isEmpty() && string2.isEmpty()) {
            return 1;
        }

        if(withDamerau){
            return calculateDamerauLevenshtein(string1, string2);
        }
        else{
            return calculateLevenshtein(string1, string2);
        }
    }

    private double calculateLevenshtein(String string1, String string2) {
        int[][] matrix = new int[string1.length() + 1][string2.length() + 1];

        for (int i = 0; i <= string1.length(); i++) {
            matrix[i][0] = i;
        }

        for (int j = 0; j <= string2.length(); j++) {
            matrix[0][j] = j;
        }

        for (int i = 1; i <= string1.length(); i++) {
            for (int j = 1; j <= string2.length(); j++) {
                if (string1.charAt(i - 1) == string2.charAt(j - 1)) {
                    matrix[i][j] = matrix[i - 1][j - 1];
                } else {
                    matrix[i][j] = 1 + Math.min(matrix[i - 1][j], Math.min(matrix[i][j - 1], matrix[i - 1][j - 1]));
                }
            }
        }

        return 1 - (double) matrix[string1.length()][string2.length()] / Math.max(string1.length(), string2.length());
    }

    private double calculateDamerauLevenshtein(String string1, String string2) {
        int[][] matrix = new int[string1.length() + 1][string2.length() + 1];

        for (int i = 0; i <= string1.length(); i++) {
            matrix[i][0] = i;
        }

        for (int j = 0; j <= string2.length(); j++) {
            matrix[0][j] = j;
        }

        for (int i = 1; i <= string1.length(); i++) {
            for (int j = 1; j <= string2.length(); j++) {
                if (string1.charAt(i - 1) == string2.charAt(j - 1)) {
                    matrix[i][j] = matrix[i - 1][j - 1];
                } else {
                    matrix[i][j] = 1 + Math.min(matrix[i - 1][j], Math.min(matrix[i][j - 1], matrix[i - 1][j - 1]));

                    if (i > 1 && j > 1 && string1.charAt(i - 1) == string2.charAt(j - 2) && string1.charAt(i - 2) == string2.charAt(j - 1)) {
                        matrix[i][j] = Math.min(matrix[i][j], matrix[i - 2][j - 2] + 1);
                    }
                }
            }
        }

        return 1 - (double) matrix[string1.length()][string2.length()] / Math.max(string1.length(), string2.length());
    }

    /**
     * Calculates the Levenshtein similarity of the two input string lists.
     * The Levenshtein similarity is defined as "1 - normalized Levenshtein distance".
     * For string lists, we consider each list as an ordered list of tokens and calculate the distance as the number of
     * token insertions, deletions, replacements (and swaps) that transform one list into the other.
     * @param strings1 The first string list argument for the similarity calculation.
     * @param strings2 The second string list argument for the similarity calculation.
     * @return The (multiset) Levenshtein similarity of the two arguments.
     */
    @Override
    public double calculate(final String[] strings1, final String[] strings2) {
        if (strings1 == null || strings2 == null) {
            return 0;
        }

        if (strings1.length == 0 && strings2.length == 0) {
            return 1;
        }

        double levenshteinSimilarity = 0;

        int[] upperupperLine = new int[strings1.length + 1];   // line for Damerau lookups
        int[] upperLine = new int[strings1.length + 1];        // line for regular Levenshtein lookups
        int[] lowerLine = new int[strings1.length + 1];        // line to be filled next by the algorithm

        for (int i = 0; i <= strings1.length; i++)
            upperLine[i] = i;

        if (withDamerau) {
            for (int j = 1; j <= strings2.length; j++) {
                lowerLine[0] = j;
                for (int i = 1; i <= strings1.length; i++) {
                    int cost = (strings1[i - 1].equals(strings2[j - 1])) ? 0 : 1;
                    lowerLine[i] = Math.min(upperLine[i] + 1, Math.min(lowerLine[i - 1] + 1, upperLine[i - 1] + cost));
                    if (i > 1 && j > 1 && strings1[i - 1].equals(strings2[j - 2]) && strings1[i - 2].equals(strings2[j - 1])) {
                        lowerLine[i] = Math.min(lowerLine[i], upperupperLine[i - 2] + cost);
                    }
                }
                int[] temp = upperupperLine;
                upperupperLine = upperLine;
                upperLine = lowerLine;
                lowerLine = temp;
            }
            levenshteinSimilarity = 1 - (double) upperLine[strings1.length] / Math.max(strings1.length, strings2.length);
        } else {
            for (int j = 1; j <= strings2.length; j++) {
                lowerLine[0] = j;
                for (int i = 1; i <= strings1.length; i++) {
                    int cost = (strings1[i - 1].equals(strings2[j - 1])) ? 0 : 1;
                    lowerLine[i] = Math.min(upperLine[i] + 1, Math.min(lowerLine[i - 1] + 1, upperLine[i - 1] + cost));
                }
                int[] temp = upperLine;
                upperLine = lowerLine;
                lowerLine = temp;
            }
            levenshteinSimilarity = 1 - (double) upperLine[strings1.length] / Math.max(strings1.length, strings2.length);
        }
        return levenshteinSimilarity;
    }

}
