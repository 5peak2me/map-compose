# map-compose [![Version](https://jitpack.io/v/5peak2me/map-compose.svg)](https://jitpack.io/#5peak2me/map-compose)

[![Kotlin](https://img.shields.io/badge/dynamic/toml?url=https://raw.githubusercontent.com/5peak2me/map-compose/main/gradle/libs.versions.toml&query=$.versions.kotlin&label=Kotlin&color=blue&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/dynamic/toml?url=https://raw.githubusercontent.com/5peak2me/map-compose/main/gradle/libs.versions.toml&query=$.versions.composeBom&label=Compose&color=blue&logo=android)](https://developer.android.com/jetpack/compose)
[![AGP](https://img.shields.io/badge/dynamic/toml?url=https://raw.githubusercontent.com/5peak2me/map-compose/main/gradle/libs.versions.toml&query=$.versions.agp&label=AGP&color=blue&logo=android)](https://developer.android.com/build/releases/gradle-plugin)

`map-compose` 是一组面向 Jetpack Compose 的 Android 地图封装，让高德、百度、腾讯等原生地图 SDK 可以像普通 Compose 组件一样接入、组合和管理生命周期。

实际接入时主要使用 `maps/*` 模块；仓库中的 `app` 仅作为参考实现。

## 支持范围

- 高德地图：`maps:amap`
- 百度地图：`maps:baidu`
- 腾讯地图：`maps:tencent`
- 华为地图：`maps:huawei`

封装层提供的核心能力：

- `AMap`、`BaiduMap`、`TencentMap` 等 Compose 地图容器
- `CameraPositionState`、`MapProperties`、`MapUiSettings`
- `Marker`、`Polyline`、`Polygon`、`Circle`、`GroundOverlay`、`TileOverlay`
- Compose 自定义 Marker / InfoWindow
- 地图点击、POI 点击、加载完成等回调
- `MapEffect` 访问底层原生地图对象

## 接入方式

推荐先按源码模块接入，确认 API、SDK Key 和隐私初始化都跑通后，再按你的发布方式切换到远程依赖。

### 1. 配置仓库

在根项目 `settings.gradle.kts` 中确保有地图 SDK 所需仓库：

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://developer.huawei.com/repo/") } // 仅 Huawei 需要
        maven { url = uri("https://jitpack.io") } // 使用 JitPack 产物时需要
    }
}
```

### 2. 引入模块

源码接入时，把需要的平台模块加入 `settings.gradle.kts`：

```kotlin
include(":maps:amap")
include(":maps:baidu")
include(":maps:tencent")
```

然后在 App 模块中按需依赖：

```kotlin
dependencies {
    implementation(project(":maps:amap"))
    implementation(project(":maps:baidu"))
    implementation(project(":maps:tencent"))

    // Google Maps 走官方 Compose 库
    implementation("com.google.maps.android:maps-compose:8.3.0")
}
```

如果你使用 JitPack，请以 JitPack 页面实际生成的模块坐标为准，把上面的 `project(...)` 替换为远程依赖即可。

### 3. 启用 Compose

宿主 App 需要启用 Compose：

```kotlin
android {
    buildFeatures {
        compose = true
    }
}
```

## Manifest 配置

宿主 App 自己配置权限和各平台 Key。下面是常用最小集合，定位能力按业务需要增减：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

在 `<application>` 中配置地图 Key：

```xml
<!-- 高德 -->
<meta-data
    android:name="com.amap.api.v2.apikey"
    android:value="${AMAP_KEY}" />

<!-- 百度 -->
<meta-data
    android:name="com.baidu.lbsapi.API_KEY"
    android:value="${BAIDU_MAP_KEY}" />

<!-- Google -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />

<!-- 腾讯 -->
<meta-data
    android:name="TencentMapSDK"
    android:value="${TENCENT_MAP_KEY}" />
```

如果使用百度定位，还需要注册定位 Service：

```xml
<service
    android:name="com.baidu.location.f"
    android:enabled="true"
    android:process=":remote" />
```

你可以用 `secrets.properties`、CI 环境变量或自己的配置系统管理这些占位符：

```properties
AMAP_KEY=your_amap_key
BAIDU_MAP_KEY=your_baidu_key
MAPS_API_KEY=your_google_maps_key
TENCENT_MAP_KEY=your_tencent_key
```

同时记得在各地图开放平台配置正确的包名、SHA-1、服务开关和配额。

## Application 初始化

各地图 SDK 对隐私合规初始化有要求。建议在用户同意隐私政策后、首次使用地图前执行初始化。

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 只初始化你的 App 实际接入的平台。
        initAMap()
        initBaiduMap()
        initTencentMap()
        initGoogleMap()
    }

    private fun initAMap() {
        com.amap.api.maps.MapsInitializer.updatePrivacyShow(this, true, true)
        com.amap.api.maps.MapsInitializer.updatePrivacyAgree(this, true)
        com.amap.api.maps.MapsInitializer.initialize(this)

        com.amap.api.location.AMapLocationClient.updatePrivacyShow(this, true, true)
        com.amap.api.location.AMapLocationClient.updatePrivacyAgree(this, true)
    }

    private fun initBaiduMap() {
        com.baidu.mapapi.SDKInitializer.setAgreePrivacy(this, true)
        com.baidu.mapapi.SDKInitializer.initialize(this)
        com.baidu.mapapi.SDKInitializer.setCoordType(com.baidu.mapapi.CoordType.BD09LL)
        com.baidu.location.LocationClient.setAgreePrivacy(true)
    }

    private fun initTencentMap() {
        com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer.setAgreePrivacy(this, true)
        com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer.start(this)
        com.tencent.map.geolocation.TencentLocationManager.setUserAgreePrivacy(true)
    }

    private fun initGoogleMap() {
        com.google.android.gms.maps.MapsInitializer.initialize(this)
    }
}
```

隐私接口的调用时机要以你 App 的合规流程为准，不要在用户未同意隐私政策前启动相关 SDK。

## 基础用法

### 高德地图

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.amap.api.maps.model.LatLng
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.Marker
import com.github.speak2me.compose.map.amap.Polyline
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import com.github.speak2me.compose.map.amap.rememberUpdatedMarkerState

@Composable
fun AMapScreen() {
    val cameraPositionState = rememberCameraPositionState()
    val markerState = rememberUpdatedMarkerState(
        position = LatLng(31.849193, 117.125301)
    )
    val route = listOf(
        LatLng(31.849193, 117.125301),
        LatLng(31.850193, 117.126301)
    )

    AMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { point ->
            println("AMap clicked: $point")
        }
    ) {
        Marker(state = markerState)
        Polyline(points = route)
    }
}
```

### 百度地图

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.baidu.mapapi.model.LatLng
import com.github.speak2me.compose.map.baidu.BaiduMap
import com.github.speak2me.compose.map.baidu.Marker
import com.github.speak2me.compose.map.baidu.rememberCameraPositionState
import com.github.speak2me.compose.map.baidu.rememberUpdatedMarkerState

@Composable
fun BaiduMapScreen() {
    val cameraPositionState = rememberCameraPositionState()
    val markerState = rememberUpdatedMarkerState(
        position = LatLng(31.849193, 117.125301)
    )

    BaiduMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { point ->
            println("Baidu clicked: $point")
        }
    ) {
        Marker(state = markerState)
    }
}
```

### 腾讯地图

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.github.speak2me.compose.map.tencent.Marker
import com.github.speak2me.compose.map.tencent.TencentMap
import com.github.speak2me.compose.map.tencent.rememberCameraPositionState
import com.github.speak2me.compose.map.tencent.rememberUpdatedMarkerState
import com.tencent.tencentmap.mapsdk.maps.model.LatLng

@Composable
fun TencentMapScreen() {
    val cameraPositionState = rememberCameraPositionState()
    val markerState = rememberUpdatedMarkerState(
        position = LatLng(31.849193, 117.125301)
    )

    TencentMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { point ->
            println("Tencent clicked: $point")
        }
    ) {
        Marker(state = markerState)
    }
}
```

### Google Maps

Google Maps 建议直接接官方 `maps-compose`：

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun GoogleMapScreen() {
    val cameraPositionState = rememberCameraPositionState()
    val markerState = rememberUpdatedMarkerState(
        position = LatLng(31.849193, 117.125301)
    )

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(state = markerState)
    }
}
```

