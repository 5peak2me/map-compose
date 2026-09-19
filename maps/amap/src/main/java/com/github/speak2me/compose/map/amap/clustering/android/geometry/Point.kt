package com.github.speak2me.compose.map.amap.clustering.android.geometry

public open class Point(
    @JvmField public val x: Double,
    @JvmField public val y: Double,
) {
    override fun toString(): String = "Point(x=$x, y=$y)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Point

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    public fun copy(
        x: Double = this.x,
        y: Double = this.y,
    ): Point = Point(x, y)
}