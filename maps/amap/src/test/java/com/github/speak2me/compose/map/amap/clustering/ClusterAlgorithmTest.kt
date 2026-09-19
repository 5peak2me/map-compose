package com.github.speak2me.compose.map.amap.clustering

import com.amap.api.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class ClusterAlgorithmTest {
    @Test
    fun points_within_radius_share_a_cluster() {
        val first = Place(39.9, 116.4)
        val nearby = Place(39.9, 116.40005)

        val clusters = ClusterAlgorithm.cluster(
            items = listOf(first, nearby),
            visibleBounds = ClusterBounds(39.8, 116.3, 40.0, 116.5),
            metersPerPixel = 1.0,
            clusterRadiusPx = 20,
            zoom = 10f,
        )

        assertEquals(1, clusters.size)
        assertEquals(setOf(first, nearby), clusters.single().items)
    }

    private data class Place(val latitude: Double, val longitude: Double) : ClusterItem {
        override val position: LatLng = LatLng(latitude, longitude)
    }
}
