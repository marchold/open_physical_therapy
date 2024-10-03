package com.catglo.openphysicaltherapy.Widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

enum class CircleSegmentCounterDirection {
    LEFT, RIGHT
}

@Composable
fun CircleSegmentCounter(numberOfArcSegments: Int,
                         numberOfHighlightedSegments: Int,
                         boxSize: Dp,
                         direction: CircleSegmentCounterDirection = CircleSegmentCounterDirection.RIGHT,
                         strokeWidth: Float = 20f,
                         arcColorDone: Color = MaterialTheme.colorScheme.outline,
                         arcColorToDo: Color = MaterialTheme.colorScheme.onBackground,
                         gap: Int = 3
)
{
    Canvas(modifier = Modifier.size(boxSize)) {
        val canvasSize = size.minDimension
        val arcSize = Size(canvasSize, canvasSize)

        val segmentSize = 360f / numberOfArcSegments
        var startAngle = if (direction == CircleSegmentCounterDirection.LEFT) -90f else -90f - segmentSize
        for (i in 0 until numberOfArcSegments) {
            val color = if (i < numberOfHighlightedSegments) {
                arcColorDone
            } else {
                arcColorToDo
            }
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = segmentSize - gap,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                topLeft = Rect(0f, 0f, canvasSize, canvasSize).topLeft,
                size = arcSize
            )
            if (direction == CircleSegmentCounterDirection.LEFT) {
                startAngle += segmentSize
            } else {
                startAngle -= segmentSize
            }
        }
    }
}
