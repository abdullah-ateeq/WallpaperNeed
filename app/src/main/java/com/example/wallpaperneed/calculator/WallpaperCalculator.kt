package com.example.wallpaperneed.calculator

import com.example.wallpaperneed.model.CalculationBreakdownStep
import com.example.wallpaperneed.model.DropOptimizationMode
import com.example.wallpaperneed.model.LengthUnit
import com.example.wallpaperneed.model.Opening
import com.example.wallpaperneed.model.PatternMatch
import com.example.wallpaperneed.model.RollCalculationMode
import com.example.wallpaperneed.model.RollCutItem
import com.example.wallpaperneed.model.SingleRollPlan
import com.example.wallpaperneed.model.WallpaperProject
import com.example.wallpaperneed.model.WallpaperResult
import com.example.wallpaperneed.model.WallpaperRollGroup
import com.example.wallpaperneed.model.WallpaperWall
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

object WallpaperCalculator {

    const val ERROR_ROLL_TOO_SHORT = "One wallpaper drop is longer than the entire roll. Increase roll length or reduce the required drop."
    const val ERROR_INVALID_ROLL_WIDTH = "Roll width must be greater than zero."
    const val ERROR_INVALID_ROLL_LENGTH = "Roll length must be greater than zero."
    const val ERROR_INVALID_DIMENSIONS = "Dimensions must be greater than zero."
    const val ERROR_NEGATIVE_PATTERN = "Pattern repeat cannot be negative."

    fun calculateWallArea(widthMm: Double, heightMm: Double): Double {
        if (widthMm <= 0.0 || heightMm <= 0.0) return 0.0
        return widthMm * heightMm
    }

    fun calculatePerimeter(roomLengthMm: Double, roomWidthMm: Double): Double {
        if (roomLengthMm <= 0.0 || roomWidthMm <= 0.0) return 0.0
        return 2.0 * (roomLengthMm + roomWidthMm)
    }

    fun calculateRequiredDrops(wallCoverageWidthMm: Double, rollWidthMm: Double): Int {
        if (wallCoverageWidthMm <= 0.0 || rollWidthMm <= 0.0) return 0
        return ceil(wallCoverageWidthMm / rollWidthMm).toInt()
    }

    fun calculateBaseDropLength(
        wallHeightMm: Double,
        topTrimMm: Double,
        bottomTrimMm: Double,
        topFinishMm: Double = 0.0,
        bottomFinishMm: Double = 0.0
    ): Double {
        if (wallHeightMm <= 0.0) return 0.0
        return wallHeightMm + max(0.0, topTrimMm) + max(0.0, bottomTrimMm) + max(0.0, topFinishMm) + max(0.0, bottomFinishMm)
    }

    fun applyPatternRepeat(baseDropMm: Double, repeatMm: Double): Double {
        if (baseDropMm <= 0.0) return 0.0
        if (repeatMm <= 0.0) return baseDropMm
        return ceil(baseDropMm / repeatMm) * repeatMm
    }

    fun calculateDropsPerRoll(rollLengthMm: Double, adjustedDropMm: Double): Int {
        if (rollLengthMm <= 0.0 || adjustedDropMm <= 0.0) return 0
        return floor(rollLengthMm / adjustedDropMm).toInt()
    }

    fun calculateRollsRequired(requiredDrops: Int, dropsPerRoll: Int): Int {
        if (requiredDrops <= 0 || dropsPerRoll <= 0) return 0
        return ceil(requiredDrops.toDouble() / dropsPerRoll.toDouble()).toInt()
    }

    fun calculatePurchaseArea(rolls: Int, rollWidthMm: Double, rollLengthMm: Double): Double {
        if (rolls <= 0 || rollWidthMm <= 0.0 || rollLengthMm <= 0.0) return 0.0
        return rolls.toDouble() * rollWidthMm * rollLengthMm
    }

    fun calculatePurchaseExcess(purchaseAreaMm2: Double, netWallAreaMm2: Double): Double {
        return max(0.0, purchaseAreaMm2 - netWallAreaMm2)
    }

    fun calculateRollEfficiency(dropsPerRoll: Int, adjustedDropMm: Double, rollLengthMm: Double): Double {
        if (rollLengthMm <= 0.0 || dropsPerRoll <= 0 || adjustedDropMm <= 0.0) return 0.0
        return ((dropsPerRoll * adjustedDropMm) / rollLengthMm) * 100.0
    }

