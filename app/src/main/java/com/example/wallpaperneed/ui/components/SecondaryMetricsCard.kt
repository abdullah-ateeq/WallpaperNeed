package com.example.wallpaperneed.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallpaperneed.model.LengthUnit
import com.example.wallpaperneed.model.WallpaperResult
import java.util.Locale

@Composable
fun SecondaryMetricsCard(
    result: WallpaperResult,
    decimalPlaces: Int = 2,
    showOpeningArea: Boolean = true,
    showPurchaseExcess: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "SURFACE AREA VS. PURCHASE MATERIAL",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            AreaMetricRow(
                label = "Gross Wall Area",
                value = LengthUnit.formatArea(result.grossWallAreaMm2, decimalPlaces),
                description = "Total wall surface based on perimeter & ceiling height"
            )

            if (showOpeningArea && result.openingAreaMm2 > 0.0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                AreaMetricRow(
                    label = "Opening Area (Doors/Windows)",
                    value = LengthUnit.formatArea(result.openingAreaMm2, decimalPlaces),
                    description = "Excluded surface area deduction for reference"
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            AreaMetricRow(
                label = "Net Wall Area",
                value = LengthUnit.formatArea(result.netWallAreaMm2, decimalPlaces),
                description = "Net wall coverage surface area (informational reference only)"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            AreaMetricRow(
                label = "Total Wallpaper Purchase Area",
                value = LengthUnit.formatArea(result.purchaseAreaMm2, decimalPlaces),
                description = "Total area contained in the purchased rolls (NOT wall area)",
                isHighlighted = true
            )

            if (showPurchaseExcess && result.purchaseExcessMm2 > 0.0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                AreaMetricRow(
                    label = "Purchase Excess",
                    value = "${LengthUnit.formatArea(result.purchaseExcessMm2, decimalPlaces)} (${String.format(Locale.US, "%.1f", result.excessPercent)}%)",
                    description = "Includes strip geometry off-cuts, repeat matching, trimming, & roll roll-ends"
                )
            }
        }
    }
}

@Composable
private fun AreaMetricRow(
    label: String,
    value: String,
    description: String,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.outline
        )
    }
}
