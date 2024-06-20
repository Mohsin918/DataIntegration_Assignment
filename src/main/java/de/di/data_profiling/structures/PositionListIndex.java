package de.di.data_profiling.structures;

import de.di.Relation;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class PositionListIndex {

    private final AttributeList attributes;
    private final List<IntArrayList> clusters;
    private final int[] invertedClusters;

    public PositionListIndex(final AttributeList attributes, final String[] values) {
        this.attributes = attributes;
        this.clusters = this.calculateClusters(values);
        this.invertedClusters = this.calculateInverted(this.clusters, values.length);
    }

    public PositionListIndex(final AttributeList attributes, final List<IntArrayList> clusters, int relationLength) {
        this.attributes = attributes;
        this.clusters = clusters;
        this.invertedClusters = this.calculateInverted(this.clusters, relationLength);
    }

    private List<IntArrayList> calculateClusters(final String[] values) {
        Map<String, IntArrayList> invertedIndex = new HashMap<>(values.length);
        for (int recordIndex = 0; recordIndex < values.length; recordIndex++) {
            invertedIndex.putIfAbsent(values[recordIndex], new IntArrayList());
            invertedIndex.get(values[recordIndex]).add(recordIndex);
        }
        return invertedIndex.values().stream().filter(cluster -> cluster.size() > 1).collect(Collectors.toList());
    }

    private int[] calculateInverted(List<IntArrayList> clusters, int relationLength) {
        int[] invertedClusters = new int[relationLength];
        Arrays.fill(invertedClusters, -1);
        for (int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++) {
            for (int recordIndex : clusters.get(clusterIndex)) {
                invertedClusters[recordIndex] = clusterIndex;
            }
        }
        return invertedClusters;
    }

    public boolean isUnique() {
        return this.clusters.isEmpty();
    }

    public int relationLength() {
        return this.invertedClusters.length;
    }

    public PositionListIndex intersect(PositionListIndex other) {
        List<IntArrayList> clustersIntersection = this.intersect(this.clusters, other.getInvertedClusters());
        AttributeList attributesUnion = this.attributes.union(other.getAttributes());

        return new PositionListIndex(attributesUnion, clustersIntersection, this.relationLength());
    }

    private List<IntArrayList> intersect(List<IntArrayList> clusters, int[] invertedClusters) {
        List<IntArrayList> clustersIntersection = new ArrayList<>();

        // Create a map to store temporary clusters for intersection
        Map<Integer, IntArrayList> tempClusters = new HashMap<>();

        // Iterate over the current clusters
        for (IntArrayList cluster : clusters) {
            // Create a map to hold the intersection of this cluster with other clusters
            Map<Integer, IntArrayList> currentClusterMap = new HashMap<>();

            // For each record in the cluster, check the inverted cluster index in the other PLI
            for (int recordIndex : cluster) {
                int otherClusterIndex = invertedClusters[recordIndex];
                if (otherClusterIndex != -1) {
                    currentClusterMap.putIfAbsent(otherClusterIndex, new IntArrayList());
                    currentClusterMap.get(otherClusterIndex).add(recordIndex);
                }
            }

            // Add all non-trivial intersections to tempClusters
            for (IntArrayList intersectedCluster : currentClusterMap.values()) {
                if (intersectedCluster.size() > 1) {
                    clustersIntersection.add(intersectedCluster);
                }
            }
        }

        return clustersIntersection;
    }
}