    /**
     * Builds detailed cutting plan roll-by-roll for the given drops and roll specifications.
     */
    fun buildCuttingPlan(
        requiredDrops: Int,
        adjustedDropMm: Double,
        rollLengthMm: Double,
        dropsPerRoll: Int,
        patternMatch: PatternMatch
    ): List<SingleRollPlan> {
        if (requiredDrops <= 0 || dropsPerRoll <= 0 || adjustedDropMm <= 0.0 || rollLengthMm <= 0.0) {
            return emptyList()
        }

        val totalRolls = calculateRollsRequired(requiredDrops, dropsPerRoll)
        val rollPlans = ArrayList<SingleRollPlan>(totalRolls)
        var remainingDrops = requiredDrops
        var currentDropNumber = 1

        for (rollIndex in 1..totalRolls) {
            val dropsInThisRoll = minOf(remainingDrops, dropsPerRoll)
            val cutItems = mutableListOf<RollCutItem>()

            for (d in 1..dropsInThisRoll) {
                val matchLabel = when (patternMatch) {
                    PatternMatch.HALF_DROP -> if (currentDropNumber % 2 == 1) "Match A" else "Match B"
                    PatternMatch.STRAIGHT_MATCH -> "Straight Match"
                    PatternMatch.RANDOM -> "Random Match"
                    PatternMatch.NONE -> "Standard Drop"
                }

                cutItems.add(
                    RollCutItem(
                        dropNumber = currentDropNumber,
                        lengthMm = adjustedDropMm,
                        matchLabel = matchLabel,
                        isLeftover = false
                    )
                )
                currentDropNumber++
            }

            remainingDrops -= dropsInThisRoll
            val usedLengthMm = dropsInThisRoll * adjustedDropMm
            val leftoverMm = max(0.0, rollLengthMm - usedLengthMm)
            val efficiency = if (rollLengthMm > 0) (usedLengthMm / rollLengthMm) * 100.0 else 0.0

            if (leftoverMm > 0.0) {
                cutItems.add(
                    RollCutItem(
                        dropNumber = 0,
                        lengthMm = leftoverMm,
                        matchLabel = "Leftover Material",
                        isLeftover = true
                    )
                )
            }

            rollPlans.add(
                SingleRollPlan(
                    rollIndex = rollIndex,
                    drops = cutItems,
                    usedLengthMm = usedLengthMm,
                    leftoverMm = leftoverMm,
                    efficiencyPercent = efficiency
                )
            )
        }

        return rollPlans
    }

    /**
     * Main calculation orchestrator for a WallpaperProject.
     */
    fun calculateProject(project: WallpaperProject): WallpaperResult {
        // Basic validations
        if (project.wallpaperProduct.rollWidthMm <= 0.0) {
            return WallpaperResult(errorMessage = ERROR_INVALID_ROLL_WIDTH)
        }
        if (project.wallpaperProduct.rollLengthMm <= 0.0) {
            return WallpaperResult(errorMessage = ERROR_INVALID_ROLL_LENGTH)
        }
        if (project.wallpaperProduct.patternRepeatMm < 0.0) {
            return WallpaperResult(errorMessage = ERROR_NEGATIVE_PATTERN)
        }

        when (project.calculationMode) {
            com.example.wallpaperneed.model.CalculationMode.COMPLETE_ROOM -> {
                return calculateCompleteRoom(project)
            }
            com.example.wallpaperneed.model.CalculationMode.SINGLE_WALL -> {
                return calculateSingleWall(project)
            }
            com.example.wallpaperneed.model.CalculationMode.CUSTOM_WALLS -> {
                return calculateCustomWalls(project)
            }
            com.example.wallpaperneed.model.CalculationMode.MULTIPLE_ROOMS -> {
                return calculateMultipleRoomsProject(project)
            }
        }
    }

