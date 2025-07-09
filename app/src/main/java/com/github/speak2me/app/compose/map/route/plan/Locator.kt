package com.github.speak2me.app.compose.map.route.plan

import android.content.Context
import android.location.Location
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 定位结果数据类
 */
data class LocationResult(
    val isSuccess: Boolean,
    val latLng: LatLng? = null,
    val bearing: Float = 0f, // 新增：方向角度
    val errorMessage: String? = null,
    val accuracy: Float = 0f,
    val time: Long = 0L
)

/**
 * 定位状态数据类
 */
data class LocationState(
    val isLocating: Boolean = false,
    val currentLocation: LatLng? = null,
    val currentLocationBearing: Float = 0f, // 新增：当前位置的方向角度
    val hasStoredLocation: Boolean = false,
    val error: String? = null
)

/**
 * 定位器类 - 使用链式调用方式
 */
class Locator private constructor(private val context: Context) {
    
    private var locationClient: AMapLocationClient? = null
    private var locationListener: AMapLocationListener? = null
    private var locationSourceListener: LocationSource.OnLocationChangedListener? = null
    
    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private var onLocationSuccess: ((LocationResult) -> Unit)? = null
    private var onLocationError: ((String) -> Unit)? = null
    
    companion object {
        @Volatile
        private var INSTANCE: Locator? = null
        
        fun with(context: Context): Locator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Locator(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * 开始定位
     */
    fun location(): Locator {
        startLocationUpdates()
        return this
    }
    
    /**
     * 设置定位成功回调
     */
    fun onSuccess(callback: (LocationResult) -> Unit): Locator {
        onLocationSuccess = callback
        return this
    }
    
    /**
     * 设置定位失败回调
     */
    fun onError(callback: (String) -> Unit): Locator {
        onLocationError = callback
        return this
    }
    
    /**
     * 更新定位状态
     */
    fun update(): Locator {
        // 触发状态更新
        _locationState.value = _locationState.value.copy(
            currentLocation = _locationState.value.currentLocation
        )
        return this
    }
    
    /**
     * 开始定位（获取当前位置）
     */
    private fun startLocationUpdates() {
        println("开始高德定位")
        
        try {
            println("开始初始化高德定位客户端...")
            _locationState.value = _locationState.value.copy(isLocating = true)

            // 初始化定位客户端
            if (locationClient == null) {
                println("创建新的高德定位客户端")
                locationClient = AMapLocationClient(context)
            } else {
                println("使用现有的高德定位客户端")
            }
            
            // 创建位置监听器
            locationListener = AMapLocationListener { aMapLocation ->
                aMapLocation?.let { location ->
                    if (location.errorCode == 0) {
                        // 定位成功
                        val latLng = LatLng(location.latitude, location.longitude)
                        val bearing = location.bearing // 获取方向角度
                        println("高德定位成功: $latLng, 方向: $bearing")
                        
                        val result = LocationResult(
                            isSuccess = true,
                            latLng = latLng,
                            bearing = bearing,
                            accuracy = location.accuracy,
                            time = location.time
                        )
                        
                        _locationState.value = _locationState.value.copy(
                            currentLocation = latLng,
                            currentLocationBearing = bearing,
                            isLocating = false,
                            hasStoredLocation = true,
                            error = null
                        )
                        
                        // 调用成功回调
                        onLocationSuccess?.invoke(result)
                        
                        // 获取到位置后停止定位
                        stopLocationUpdates()
                    } else {
                        // 定位失败
                        val errorMsg = "定位失败: ${location.errorInfo}, ${location.errorCode}"
                        println("高德定位失败: $errorMsg")
                        
                        _locationState.value = _locationState.value.copy(
                            isLocating = false,
                            error = errorMsg
                        )
                        
                        // 调用错误回调
                        onLocationError?.invoke(errorMsg)
                    }
                }
            }
            
            // 设置定位参数
            val locationOption = AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy // 高精度定位
                isOnceLocation = true // 单次定位
                isWifiScan = true
                isLocationCacheEnable = false // 不使用缓存定位
                interval = 2000 // 定位间隔2秒
                isSysNetworkLocEnable = true
            }
            
            // 设置定位监听器
            locationClient?.setLocationListener(locationListener!!)
            
            // 设置定位参数
            locationClient?.setLocationOption(locationOption)
            
            // 开始定位
            println("开始高德定位...")
            locationClient?.startLocation()
            println("高德定位已启动")
            
        } catch (e: Exception) {
            println("高德定位初始化失败: ${e.message}")
            val errorMsg = "定位初始化失败: ${e.message}"
            
            _locationState.value = _locationState.value.copy(
                isLocating = false,
                error = errorMsg
            )
            
            onLocationError?.invoke(errorMsg)
        }
    }
    
    /**
     * 定位到当前位置（不重新定位，直接跳转到已存储的位置）
     */
    fun locateToCurrentPosition(): Locator {
        val currentLocation = _locationState.value.currentLocation
        if (currentLocation != null) {
            println("定位到当前位置: $currentLocation")
            // 触发状态更新
            _locationState.value = _locationState.value.copy(
                currentLocation = currentLocation
            )
        } else {
            println("没有存储的位置信息")
            val errorMsg = "没有位置信息，请先进行定位"
            _locationState.value = _locationState.value.copy(error = errorMsg)
            onLocationError?.invoke(errorMsg)
        }
        return this
    }
    
    /**
     * 获取LocationSource实例
     */
    fun getLocationSource(): LocationSource {
        return object : LocationSource {
            override fun activate(listener: LocationSource.OnLocationChangedListener) {
                println("LocationSource激活")
                locationSourceListener = listener
                
                // 如果有存储的位置，直接通知
                _locationState.value.currentLocation?.let { latLng ->
                    val location = Location("AMap").apply {
                        latitude = latLng.latitude
                        longitude = latLng.longitude
                        accuracy = 10f
                        time = System.currentTimeMillis()
                    }
                    listener.onLocationChanged(location)
                }
            }

            override fun deactivate() {
                println("LocationSource停用")
                locationSourceListener = null
            }
        }
    }
    
    /**
     * 停止定位
     */
    private fun stopLocationUpdates() {
        try {
            locationClient?.stopLocation()
            locationClient?.onDestroy()
            locationClient = null
            locationListener = null
            // 清理回调引用，防止内存泄漏
            onLocationSuccess = null
            onLocationError = null
        } catch (e: Exception) {
            println("停止高德定位失败: ${e.message}")
        }
    }

    /**
     * 销毁定位器
     */
    fun destroy() {
        try {
            stopLocationUpdates()
            INSTANCE = null
        } catch (e: Exception) {
            println("定位器销毁失败: ${e.message}")
        }
    }
} 