## 地图状态与配置

使用 `MapProperties` 控制地图类型、定位开关等地图属性；使用 `MapUiSettings` 控制缩放按钮、指南针、定位按钮等 UI 行为。

```kotlin
var properties by remember {
    mutableStateOf(
        MapProperties(
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL
        )
    )
}

val uiSettings = remember {
    MapUiSettings(
        compassEnabled = false,
        myLocationButtonEnabled = true,
        scaleControlsEnabled = true
    )
}

AMap(
    properties = properties,
    uiSettings = uiSettings
)
```

`MapProperties`、`MapUiSettings` 和 `MapType` 位于各平台自己的 package 下。多平台同时接入时，建议显式 import，避免类型混淆。

## 访问原生 SDK

封装层没有覆盖的平台能力，可以用 `MapEffect` 获取原生地图对象：

```kotlin
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.MapEffect
import com.github.speak2me.compose.map.amap.MapsComposeExperimentalApi

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun NativeAmapScreen() {
    AMap {
        MapEffect(Unit) { map ->
            map.uiSettings.isScaleControlsEnabled = true
        }
    }
}
```

`MapEffect` 适合处理 SDK 专属设置、截图、原生监听器等能力。优先使用封装层已有的 Compose API；只有封装层没有暴露时再下沉到原生对象。

## 坐标系提醒

不同地图 SDK 的坐标系不同，接入时要统一你的业务数据坐标：

- 高德、腾讯通常使用 GCJ-02
- 百度通常使用 BD-09LL
- Google Maps 通常使用 WGS84

如果同一份轨迹、POI 或路线要在多个地图上展示，需要在进入对应地图前完成坐标转换。

## 常见问题

地图空白时，优先检查 Key、包名、SHA-1、地图平台服务开关、设备网络、平台控制台限制，以及是否已经执行 SDK 初始化。

定位不生效时，检查运行时权限、系统定位开关、隐私合规初始化、后台定位策略，以及对应平台定位 SDK 是否已正确接入。

多平台混用时，注意每个平台的 `LatLng`、`MapType`、`Marker` 等类型不是同一个类，建议不要用通配符 import。

## License

源码文件头部声明使用 Apache License 2.0。