    private fun calculateCompleteRoom(project: WallpaperProject): WallpaperResult {
        val lengthMm = project.roomLengthMm
        val widthMm = project.roomWidthMm
        val heightMm = project.wallHeightMm

        if (lengthMm <= 0.0 || widthMm <= 0.0 || (project.sameWallHeight && heightMm <= 0.0)) {
            return WallpaperResult(errorMessage = ERROR_INVALID_DIMENSIONS)
        }

        val perimeterMm = calculatePerimeter(lengthMm, widthMm)

        // If individual heights are used
        if (!project.sameWallHeight) {
            return calculateWallsList(
                walls = project.walls,
                project = project,
                perimeterMm = perimeterMm
            )
        }

        val grossWallAreaMm2 = perimeterMm * heightMm
        val openingAreaMm2 = project.openings.sumOf { it.totalAreaMm2 }
        val netWallAreaMm2 = max(0.0, grossWallAreaMm2 - openingAreaMm2)

        val rollWidthMm = project.wallpaperProduct.rollWidthMm
        val rollLengthMm = project.wallpaperProduct.rollLengthMm
        val repeatMm = project.wallpaperProduct.patternRepeatMm

        // Drop deduction with Basic Opening Optimization if enabled
        var wallCoverageWidthMm = perimeterMm
        if (project.dropOptimizationMode == DropOptimizationMode.BASIC_OPENING_OPTIMIZATION) {
            // Deduct whole strips that fall completely inside full-height openings
            var eliminatedStrips = 0
            for (op in project.openings) {
                // If opening is nearly ceiling height (e.g. within 15 cm of wall height)
                if (op.heightMm >= (heightMm - 150.0)) {
                    val stripsOccupied = floor(op.widthMm / rollWidthMm).toInt()
                    eliminatedStrips += (stripsOccupied * op.quantity)
                }
            }
            val stripsWidthToDeduct = eliminatedStrips * rollWidthMm
            wallCoverageWidthMm = max(rollWidthMm, wallCoverageWidthMm - stripsWidthToDeduct)
        }

        var requiredDrops = calculateRequiredDrops(wallCoverageWidthMm, rollWidthMm)
        val baseDropMm = calculateBaseDropLength(
            wallHeightMm = heightMm,
            topTrimMm = project.topTrimMm,
            bottomTrimMm = project.bottomTrimMm,
            topFinishMm = project.topFinishMm,
            bottomFinishMm = project.bottomFinishMm
        )
        val adjustedDropMm = applyPatternRepeat(baseDropMm, repeatMm)

        if (adjustedDropMm > rollLengthMm) {
            return WallpaperResult(
                grossWallAreaMm2 = grossWallAreaMm2,
                openingAreaMm2 = openingAreaMm2,
                netWallAreaMm2 = netWallAreaMm2,
                perimeterMm = perimeterMm,
                baseDropLengthMm = baseDropMm,
                adjustedDropLengthMm = adjustedDropMm,
                errorMessage = ERROR_ROLL_TOO_SHORT
            )
        }

        val dropsPerRoll = calculateDropsPerRoll(rollLengthMm, adjustedDropMm)
        if (dropsPerRoll <= 0) {
            return WallpaperResult(
                grossWallAreaMm2 = grossWallAreaMm2,
                openingAreaMm2 = openingAreaMm2,
                netWallAreaMm2 = netWallAreaMm2,
                perimeterMm = perimeterMm,
                baseDropLengthMm = baseDropMm,
                adjustedDropLengthMm = adjustedDropMm,
                errorMessage = ERROR_ROLL_TOO_SHORT
            )
        }

        val requiredRolls = calculateRollsRequired(requiredDrops, dropsPerRoll)

        // Recommended rolls including extra roll allowance & extra material percent
        var recommendedRolls = requiredRolls + max(0, project.extraRollAllowance)
        if (project.extraMaterialPercent > 0.0) {
            val marginRolls = ceil(requiredRolls * (1.0 + project.extraMaterialPercent / 100.0)).toInt()
            recommendedRolls = max(recommendedRolls, marginRolls)
        }

        val totalPurchaseLengthMm = requiredRolls * rollLengthMm
        val purchaseAreaMm2 = calculatePurchaseArea(requiredRolls, rollWidthMm, rollLengthMm)
        val purchaseExcessMm2 = calculatePurchaseExcess(purchaseAreaMm2, netWallAreaMm2)
        val excessPercent = if (netWallAreaMm2 > 0.0) (purchaseExcessMm2 / netWallAreaMm2) * 100.0 else 0.0
        val leftoverPerRollMm = max(0.0, rollLengthMm - (dropsPerRoll * adjustedDropMm))
        val rollEfficiencyPercent = calculateRollEfficiency(dropsPerRoll, adjustedDropMm, rollLengthMm)

        val cuttingPlan = buildCuttingPlan(
            requiredDrops = requiredDrops,
            adjustedDropMm = adjustedDropMm,
            rollLengthMm = rollLengthMm,
            dropsPerRoll = dropsPerRoll,
            patternMatch = project.wallpaperProduct.patternMatch
        )

        val rollGroup = WallpaperRollGroup(
            label = "All Walls (Height: ${(heightMm / 10.0).toInt()} cm)",
            dropLengthMm = adjustedDropMm,
            rollWidthMm = rollWidthMm,
            rollLengthMm = rollLengthMm,
            requiredDrops = requiredDrops,
            usableDropsPerRoll = dropsPerRoll,
            requiredRolls = requiredRolls,
            leftoverPerRollMm = leftoverPerRollMm,
            efficiencyPercent = rollEfficiencyPercent,
            rollPlans = cuttingPlan
        )

        val warnings = mutableListOf<String>()
        if (project.wallpaperProduct.patternMatch == PatternMatch.HALF_DROP) {
            warnings.add("⚠ Half-drop pattern selected. Additional matching material may be required. Verify the manufacturer's pattern-repeat instructions.")
        }
        if (project.direction == com.example.wallpaperneed.model.WallpaperDirection.VERTICAL) {
            warnings.add("⚠ Directional wallpaper: All drops must maintain the same vertical orientation.")
        }
        if (repeatMm > 0.0) {
            warnings.add("⚠ Pattern repeat affects usable drops per roll.")
        }

        val breakdownSteps = mutableListOf<CalculationBreakdownStep>()
        breakdownSteps.add(
            CalculationBreakdownStep(
                title = "1. Room Perimeter",
                formula = "2 × (Length + Width)",
                calculation = "2 × (${formatMm(lengthMm)} + ${formatMm(widthMm)})",
                result = formatMeters(perimeterMm)
            )
        )
        breakdownSteps.add(
            CalculationBreakdownStep(
                title = "2. Gross Wall Area",
                formula = "Perimeter × Wall Height",
                calculation = "${formatMeters(perimeterMm)} × ${formatMeters(heightMm)}",
                result = formatSqM(grossWallAreaMm2),
                note = "Surface area measurement only; wallpaper is calculated in vertical drops."
            )
        )
        if (openingAreaMm2 > 0.0) {
            breakdownSteps.add(
                CalculationBreakdownStep(
                    title = "3. Openings & Net Wall Area",
                    formula = "Gross Wall Area − Opening Area",
                    calculation = "${formatSqM(grossWallAreaMm2)} − ${formatSqM(openingAreaMm2)}",
                    result = formatSqM(netWallAreaMm2),
                    note = if (project.dropOptimizationMode == DropOptimizationMode.CONSERVATIVE)
                        "Conservative mode: Openings deduct surface area for reference without reducing drop count."
                    else "Basic Optimization: Deducts complete drops for full-height openings."
                )
            )
        }
        breakdownSteps.add(
            CalculationBreakdownStep(
                title = "4. Base Drop Length",
                formula = "Wall Height + Top Trim + Bottom Trim",
                calculation = "${formatMm(heightMm)} + ${formatMm(project.topTrimMm)} + ${formatMm(project.bottomTrimMm)}" +
                        if (project.topFinishMm > 0 || project.bottomFinishMm > 0) " + allowances" else "",
                result = formatMm(baseDropMm)
            )
        )
        if (repeatMm > 0.0) {
            breakdownSteps.add(
                CalculationBreakdownStep(
                    title = "5. Pattern Repeat Adjustment",
                    formula = "ceil(Base Drop ÷ Repeat) × Repeat",
                    calculation = "ceil(${formatMm(baseDropMm)} ÷ ${formatMm(repeatMm)}) × ${formatMm(repeatMm)}",
                    result = formatMm(adjustedDropMm),
                    note = "Adjusted drop is rounded UP to the nearest repeat increment."
                )
            )
        }
        breakdownSteps.add(
            CalculationBreakdownStep(
                title = "6. Required Wallpaper Drops",
                formula = "ceil(Wall Coverage ÷ Roll Width)",
                calculation = "ceil(${formatMm(wallCoverageWidthMm)} ÷ ${formatMm(rollWidthMm)})",
                result = "$requiredDrops drops",
                note = "Never rounded down."
            )
        )
        breakdownSteps.add(
            CalculationBreakdownStep(
                title = "7. Usable Drops Per Roll",
                formula = "floor(Roll Length ÷ Adjusted Drop)",
                calculation = "floor(${formatMeters(rollLengthMm)} ÷ ${formatMm(adjustedDropMm)})",
                result = "$dropsPerRoll drops/roll",
                note = "Leftover material per roll: ${formatMm(leftoverPerRollMm)} (Efficiency: ${String.format(Locale.US, "%.1f", rollEfficiencyPercent)}%)"
            )
        )
        breakdownSteps.add(
            CalculationBreakdownStep(
                title = "8. Rolls Required",
                formula = "ceil(Required Drops ÷ Drops Per Roll)",
                calculation = "ceil($requiredDrops ÷ $dropsPerRoll)",
                result = "$requiredRolls rolls"
            )
        )
        if (recommendedRolls > requiredRolls) {
            breakdownSteps.add(
                CalculationBreakdownStep(
                    title = "9. Recommended Purchase",
                    formula = "Calculated Rolls + Extra Allowances",
                    calculation = "$requiredRolls + ${recommendedRolls - requiredRolls} extra",
                    result = "$recommendedRolls rolls",
                    note = "Includes user extra roll allowance and safety margin."
                )
            )
        }

        return WallpaperResult(
            grossWallAreaMm2 = grossWallAreaMm2,
            openingAreaMm2 = openingAreaMm2,
            netWallAreaMm2 = netWallAreaMm2,
            requiredDrops = requiredDrops,
            baseDropLengthMm = baseDropMm,
            adjustedDropLengthMm = adjustedDropMm,
            dropsPerRoll = dropsPerRoll,
            requiredRolls = requiredRolls,
            recommendedRolls = recommendedRolls,
            extraRollAllowance = project.extraRollAllowance,
            totalPurchaseLengthMm = totalPurchaseLengthMm,
            purchaseAreaMm2 = purchaseAreaMm2,
            purchaseExcessMm2 = purchaseExcessMm2,
            excessPercent = excessPercent,
            leftoverPerRollMm = leftoverPerRollMm,
            rollEfficiencyPercent = rollEfficiencyPercent,
            perimeterMm = perimeterMm,
            rollGroups = listOf(rollGroup),
            breakdownSteps = breakdownSteps,
            warnings = warnings
        )
    }

