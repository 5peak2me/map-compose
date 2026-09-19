package com.github.speak2me.compose.map.amap.clustering.android.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.github.speak2me.lib.compose.amap.R

/**
 * Draws a bubble with a shadow, filled with any color.
 */
internal class BubbleDrawable(
    context: Context,
) : Drawable() {
    private val shadow: Drawable? = ContextCompat.getDrawable(context, R.drawable.amu_bubble_shadow)
    private val mask: Drawable? = ContextCompat.getDrawable(context, R.drawable.amu_bubble_mask)
    private var color: Int = Color.WHITE

    fun setColor(color: Int) {
        this.color = color
    }

    override fun draw(canvas: Canvas) {
        mask?.draw(canvas)
        canvas.drawColor(color, PorterDuff.Mode.SRC_IN)
        shadow?.draw(canvas)
    }

    override fun setAlpha(alpha: Int): Unit = throw UnsupportedOperationException()

    override fun setColorFilter(cf: ColorFilter?): Unit = throw UnsupportedOperationException()

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"),
    )
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setBounds(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        mask?.setBounds(left, top, right, bottom)
        shadow?.setBounds(left, top, right, bottom)
    }

    override fun setBounds(bounds: Rect) {
        mask?.bounds = bounds
        shadow?.bounds = bounds
    }

    override fun getPadding(padding: Rect): Boolean = mask?.getPadding(padding) ?: false
}