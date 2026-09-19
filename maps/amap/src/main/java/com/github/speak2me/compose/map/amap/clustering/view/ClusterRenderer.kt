package com.github.speak2me.compose.map.amap.clustering.view

import androidx.annotation.StyleRes
import com.github.speak2me.compose.map.amap.clustering.Cluster
import com.github.speak2me.compose.map.amap.clustering.ClusterItem
import com.github.speak2me.compose.map.amap.clustering.ClusterManager.OnClusterClickListener
import com.github.speak2me.compose.map.amap.clustering.ClusterManager.OnClusterInfoWindowClickListener
import com.github.speak2me.compose.map.amap.clustering.ClusterManager.OnClusterInfoWindowLongClickListener
import com.github.speak2me.compose.map.amap.clustering.ClusterManager.OnClusterItemClickListener
import com.github.speak2me.compose.map.amap.clustering.ClusterManager.OnClusterItemInfoWindowClickListener
import com.github.speak2me.compose.map.amap.clustering.ClusterManager.OnClusterItemInfoWindowLongClickListener

/**
 * Renders clusters.
 */
public interface ClusterRenderer<T : ClusterItem> {
    /**
     * Called when the view needs to be updated because new clusters need to be displayed.
     *
     * @param clusters the clusters to be displayed.
     */
    public fun onClustersChanged(clusters: Set<Cluster<T>>)

    public fun setOnClusterClickListener(listener: OnClusterClickListener<T>?)

    public fun setOnClusterInfoWindowClickListener(listener: OnClusterInfoWindowClickListener<T>?)

    public fun setOnClusterInfoWindowLongClickListener(listener: OnClusterInfoWindowLongClickListener<T>?)

    public fun setOnClusterItemClickListener(listener: OnClusterItemClickListener<T>?)

    public fun setOnClusterItemInfoWindowClickListener(listener: OnClusterItemInfoWindowClickListener<T>?)

    public fun setOnClusterItemInfoWindowLongClickListener(listener: OnClusterItemInfoWindowLongClickListener<T>?)

    /**
     * Called to set animation on or off
     */
    public fun setAnimation(animate: Boolean)

    /**
     * Sets the length of the animation in milliseconds.
     */
    public fun setAnimationDuration(animationDurationMs: Long)

    /**
     * Called when the view is added.
     */
    public fun onAdd()

    /**
     * Called when the view is removed.
     */
    public fun onRemove()

    /**
     * Called to determine the color of a Cluster.
     */
    public fun getColor(clusterSize: Int): Int

    /**
     * Called to determine the text appearance of a cluster.
     */
    @StyleRes
    public fun getClusterTextAppearance(clusterSize: Int): Int
}
 