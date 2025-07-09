package com.github.speak2me.compose.map.amap.cluster

import android.graphics.drawable.Drawable

public interface ClusterRender {
    /**
     * 根据聚合点的元素数目返回渲染背景样式
     */
    public fun getDrawable(clusterNum: Int): Drawable?
}