package com.example.wallpaperneed

import com.example.wallpaperneed.calculator.WallpaperCalculator
import com.example.wallpaperneed.model.CalculationMode
import com.example.wallpaperneed.model.DropOptimizationMode
import com.example.wallpaperneed.model.Opening
import com.example.wallpaperneed.model.OpeningType
import com.example.wallpaperneed.model.PatternMatch
import com.example.wallpaperneed.model.RollCalculationMode
import com.example.wallpaperneed.model.WallpaperProduct
import com.example.wallpaperneed.model.WallpaperProject
import com.example.wallpaperneed.model.WallpaperRoom
import com.example.wallpaperneed.model.WallpaperWall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperCalculatorTest {

    @Test
    fun testBasicRoomCalculation() {
        val project = WallpaperProject(
            roomLengthMm = 5000.0, // 500 cm
            roomWidthMm = 4000.0,  // 400 cm
            wallHeightMm = 3000.0, // 300 cm
            wallpaperProduct = WallpaperProduct(
                rollWidthMm = 530.0,   // 53 cm
                rollLengthMm = 10050.0, // 10.05 m
                patternRepeatMm = 0.0,
                patternMatch = PatternMatch.NONE
            ),
            topTrimMm = 50.0,    // 5 cm
            bottomTrimMm = 50.0  // 5 cm
        )

        val result = WallpaperCalculator.calculateProject(project)

        // Perimeter: 2 * (500 + 400) = 1800 cm = 18000 mm
        assertEquals(18000.0, result.perimeterMm, 0.01)

        // Gross wall area: 18m * 3m = 54.00 m² = 54_000_000 mm²
        assertEquals(54_000_000.0, result.grossWallAreaMm2, 0.01)

        // Base drop: 300 + 5 + 5 = 310 cm = 3100 mm
        assertEquals(3100.0, result.baseDropLengthMm, 0.01)
        assertEquals(3100.0, result.adjustedDropLengthMm, 0.01)

        // Required drops: ceil(1800 / 53) = ceil(33.962) = 34
        assertEquals(34, result.requiredDrops)

        // Drops per roll: floor(1005 / 310) = floor(3.24) = 3
        assertEquals(3, result.dropsPerRoll)

        // Rolls: ceil(34 / 3) = 12
        assertEquals(12, result.requiredRolls)
        assertTrue(result.isValid)
    }

    @Test
    fun testPatternRepeatAdjustment() {
        val baseDropMm = 3100.0 // 310 cm
        val repeatMm = 300.0    // 30 cm

        val adjustedDropMm = WallpaperCalculator.applyPatternRepeat(baseDropMm, repeatMm)

        // ceil(310 / 30) = 11, 11 * 30 = 330 cm = 3300 mm
        assertEquals(3300.0, adjustedDropMm, 0.01)
    }

    @Test
    fun testDropsPerRollCalculation() {
        val rollLengthMm = 10050.0 // 10.05 m = 1005 cm
        val adjustedDropMm = 3300.0 // 330 cm

        val dropsPerRoll = WallpaperCalculator.calculateDropsPerRoll(rollLengthMm, adjustedDropMm)
        assertEquals(3, dropsPerRoll)
    }

    @Test
    fun testRollsRequiredCalculation() {
        val requiredDrops = 34
        val dropsPerRoll = 3

        val rolls = WallpaperCalculator.calculateRollsRequired(requiredDrops, dropsPerRoll)
        assertEquals(12, rolls)
    }

    @Test
    fun testLeftoverCalculation() {
        val rollLengthMm = 10050.0 // 1005 cm
        val adjustedDropMm = 3300.0 // 330 cm
        val dropsPerRoll = 3

        val usedLengthMm = dropsPerRoll * adjustedDropMm // 9900 mm = 990 cm
        val leftoverMm = rollLengthMm - usedLengthMm     // 150 mm = 15 cm

        assertEquals(9900.0, usedLengthMm, 0.01)
        assertEquals(150.0, leftoverMm, 0.01)
    }

    @Test
    fun testOpeningAreaDeduction() {
        val door = Opening(
            name = "Main Door",
            type = OpeningType.DOOR,
            widthMm = 900.0,  // 90 cm
            heightMm = 2100.0, // 210 cm
            quantity = 1
        )

        val project = WallpaperProject(
            roomLengthMm = 5000.0,
            roomWidthMm = 4000.0,
            wallHeightMm = 3000.0,
            openings = listOf(door),
            dropOptimizationMode = DropOptimizationMode.CONSERVATIVE
        )

        val result = WallpaperCalculator.calculateProject(project)

        // Gross: 54.00 m²
        assertEquals(54_000_000.0, result.grossWallAreaMm2, 0.01)
        // Door: 0.9 * 2.1 = 1.89 m² = 1_890_000 mm²
        assertEquals(1_890_000.0, result.openingAreaMm2, 0.01)
        // Net: 54.00 - 1.89 = 52.11 m² = 52_110_000 mm²
        assertEquals(52_110_000.0, result.netWallAreaMm2, 0.01)

        // Under conservative mode, drops must still be based on full coverage
        assertEquals(34, result.requiredDrops)
        assertEquals(12, result.requiredRolls)
    }

    @Test
    fun testWindowAreaCalculation() {
        val window = Opening(
            name = "Window",
            type = OpeningType.WINDOW,
            widthMm = 1500.0,  // 150 cm
            heightMm = 1200.0, // 120 cm
            quantity = 2
        )

        // Area = 1.5 * 1.2 * 2 = 3.60 m² = 3_600_000 mm²
        assertEquals(3_600_000.0, window.totalAreaMm2, 0.01)
    }

    @Test
    fun testNegativeDimensionRejection() {
        val project = WallpaperProject(
            roomLengthMm = -5000.0,
            roomWidthMm = 4000.0,
            wallHeightMm = 3000.0
        )
        val result = WallpaperCalculator.calculateProject(project)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun testZeroRollWidthRejection() {
        val project = WallpaperProject(
            wallpaperProduct = WallpaperProduct(
                rollWidthMm = 0.0,
                rollLengthMm = 10050.0
            )
        )
        val result = WallpaperCalculator.calculateProject(project)
        assertFalse(result.isValid)
        assertEquals(WallpaperCalculator.ERROR_INVALID_ROLL_WIDTH, result.errorMessage)
    }

    @Test
    fun testZeroRollLengthRejection() {
        val project = WallpaperProject(
            wallpaperProduct = WallpaperProduct(
                rollWidthMm = 530.0,
                rollLengthMm = 0.0
            )
        )
        val result = WallpaperCalculator.calculateProject(project)
        assertFalse(result.isValid)
        assertEquals(WallpaperCalculator.ERROR_INVALID_ROLL_LENGTH, result.errorMessage)
    }

    @Test
    fun testPatternRepeatZero() {
        val baseDropMm = 3100.0
        val adjustedDropMm = WallpaperCalculator.applyPatternRepeat(baseDropMm, 0.0)
        assertEquals(baseDropMm, adjustedDropMm, 0.01)
    }

    @Test
    fun testPatternRepeatNegativeRejection() {
        val project = WallpaperProject(
            wallpaperProduct = WallpaperProduct(
                rollWidthMm = 530.0,
                rollLengthMm = 10050.0,
                patternRepeatMm = -300.0
            )
        )
        val result = WallpaperCalculator.calculateProject(project)
        assertFalse(result.isValid)
        assertEquals(WallpaperCalculator.ERROR_NEGATIVE_PATTERN, result.errorMessage)
    }

    @Test
    fun testRollTooShortError() {
        // Roll: 250 cm, Adjusted Drop: 310 cm
        val project = WallpaperProject(
            roomLengthMm = 5000.0,
            roomWidthMm = 4000.0,
            wallHeightMm = 3000.0, // Base drop with 5+5 trim = 310 cm
            wallpaperProduct = WallpaperProduct(
                rollWidthMm = 530.0,
                rollLengthMm = 2500.0 // 250 cm
            )
        )
        val result = WallpaperCalculator.calculateProject(project)
        assertFalse(result.isValid)
        assertEquals(WallpaperCalculator.ERROR_ROLL_TOO_SHORT, result.errorMessage)
    }

    @Test
    fun testMixedWallHeightGrouping() {
        val wallA = WallpaperWall(
            id = "w1",
            name = "Wall A",
            widthMm = 5000.0, // 500 cm
            heightMm = 3000.0 // 300 cm -> drop 310 cm
        )
        val wallB = WallpaperWall(
            id = "w2",
            name = "Wall B",
            widthMm = 4000.0, // 400 cm
            heightMm = 2800.0 // 280 cm -> drop 290 cm
        )

        val project = WallpaperProject(
            calculationMode = CalculationMode.CUSTOM_WALLS,
            rollCalculationMode = RollCalculationMode.COMBINED,
            walls = listOf(wallA, wallB),
            wallpaperProduct = WallpaperProduct(
                rollWidthMm = 530.0,
                rollLengthMm = 10050.0
            ),
            topTrimMm = 50.0,
            bottomTrimMm = 50.0
        )

        val result = WallpaperCalculator.calculateProject(project)
        assertTrue(result.isValid)
        // Drops should not be blindly mixed into 1 drop group; there should be 2 roll groups
        assertEquals(2, result.rollGroups.size)
        // Group A: 5000 / 530 = 10 drops, drops per roll floor(10050 / 3100) = 3 -> 4 rolls
        // Group B: 4000 / 530 = 8 drops, drops per roll floor(10050 / 2900) = 3 -> 3 rolls
        // Total rolls = 4 + 3 = 7
        assertEquals(7, result.requiredRolls)
    }

    @Test
    fun testFeatureWallCalculation() {
        // Wall: 500 x 300 cm, Roll: 53 x 1005 cm, Trim: 5 + 5 cm
        val project = WallpaperProject(
            calculationMode = CalculationMode.SINGLE_WALL,
            singleWallWidthMm = 5000.0, // 500 cm
            singleWallHeightMm = 3000.0, // 300 cm
            wallpaperProduct = WallpaperProduct(
                rollWidthMm = 530.0,
                rollLengthMm = 10050.0,
                patternRepeatMm = 0.0
            ),
            topTrimMm = 50.0,
            bottomTrimMm = 50.0
        )

        val result = WallpaperCalculator.calculateProject(project)
        assertTrue(result.isValid)

        // Required drops: ceil(500 / 53) = 10
        assertEquals(10, result.requiredDrops)

        // Adjusted drop: 310 cm = 3100 mm
        assertEquals(3100.0, result.adjustedDropLengthMm, 0.01)

        // Drops per roll: floor(1005 / 310) = 3
        assertEquals(3, result.dropsPerRoll)

        // Rolls: ceil(10 / 3) = 4
        assertEquals(4, result.requiredRolls)
    }

    @Test
    fun testMultipleRoomsCalculation() {
        val room1 = WallpaperRoom(
            name = "Living Room",
            lengthMm = 5000.0,
            widthMm = 4000.0,
            heightMm = 3000.0,
            wallpaperProduct = WallpaperProduct.STANDARD_EURO
        )
        val room2 = WallpaperRoom(
            name = "Bedroom",
            lengthMm = 4000.0,
            widthMm = 3500.0,
            heightMm = 2800.0,
            wallpaperProduct = WallpaperProduct.STANDARD_EURO
        )

        val project = WallpaperProject(
            calculationMode = CalculationMode.MULTIPLE_ROOMS,
            rooms = listOf(room1, room2)
        )

        val result = WallpaperCalculator.calculateProject(project)
        assertTrue(result.isValid)
        // Room 1: 54.00 m², Room 2: 2 * (4 + 3.5) * 2.8 = 15 * 2.8 = 42.00 m²
        // Gross total: 54.00 + 42.00 = 96.00 m²
        assertEquals(96_000_000.0, result.grossWallAreaMm2, 0.01)
        assertTrue(result.requiredRolls > 0)
        assertEquals(2, result.rollGroups.size)
    }
}
