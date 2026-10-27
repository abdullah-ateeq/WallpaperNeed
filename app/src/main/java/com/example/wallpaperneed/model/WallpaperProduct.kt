package com.example.wallpaperneed.model

import java.util.UUID

data class WallpaperProduct(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Standard Wallpaper",
    val rollWidthMm: Double = 530.0,
    val rollLengthMm: Double = 10050.0,
    val patternRepeatMm: Double = 0.0,
    val patternMatch: PatternMatch = PatternMatch.NONE
) {
    companion object {
        val STANDARD_EURO = WallpaperProduct(
            id = "preset_euro",
            name = "Standard Euro (53 cm × 10.05 m)",
            rollWidthMm = 530.0,
            rollLengthMm = 10050.0,
            patternRepeatMm = 0.0,
            patternMatch = PatternMatch.NONE
        )

        val WIDE_AMERICAN = WallpaperProduct(
            id = "preset_wide",
            name = "Wide American (70 cm × 10.05 m)",
            rollWidthMm = 700.0,
            rollLengthMm = 10050.0,
            patternRepeatMm = 0.0,
            patternMatch = PatternMatch.NONE
        )

        val DOUBLE_WIDE = WallpaperProduct(
            id = "preset_double",
            name = "Double Wide (106 cm × 10.05 m)",
            rollWidthMm = 1060.0,
            rollLengthMm = 10050.0,
            patternRepeatMm = 0.0,
            patternMatch = PatternMatch.NONE
        )

        val LONG_ROLL = WallpaperProduct(
            id = "preset_long",
            name = "Long Roll (53 cm × 15.00 m)",
            rollWidthMm = 530.0,
            rollLengthMm = 15000.0,
            patternRepeatMm = 0.0,
            patternMatch = PatternMatch.NONE
        )

        val DEFAULT_PRESETS = listOf(STANDARD_EURO, WIDE_AMERICAN, DOUBLE_WIDE, LONG_ROLL)
    }
}
