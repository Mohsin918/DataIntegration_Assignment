package de.di.schema_matching;

import de.di.schema_matching.structures.CorrespondenceMatrix;
import de.di.schema_matching.structures.SimilarityMatrix;

import java.util.Arrays;

public class SecondLineSchemaMatcher {

    /**
     * Converts the similarity matrix into a correspondence matrix using the Hungarian algorithm.
     * @param similarityMatrix The input similarity matrix.
     * @return The binary correspondence matrix.
     */
    public CorrespondenceMatrix match(SimilarityMatrix similarityMatrix) {
        double[][] simMatrix = similarityMatrix.getMatrix();

        // Transform similarity matrix to cost matrix
        int numRows = simMatrix.length;
        int numCols = simMatrix[0].length;
        double[][] costMatrix = new double[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                costMatrix[i][j] = 1.0 - simMatrix[i][j];
            }
        }

        // Apply the Hungarian Algorithm
        HungarianAlgorithm hungarian = new HungarianAlgorithm(costMatrix, numRows, numCols);
        int[] optimalMatches = hungarian.execute();

        // Convert matches to binary correspondence matrix
        int[][] corrMatrix = generateCorrespondenceMatrix(optimalMatches, numRows, numCols);
        return new CorrespondenceMatrix(corrMatrix, similarityMatrix.getSourceRelation(), similarityMatrix.getTargetRelation());
    }

    /**
     * Generates a binary correspondence matrix from the optimal matches.
     * @param matches The optimal matches from the Hungarian algorithm.
     * @param numRows Number of rows in the original matrix.
     * @param numCols Number of columns in the original matrix.
     * @return The binary correspondence matrix.
     */
    private int[][] generateCorrespondenceMatrix(int[] matches, int numRows, int numCols) {
        int[][] corrMatrix = new int[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            Arrays.fill(corrMatrix[i], 0);
        }
        for (int i = 0; i < matches.length; i++) {
            if (matches[i] >= 0 && matches[i] < numCols) {
                corrMatrix[i][matches[i]] = 1;
            }
        }
        return corrMatrix;
    }

    private class HungarianAlgorithm {
        private final double[][] costMatrix;
        private final int numRows;
        private final int numCols;
        private final int dimension;
        private final double[] rowLabels, colLabels;
        private final int[] minSlackRowByCol;
        private final double[] minSlackValueByCol;
        private final int[] matchColByRow, matchRowByCol, parentRowByCommittedCol;
        private final boolean[] committedRows;

        public HungarianAlgorithm(double[][] costMatrix, int numRows, int numCols) {
            this.numRows = numRows;
            this.numCols = numCols;
            this.dimension = Math.max(numRows, numCols);
            this.costMatrix = new double[this.dimension][this.dimension];
            for (int i = 0; i < this.dimension; i++) {
                if (i < costMatrix.length) {
                    this.costMatrix[i] = Arrays.copyOf(costMatrix[i], this.dimension);
                } else {
                    this.costMatrix[i] = new double[this.dimension];
                }
            }
            this.rowLabels = new double[this.dimension];
            this.colLabels = new double[this.dimension];
            this.minSlackRowByCol = new int[this.dimension];
            this.minSlackValueByCol = new double[this.dimension];
            this.committedRows = new boolean[this.dimension];
            this.parentRowByCommittedCol = new int[this.dimension];
            this.matchColByRow = new int[this.dimension];
            Arrays.fill(this.matchColByRow, -1);
            this.matchRowByCol = new int[this.dimension];
            Arrays.fill(this.matchRowByCol, -1);
        }

        public int[] execute() {
            reduceMatrix();
            computeInitialLabels();
            greedyMatch();
            int unassignedRow = fetchUnassignedRow();
            while (unassignedRow < dimension) {
                initializePhase(unassignedRow);
                executePhase();
                unassignedRow = fetchUnassignedRow();
            }
            int[] result = Arrays.copyOf(matchColByRow, numRows);
            for (int i = 0; i < result.length; i++) {
                if (result[i] >= numCols) {
                    result[i] = -1;
                }
            }
            return result;
        }

        private void reduceMatrix() {
            for (int i = 0; i < dimension; i++) {
                double minValue = Double.POSITIVE_INFINITY;
                for (int j = 0; j < dimension; j++) {
                    if (costMatrix[i][j] < minValue) {
                        minValue = costMatrix[i][j];
                    }
                }
                for (int j = 0; j < dimension; j++) {
                    costMatrix[i][j] -= minValue;
                }
            }
            double[] minColumnValues = new double[dimension];
            Arrays.fill(minColumnValues, Double.POSITIVE_INFINITY);
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    if (costMatrix[i][j] < minColumnValues[j]) {
                        minColumnValues[j] = costMatrix[i][j];
                    }
                }
            }
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    costMatrix[i][j] -= minColumnValues[j];
                }
            }
        }

        private void computeInitialLabels() {
            Arrays.fill(colLabels, Double.POSITIVE_INFINITY);
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    if (costMatrix[i][j] < colLabels[j]) {
                        colLabels[j] = costMatrix[i][j];
                    }
                }
            }
        }

        private void greedyMatch() {
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    if (matchColByRow[i] == -1 && matchRowByCol[j] == -1
                            && costMatrix[i][j] - rowLabels[i] - colLabels[j] == 0) {
                        match(i, j);
                    }
                }
            }
        }

        private int fetchUnassignedRow() {
            int i;
            for (i = 0; i < dimension; i++) {
                if (matchColByRow[i] == -1) {
                    break;
                }
            }
            return i;
        }

        private void initializePhase(int i) {
            Arrays.fill(committedRows, false);
            Arrays.fill(parentRowByCommittedCol, -1);
            committedRows[i] = true;
            for (int j = 0; j < dimension; j++) {
                minSlackValueByCol[j] = costMatrix[i][j] - rowLabels[i] - colLabels[j];
                minSlackRowByCol[j] = i;
            }
        }

        private void executePhase() {
            while (true) {
                int minSlackRow = -1, minSlackCol = -1;
                double minSlackValue = Double.POSITIVE_INFINITY;
                for (int j = 0; j < dimension; j++) {
                    if (parentRowByCommittedCol[j] == -1) {
                        if (minSlackValueByCol[j] < minSlackValue) {
                            minSlackValue = minSlackValueByCol[j];
                            minSlackRow = minSlackRowByCol[j];
                            minSlackCol = j;
                        }
                    }
                }
                if (minSlackValue > 0) {
                    updateLabels(minSlackValue);
                }
                parentRowByCommittedCol[minSlackCol] = minSlackRow;
                if (matchRowByCol[minSlackCol] == -1) {
                    augmentMatching(minSlackCol);
                    return;
                }
                int row = matchRowByCol[minSlackCol];
                committedRows[row] = true;
                for (int j = 0; j < dimension; j++) {
                    if (parentRowByCommittedCol[j] == -1) {
                        double slack = costMatrix[row][j] - rowLabels[row] - colLabels[j];
                        if (minSlackValueByCol[j] > slack) {
                            minSlackValueByCol[j] = slack;
                            minSlackRowByCol[j] = row;
                        }
                    }
                }
            }
        }

        private void updateLabels(double slack) {
            for (int i = 0; i < dimension; i++) {
                if (committedRows[i]) {
                    rowLabels[i] += slack;
                }
            }
            for (int j = 0; j < dimension; j++) {
                if (parentRowByCommittedCol[j] != -1) {
                    colLabels[j] -= slack;
                } else {
                    minSlackValueByCol[j] -= slack;
                }
            }
        }

        private void augmentMatching(int minSlackCol) {
            int committedCol = minSlackCol;
            int parentRow;
            while (true) {
                parentRow = parentRowByCommittedCol[committedCol];
                int temp = matchColByRow[parentRow];
                match(parentRow, committedCol);
                committedCol = temp;
                if (committedCol == -1) {
                    break;
                }
            }
        }

        private void match(int i, int j) {
            matchColByRow[i] = j;
            matchRowByCol[j] = i;
        }
    }
}
