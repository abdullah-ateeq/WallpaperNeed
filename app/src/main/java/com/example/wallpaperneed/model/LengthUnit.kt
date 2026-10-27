package com.example.wallpaperneed.model

import java.util.Locale

enum class LengthUnit(val symbol: String, val displayName: String, val toMmFactor: Double) {
    MILLIMETERS("mm", "Millimeters", 1.0),
    CENTIMETERS("cm", "Centimeters", 10.0),
    METERS("m", "Meters", 1000.0),
    INCHES("in", "Inches", 25.4),
    FEET("ft", "Feet", 304.8);

    fun toMm(value: Double): Double = value * toMmFactor

    fun fromMm(valueMm: Double): Double = valueMm / toMmFactor

    fun format(valueMm: Double, decimals: Int = 2): String {
        val converted = fromMm(valueMm)
        return String.format(Locale.US, "%.${decimals}f %s", converted, symbol)
    }

    fun formatValue(valueMm: Double, decimals: Int = 2): String {
        val converted = fromMm(valueMm)
        return String.format(Locale.US, "%.${decimals}f", converted)
    }

    companion object {
        fun formatArea(areaMm2: Double, decimals: Int = 2): String {
            val areaM2 = areaMm2 / 1_000_000.0
            return String.format(Locale.US, "%.${decimals}f m²", areaM2)
        }

        fun formatLengthMeters(lengthMm: Double, decimals: Int = 2): String {
            val meters = lengthMm / 1000.0
            return String.format(Locale.US, "%.${decimals}f m", meters)
        }
    }
}
