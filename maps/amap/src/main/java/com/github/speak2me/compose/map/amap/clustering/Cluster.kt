package com.github.speak2me.compose.map.amap.clustering

import com.amap.api.maps.model.LatLng

public interface Cluster<T : ClusterItem> {
    public val position: LatLng
    public val items: Collection<T>
    public val size: Int
}
