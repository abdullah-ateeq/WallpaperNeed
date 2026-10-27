package com.example.wallpaperneed.model

import java.util.UUID

enum class OpeningType(val displayName: String) {
    DOOR("Door"),
    WINDOW("Window"),
    ARCH("Arch Approximation"),
    CABINET("Built-in Cabinet"),
    OTHER("Other Opening")
}

data class Opening(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Opening",
    val type: OpeningType = OpeningType.DOOR,
    val widthMm: Double = 900.0,
    val heightMm: Double = 2100.0,
    val quantity: Int = 1,
    val wallId: String = "" // Optional reference to a specific wall
) {
    val singleAreaMm2: Double
        get() = widthMm * heightMm

    val totalAreaMm2: Double
        get() = singleAreaMm2 * quantity
}
