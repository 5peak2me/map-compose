package com.github.speak2me.app.compose.map

import android.app.Application
import com.github.speak2me.app.compose.map.demo.amap.AMapInitializer
import com.github.speak2me.app.compose.map.demo.baidu.BMapInitializer
import com.github.speak2me.app.compose.map.demo.google.GMapInitializer
import com.github.speak2me.app.compose.map.demo.tencent.TMapInitializer

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        // 高德
        AMapInitializer.initialize(this)

        // 百度
        BMapInitializer.initialize(this)

        // 谷歌
        GMapInitializer.initialize(this)

        // 腾讯
        TMapInitializer.initialize(this)
    }

}
