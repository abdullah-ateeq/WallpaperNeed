package com.example.wallpaperneed.model

data class RollCutItem(
    val dropNumber: Int,
    val lengthMm: Double,
    val matchLabel: String = "Standard",
    val isLeftover: Boolean = false
)

data class SingleRollPlan(
    val rollIndex: Int,
    val drops: List<RollCutItem>,
    val usedLengthMm: Double,
    val leftoverMm: Double,
    val efficiencyPercent: Double
)

data class WallpaperRollGroup(
    val label: String = "Standard Group",
    val dropLengthMm: Double,
    val rollWidthMm: Double,
    val rollLengthMm: Double,
    val requiredDrops: Int,
    val usableDropsPerRoll: Int,
    val requiredRolls: Int,
    val leftoverPerRollMm: Double = maxOf(0.0, rollLengthMm - (usableDropsPerRoll * dropLengthMm)),
    val efficiencyPercent: Double = if (rollLengthMm > 0) ((usableDropsPerRoll * dropLengthMm) / rollLengthMm) * 100.0 else 0.0,
    val rollPlans: List<SingleRollPlan> = emptyList()
)
