package com.github.speak2me.app.compose.map

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2

class FragmentDemoActivity : FragmentActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: MapFragmentPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_demo)

        viewPager = findViewById(R.id.view_pager)

        pagerAdapter = MapFragmentPagerAdapter(this)
        viewPager.adapter = pagerAdapter

    }
}