# Map Compose - 多平台地图集成框架

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.7.0-blue.svg)](https://developer.android.com/jetpack/compose)

一个基于 Jetpack Compose 的多平台地图集成框架，支持高德、百度、腾讯和 Google 四种地图服务。

## 📱 功能特性

- 🗺️ **多平台支持**: 高德地图、百度地图、腾讯地图、Google Maps
- 🎨 **现代化UI**: 基于 Jetpack Compose 的声明式UI框架
- 🚀 **高性能**: 支持 Compose 编译器，优化编译速度和性能
- 🔧 **模块化设计**: 每个地图平台独立模块，按需引入
- 📍 **位置服务**: 集成各平台的定位SDK
- 💻 **开发友好**: 完整的Kotlin DSL配置和示例代码

## 📦 项目结构

```
map-compose/
├── app/                     # 演示应用模块
├── maps/                   # 地图集成模块
│   ├── amap/              # 高德地图集成
│   ├── baidu/             # 百度地图集成
│   └── tencent/           # 腾讯地图集成
├── elf-16k-alignment/     # ELF文件16K对齐优化插件
└── build.gradle.kts       # 项目构建配置
```

## 🛠️ 快速开始

### 1. 项目配置

```kotlin
// settings.gradle.kts
include(":app")
include(":elf-16k-alignment")
include(":maps:amap")
include(":maps:baidu")
include(":maps:tencent")
```

```kotlin
// app/build.gradle.kts
dependencies {
    // 高德地图
    implementation(project(":maps:amap"))

    // 百度地图
    implementation(project(":maps:baidu"))

    // 腾讯地图
    implementation(project(":maps:tencent"))

    // Google Maps (通过外部依赖)
    implementation("com.google.maps.android:maps-compose:7.0.0")
}
```

### 2. Application 初始化

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 初始化各地图平台
        AMapInitializer.initialize(this)
        BMapInitializer.initialize(this)
        GMapInitializer.initialize(this)
        TMapInitializer.initialize(this)
    }
}
```

### 3. 基础地图使用

#### 高德地图

```kotlin
@Composable
@AMapComposable
fun AmapScreen() {
    val mapState = rememberAmapState()

    Amap(
        mapState = mapState,
        modifier = Modifier.fillMaxSize()
    )
}
```

#### 百度地图

```kotlin
@Composable
@BaiduMapComposable
fun BaiduMapScreen() {
    val mapState = rememberBaiduMapState()

    BaiduMap(
        mapState = mapState,
        modifier = Modifier.fillMaxSize()
    )
}
```

#### 腾讯地图

```kotlin
@Composable
@TencentMapComposable
fun TencentMapScreen() {
    val mapState = rememberTencentMapState()

    TencentMap(
        mapState = mapState,
        modifier = Modifier.fillMaxSize()
    )
}
```

#### Google Maps

```kotlin
@Composable
fun GoogleMapScreen() {
    val mapState = rememberGoogleMapState()

    GoogleMap(
        mapState = mapState,
        modifier = Modifier.fillMaxSize()
    )
}
```

## 🚀 高级功能

### 地图标记

```kotlin
@Composable
fun MapWithMarkers() {
    val mapState = rememberAmapState()
    val markers = remember {
        listOf(
            LatLng(39.9042, 116.4074), // 北京
            LatLng(31.2304, 121.4737)  // 上海
        )
    }

    Amap(
        mapState = mapState,
        modifier = Modifier.fillMaxSize()
    ) {
        markers.forEach { latLng ->
            Marker(
                position = latLng,
                title = "标记点",
                snippet = "这是一个示例标记"
            )
        }
    }
}
```

### 路线绘制

```kotlin
@Composable
fun MapWithRoute() {
    val mapState = rememberAmapState()
    val polyline = remember {
        listOf(
            LatLng(39.9042, 116.4074), // 起点
            LatLng(39.9142, 116.4174), // 途经点
            LatLng(39.9242, 116.4274)  // 终点
        )
    }

    Amap(
        mapState = mapState,
        modifier = Modifier.fillMaxSize()
    ) {
        Polyline(
            points = polyline,
            color = Color.Red,
            width = 10f
        )
    }
}
```

### 位置服务

```kotlin
@Composable
fun LocationScreen() {
    val locationPermissionHelper = rememberLocationPermissionHelper()
    val lastLocation = remember { mutableStateOf<LatLng?>(null) }

    if (locationPermissionHelper.hasPermission) {
        // 获取当前位置
        locationPermissionHelper.lastLocation?.let { location ->
            lastLocation.value = LatLng(location.latitude, location.longitude)
        }
    } else {
        // 请求位置权限
        locationPermissionHelper.RequestPermission()
    }
}
```

## 🔧 依赖配置

### Gradle 配置

```kotlin
// 项目根目录 build.gradle.kts
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
}

// elf-16k-alignment 插件
apply(plugin = "elf-16k-alignment")
```

### Manifest 权限配置

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- 高德地图 -->
<meta-data
    android:name="com.amap.api.v2.apikey"
    android:value="YOUR_AMAP_API_KEY" />

<!-- 百度地图 -->
<meta-data
    android:name="com.baidu.lbsapi.API_KEY"
    android:value="YOUR_BAIDU_API_KEY" />

<!-- 腾讯地图 -->
<meta-data
    android:name="TencentMapSDK"
    android:value="YOUR_TENCENT_API_KEY" />
```

## 📖 API 参考

### 地图状态

```kotlin
// 地图状态接口
interface MapState {
    val cameraPosition: CameraPosition
    val isLoaded: Boolean
    val map: Any? // 实际的地图对象

    fun moveCamera(cameraUpdate: CameraUpdate)
    fun animateCamera(cameraUpdate: CameraUpdate)
    fun addMarker(options: MarkerOptions): Marker
    fun addPolyline(options: PolylineOptions): Polyline
}
```

### 相机控制

```kotlin
// 相机更新
sealed class CameraUpdate {
    class ZoomTo(val zoom: Float) : CameraUpdate()
    class MoveTo(val target: LatLng, val zoom: Float = 15f) : CameraUpdate()
    class AnimatedMoveTo(val target: LatLng, val zoom: Float = 15f) : CameraUpdate()
}

// 相机位置
data class CameraPosition(
    val target: LatLng,
    val zoom: Float,
    val bearing: Float = 0f,
    val tilt: Float = 0f
)
```

## 🧪 测试

```kotlin
// 运行单元测试
./gradlew test

// 运行Android测试
./gradlew connectedAndroidTest

// 运行特定模块测试
./gradlew :app:test
```

## 📝 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 开发环境设置

1. 克隆项目
```bash
git clone https://github.com/your-username/map-compose.git
cd map-compose
```

2. 打开项目
```bash
./gradlew build
```

3. 运行示例应用
```bash
./gradlew :app:installDebug
```

## 📞 联系方式

- 项目地址: https://github.com/your-username/map-compose
- 问题反馈: https://github.com/your-username/map-compose/issues

## 🔗 相关链接

- [Jetpack Compose 官方文档](https://developer.android.com/jetpack/compose)
- [高德地图开放平台](https://lbs.amap.com/)
- [百度地图开放平台](https://lbsyun.baidu.com/)
- [腾讯地图开放平台](https://lbs.qq.com/)
- [Google Maps Platform](https://developers.google.com/maps)