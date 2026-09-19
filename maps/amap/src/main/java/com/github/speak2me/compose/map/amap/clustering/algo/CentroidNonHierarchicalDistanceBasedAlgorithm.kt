package com.github.speak2me.compose.map.amap.clustering.algo

import com.amap.api.maps.model.LatLng
import com.github.speak2me.compose.map.amap.clustering.Cluster
import com.github.speak2me.compose.map.amap.clustering.ClusterItem

/**
 * A variant of [NonHierarchicalDistanceBasedAlgorithm] that clusters items
 * based on distance but assigns cluster positions at the centroid of their items,
 * instead of using the position of a single item as the cluster position.
 *
 * This algorithm overrides [.getClusters] to compute a geographic centroid
 * for each cluster and creates [StaticCluster] instances positioned at these centroids.
 * This can provide a more accurate visual representation of the cluster location.
 *
 * @param <T> the type of cluster item
</T> */
public open class CentroidNonHierarchicalDistanceBasedAlgorithm<T : ClusterItem> : NonHierarchicalDistanceBasedAlgorithm<T>() {
    /**
     * Computes the centroid (average latitude and longitude) of a collection of cluster items.
     *
     * @param items the collection of cluster items to compute the centroid for
     * @return the centroid [LatLng] of the items
     */
    protected fun computeCentroid(items: Collection<T>): LatLng {
        var latSum = 0.0
        var lngSum = 0.0
        var count = 0
        for (item in items) {
            latSum += item.position.latitude
            lngSum += item.position.longitude
            count++
        }
        return LatLng(latSum / count, lngSum / count)
    }

    /**
     * Returns clusters of items for the given zoom level, with cluster positions
     * set to the centroid of their constituent items rather than the position of
     * any single item.
     *
     * @param zoom the current zoom level
     * @return a set of clusters with centroid positions
     */
    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        val originalClusters = super.getClusters(zoom)
        val newClusters = HashSet<Cluster<T>>()

        for (cluster in originalClusters) {
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