    private fun calculateSingleWall(project: WallpaperProject): WallpaperResult {
        val widthMm = project.singleWallWidthMm
        val heightMm = project.singleWallHeightMm

        if (widthMm <= 0.0 || heightMm <= 0.0) {
            return WallpaperResult(errorMessage = ERROR_INVALID_DIMENSIONS)
        }

        val grossWallAreaMm2 = calculateWallArea(widthMm, heightMm)
        val openingAreaMm2 = project.openings.sumOf { it.totalAreaMm2 }
        val netWallAreaMm2 = max(0.0, grossWallAreaMm2 - openingAreaMm2)

        val rollWidthMm = project.wallpaperProduct.rollWidthMm
        val rollLengthMm = project.wallpaperProduct.rollLengthMm
        val repeatMm = project.wallpaperProduct.patternRepeatMm

        val requiredDrops = calculateRequiredDrops(widthMm, rollWidthMm)
        val baseDropMm = calculateBaseDropLength(
            wallHeightMm = heightMm,
            topTrimMm = project.topTrimMm,
            bottomTrimMm = project.bottomTrimMm,
            topFinishMm = project.topFinishMm,
            bottomFinishMm = project.bottomFinishMm
        )
        val adjustedDropMm = applyPatternRepeat(baseDropMm, repeatMm)

        if (adjustedDropMm > rollLengthMm) {
            return WallpaperResult(
                grossWallAreaMm2 = grossWallAreaMm2,
                openingAreaMm2 = openingAreaMm2,
                netWallAreaMm2 = netWallAreaMm2,
                baseDropLengthMm = baseDropMm,
                adjustedDropLengthMm = adjustedDropMm,
                errorMessage = ERROR_ROLL_TOO_SHORT
            )
        }

        val dropsPerRoll = calculateDropsPerRoll(rollLengthMm, adjustedDropMm)
        if (dropsPerRoll <= 0) {
            return WallpaperResult(
                grossWallAreaMm2 = grossWallAreaMm2,
                openingAreaMm2 = openingAreaMm2,
                netWallAreaMm2 = netWallAreaMm2,
                baseDropLengthMm = baseDropMm,
                adjustedDropLengthMm = adjustedDropMm,
                errorMessage = ERROR_ROLL_TOO_SHORT
            )
        }

        val requiredRolls = calculateRollsRequired(requiredDrops, dropsPerRoll)
        var recommendedRolls = requiredRolls + max(0, project.extraRollAllowance)
        if (project.extraMaterialPercent > 0.0) {
            val marginRolls = ceil(requiredRolls * (1.0 + project.extraMaterialPercent / 100.0)).toInt()
            recommendedRolls = max(recommendedRolls, marginRolls)
        }

        val totalPurchaseLengthMm = requiredRolls * rollLengthMm
        val purchaseAreaMm2 = calculatePurchaseArea(requiredRolls, rollWidthMm, rollLengthMm)
        val purchaseExcessMm2 = calculatePurchaseExcess(purchaseAreaMm2, netWallAreaMm2)
        val excessPercent = if (netWallAreaMm2 > 0.0) (purchaseExcessMm2 / netWallAreaMm2) * 100.0 else 0.0
        val leftoverPerRollMm = max(0.0, rollLengthMm - (dropsPerRoll * adjustedDropMm))
        val rollEfficiencyPercent = calculateRollEfficiency(dropsPerRoll, adjustedDropMm, rollLengthMm)

        val cuttingPlan = buildCuttingPlan(
            requiredDrops = requiredDrops,
            adjustedDropMm = adjustedDropMm,
            rollLengthMm = rollLengthMm,
            dropsPerRoll = dropsPerRoll,
            patternMatch = project.wallpaperProduct.patternMatch
        )

        val rollGroup = WallpaperRollGroup(
            label = "Feature Wall (${(widthMm / 10.0).toInt()} × ${(heightMm / 10.0).toInt()} cm)",
            dropLengthMm = adjustedDropMm,
            rollWidthMm = rollWidthMm,
            rollLengthMm = rollLengthMm,
            requiredDrops = requiredDrops,
            usableDropsPerRoll = dropsPerRoll,
            requiredRolls = requiredRolls,
            leftoverPerRollMm = leftoverPerRollMm,
            efficiencyPercent = rollEfficiencyPercent,
            rollPlans = cuttingPlan
        )

        val breakdownSteps = listOf(
            CalculationBreakdownStep(
                title = "1. Feature Wall Area",
                formula = "Width × Height",
                calculation = "${formatMeters(widthMm)} × ${formatMeters(heightMm)}",
                result = formatSqM(grossWallAreaMm2)
            ),
            CalculationBreakdownStep(
                title = "2. Base Drop Length",
                formula = "Height + Top Trim + Bottom Trim",
                calculation = "${formatMm(heightMm)} + ${formatMm(project.topTrimMm)} + ${formatMm(project.bottomTrimMm)}",
                result = formatMm(baseDropMm)
            ),
            CalculationBreakdownStep(
                title = "3. Required Drops",
                formula = "ceil(Wall Width ÷ Roll Width)",
                calculation = "ceil(${formatMm(widthMm)} ÷ ${formatMm(rollWidthMm)})",
                result = "$requiredDrops drops"
            ),
            CalculationBreakdownStep(
                title = "4. Drops Per Roll",
                formula = "floor(Roll Length ÷ Drop Length)",
                calculation = "floor(${formatMeters(rollLengthMm)} ÷ ${formatMm(adjustedDropMm)})",
                result = "$dropsPerRoll drops/roll"
            ),
            CalculationBreakdownStep(
                title = "5. Rolls Required",
                formula = "ceil(Drops ÷ Drops Per Roll)",
                calculation = "ceil($requiredDrops ÷ $dropsPerRoll)",
                result = "$requiredRolls rolls"
            )
        )

        return WallpaperResult(
            grossWallAreaMm2 = grossWallAreaMm2,
            openingAreaMm2 = openingAreaMm2,
            netWallAreaMm2 = netWallAreaMm2,
            requiredDrops = requiredDrops,
            baseDropLengthMm = baseDropMm,
            adjustedDropLengthMm = adjustedDropMm,
            dropsPerRoll = dropsPerRoll,
            requiredRolls = requiredRolls,
            recommendedRolls = recommendedRolls,
            extraRollAllowance = project.extraRollAllowance,
            totalPurchaseLengthMm = totalPurchaseLengthMm,
            purchaseAreaMm2 = purchaseAreaMm2,
            purchaseExcessMm2 = purchaseExcessMm2,
            excessPercent = excessPercent,
            leftoverPerRollMm = leftoverPerRollMm,
            rollEfficiencyPercent = rollEfficiencyPercent,
            perimeterMm = widthMm,
            rollGroups = listOf(rollGroup),
            breakdownSteps = breakdownSteps
        )
    }

