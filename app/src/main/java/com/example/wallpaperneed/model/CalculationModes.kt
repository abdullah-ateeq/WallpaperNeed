package com.example.wallpaperneed.model

enum class CalculationMode(val displayName: String, val description: String) {
    COMPLETE_ROOM("Complete Room", "Calculate for a rectangular 4-wall room using perimeter and height."),
    SINGLE_WALL("Single / Feature Wall", "Calculate for an individual accent or feature wall."),
    CUSTOM_WALLS("Custom Wall Sections", "Calculate for arbitrary individual walls (e.g., L-shaped, corridors)."),
    MULTIPLE_ROOMS("Multiple Rooms", "Plan an entire property across multiple distinct rooms.")
}

enum class RollCalculationMode(val displayName: String, val description: String) {
    CONSERVATIVE("Conservative wall-by-wall", "Each wall's rolls are calculated separately without assuming leftover strips are interchangeable between walls."),
    COMBINED("Combined project drops", "Drops with compatible lengths and identical wallpaper are pooled together to maximize roll efficiency.")
}

enum class DropOptimizationMode(val displayName: String, val description: String) {
    CONSERVATIVE("Conservative", "Required drops are based on total wall coverage. Openings are subtracted for surface area reference only."),
    BASIC_OPENING_OPTIMIZATION("Basic Opening Optimization", "Allows deducting whole wallpaper strips that fall entirely inside full-height openings.")
}

enum class WallpaperDirection(val displayName: String) {
    VERTICAL("Vertical drops"),
    CUSTOM("Custom")
}
