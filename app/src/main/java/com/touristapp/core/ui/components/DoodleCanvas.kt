package com.touristapp.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.touristapp.core.util.DoodleStroke

@Composable
fun DoodleCanvas(
    strokes: List<DoodleStroke>,
    onStrokeAdded: (DoodleStroke) -> Unit,
    onSizeMeasured: (IntSize) -> Unit,
    modifier: Modifier = Modifier,
    strokeColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    strokeWidthPx: Float = 6f
) {
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var measuredSize by remember { mutableStateOf(IntSize.Zero) }
    val strokeColorArgb = strokeColor.toArgb().toLong() and 0xFFFFFFFFL

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .onGloballyPositioned { coords ->
                val s = coords.size
                if (s != measuredSize) {
                    measuredSize = s
                    onSizeMeasured(s)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> currentPoints = listOf(offset) },
                    onDrag = { change, _ ->
                        change.consume()
                        currentPoints = currentPoints + change.position
                    },
                    onDragEnd = {
                        if (currentPoints.size > 1) {
                            onStrokeAdded(
                                DoodleStroke(
                                    points = currentPoints,
                                    color = strokeColorArgb,
                                    widthPx = strokeWidthPx
                                )
                            )
                        }
                        currentPoints = emptyList()
                    },
                    onDragCancel = { currentPoints = emptyList() }
                )
            }
    ) {
        strokes.forEach { stroke ->
            drawStroke(stroke.points, Color(stroke.color.toInt()), stroke.widthPx)
        }
        if (currentPoints.isNotEmpty()) {
            drawStroke(currentPoints, strokeColor, strokeWidthPx)
        }
    }
}

private fun DrawScope.drawStroke(points: List<Offset>, color: Color, widthPx: Float) {
    if (points.isEmpty()) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = widthPx, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

@Preview
@Composable
private fun PreviewDoodleCanvas() {
    MaterialTheme {
        DoodleCanvas(
            strokes = emptyList(),
            onStrokeAdded = {},
            onSizeMeasured = {}
        )
    }
}
