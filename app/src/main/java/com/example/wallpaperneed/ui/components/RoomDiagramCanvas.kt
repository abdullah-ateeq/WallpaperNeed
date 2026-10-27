package com.example.wallpaperneed.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallpaperneed.model.LengthUnit
import com.example.wallpaperneed.model.Opening
import com.example.wallpaperneed.model.OpeningType
import kotlin.math.max
import kotlin.math.min

@Composable
fun RoomDiagramCanvas(
    lengthMm: Double,
    widthMm: Double,
    unit: LengthUnit,
    openings: List<Opening> = emptyList(),
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    val backgroundColor = MaterialTheme.colorScheme.background

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ROOM TOP-VIEW DIAGRAM",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Reserve padding for dimension labels
                val padHorizontal = 50.dp.toPx()
                val padVertical = 40.dp.toPx()
                val availableW = canvasWidth - (padHorizontal * 2)
                val availableH = canvasHeight - (padVertical * 2)

                // Aspect ratio calculation
                val safeLength = if (lengthMm > 0) lengthMm else 5000.0
                val safeWidth = if (widthMm > 0) widthMm else 4000.0
                val ratio = safeLength / safeWidth

                var rectW: Float
                var rectH: Float

                if (availableW / availableH > ratio) {
                    rectH = availableH * 0.85f
                    rectW = (rectH * ratio).toFloat()
                } else {
                    rectW = availableW * 0.85f
                    rectH = (rectW / ratio).toFloat()
                }

                // Center rectangle
                val left = (canvasWidth - rectW) / 2f
                val top = (canvasHeight - rectH) / 2f

                // Draw background grid lines (technical blueprint aesthetic)
                val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                val gridStep = 24.dp.toPx()
                var x = 0f
                while (x < canvasWidth) {
                    drawLine(
                        color = outlineColor.copy(alpha = 0.12f),
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1f,
                        pathEffect = dash
                    )
                    x += gridStep
                }
                var y = 0f
                while (y < canvasHeight) {
                    drawLine(
                        color = outlineColor.copy(alpha = 0.12f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f,
                        pathEffect = dash
                    )
                    y += gridStep
                }

                // Fill room
                drawRoundRect(
                    color = surfaceColor,
                    topLeft = Offset(left, top),
                    size = Size(rectW, rectH),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Draw room perimeter walls
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(left, top),
                    size = Size(rectW, rectH),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw Wall Names (North, South, East, West)
                val wallLabelStyle = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = outlineColor
                )

                // North (Top)
                val northLayout = textMeasurer.measure("NORTH", style = wallLabelStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "NORTH",
                    style = wallLabelStyle,
                    topLeft = Offset(left + (rectW - northLayout.size.width) / 2f, top - northLayout.size.height - 4f)
                )

                // South (Bottom)
                val southLayout = textMeasurer.measure("SOUTH", style = wallLabelStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "SOUTH",
                    style = wallLabelStyle,
                    topLeft = Offset(left + (rectW - southLayout.size.width) / 2f, top + rectH + 6f)
                )

                // West (Left)
                val westLayout = textMeasurer.measure("WEST", style = wallLabelStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "WEST",
                    style = wallLabelStyle,
                    topLeft = Offset(left - westLayout.size.width - 6f, top + (rectH - westLayout.size.height) / 2f)
                )

                // East (Right)
                val eastLayout = textMeasurer.measure("EAST", style = wallLabelStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "EAST",
                    style = wallLabelStyle,
                    topLeft = Offset(left + rectW + 6f, top + (rectH - eastLayout.size.height) / 2f)
                )

                // Draw dimension calipers / arrows
                val caliperColor = secondaryColor
                val dimStyle = TextStyle(
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = caliperColor
                )

                // Top dimension text (Length)
                val lengthText = unit.format(lengthMm, 0)
                val lengthLayout = textMeasurer.measure(lengthText, style = dimStyle)
                val dimY = top - 24.dp.toPx()
                drawLine(
                    color = caliperColor,
                    start = Offset(left, dimY),
                    end = Offset(left + rectW, dimY),
                    strokeWidth = 1.5f
                )
                // Dimension end ticks
                drawLine(color = caliperColor, start = Offset(left, dimY - 4f), end = Offset(left, dimY + 4f), strokeWidth = 1.5f)
                drawLine(color = caliperColor, start = Offset(left + rectW, dimY - 4f), end = Offset(left + rectW, dimY + 4f), strokeWidth = 1.5f)
                drawText(
                    textMeasurer = textMeasurer,
                    text = lengthText,
                    style = dimStyle,
                    topLeft = Offset(left + (rectW - lengthLayout.size.width) / 2f, dimY - lengthLayout.size.height - 2f)
                )

                // Left dimension text (Width)
                val widthText = unit.format(widthMm, 0)
                val widthLayout = textMeasurer.measure(widthText, style = dimStyle)
                val dimX = left - 30.dp.toPx()
                drawLine(
                    color = caliperColor,
                    start = Offset(dimX, top),
                    end = Offset(dimX, top + rectH),
                    strokeWidth = 1.5f
                )
                drawLine(color = caliperColor, start = Offset(dimX - 4f, top), end = Offset(dimX + 4f, top), strokeWidth = 1.5f)
                drawLine(color = caliperColor, start = Offset(dimX - 4f, top + rectH), end = Offset(dimX + 4f, top + rectH), strokeWidth = 1.5f)
                drawText(
                    textMeasurer = textMeasurer,
                    text = widthText,
                    style = dimStyle,
                    topLeft = Offset(dimX - widthLayout.size.width - 4f, top + (rectH - widthLayout.size.height) / 2f)
                )

                // Draw room label in center
                val centerStyle = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = onSurfaceColor.copy(alpha = 0.7f)
                )
                val centerLayout = textMeasurer.measure("ROOM", style = centerStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "ROOM",
                    style = centerStyle,
                    topLeft = Offset(left + (rectW - centerLayout.size.width) / 2f, top + (rectH - centerLayout.size.height) / 2f)
                )

                // Draw opening indicators on walls
                if (openings.isNotEmpty()) {
                    var doorCount = 0
                    var windowCount = 0
                    for (op in openings) {
                        if (op.type == OpeningType.DOOR) doorCount += op.quantity
                        if (op.type == OpeningType.WINDOW) windowCount += op.quantity
                    }

                    // Indicate door on south wall if doors present
                    if (doorCount > 0) {
                        val doorWidthPx = min(rectW * 0.35f, 32.dp.toPx())
                        val doorStartX = left + (rectW - doorWidthPx) / 2f
                        drawRect(
                            color = backgroundColor,
                            topLeft = Offset(doorStartX, top + rectH - 3.dp.toPx()),
                            size = Size(doorWidthPx, 6.dp.toPx())
                        )
                        drawLine(
                            color = secondaryColor,
                            start = Offset(doorStartX, top + rectH),
                            end = Offset(doorStartX + doorWidthPx, top + rectH - doorWidthPx * 0.7f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Indicate window on east wall if windows present
                    if (windowCount > 0) {
                        val winHeightPx = min(rectH * 0.4f, 30.dp.toPx())
                        val winStartY = top + (rectH - winHeightPx) / 2f
                        drawRect(
                            color = primaryColor.copy(alpha = 0.8f),
                            topLeft = Offset(left + rectW - 4.dp.toPx(), winStartY),
                            size = Size(8.dp.toPx(), winHeightPx)
                        )
                    }
                }
            }
        }
    }
}
