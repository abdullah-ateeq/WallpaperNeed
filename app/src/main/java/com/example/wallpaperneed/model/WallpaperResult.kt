package com.example.wallpaperneed.model

data class CalculationBreakdownStep(
    val title: String,
    val formula: String,
    val calculation: String,
    val result: String,
    val note: String = ""
)

data class WallpaperResult(
    val grossWallAreaMm2: Double = 0.0,
    val openingAreaMm2: Double = 0.0,
    val netWallAreaMm2: Double = 0.0,
    val requiredDrops: Int = 0,
    val baseDropLengthMm: Double = 0.0,
    val adjustedDropLengthMm: Double = 0.0,
    val dropsPerRoll: Int = 0,
    val requiredRolls: Int = 0,
    val recommendedRolls: Int = 0,
    val extraRollAllowance: Int = 0,
    val totalPurchaseLengthMm: Double = 0.0,
    val purchaseAreaMm2: Double = 0.0,
    val purchaseExcessMm2: Double = 0.0,
    val excessPercent: Double = 0.0,
    val leftoverPerRollMm: Double = 0.0,
    val rollEfficiencyPercent: Double = 0.0,
    val perimeterMm: Double = 0.0,
    val rollGroups: List<WallpaperRollGroup> = emptyList(),
    val breakdownSteps: List<CalculationBreakdownStep> = emptyList(),
    val warnings: List<String> = emptyList(),
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() = errorMessage == null && requiredRolls > 0
}
