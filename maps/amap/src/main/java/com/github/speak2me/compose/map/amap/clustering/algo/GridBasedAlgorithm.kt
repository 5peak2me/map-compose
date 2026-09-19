package com.github.speak2me.compose.map.amap.clustering.algo

import androidx.collection.LongSparseArray
import com.github.speak2me.compose.map.amap.clustering.Cluster
import com.github.speak2me.compose.map.amap.clustering.ClusterItem
import com.github.speak2me.compose.map.amap.clustering.android.geometry.Point
import com.github.speak2me.compose.map.amap.clustering.android.projection.SphericalMercatorProjection
import java.util.Collections
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

/**
 * Groups markers into a grid for clustering. This algorithm organizes items into a two-dimensional grid,
 * facilitating the formation of clusters based on proximity within each grid cell. The grid size determines
 * the spatial granularity of clustering, and clusters are created by aggregating items within the same grid cell.
 *
 * The effectiveness of clustering is influenced by the specified grid size, which determines the spatial resolution of the grid.
 * Smaller grid sizes result in more localized clusters, whereas larger grid sizes lead to broader clusters covering larger areas.
 *
 * @param <T> The type of {@link ClusterItem} to be clustered.
 */
public class GridBasedAlgorithm<T : ClusterItem> : AbstractAlgorithm<T>() {
    private var mGridSize = DEFAULT_GRID_SIZE
    private val mItems: MutableSet<T> = Collections.synchronizedSet(HashSet())

    override fun addItem(item: T): Boolean = mItems.add(item)

    override fun addItems(items: Collection<T>): Boolean = mItems.addAll(items)

    override fun clearItems() {
        mItems.clear()
    }

    override fun removeItem(item: T): Boolean = mItems.remove(item)

    override fun removeItems(items: Collection<T>): Boolean = mItems.removeAll(items.toSet())

    override fun updateItem(item: T): Boolean {
        var result: Boolean
        synchronized(mItems) {
            result = removeItem(item)
            if (result) {
                // Only add the item if it was removed (to help prevent accidental duplicates on map)
                result = addItem(item)
            }
        }
        return result
    }

    override var maxDistanceBetweenClusteredItems: Int
        get() = mGridSize
        set(maxDistance) {
            mGridSize = maxDistance
        }

    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        val numCells = ceil(256 * 2.0.pow(zoom.toDouble()) / mGridSize).toLong()
        val proj = SphericalMercatorProjection(numCells.toDouble())

        val clusters = HashSet<Cluster<T>>()
        val sparseArray = LongSparseArray<StaticCluster<T>>()

        synchronized(mItems) {
            for (item in mItems) {
                val p = proj.toPoint(item.position)
                val coord = getCoord(numCells, p.x, p.y)

                var cluster = sparseArray[coord]
                if (cluster == null) {
                    cluster =
                        StaticCluster(
                            proj.toLatLng(
                                Point(floor(p.x) + .5, floor(p.y) + .5),
                            ),
                        )
                    sparseArray.put(coord, cluster)
                    clusters.add(cluster)
                }
                cluster.add(item)
            }
        }

        return clusters
    }

    override val items: Collection<T>
        get() = mItems

    public companion object {
        private const val DEFAULT_GRID_SIZE = 100

        private fun getCoord(
            numCells: Long,
            x: Double,
            y: Double,
        ): Long = (numCells * floor(x) + floor(y)).toLong()
    }
}
 