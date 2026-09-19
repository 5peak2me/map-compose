package com.github.speak2me.compose.map.amap.clustering.algo

import com.amap.api.maps.model.CameraPosition
import com.github.speak2me.compose.map.amap.clustering.ClusterItem

/**
 * This abstract interface provides two methods: one to determine if the map should recluster when
 * the map moves ({@link  #shouldReclusterOnMapMovement()}), and another method to determine the
 * behavior when the camera moves ({@link  #onCameraChange(CameraPosition)} ()})
 *
 * @param <T> The {@link ClusterItem} type
 */
public interface ScreenBasedAlgorithm<T : ClusterItem> : Algorithm<T> {
    public fun shouldReclusterOnMapMovement(): Boolean

    public fun onCameraChange(position: CameraPosition)
}