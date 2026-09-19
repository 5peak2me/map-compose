package com.github.speak2me.compose.map.amap.clustering.algo

import com.github.speak2me.compose.map.amap.clustering.Cluster
import com.github.speak2me.compose.map.amap.clustering.ClusterItem

/**
 * Logic for computing clusters
 */
public interface Algorithm<T : ClusterItem> {
    /**
     * Adds an item to the algorithm
     * @param item the item to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    public fun addItem(item: T): Boolean

    /**
     * Adds a collection of items to the algorithm
     * @param items the items to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    public fun addItems(items: Collection<T>): Boolean

    public fun clearItems()

    /**
     * Removes an item from the algorithm
     * @param item the item to be removed
     * @return true if this algorithm contained the specified element (or equivalently, if this
     * algorithm changed as a result of the call).
     */
    public fun removeItem(item: T): Boolean

    /**
     * Updates the provided item in the algorithm
     * @param item the item to be updated
     * @return true if the item existed in the algorithm and was updated, or false if the item did
     * not exist in the algorithm and the algorithm contents remain unchanged.
     */
    public fun updateItem(item: T): Boolean

    /**
     * Removes a collection of items from the algorithm
     * @param items the items to be removed
     * @return true if this algorithm contents changed as a result of the call
     */
    public fun removeItems(items: Collection<T>): Boolean

    public fun getClusters(zoom: Float): Set<Cluster<T>>

    public val items: Collection<T>

    public var maxDistanceBetweenClusteredItems: Int

    public fun lock()

    public fun unlock()
}
