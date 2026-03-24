package com.github.speak2me.app.compose.map.offline

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center

/**
 * 边框求解器：对外暴露单一入口，初始化可以视作 frameWidthMeters 为空时的特殊缩放。
 */
internal interface FrameResolver {
    /**
     * 基于容器尺寸与可选宽度距离，计算当前应显示的边框。
     * frameWidthMeters 为空时返回初始边框；不为空时按约束进行缩放。
     */
    fun resolveFrame(
        containerSize: IntSize,
        aspectRatio: Float,
        frameWidthMeters: Float? = null,
    ): Rect
}

internal interface InitialFrameFactory {
    fun create(containerSize: IntSize, aspectRatio: Float): Rect
}

internal class TopInsetInitialFrameFactory(
    private val marginRatio: Float = INITIAL_MARGIN_RATIO,
) : InitialFrameFactory {
    override fun create(containerSize: IntSize, aspectRatio: Float): Rect {
        val containerWidth = containerSize.width.toFloat()
        val containerHeight = containerSize.height.toFloat()
        val margin = minOf(containerWidth, containerHeight) * marginRatio
        val width = (containerWidth - margin * 2f).coerceAtLeast(0f)
        val height = (width * aspectRatio).coerceAtLeast(0f)
        val halfWidth = width / 2f
        val halfHeight = height / 2f
        val centerX = containerSize.center.x.toFloat()
        val centerY = margin + halfHeight
        return Rect(
            left = centerX - halfWidth,
            top = centerY - halfHeight,
            right = centerX + halfWidth,
            bottom = centerY + halfHeight
        )
    }
}

internal interface FrameScalePolicy {
    fun apply(frame: Rect, frameWidthMeters: Float?): Rect
}

internal class MaxDistanceFrameScalePolicy(
    maxDistanceMeters: Float,
) : FrameScalePolicy {
    private val safeMaxDistanceMeters = maxDistanceMeters.coerceAtLeast(MIN_WIDTH_METERS)

    override fun apply(frame: Rect, frameWidthMeters: Float?): Rect {
        val widthMeters = frameWidthMeters ?: return frame
        if (widthMeters <= safeMaxDistanceMeters) return frame
        val scale = (safeMaxDistanceMeters / widthMeters).coerceIn(0f, 1f)
        return frame.scaleFromCenter(scale)
    }
}

internal class DefaultFrameResolver(
    private val initialFrameFactory: InitialFrameFactory,
    private val frameScalePolicy: FrameScalePolicy,
) : FrameResolver {
    constructor(maxDistanceMeters: Float) : this(
        initialFrameFactory = TopInsetInitialFrameFactory(),
        frameScalePolicy = MaxDistanceFrameScalePolicy(maxDistanceMeters = maxDistanceMeters),
    )

    override fun resolveFrame(
        containerSize: IntSize,
        aspectRatio: Float,
        frameWidthMeters: Float?,
    ): Rect {
        val initialFrame = initialFrameFactory.create(containerSize, aspectRatio)
        return frameScalePolicy.apply(initialFrame, frameWidthMeters)
    }
}

private fun Rect.scaleFromCenter(scale: Float): Rect {
    val rectCenter = center
    val halfWidth = width * scale / 2f
    val halfHeight = height * scale / 2f
    return Rect(
        left = rectCenter.x - halfWidth,
        top = rectCenter.y - halfHeight,
        right = rectCenter.x + halfWidth,
        bottom = rectCenter.y + halfHeight
    )
}