    private fun calculateCustomWalls(project: WallpaperProject): WallpaperResult {
        return calculateWallsList(walls = project.walls, project = project, perimeterMm = project.walls.sumOf { it.widthMm })
    }

    private fun calculateWallsList(
        walls: List<WallpaperWall>,
        project: WallpaperProject,
        perimeterMm: Double
    ): WallpaperResult {
        val enabledWalls = walls.filter { it.wallpaperEnabled && it.widthMm > 0.0 && it.heightMm > 0.0 }
        if (enabledWalls.isEmpty()) {
            return WallpaperResult(errorMessage = "Please enable at least one wall with valid dimensions.")
        }

        var totalGrossAreaMm2 = 0.0
        var totalOpeningAreaMm2 = 0.0
        var totalNetAreaMm2 = 0.0

        for (wall in walls) {
            totalGrossAreaMm2 += wall.grossAreaMm2
            totalOpeningAreaMm2 += wall.openingAreaMm2
            totalNetAreaMm2 += wall.netAreaMm2
        }

        val rollWidthMm = project.wallpaperProduct.rollWidthMm
        val rollLengthMm = project.wallpaperProduct.rollLengthMm
        val repeatMm = project.wallpaperProduct.patternRepeatMm

        // Group enabled walls by compatible drop length & wallpaper specifications
        // If conservative mode is selected, each wall is its own group!
        // If combined mode is selected, walls with the same drop length & same wallpaper product are grouped together.
        val wallGroups = if (project.rollCalculationMode == RollCalculationMode.CONSERVATIVE) {
            enabledWalls.map { listOf(it) }
        } else {
            // Group by adjusted drop length and wallpaper product id
            enabledWalls.groupBy { wall ->
                val baseDrop = calculateBaseDropLength(
                    wallHeightMm = wall.heightMm,
                    topTrimMm = project.topTrimMm,
                    bottomTrimMm = project.bottomTrimMm,
                    topFinishMm = project.topFinishMm,
                    bottomFinishMm = project.bottomFinishMm
                )
                val adjustedDrop = applyPatternRepeat(baseDrop, repeatMm)
                val prodId = wall.wallpaperProduct?.id ?: project.wallpaperProduct.id
                "${adjustedDrop}_$prodId"
            }.values.toList()
        }

        val rollGroups = mutableListOf<WallpaperRollGroup>()
        var totalRequiredDrops = 0
        var totalRequiredRolls = 0
        var weightedAdjustedDropMm = 0.0

        for (group in wallGroups) {
            val firstWall = group.first()
            val product = firstWall.wallpaperProduct ?: project.wallpaperProduct
            val rWidthMm = product.rollWidthMm
            val rLengthMm = product.rollLengthMm
            val rRepeatMm = product.patternRepeatMm

            val baseDropMm = calculateBaseDropLength(
                wallHeightMm = firstWall.heightMm,
                topTrimMm = project.topTrimMm,
                bottomTrimMm = project.bottomTrimMm,
                topFinishMm = project.topFinishMm,
                bottomFinishMm = project.bottomFinishMm
            )
            val adjustedDropMm = applyPatternRepeat(baseDropMm, rRepeatMm)

            if (adjustedDropMm > rLengthMm) {
                return WallpaperResult(
                    grossWallAreaMm2 = totalGrossAreaMm2,
                    openingAreaMm2 = totalOpeningAreaMm2,
                    netWallAreaMm2 = totalNetAreaMm2,
                    errorMessage = "$ERROR_ROLL_TOO_SHORT (Wall: ${firstWall.name})"
                )
            }

            val dropsPerRoll = calculateDropsPerRoll(rLengthMm, adjustedDropMm)
            if (dropsPerRoll <= 0) {
                return WallpaperResult(
                    grossWallAreaMm2 = totalGrossAreaMm2,
                    openingAreaMm2 = totalOpeningAreaMm2,
                    netWallAreaMm2 = totalNetAreaMm2,
                    errorMessage = "$ERROR_ROLL_TOO_SHORT (Wall: ${firstWall.name})"
                )
            }

            // Calculate drops for this group
            val groupDrops = group.sumOf { wall ->
                calculateRequiredDrops(wall.widthMm, rWidthMm)
            }
            val groupRolls = calculateRollsRequired(groupDrops, dropsPerRoll)

            val leftoverPerRollMm = max(0.0, rLengthMm - (dropsPerRoll * adjustedDropMm))
            val efficiency = calculateRollEfficiency(dropsPerRoll, adjustedDropMm, rLengthMm)

            val groupPlan = buildCuttingPlan(
                requiredDrops = groupDrops,
                adjustedDropMm = adjustedDropMm,
                rollLengthMm = rLengthMm,
                dropsPerRoll = dropsPerRoll,
                patternMatch = product.patternMatch
            )

            val groupLabel = if (group.size == 1) {
                firstWall.name
            } else {
                "${group.size} Walls (Height: ${(firstWall.heightMm / 10.0).toInt()} cm)"
            }

            rollGroups.add(
                WallpaperRollGroup(
                    label = groupLabel,
                    dropLengthMm = adjustedDropMm,
                    rollWidthMm = rWidthMm,
                    rollLengthMm = rLengthMm,
                    requiredDrops = groupDrops,
                    usableDropsPerRoll = dropsPerRoll,
                    requiredRolls = groupRolls,
                    leftoverPerRollMm = leftoverPerRollMm,
                    efficiencyPercent = efficiency,
                    rollPlans = groupPlan
                )
            )

            totalRequiredDrops += groupDrops
            totalRequiredRolls += groupRolls
            weightedAdjustedDropMm = max(weightedAdjustedDropMm, adjustedDropMm)
        }

        var recommendedRolls = totalRequiredRolls + max(0, project.extraRollAllowance)
        if (project.extraMaterialPercent > 0.0) {
            val marginRolls = ceil(totalRequiredRolls * (1.0 + project.extraMaterialPercent / 100.0)).toInt()
            recommendedRolls = max(recommendedRolls, marginRolls)
        }

        val totalPurchaseLengthMm = rollGroups.sumOf { it.requiredRolls * it.rollLengthMm }
        val purchaseAreaMm2 = rollGroups.sumOf { it.requiredRolls * it.rollWidthMm * it.rollLengthMm }
        val purchaseExcessMm2 = calculatePurchaseExcess(purchaseAreaMm2, totalNetAreaMm2)
        val excessPercent = if (totalNetAreaMm2 > 0.0) (purchaseExcessMm2 / totalNetAreaMm2) * 100.0 else 0.0
        val avgLeftoverMm = if (rollGroups.isNotEmpty()) rollGroups.map { it.leftoverPerRollMm }.average() else 0.0
        val avgEfficiency = if (rollGroups.isNotEmpty()) rollGroups.map { it.efficiencyPercent }.average() else 0.0

        val warnings = mutableListOf<String>()
        if (project.rollCalculationMode == RollCalculationMode.CONSERVATIVE) {
            warnings.add("Conservative mode may require more rolls because leftover drops are not assumed to be interchangeable between walls.")
        }
        if (rollGroups.size > 1) {
            warnings.add("Mixed wall heights detected. Drops have been calculated in compatible height groups to prevent short-drop misallocations.")
        }

        val breakdownSteps = mutableListOf<CalculationBreakdownStep>()
        breakdownSteps.add(
            CalculationBreakdownStep(
                title = "1. Active Walls & Perimeter",
                formula = "Sum of Enabled Wall Widths",
                calculation = enabledWalls.joinToString(" + ") { formatMm(it.widthMm) },
                result = formatMeters(enabledWalls.sumOf { it.widthMm })
            )
        )
        breakdownSteps.add(
            CalculationBreakdownStep(
                title = "2. Total Wall Surface Area",
                formula = "Sum of (Wall Width × Wall Height)",
                calculation = "${enabledWalls.size} active walls",
                result = "Gross: ${formatSqM(totalGrossAreaMm2)}, Net: ${formatSqM(totalNetAreaMm2)}"
            )
        )
        breakdownSteps.add(
            CalculationBreakdownStep(
                title = "3. Grouped Roll Allocation",
                formula = if (project.rollCalculationMode == RollCalculationMode.CONSERVATIVE)
                    "Conservative wall-by-wall" else "Compatible group drop pooling",
                calculation = rollGroups.joinToString("; ") { "${it.label}: ${it.requiredDrops} drops → ${it.requiredRolls} rolls" },
                result = "$totalRequiredRolls rolls total"
            )
        )

        return WallpaperResult(
            grossWallAreaMm2 = totalGrossAreaMm2,
            openingAreaMm2 = totalOpeningAreaMm2,
            netWallAreaMm2 = totalNetAreaMm2,
            requiredDrops = totalRequiredDrops,
            baseDropLengthMm = weightedAdjustedDropMm,
            adjustedDropLengthMm = weightedAdjustedDropMm,
            dropsPerRoll = if (rollGroups.isNotEmpty()) rollGroups.first().usableDropsPerRoll else 0,
            requiredRolls = totalRequiredRolls,
            recommendedRolls = recommendedRolls,
            extraRollAllowance = project.extraRollAllowance,
            totalPurchaseLengthMm = totalPurchaseLengthMm,
            purchaseAreaMm2 = purchaseAreaMm2,
            purchaseExcessMm2 = purchaseExcessMm2,
            excessPercent = excessPercent,
            leftoverPerRollMm = avgLeftoverMm,
            rollEfficiencyPercent = avgEfficiency,
            perimeterMm = perimeterMm,
            rollGroups = rollGroups,
            breakdownSteps = breakdownSteps,
            warnings = warnings
        )
    }

