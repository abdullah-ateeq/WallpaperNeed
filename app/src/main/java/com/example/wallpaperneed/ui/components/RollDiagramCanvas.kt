package com.example.wallpaperneed.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallpaperneed.model.LengthUnit
import com.example.wallpaperneed.model.SingleRollPlan
import java.util.Locale

@Composable
fun RollDiagramCanvas(
    rollPlan: SingleRollPlan,
    rollWidthMm: Double,
    rollLengthMm: Double,
    adjustedDropMm: Double,
    unit: LengthUnit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error
    val leftoverColor = errorColor.copy(alpha = 0.85f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ROLL #${rollPlan.rollIndex} CUTTING SCHEMATIC",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${String.format(Locale.US, "%.1f", rollPlan.efficiencyPercent)}% Usable",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (rollPlan.efficiencyPercent >= 85) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                val canvasW = size.width
                val canvasH = size.height

                // Draw roll as a vertical strip in the center
                val rollDisplayWidth = minOf(canvasW * 0.42f, 150.dp.toPx())
                val rollDisplayHeight = canvasH - 40.dp.toPx()
                val left = (canvasW - rollDisplayWidth) / 2f
                val top = 20.dp.toPx()

                // Draw roll outer container
                drawRoundRect(
                    color = surfaceColor.copy(alpha = 0.35f),
                    topLeft = Offset(left, top),
                    size = Size(rollDisplayWidth, rollDisplayHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(left, top),
                    size = Size(rollDisplayWidth, rollDisplayHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Total roll length scale
                val safeRollLen = if (rollLengthMm > 0) rollLengthMm else 10050.0
                var currentY = top

                // Font styles
                val dropTitleStyle = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                val dropSubStyle = TextStyle(
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = onSurfaceColor.copy(alpha = 0.7f)
                )
                val annotStyle = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryColor
                )

                for (dropItem in rollPlan.drops) {
                    val sectionHeight = (dropItem.lengthMm / safeRollLen * rollDisplayHeight).toFloat()
                    val isLeftover = dropItem.isLeftover

                    if (isLeftover) {
                        // Leftover section (drawn with diagonal hatch or distinct color)
                        drawRoundRect(
                            color = leftoverColor.copy(alpha = 0.2f),
                            topLeft = Offset(left + 2f, currentY),
                            size = Size(rollDisplayWidth - 4f, sectionHeight - 2f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        drawLine(
                            color = leftoverColor,
                            start = Offset(left, currentY),
                            end = Offset(left + rollDisplayWidth, currentY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Leftover text
                        val leftText = "LEFTOVER: ${unit.format(dropItem.lengthMm, 1)}"
                        val leftLayout = textMeasurer.measure(leftText, style = dropSubStyle)
                        drawText(
                            textMeasurer = textMeasurer,
                            text = leftText,
                            style = dropSubStyle.copy(color = errorColor, fontWeight = FontWeight.Bold),
                            topLeft = Offset(left + (rollDisplayWidth - leftLayout.size.width) / 2f, currentY + (sectionHeight - leftLayout.size.height) / 2f)
                        )
                    } else {
                        // Regular wallpaper drop
                        // Inner fill
                        drawRect(
                            color = primaryColor.copy(alpha = 0.08f),
                            topLeft = Offset(left + 2f, currentY + 1f),
                            size = Size(rollDisplayWidth - 4f, sectionHeight - 2f)
                        )

                        // Dividing cut line
                        if (currentY > top) {
                            drawLine(
                                color = primaryColor,
                                start = Offset(left, currentY),
                                end = Offset(left + rollDisplayWidth, currentY),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }

                        // Drop label inside
                        val dropLabel = "DROP ${dropItem.dropNumber}"
                        val matchLabel = "${dropItem.matchLabel} (${unit.format(dropItem.lengthMm, 0)})"

                        val titleLayout = textMeasurer.measure(dropLabel, style = dropTitleStyle)
                        val subLayout = textMeasurer.measure(matchLabel, style = dropSubStyle)

                        val textBlockH = titleLayout.size.height + subLayout.size.height + 2f
                        val textStartY = currentY + (sectionHeight - textBlockH) / 2f

                        drawText(
                            textMeasurer = textMeasurer,
                            text = dropLabel,
                            style = dropTitleStyle,
                            topLeft = Offset(left + (rollDisplayWidth - titleLayout.size.width) / 2f, textStartY)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = matchLabel,
                            style = dropSubStyle,
                            topLeft = Offset(left + (rollDisplayWidth - subLayout.size.width) / 2f, textStartY + titleLayout.size.height + 2f)
                        )

                        // Right-hand measurement bracket
                        val bracketX = left + rollDisplayWidth + 8.dp.toPx()
                        drawLine(
                            color = secondaryColor,
                            start = Offset(bracketX, currentY + 4f),
                            end = Offset(bracketX, currentY + sectionHeight - 4f),
                            strokeWidth = 1.5f
                        )
                        drawLine(color = secondaryColor, start = Offset(bracketX - 4f, currentY + 4f), end = Offset(bracketX, currentY + 4f), strokeWidth = 1.5f)
                        drawLine(color = secondaryColor, start = Offset(bracketX - 4f, currentY + sectionHeight - 4f), end = Offset(bracketX, currentY + sectionHeight - 4f), strokeWidth = 1.5f)

                        val dimText = unit.format(dropItem.lengthMm, 0)
                        val dimLayout = textMeasurer.measure(dimText, style = annotStyle)
                        drawText(
                            textMeasurer = textMeasurer,
                            text = dimText,
                            style = annotStyle,
                            topLeft = Offset(bracketX + 4f, currentY + (sectionHeight - dimLayout.size.height) / 2f)
                        )
                    }

                    currentY += sectionHeight
                }

                // Left dimension for full roll length
                val leftBracketX = left - 10.dp.toPx()
                drawLine(color = primaryColor, start = Offset(leftBracketX, top), end = Offset(leftBracketX, top + rollDisplayHeight), strokeWidth = 1.5f)
                drawLine(color = primaryColor, start = Offset(leftBracketX, top), end = Offset(leftBracketX + 4f, top), strokeWidth = 1.5f)
                drawLine(color = primaryColor, start = Offset(leftBracketX, top + rollDisplayHeight), end = Offset(leftBracketX + 4f, top + rollDisplayHeight), strokeWidth = 1.5f)

                val fullLenText = unit.format(rollLengthMm, 0)
                val fullLenLayout = textMeasurer.measure(fullLenText, style = annotStyle.copy(color = primaryColor))
                drawText(
                    textMeasurer = textMeasurer,
                    text = fullLenText,
                    style = annotStyle.copy(color = primaryColor),
                    topLeft = Offset(leftBracketX - fullLenLayout.size.width - 4f, top + (rollDisplayHeight - fullLenLayout.size.height) / 2f)
                )

                // Top dimension for roll width
                val topDimY = top - 12.dp.toPx()
                val widthText = unit.format(rollWidthMm, 0)
                val widthLayout = textMeasurer.measure(widthText, style = annotStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = widthText,
                    style = annotStyle,
                    topLeft = Offset(left + (rollDisplayWidth - widthLayout.size.width) / 2f, topDimY - widthLayout.size.height)
                )
            }
        }
    }
}
