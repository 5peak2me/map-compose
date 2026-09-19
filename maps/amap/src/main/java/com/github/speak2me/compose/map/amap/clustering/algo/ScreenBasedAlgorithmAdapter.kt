package com.github.speak2me.compose.map.amap.clustering.algo

import com.amap.api.maps.model.CameraPosition
import com.github.speak2me.compose.map.amap.clustering.Cluster
import com.github.speak2me.compose.map.amap.clustering.ClusterItem

public class ScreenBasedAlgorithmAdapter<T : ClusterItem>(
    private val algorithm: Algorithm<T>,
) : AbstractAlgorithm<T>(),
    ScreenBasedAlgorithm<T> {
    override fun shouldReclusterOnMapMovement(): Boolean = false

    override fun addItem(item: T): Boolean = algorithm.addItem(item)

    override fun addItems(items: Collection<T>): Boolean = algorithm.addItems(items)

    override fun clearItems() {
        algorithm.clearItems()
    }

    override fun removeItem(item: T): Boolean = algorithm.removeItem(item)

    override fun removeItems(items: Collection<T>): Boolean = algorithm.removeItems(items)

    override fun updateItem(item: T): Boolean = algorithm.updateItem(item)

    override fun getClusters(zoom: Float): Set<Cluster<T>> = algorithm.getClusters(zoom)

    override val items: Collection<T>
        get() = algorithm.items

    override var maxDistanceBetweenClusteredItems: Int
        get() = algorithm.maxDistanceBetweenClusteredItems
        set(maxDistance) {
            algorithm.maxDistanceBetweenClusteredItems = maxDistance
        }

    override fun onCameraChange(position: CameraPosition) {
        // stub
    }
}