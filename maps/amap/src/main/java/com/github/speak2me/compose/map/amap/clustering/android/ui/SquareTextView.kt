package com.github.speak2me.compose.map.amap.clustering.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.TextView
import kotlin.math.max

/**
 * This class is extending from TextView to avoid introducing App Compat dependencies. Android Studio might show an error here or
 * not, depending on the Inspection Settings. It's not really an error, just a warning.
 */
@SuppressLint("AppCompatCustomView")
public class SquareTextView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : TextView(context, attrs, defStyle) {
        private var offsetTop = 0
        private var offsetLeft = 0

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            val width = measuredWidth
            val height = measuredHeight
            val dimension = max(width, height)
            if (width > height) {
                offsetTop = width - height
                offsetLeft = 0
            } else {
                offsetTop = 0
                offsetLeft = height - width
            }
            setMeasuredDimension(dimension, dimension)
        }

        override fun draw(canvas: Canvas) {
            canvas.translate(offsetLeft / 2f, offsetTop / 2f)
            super.draw(canvas)
        }
    }
