package com.github.speak2me.compose.map.tencent

import android.os.Parcelable
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
public class CameraPosition(
    public val target: LatLng?,
    public val zoom: Float,
    public val tilt: Float,
    public val bearing: Float,
    private val thisTriggers: Array<Trigger>
) : Parcelable {
    @IgnoredOnParcel
    public val changedReason: String = ""
    @IgnoredOnParcel
    public val changedReasons: Array<String> = emptyArray()

    public constructor(lat: Double, lng: Double, scale: Float, rotate: Float, skew: Float) :
            this(LatLng(lat, lng), scale, skew, rotate)

    public constructor(target: LatLng, zoom: Float, tilt: Float, bearing: Float) :
            this(target, zoom, tilt, bearing, arrayOf(Trigger.API))

    public fun toSdkPosition(): com.tencent.tencentmap.mapsdk.maps.model.CameraPosition {
        return com.tencent.tencentmap.mapsdk.maps.model.CameraPosition(target, zoom, bearing, tilt)
    }

    public fun getTriggers(): Array<Trigger> = thisTriggers

    public fun fromLatLngZoom(target: LatLng): CameraPosition {
        return CameraPosition(target, zoom, 0.0F, 0.0F, arrayOf(Trigger.API))
    }

    public companion object {
        @JvmStatic
        public fun fromLatLngZoom(target: LatLng, zoomLevel: Float): CameraPosition {
            return CameraPosition(target, zoomLevel, 0.0f, 0.0f)
        }

        @JvmStatic
        public fun builder(): Builder = Builder()

        @JvmStatic
        public fun builder(cameraPosition: CameraPosition): Builder = Builder(cameraPosition)
    }

    public enum class Trigger {
        API,
        GESTURE,
        ANIMATION,
        OTHER;
    }

    public class Builder internal constructor() {

        private var target: LatLng? = null
        private var zoom: Float = 0f
        private var tilt: Float = Float.MIN_VALUE
        private var bearing: Float = Float.MIN_VALUE
        private var thisTriggers: Array<Trigger> = arrayOf(Trigger.API)

        public constructor(cameraPosition: CameraPosition) : this() {
            target = cameraPosition.target
            zoom = cameraPosition.zoom
            tilt = cameraPosition.tilt
            bearing = cameraPosition.bearing
            thisTriggers = cameraPosition.thisTriggers
        }

        public fun trigger(vararg triggers: Trigger): Builder {
            this.thisTriggers = arrayOf(*triggers)
            return this
        }

        public fun target(target: LatLng): Builder {
            this.target = target
            return this
        }

        public fun zoom(zoom: Float): Builder {
            this.zoom = zoom
            return this
        }

        public fun tilt(tilt: Float): Builder {
            this.tilt = tilt
            return this
        }

        public fun bearing(bearing: Float): Builder {
            this.bearing = bearing
            return this
        }

        public fun build(): CameraPosition {
            return CameraPosition(target, zoom, tilt, bearing, thisTriggers)
        }
    }
}