    private fun calculateMultipleRoomsProject(project: WallpaperProject): WallpaperResult {
        if (project.rooms.isEmpty()) {
            return WallpaperResult(errorMessage = "Please add at least one room to the project.")
        }

        var totalGrossMm2 = 0.0
        var totalOpeningMm2 = 0.0
        var totalNetMm2 = 0.0
        var totalDrops = 0
        var totalRolls = 0
        var totalPurchaseLengthMm = 0.0
        var totalPurchaseAreaMm2 = 0.0
        val allRollGroups = mutableListOf<WallpaperRollGroup>()
        val warnings = mutableListOf<String>()

        for (room in project.rooms) {
            val roomProj = WallpaperProject(
                id = room.id,
                name = room.name,
                calculationMode = com.example.wallpaperneed.model.CalculationMode.COMPLETE_ROOM,
                rollCalculationMode = project.rollCalculationMode,
                dropOptimizationMode = project.dropOptimizationMode,
                roomLengthMm = room.lengthMm,
                roomWidthMm = room.widthMm,
                wallHeightMm = room.heightMm,
                sameWallHeight = room.sameWallHeight,
                walls = room.walls,
                wallpaperProduct = room.wallpaperProduct,
                topTrimMm = room.topTrimMm,
                bottomTrimMm = room.bottomTrimMm,
                topFinishMm = room.topFinishMm,
                bottomFinishMm = room.bottomFinishMm,
                openings = room.openings
            )

            val roomResult = calculateCompleteRoom(roomProj)
            if (!roomResult.isValid) {
                return WallpaperResult(
                    errorMessage = "Error in ${room.name}: ${roomResult.errorMessage}"
                )
            }

            totalGrossMm2 += roomResult.grossWallAreaMm2
            totalOpeningMm2 += roomResult.openingAreaMm2
            totalNetMm2 += roomResult.netWallAreaMm2
            totalDrops += roomResult.requiredDrops
            totalRolls += roomResult.requiredRolls
            totalPurchaseLengthMm += roomResult.totalPurchaseLengthMm
            totalPurchaseAreaMm2 += roomResult.purchaseAreaMm2

            roomResult.rollGroups.forEach { grp ->
                allRollGroups.add(grp.copy(label = "${room.name} - ${grp.label}"))
            }
        }

        var recommendedRolls = totalRolls + max(0, project.extraRollAllowance)
        if (project.extraMaterialPercent > 0.0) {
            val marginRolls = ceil(totalRolls * (1.0 + project.extraMaterialPercent / 100.0)).toInt()
            recommendedRolls = max(recommendedRolls, marginRolls)
        }

        val totalExcessMm2 = calculatePurchaseExcess(totalPurchaseAreaMm2, totalNetMm2)
        val excessPercent = if (totalNetMm2 > 0.0) (totalExcessMm2 / totalNetMm2) * 100.0 else 0.0

        val breakdownSteps = listOf(
            CalculationBreakdownStep(
                title = "Multi-Room Summary",
                formula = "Sum of ${project.rooms.size} Rooms",
                calculation = project.rooms.joinToString(", ") { it.name },
                result = "$totalRolls rolls total"
            )
        )

        return WallpaperResult(
            grossWallAreaMm2 = totalGrossMm2,
            openingAreaMm2 = totalOpeningMm2,
            netWallAreaMm2 = totalNetMm2,
            requiredDrops = totalDrops,
            requiredRolls = totalRolls,
            recommendedRolls = recommendedRolls,
            extraRollAllowance = project.extraRollAllowance,
            totalPurchaseLengthMm = totalPurchaseLengthMm,
            purchaseAreaMm2 = totalPurchaseAreaMm2,
            purchaseExcessMm2 = totalExcessMm2,
            excessPercent = excessPercent,
            rollGroups = allRollGroups,
            breakdownSteps = breakdownSteps,
            warnings = warnings
        )
    }

    private fun formatMm(mm: Double): String {
        return String.format(Locale.US, "%.1f cm", mm / 10.0)
    }

    private fun formatMeters(mm: Double): String {
        return String.format(Locale.US, "%.2f m", mm / 1000.0)
    }

    private fun formatSqM(mm2: Double): String {
        return String.format(Locale.US, "%.2f m²", mm2 / 1_000_000.0)
    }
}
