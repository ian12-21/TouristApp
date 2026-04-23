package com.touristapp.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Base64
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

data class DoodleStroke(
    val points: List<Offset>,
    val color: Long,
    val widthPx: Float = 6f
)

const val DOODLE_CANVAS_WIDTH_PX = 600
const val DOODLE_CANVAS_HEIGHT_PX = 200
private const val MAX_ENCODED_LENGTH = 500_000

sealed interface DoodleEncodeResult {
    data class Success(val base64: String) : DoodleEncodeResult
    data object Empty : DoodleEncodeResult
    data object TooLarge : DoodleEncodeResult
    data class Failure(val cause: Throwable) : DoodleEncodeResult
}

fun encodeDoodle(
    strokes: List<DoodleStroke>,
    widthPx: Int = DOODLE_CANVAS_WIDTH_PX,
    heightPx: Int = DOODLE_CANVAS_HEIGHT_PX,
    sourceWidthPx: Int = widthPx,
    sourceHeightPx: Int = heightPx
): DoodleEncodeResult {
    if (strokes.isEmpty() || strokes.all { it.points.isEmpty() }) return DoodleEncodeResult.Empty

    return runCatching {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scaleX = widthPx.toFloat() / sourceWidthPx.coerceAtLeast(1)
        val scaleY = heightPx.toFloat() / sourceHeightPx.coerceAtLeast(1)

        strokes.forEach { stroke ->
            if (stroke.points.isEmpty()) return@forEach
            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                strokeWidth = stroke.widthPx * scaleX
                color = stroke.color.toInt()
            }
            val path = Path().apply {
                val first = stroke.points.first()
                moveTo(first.x * scaleX, first.y * scaleY)
                stroke.points.drop(1).forEach { p ->
                    lineTo(p.x * scaleX, p.y * scaleY)
                }
            }
            canvas.drawPath(path, paint)
        }

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        bitmap.recycle()
        val encoded = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        if (encoded.length > MAX_ENCODED_LENGTH) {
            DoodleEncodeResult.TooLarge
        } else {
            DoodleEncodeResult.Success(encoded)
        }
    }.getOrElse { DoodleEncodeResult.Failure(it) }
}

fun decodeDoodle(base64: String): ImageBitmap? = runCatching {
    val bytes = Base64.decode(base64, Base64.NO_WRAP)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
}.getOrNull()
