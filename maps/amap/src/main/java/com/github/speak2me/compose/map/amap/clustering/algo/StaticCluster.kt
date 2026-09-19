package com.github.speak2me.compose.map.amap.clustering.algo

import com.amap.api.maps.model.LatLng
import com.github.speak2me.compose.map.amap.clustering.Cluster
import com.github.speak2me.compose.map.amap.clustering.ClusterItem

/**
 * A cluster whose center is determined upon creation.
 */
public class StaticCluster<T : ClusterItem>(
    private val mCenter: LatLng,
) : Cluster<T> {
    private val mItems: MutableCollection<T> = LinkedHashSet()

    public fun add(t: T): Boolean = mItems.add(t)

    override val position: LatLng
        get() = mCenter

    public fun remove(t: T): Boolean = mItems.remove(t)

    override val items: Collection<T>
        get() = mItems

    override val size: Int
        get() = mItems.size

    override fun toString(): String =
        "StaticCluster{" +
                "mCenter=" + mCenter +
                ", mItems.size=" + mItems.size +
                '}'

    override fun hashCode(): Int = mCenter.hashCode() + mItems.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is StaticCluster<*>) {
            return false
        }

        return other.mCenter == mCenter && other.mItems == mItems
    }
}
 