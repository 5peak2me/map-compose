package com.github.speak2me.compose.map.amap.cluster

import com.amap.api.maps.model.LatLng

public interface ClusterItem {
    /**
     * 返回聚合元素的地理位置
     */
    public fun getPosition(): LatLng
}