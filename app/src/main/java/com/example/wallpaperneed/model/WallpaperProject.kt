package com.example.wallpaperneed.model

import java.util.UUID

data class WallpaperRoom(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Living Room",
    val lengthMm: Double = 5000.0,
    val widthMm: Double = 4000.0,
    val heightMm: Double = 3000.0,
    val sameWallHeight: Boolean = true,
    val walls: List<WallpaperWall> = emptyList(),
    val wallpaperProduct: WallpaperProduct = WallpaperProduct.STANDARD_EURO,
    val topTrimMm: Double = 50.0,
    val bottomTrimMm: Double = 50.0,
    val topFinishMm: Double = 0.0,
    val bottomFinishMm: Double = 0.0,
    val openings: List<Opening> = emptyList(),
    val result: WallpaperResult = WallpaperResult()
)

data class WallpaperProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Living Room",
    val calculationMode: CalculationMode = CalculationMode.COMPLETE_ROOM,
    val rollCalculationMode: RollCalculationMode = RollCalculationMode.COMBINED,
    val dropOptimizationMode: DropOptimizationMode = DropOptimizationMode.CONSERVATIVE,
    val lengthUnit: LengthUnit = LengthUnit.CENTIMETERS,
    // Room dimensions (for COMPLETE_ROOM)
    val roomLengthMm: Double = 5000.0,
    val roomWidthMm: Double = 4000.0,
    val wallHeightMm: Double = 3000.0,
    val sameWallHeight: Boolean = true,
    // Single wall dimensions (for SINGLE_WALL)
    val singleWallWidthMm: Double = 5000.0,
    val singleWallHeightMm: Double = 3000.0,
    // Walls list (for individual wall heights, custom wall sections, or feature wall)
    val walls: List<WallpaperWall> = listOf(
        WallpaperWall(id = "wall_north", name = "North Wall", widthMm = 5000.0, heightMm = 3000.0),
        WallpaperWall(id = "wall_east", name = "East Wall", widthMm = 4000.0, heightMm = 3000.0),
        WallpaperWall(id = "wall_south", name = "South Wall", widthMm = 5000.0, heightMm = 3000.0),
        WallpaperWall(id = "wall_west", name = "West Wall", widthMm = 4000.0, heightMm = 3000.0)
    ),
    // Multiple rooms
    val rooms: List<WallpaperRoom> = emptyList(),
    // Wallpaper roll specifications
    val wallpaperProduct: WallpaperProduct = WallpaperProduct.STANDARD_EURO,
    val topTrimMm: Double = 50.0,
    val bottomTrimMm: Double = 50.0,
    val topFinishMm: Double = 0.0,
    val bottomFinishMm: Double = 0.0,
    // Openings (Doors, Windows, etc.)
    val openings: List<Opening> = emptyList(),
    // Allowances
    val extraRollAllowance: Int = 0,
    val extraMaterialPercent: Double = 0.0,
    // Wallpaper orientation
    val direction: WallpaperDirection = WallpaperDirection.VERTICAL,
    // Calculated Result
    val result: WallpaperResult = WallpaperResult()
)

data class WallpaperHistory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val project: WallpaperProject,
    val createdAt: Long = System.currentTimeMillis()
)
