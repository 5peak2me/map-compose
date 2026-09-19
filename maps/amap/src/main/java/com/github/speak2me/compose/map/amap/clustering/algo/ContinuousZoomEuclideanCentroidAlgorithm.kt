package com.github.speak2me.compose.map.amap.clustering.algo

import com.github.speak2me.compose.map.amap.clustering.Cluster
import com.github.speak2me.compose.map.amap.clustering.ClusterItem
import kotlin.math.pow

/**
 * A variant of [CentroidNonHierarchicalDistanceBasedAlgorithm] that uses
 * continuous zoom scaling and Euclidean distance for clustering.
 *
 * This class overrides [.getClusters] to compute
 * clusters with a zoom-dependent radius, while keeping the centroid-based cluster positions.
 *
 * @param <T> the type of cluster item
 */
public class ContinuousZoomEuclideanCentroidAlgorithm<T : ClusterItem> : CentroidNonHierarchicalDistanceBasedAlgorithm<T>() {
    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        // Continuous zoom — no casting to int
        val zoomSpecificSpan = maxDistanceBetweenClusteredItems.toDouble() / 2.0.pow(zoom.toDouble()) / 256.0

        val visitedCandidates = HashSet<QuadItem<T>>()
        val results = HashSet<Cluster<T>>()
        val distanceToCluster = HashMap<QuadItem<T>, Double>()
        val itemToCluster = HashMap<QuadItem<T>, StaticCluster<T>>()

        synchronized(mQuadTree) {
            for (candidate in getClusteringItems(mQuadTree, zoom)) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue
                }

                val searchBounds = createBoundsFromSpan(candidate.point, zoomSpecificSpan)
                val clusterItems = ArrayList<QuadItem<T>>()
                for (clusterItem in mQuadTree.search(searchBounds)) {
                    val distance = distanceSquared(clusterItem.point, candidate.point)
                    val radiusSquared = (zoomSpecificSpan / 2).pow(2.0)
                    if (distance < radiusSquared) {
                        clusterItems.add(clusterItem)
                    }
                }

                if (clusterItems.size == 1) {
                    // Only the current marker is in range. Just add the single item to the results.
                    results.add(candidate)
                    visitedCandidates.add(candidate)
                    distanceToCluster[candidate] = 0.0
                    continue
                }
                val cluster = StaticCluster<T>(candidate.mClusterItem.position)
                results.add(cluster)

                for (clusterItem in clusterItems) {
                    val existingDistance = distanceToCluster[clusterItem]
                    val distance = distanceSquared(clusterItem.point, candidate.point)
                    if (existingDistance != null) {
                        // Item already belongs to another cluster. Check if it's closer to this cluster.
                        if (existingDistance < distance) {
                            continue
                        }
                        // Move item to the closer cluster.
                        itemToCluster[clusterItem]?.remove(clusterItem.mClusterItem)
                    }
                    distanceToCluster[clusterItem] = distance
                    cluster.add(clusterItem.mClusterItem)
                    itemToCluster[clusterItem] = cluster
                }
                visitedCandidates.addAll(clusterItems)
            }
        }

        // Now, apply the centroid logic from CentroidNonHierarchicalDistanceBasedAlgorithm
        val newClusters = HashSet<Cluster<T>>()
        for (cluster in results) {
            val centroid = computeCentroid(cluster.items)
            val newCluster = StaticCluster<T>(centroid)
            for (item in cluster.items) {
                newCluster.add(item)
            }
            newClusters.add(newCluster)
        }

        return newClusters
    }
}
 