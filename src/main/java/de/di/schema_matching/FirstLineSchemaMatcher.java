package de.di.schema_matching;

import de.di.Relation;
import de.di.schema_matching.structures.SimilarityMatrix;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.helper.Tokenizer;

public class FirstLineSchemaMatcher {

    /**
     * Matches the attributes of the source and target table and produces a similarity matrix that represents
     * the attribute-to-attribute similarities of the two relations.
     * @param sourceRelation The first relation for the matching that determines the first dimension of the similarity matrix.
     * @param targetRelation The second relation for the matching that determines the second dimension of the similarity matrix.
     * @return The similarity matrix that describes the attribute-to-attribute similarities of the two relations.
     */
    public SimilarityMatrix match(Relation sourceRelation, Relation targetRelation) {
        String[][] sourceColumns = sourceRelation.getColumns();
        String[][] targetColumns = targetRelation.getColumns();

        // Initialize the similarity matrix
        double[][] similarityMatrix = new double[sourceColumns.length][targetColumns.length];

        // Initialize the Tokenizer and Jaccard similarity measure
        Tokenizer tokenizer = new Tokenizer(3, false); // Example token size of 3 and padding set to false
        Jaccard jaccard = new Jaccard(tokenizer, false); // or true if you need bag semantics

        // Calculate the similarity for each pair of columns
        for (int i = 0; i < sourceColumns.length; i++) {
            for (int j = 0; j < targetColumns.length; j++) {
                similarityMatrix[i][j] = jaccard.calculate(sourceColumns[i], targetColumns[j]);
            }
        }

        return new SimilarityMatrix(similarityMatrix, sourceRelation, targetRelation);
    }
}
