package de.di.duplicate_detection;

import de.di.Relation;
import de.di.duplicate_detection.structures.Duplicate;

import java.util.HashSet;
import java.util.Set;

public class TransitiveClosure {

    /**
     * Calculates the transitive close over the provided set of duplicates. The result of the transitive closure
     * calculation are all input duplicates together with all additional duplicates that follow from the input
     * duplicates via transitive inference. For example, if (1,2) and (2,3) are two input duplicates, the algorithm
     * adds the transitive duplicate (1,3). Note that the duplicate relationship is commutative, i.e., (1,2) and (2,1)
     * both describe the same duplicate. The algorithm does not add identity duplicates, such as (1,1).
     * @param duplicates The duplicates over which the transitive closure is to be calculated.
     * @return The input set of duplicates with all transitively inferrable additional duplicates.
     */
    public Set<Duplicate> calculate(Set<Duplicate> duplicates) {
        Set<Duplicate> closedDuplicates = new HashSet<>(2 * duplicates.size());

        if (duplicates.size() <= 1)
            return duplicates;

        Relation relation = duplicates.iterator().next().getRelation();
        int numRecords = relation.getRecords().length;

        // Initialize the adjacency matrix
        boolean[][] adjMatrix = new boolean[numRecords][numRecords];

        // Fill the adjacency matrix with the given duplicates
        for (Duplicate dup : duplicates) {
            int index1 = dup.getIndex1();
            int index2 = dup.getIndex2();
            adjMatrix[index1][index2] = true;
            adjMatrix[index2][index1] = true;
        }

        // Apply Warshall's algorithm to compute the transitive closure
        for (int k = 0; k < numRecords; k++) {
            for (int i = 0; i < numRecords; i++) {
                for (int j = 0; j < numRecords; j++) {
                    if (adjMatrix[i][k] && adjMatrix[k][j]) {
                        adjMatrix[i][j] = true;
                    }
                }
            }
        }

        // Collect all pairs from the transitive closure
        for (int i = 0; i < numRecords; i++) {
            for (int j = i + 1; j < numRecords; j++) { // j starts from i+1 to avoid identity and duplicate pairs
                if (adjMatrix[i][j]) {
                    closedDuplicates.add(new Duplicate(i, j, 1.0, relation));
                    closedDuplicates.add(new Duplicate(j, i, 1.0, relation));
                }
            }
        }

        return closedDuplicates;
    }
}
