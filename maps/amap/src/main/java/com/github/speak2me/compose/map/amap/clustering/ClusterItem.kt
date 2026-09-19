package com.github.speak2me.compose.map.amap.clustering

import com.amap.api.maps.model.LatLng

public interface ClusterItem {
    /**
     * The position of this marker. This must always return the same value.
     */
    public val position: LatLng

    /**
     * The title of this marker.
     */
    public val title: String?

    /**
     * The description of this marker.
     */
    public val snippet: String?

    /**
     * The z-index of this marker.
     */
    public val zIndex: Float?
}