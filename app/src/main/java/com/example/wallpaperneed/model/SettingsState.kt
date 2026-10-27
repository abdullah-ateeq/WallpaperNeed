package com.example.wallpaperneed.model

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

data class SettingsState(
    // Appearance
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,

    // Calculator Defaults
    val defaultUnit: LengthUnit = LengthUnit.CENTIMETERS,
    val defaultRollWidthMm: Double = 530.0,
    val defaultRollLengthMm: Double = 10050.0,
    val defaultTopTrimMm: Double = 50.0,
    val defaultBottomTrimMm: Double = 50.0,
    val defaultPatternRepeatMm: Double = 0.0,
    val defaultRollCalculationMode: RollCalculationMode = RollCalculationMode.COMBINED,

    // Display Toggles
    val showRoomDiagram: Boolean = true,
    val showRollDiagram: Boolean = true,
    val showCuttingPlan: Boolean = true,
    val showCalculationBreakdown: Boolean = true,
    val showOpeningArea: Boolean = true,
    val showPurchaseExcess: Boolean = true,

    // Precision
    val decimalPlaces: Int = 2,

    // Behavior
    val liveCalculation: Boolean = true,
    val haptics: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val restoreLastCalculation: Boolean = true
)
