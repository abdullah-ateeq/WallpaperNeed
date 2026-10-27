package com.example.wallpaperneed.model

import java.util.UUID

data class WallpaperWall(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Wall",
    val widthMm: Double = 5000.0,
    val heightMm: Double = 3000.0,
    val wallpaperEnabled: Boolean = true,
    val isFeatureWall: Boolean = false,
    val openings: List<Opening> = emptyList(),
    val wallpaperProduct: WallpaperProduct? = null
) {
    val grossAreaMm2: Double
        get() = widthMm * heightMm

    val openingAreaMm2: Double
        get() = openings.sumOf { it.totalAreaMm2 }

    val netAreaMm2: Double
        get() = maxOf(0.0, grossAreaMm2 - openingAreaMm2)
}
