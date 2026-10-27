package com.example.wallpaperneed.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallpaperneed.model.LengthUnit
import kotlin.math.abs

@Composable
fun <T> PresetChipsRow(
    presets: List<Pair<String, T>>,
    selectedValue: T,
    onSelect: (T) -> Unit,
    isEqual: (T, T) -> Boolean = { a, b -> a == b },
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for ((label, value) in presets) {
            val selected = isEqual(selectedValue, value)
            FilterChip(
                selected = selected,
                onClick = { onSelect(value) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

// Specialized preset helpers
object PresetCollections {
    // Width presets in mm: 53 cm (530mm), 70 cm (700mm), 106 cm (1060mm)
    fun widthPresets(unit: LengthUnit): List<Pair<String, Double>> = listOf(
        Pair("53 cm (Euro)", 530.0),
        Pair("70 cm (Wide)", 700.0),
        Pair("106 cm (Double)", 1060.0)
    )

    // Length presets in mm: 5 m (5000mm), 10.05 m (10050mm), 15 m (15000mm)
    fun lengthPresets(unit: LengthUnit): List<Pair<String, Double>> = listOf(
        Pair("10.05 m (Standard)", 10050.0),
        Pair("15.00 m (Long)", 15000.0),
        Pair("5.00 m (Short)", 5000.0)
    )

    // Trim presets in mm: 0 cm, 2 cm, 5 cm, 10 cm
    fun trimPresets(unit: LengthUnit): List<Pair<String, Double>> = listOf(
        Pair("0 cm", 0.0),
        Pair("2 cm", 20.0),
        Pair("5 cm (Default)", 50.0),
        Pair("10 cm", 100.0)
    )

    // Pattern repeat presets in mm: 0, 10 cm, 20 cm, 30 cm, 40 cm, 50 cm, 60 cm
    fun repeatPresets(unit: LengthUnit): List<Pair<String, Double>> = listOf(
        Pair("None", 0.0),
        Pair("10 cm", 100.0),
        Pair("20 cm", 200.0),
        Pair("30 cm", 300.0),
        Pair("40 cm", 400.0),
        Pair("50 cm", 500.0),
        Pair("60 cm", 600.0),
        Pair("64 cm", 640.0)
    )

    // Extra rolls presets
    val extraRollPresets: List<Pair<String, Int>> = listOf(
        Pair("+0 Rolls", 0),
        Pair("+1 Roll (Safety)", 1),
        Pair("+2 Rolls", 2),
        Pair("+3 Rolls", 3)
    )

    fun isCloseMm(a: Double, b: Double): Boolean = abs(a - b) < 1.0
}
