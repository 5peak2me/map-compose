package com.github.speak2me.app.compose.map

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.amap.api.maps.model.LatLng

class MapFragmentPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val mapConfigs = listOf(
        MapConfig( // First map - Nanjing
            initialLatLng = LatLng(29.564814, 106.553027),
            initialZoom = 10f,
            title = "Chongqing",
            // LA gets the custom content marker
            markerType = MarkerType.CUSTOM_CONTENT_MARKER
        ),
        MapConfig( // Second map - Hefei
            initialLatLng = LatLng(31.837825, 117.117479),
            initialZoom = 10f,
            title = "Hefei",
            // NYC gets the standard marker
            markerType = MarkerType.STANDARD_MARKER_WITH_SNIPPET,
            standardMarkerSnippet = "The Big Apple!"
        )
    )

    override fun getItemCount(): Int = mapConfigs.size

    override fun createFragment(position: Int): Fragment {
        return AMapComposeFragment.newInstance(mapConfigs[position])
    }
}