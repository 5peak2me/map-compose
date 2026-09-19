package com.github.speak2me.compose.map.amap.clustering

import android.content.Context
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnCameraChangeListener
import com.amap.api.maps.AMap.OnInfoWindowClickListener
import com.amap.api.maps.AMap.OnMarkerClickListener
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.Marker
import com.github.speak2me.compose.map.amap.clustering.algo.Algorithm
import com.github.speak2me.compose.map.amap.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.github.speak2me.compose.map.amap.clustering.algo.PreCachingAlgorithmDecorator
import com.github.speak2me.compose.map.amap.clustering.algo.ScreenBasedAlgorithm
import com.github.speak2me.compose.map.amap.clustering.algo.ScreenBasedAlgorithmAdapter
import com.github.speak2me.compose.map.amap.clustering.android.collections.MarkerManager
import com.github.speak2me.compose.map.amap.clustering.view.ClusterRenderer
import com.github.speak2me.compose.map.amap.clustering.view.DefaultClusterRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Groups many items on a map based on zoom level.
 *
 * ClusterManager should be added to the map as an:
 * - [com.amap.api.maps.AMap.OnCameraChangeListener]
 * - [com.amap.api.maps.AMap.OnMarkerClickListener]
 */
public open class ClusterManager<T : ClusterItem>
@JvmOverloads
constructor(
    context: Context,
    private val mMap: AMap,
    public val markerManager: MarkerManager = MarkerManager(mMap),
) : OnCameraChangeListener,
    OnMarkerClickListener,
    OnInfoWindowClickListener {
    public val markerCollection: MarkerManager.Collection = markerManager.newCollection()
    public val clusterMarkerCollection: MarkerManager.Collection = markerManager.newCollection()

    private var mAlgorithm: ScreenBasedAlgorithm<T>
    private var mRenderer: ClusterRenderer<T>

    private var mPreviousCameraPosition: CameraPosition? = null
    private var mClusterTask: Job? = null
    private val mClusterTaskLock: ReadWriteLock = ReentrantReadWriteLock()

    private var mOnClusterItemClickListener: OnClusterItemClickListener<T>? = null
    private var mOnClusterInfoWindowClickListener: OnClusterInfoWindowClickListener<T>? = null
    private var mOnClusterInfoWindowLongClickListener: OnClusterInfoWindowLongClickListener<T>? = null
    private var mOnClusterItemInfoWindowClickListener: OnClusterItemInfoWindowClickListener<T>? = null
    private var mOnClusterItemInfoWindowLongClickListener: OnClusterItemInfoWindowLongClickListener<T>? = null
    private var mOnClusterClickListener: OnClusterClickListener<T>? = null

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        mRenderer = DefaultClusterRenderer(context, mMap, this)
        mAlgorithm =
            ScreenBasedAlgorithmAdapter(
                PreCachingAlgorithmDecorator(
                    NonHierarchicalDistanceBasedAlgorithm(),
                ),
            )
        mRenderer.onAdd()
    }

    public open var renderer: ClusterRenderer<T>
        get() = mRenderer
        set(value) {
            mRenderer.setOnClusterClickListener(null)
            mRenderer.setOnClusterItemClickListener(null)
            clusterMarkerCollection.clear()
            markerCollection.clear()
            mRenderer.onRemove()
            mRenderer = value
            mRenderer.onAdd()
            mRenderer.setOnClusterClickListener(mOnClusterClickListener)
            mRenderer.setOnClusterInfoWindowClickListener(mOnClusterInfoWindowClickListener)
            mRenderer.setOnClusterInfoWindowLongClickListener(mOnClusterInfoWindowLongClickListener)
            mRenderer.setOnClusterItemClickListener(mOnClusterItemClickListener)
            mRenderer.setOnClusterItemInfoWindowClickListener(mOnClusterItemInfoWindowClickListener)
            mRenderer.setOnClusterItemInfoWindowLongClickListener(mOnClusterItemInfoWindowLongClickListener)
            cluster()
        }

    public open var algorithm: Algorithm<T>
        get() = mAlgorithm
        set(value) {
            if (value is ScreenBasedAlgorithm<*>) {
                setAlgorithm(value as ScreenBasedAlgorithm<T>)
            } else {
                setAlgorithm(ScreenBasedAlgorithmAdapter(value))
            }
        }

    public open fun setAlgorithm(algorithm: ScreenBasedAlgorithm<T>) {
        algorithm.lock()
        try {
            val oldAlgorithm = this.algorithm
            mAlgorithm = algorithm

            oldAlgorithm.lock()
            try {
                algorithm.addItems(oldAlgorithm.items)
            } finally {
                oldAlgorithm.unlock()
            }
        } finally {
            algorithm.unlock()
        }

        if (mAlgorithm.shouldReclusterOnMapMovement()) {
            mAlgorithm.onCameraChange(mMap.cameraPosition)
        }

        cluster()
    }

    public open fun setAnimation(animate: Boolean) {
        mRenderer.setAnimation(animate)
    }

    /**
     * Removes all items from the cluster manager. After calling this method you must invoke
     * [.cluster] for the map to be cleared.
     */
    public open fun clearItems() {
        val algorithm = algorithm
        algorithm.lock()
        try {
            algorithm.clearItems()
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Adds items to clusters. After calling this method you must invoke [.cluster] for the
     * state of the clusters to be updated on the map.
     * @param items items to add to clusters
     * @return true if the cluster manager contents changed as a result of the call
     */
    public open fun addItems(items: Collection<@JvmSuppressWildcards T>?): Boolean {
        val algorithm = algorithm
        algorithm.lock()
        try {
            return algorithm.addItems(items!!)
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Adds an item to a cluster. After calling this method you must invoke [.cluster] for
     * the state of the clusters to be updated on the map.
     * @param myItem item to add to clusters
     * @return true if the cluster manager contents changed as a result of the call
     */
    public open fun addItem(myItem: T): Boolean {
        val algorithm = algorithm
        algorithm.lock()
        try {
            return algorithm.addItem(myItem)
        } finally {
            algorithm.unlock()
        }
    }

    public open fun diff(
        add: Collection<@JvmSuppressWildcards T>?,
        remove: Collection<@JvmSuppressWildcards T>?,
        modify: Collection<@JvmSuppressWildcards T>?,
    ) {
        val algorithm = algorithm
        algorithm.lock()
        try {
            // Add items
            if (add != null) {
                for (item in add) {
                    algorithm.addItem(item)
                }
            }

            // Remove items
            if (remove != null) {
                algorithm.removeItems(remove)
            }

            // Modify items
            if (modify != null) {
                for (item in modify) {
                    updateItem(item)
                }
            }
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Removes items from clusters. After calling this method you must invoke [.cluster] for
     * the state of the clusters to be updated on the map.
     * @param items items to remove from clusters
     * @return true if the cluster manager contents changed as a result of the call
     */
    public open fun removeItems(items: Collection<@JvmSuppressWildcards T>?): Boolean {
        val algorithm = algorithm
        algorithm.lock()
        try {
            return algorithm.removeItems(items!!)
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Removes an item from clusters. After calling this method you must invoke [.cluster]
     * for the state of the clusters to be updated on the map.
     * @param item item to remove from clusters
     * @return true if the item was removed from the cluster manager as a result of this call
     */
    public open fun removeItem(item: T): Boolean {
        val algorithm = algorithm
        algorithm.lock()
        try {
            return algorithm.removeItem(item)
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Updates an item in clusters. After calling this method you must invoke [.cluster] for
     * the state of the clusters to be updated on the map.
     * @param item item to update in clusters
     * @return true if the item was updated in the cluster manager, false if the item is not
     * contained within the cluster manager and the cluster manager contents are unchanged
     */
    public open fun updateItem(item: T): Boolean {
        val algorithm = algorithm
        algorithm.lock()
        try {
            return algorithm.updateItem(item)
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Force a re-cluster on the map. You should call this after adding, removing, updating,
     * or clearing item(s).
     */
    public open fun cluster() {
        mClusterTaskLock.writeLock().lock()
        try {
            // Attempt to cancel the in-flight request.
            mClusterTask?.cancel()
            mClusterTask =
                scope.launch {
                    val param = mMap.cameraPosition.zoom
                    val clusters =
                        withContext(Dispatchers.Default) {
                            val algorithm = this@ClusterManager.algorithm
                            algorithm.lock()
                            try {
                                algorithm.getClusters(param)
                            } finally {
                                algorithm.unlock()
                            }
                        }
                    mRenderer.onClustersChanged(clusters)
                }
        } finally {
            mClusterTaskLock.writeLock().unlock()
        }
    }

    override fun onCameraChange(position: CameraPosition?) {

    }

    override fun onCameraChangeFinish(position: CameraPosition?) {
        if (mRenderer is OnCameraChangeListener) {
            (mRenderer as OnCameraChangeListener).onCameraChangeFinish(position)
        }

        mAlgorithm.onCameraChange(mMap.cameraPosition)

        // delegate clustering to the algorithm
        if (mAlgorithm.shouldReclusterOnMapMovement()) {
            cluster()

            // Don't re-compute clusters if the map has just been panned/tilted/rotated.
        } else if (mPreviousCameraPosition == null || mPreviousCameraPosition!!.zoom != mMap.cameraPosition.zoom) {
            mPreviousCameraPosition = mMap.cameraPosition
            cluster()
        }
    }

    /**
     * Might re-cluster.
     */
//    override fun onCameraIdle() {
//        if (mRenderer is OnCameraIdleListener) {
//            (mRenderer as OnCameraIdleListener).onCameraIdle()
//        }
//
//        mAlgorithm.onCameraChange(mMap.cameraPosition)
//
//        // delegate clustering to the algorithm
//        if (mAlgorithm.shouldReclusterOnMapMovement()) {
//            cluster()
//
//            // Don't re-compute clusters if the map has just been panned/tilted/rotated.
//        } else if (mPreviousCameraPosition == null || mPreviousCameraPosition!!.zoom != mMap.cameraPosition.zoom) {
//            mPreviousCameraPosition = mMap.cameraPosition
//            cluster()
//        }
//    }

    override fun onMarkerClick(marker: Marker): Boolean = markerManager.onMarkerClick(marker)

    override fun onInfoWindowClick(marker: Marker) {
        markerManager.onInfoWindowClick(marker)
    }

    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function,
     * the ClusterManager must be added as a click listener to the map.
     */
    public open fun setOnClusterClickListener(listener: OnClusterClickListener<T>?) {
        mOnClusterClickListener = listener
        mRenderer.setOnClusterClickListener(listener)
    }

    /**
     * Sets a callback that's invoked when a Cluster info window is tapped. Note: For this listener to function,
     * the ClusterManager must be added as a info window click listener to the map.
     */
    public open fun setOnClusterInfoWindowClickListener(listener: OnClusterInfoWindowClickListener<T>?) {
        mOnClusterInfoWindowClickListener = listener
        mRenderer.setOnClusterInfoWindowClickListener(listener)
    }

    /**
     * Sets a callback that's invoked when a Cluster info window is long-pressed. Note: For this listener to function,
     * the ClusterManager must be added as a info window click listener to the map.
     */
    public open fun setOnClusterInfoWindowLongClickListener(listener: OnClusterInfoWindowLongClickListener<T>?) {
        mOnClusterInfoWindowLongClickListener = listener
        mRenderer.setOnClusterInfoWindowLongClickListener(listener)
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem is tapped. Note: For this
     * listener to function, the ClusterManager must be added as a click listener to the map.
     */
    public open fun setOnClusterItemClickListener(listener: OnClusterItemClickListener<T>?) {
        mOnClusterItemClickListener = listener
        mRenderer.setOnClusterItemClickListener(listener)
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem's Info Window is tapped. Note: For this
     * listener to function, the ClusterManager must be added as a info window click listener to the map.
     */
    public open fun setOnClusterItemInfoWindowClickListener(listener: OnClusterItemInfoWindowClickListener<T>?) {
        mOnClusterItemInfoWindowClickListener = listener
        mRenderer.setOnClusterItemInfoWindowClickListener(listener)
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem's Info Window is long-pressed. Note: For this
     * listener to function, the ClusterManager must be added as a info window click listener to the map.
     */
    public open fun setOnClusterItemInfoWindowLongClickListener(listener: OnClusterItemInfoWindowLongClickListener<T>?) {
        mOnClusterItemInfoWindowLongClickListener = listener
        mRenderer.setOnClusterItemInfoWindowLongClickListener(listener)
    }

    /**
     * Called when a Cluster is clicked.
     */
    public fun interface OnClusterClickListener<T : ClusterItem> {
        /**
         * Called when cluster is clicked.
         * Return true if click has been handled
         * Return false and the click will dispatched to the next listener
         */
        public fun onClusterClick(cluster: Cluster<T>): Boolean
    }

    /**
     * Called when a Cluster's Info Window is clicked.
     */
    public fun interface OnClusterInfoWindowClickListener<T : ClusterItem> {
        public fun onClusterInfoWindowClick(cluster: Cluster<T>)
    }

    /**
     * Called when a Cluster's Info Window is long clicked.
     */
    public fun interface OnClusterInfoWindowLongClickListener<T : ClusterItem> {
        public fun onClusterInfoWindowLongClick(cluster: Cluster<T>)
    }

    /**
     * Called when an individual ClusterItem is clicked.
     */
    public fun interface OnClusterItemClickListener<T : ClusterItem> {
        /**
         * Called when `item` is clicked.
         *
         * @param item the item clicked
         *
         * @return true if the listener consumed the event (i.e. the default behavior should not
         * occur), false otherwise (i.e. the default behavior should occur).  The default behavior
         * is for the camera to move to the marker and an info window to appear.
         */
        public fun onClusterItemClick(item: T): Boolean
    }

    /**
     * Called when an individual ClusterItem's Info Window is clicked.
     */
    public fun interface OnClusterItemInfoWindowClickListener<T : ClusterItem> {
        public fun onClusterItemInfoWindowClick(item: T)
    }

    /**
     * Called when an individual ClusterItem's Info Window is long clicked.
     */
    public fun interface OnClusterItemInfoWindowLongClickListener<T : ClusterItem> {
        public fun onClusterItemInfoWindowLongClick(item: T)
    }
}
 