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
// Same cap as `doodleBase64` in the review security rules (tourist-admin firestore.rules):
// anything longer would pass here and then be rejected by the server.
private const val MAX_ENCODED_LENGTH = 200_000

/** Canvas scales tried in order; PNG keeps the stored format the same at every size. */
private val FALLBACK_SCALES = listOf(1f, 0.75f, 0.5f)

sealed interface DoodleEncodeResult {
    data class Success(val base64: String) : DoodleEncodeResult
    data object Empty : DoodleEncodeResult
    data object TooLarge : DoodleEncodeResult
    data class Failure(val cause: Throwable) : DoodleEncodeResult
}

/**
 * Encodes the strokes as a base64 PNG no longer than [MAX_ENCODED_LENGTH]. A drawing too
 * detailed for the full canvas size is rendered again at each of [FALLBACK_SCALES]; the
 * reviews list scales it back up to the same 3:1 box, only slightly softer. At half size
 * even pure noise fits (300 × 100 × 4 raw bytes ≈ 160 000 base64 characters), so
 * [DoodleEncodeResult.TooLarge] only matters if the canvas constants grow.
 */
fun encodeDoodle(
    strokes: List<DoodleStroke>,
    widthPx: Int = DOODLE_CANVAS_WIDTH_PX,
    heightPx: Int = DOODLE_CANVAS_HEIGHT_PX,
    sourceWidthPx: Int = widthPx,
    sourceHeightPx: Int = heightPx
): DoodleEncodeResult {
    if (strokes.isEmpty() || strokes.all { it.points.isEmpty() }) return DoodleEncodeResult.Empty

    return runCatching {
        for (scale in FALLBACK_SCALES) {
            val png = renderPng(
                strokes = strokes,
                widthPx = (widthPx * scale).toInt(),
                heightPx = (heightPx * scale).toInt(),
                sourceWidthPx = sourceWidthPx,
                sourceHeightPx = sourceHeightPx
            )
            val encoded = Base64.encodeToString(png, Base64.NO_WRAP)
            if (encoded.length <= MAX_ENCODED_LENGTH) {
                return@runCatching DoodleEncodeResult.Success(encoded)
            }
        }
        DoodleEncodeResult.TooLarge
    }.getOrElse { DoodleEncodeResult.Failure(it) }
}

private fun renderPng(
    strokes: List<DoodleStroke>,
    widthPx: Int,
    heightPx: Int,
    sourceWidthPx: Int,
    sourceHeightPx: Int
): ByteArray {
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
    return baos.toByteArray()
}

fun decodeDoodle(base64: String): ImageBitmap? = runCatching {
    val bytes = Base64.decode(base64, Base64.NO_WRAP)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
}.getOrNull()
