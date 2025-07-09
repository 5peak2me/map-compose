package com.github.speak2me.app.compose.map.demo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun drawBitmapWithBackground(@ColorInt color: Int, bitmap: Bitmap): Bitmap {
    val paint = Paint()
    paint.setColor(color)
    return createBitmap(bitmap.getWidth(), bitmap.getHeight()).applyCanvas {
        drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
        drawBitmap(bitmap, 0f, 0f, paint)
    }
}

internal fun Drawable.merge(
    overlay: Drawable,
    width: Int = intrinsicWidth,
    height: Int = intrinsicHeight
): Bitmap {

    val overlayW = (overlay.intrinsicWidth * 0.6).roundToInt()
    val overlayH = overlayW

    val padding = (width - overlayW) / 2

    return createBitmap(width, height).applyCanvas {
        setBounds(0, 0, width, height)
        draw(this)

        overlay.setBounds(padding, padding, padding + overlayW, padding + overlayH)
        overlay.draw(this)
    }
}

/**
 * 保存bitmap到本地文件
 */
internal suspend fun Context.saveBitmapToFile(bitmap: Bitmap, path: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "map_screenshot_$timeStamp.jpg"

            // 对于Android 10+，使用应用专属目录
            val picturesDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MapCompose/$path")
            } else {
                val externalPicturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                File(externalPicturesDir, "MapCompose/$path")
            }

            if (!picturesDir.exists()) {
                picturesDir.mkdirs()
            }

            val file = File(picturesDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }.also {
            if (!it.isNullOrBlank()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@saveBitmapToFile, "保存bitmap到本地文件成功: $it", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
