package com.github.speak2me.compose.map.amap.clustering.android.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * RotationLayout rotates the contents of the layout by multiples of 90 degrees.
 *
 * May not work with padding.
 */
public class RotationLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : FrameLayout(context, attrs, defStyle) {
        private var rotation = 0

        public override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            if (rotation == 1 || rotation == 3) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                setMeasuredDimension(measuredHeight, measuredWidth)
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }

        /**
         * @param degrees the rotation, in degrees.
         */
        public fun setViewRotation(degrees: Int) {
            rotation = (degrees + 360) % 360 / 90
        }

        public override fun dispatchDraw(canvas: Canvas) {
            if (rotation == 0) {
                super.dispatchDraw(canvas)
                return
            }
            if (rotation == 1) {
                canvas.translate(width.toFloat(), 0f)
                canvas.rotate(90f, width / 2f, 0f)
                canvas.translate(height / 2f, width / 2f)
            } else if (rotation == 2) {
                canvas.rotate(180f, width / 2f, height / 2f)
            } else {
                canvas.translate(0f, height.toFloat())
                canvas.rotate(270f, width / 2f, 0f)
                canvas.translate(height / 2f, -width / 2f)
            }
            super.dispatchDraw(canvas)
        }
    }