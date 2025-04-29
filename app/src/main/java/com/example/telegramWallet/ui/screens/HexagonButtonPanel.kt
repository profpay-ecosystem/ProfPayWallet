package com.example.telegramWallet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


fun drawHexagon(size: Float, center: Offset): Path {
    val hexagon = Path()
    val degreeStep = 60.0 // Использование Double для точности вычислений
    // Первая вершина
    hexagon.moveTo(
        (center.x + size * cos(Math.toRadians(0.0))).toFloat(),
        (center.y + size * sin(Math.toRadians(0.0))).toFloat()
    )
    // Следующие вершины
    for (i in 1..6) {
        val angle = Math.toRadians(degreeStep * i)
        hexagon.lineTo(
            (center.x + size * cos(angle)).toFloat(),
            (center.y + size * sin(angle)).toFloat()
        )
    }
    hexagon.close()
    return hexagon
}

@Preview
@Composable
fun HexagonButtonPanel() {
    val hexSize = 60.dp // Размер шестиугольника
    val density = LocalDensity.current
    val hexSizePx = with(density) { hexSize.toPx() }
    val distance = hexSizePx * sqrt(5.0).toFloat()

    val centers = remember { mutableStateListOf<Offset>() }

    MaterialTheme {
        Canvas(modifier = Modifier
            .size(400.dp)
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    centers.forEachIndexed { index, center ->
                        if (pointInsideHexagon(hexSizePx, center, tapOffset)) {
                            println("Hexagon $index tapped")
                        }
                    }
                }
            }
        ) {
            val center = Offset(size.width / 2, size.height / 2)

            // Draw central hexagon
            val hexPath = drawHexagon(hexSizePx, center)
            drawPath(hexPath, color = Color.Red)
            centers.add(center)

            // Draw surrounding hexagons
            for (i in 0..5) {
                val angle = Math.toRadians(60.0 * i).toFloat()
                val xOffset = cos(angle) * distance
                val yOffset = sin(angle) * distance
                val hexPathAround = drawHexagon(hexSizePx, Offset(center.x + xOffset, center.y + yOffset))
                drawPath(hexPathAround, color = Color.Blue)
                centers.add(Offset(center.x + xOffset, center.y + yOffset))
            }
        }
    }
}

fun pointInsideHexagon(size: Float, hexCenter: Offset, point: Offset): Boolean {
    val degreeStep = 60
    var intersections = 0
    var lastVertex = Offset(
        (hexCenter.x + size * cos(Math.toRadians(0.0))).toFloat(),
        (hexCenter.y + size * sin(Math.toRadians(0.0))).toFloat()
    )

    for (i in 1..6) {
        val angle = Math.toRadians((degreeStep * i).toDouble())
        val vertex = Offset(
            (hexCenter.x + size * cos(angle)).toFloat(),
            (hexCenter.y + size * sin(angle)).toFloat()
        )

        // Check if line from lastVertex to vertex crosses horizontal line from point
        if (isLineCross(point, lastVertex, vertex)) {
            intersections++
        }

        lastVertex = vertex
    }

    // Odd number of crossings means the point is inside
    return intersections % 2 == 1
}

fun isLineCross(point: Offset, start: Offset, end: Offset): Boolean {
    if ((start.y > point.y) != (end.y > point.y)) {
        val intersectX = (end.x - start.x) * (point.y - start.y) / (end.y - start.y) + start.x
        return intersectX > point.x
    }
    return false
}
