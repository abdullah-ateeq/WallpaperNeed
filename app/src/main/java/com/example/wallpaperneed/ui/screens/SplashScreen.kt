package com.example.wallpaperneed.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stripProgress = remember { Animatable(0f) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.background

    LaunchedEffect(Unit) {
        // Subtle wallpaper-strip animation over 1.1s
        stripProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1100,
                easing = FastOutSlowInEasing
            )
        )
        delay(150)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "WallpaperNeed",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                ),
                color = primaryColor
            )

            Text(
                text = "WALLPAPER ROLL CALCULATOR",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp
                ),
                color = secondaryColor,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Wallpaper strip diagram illustration
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 140.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw outer wall border
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                        size = Size(w - 16.dp.toPx(), h - 16.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw animated vertical wallpaper strips
                    val numStrips = 8
                    val stripWidth = (w - 24.dp.toPx()) / numStrips
                    val fullHeight = h - 24.dp.toPx()

                    for (i in 0 until numStrips) {
                        val stripX = 12.dp.toPx() + (i * stripWidth)
                        val stripDelayFraction = i.toFloat() / numStrips
                        val stripProgressValue = ((stripProgress.value - stripDelayFraction * 0.4f) / 0.6f).coerceIn(0f, 1f)
                        val animatedStripHeight = fullHeight * stripProgressValue

                        // Wallpaper strip fill
                        val stripColor = if (i % 2 == 0) primaryColor.copy(alpha = 0.18f) else secondaryColor.copy(alpha = 0.18f)
                        drawRoundRect(
                            color = stripColor,
                            topLeft = Offset(stripX + 2f, 12.dp.toPx()),
                            size = Size(stripWidth - 4f, animatedStripHeight),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )

                        // Vertical strip boundary line
                        drawLine(
                            color = primaryColor.copy(alpha = 0.5f),
                            start = Offset(stripX, 12.dp.toPx()),
                            end = Offset(stripX, 12.dp.toPx() + animatedStripHeight),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Measure the wall. Plan the drops. Buy the right number of rolls.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
