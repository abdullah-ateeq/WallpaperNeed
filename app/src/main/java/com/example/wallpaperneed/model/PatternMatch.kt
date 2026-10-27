package com.example.wallpaperneed.model

enum class PatternMatch(val displayName: String, val description: String) {
    NONE("None", "No repeat or pattern matching needed."),
    STRAIGHT_MATCH("Straight Match", "Pattern matches horizontally across adjacent strips at the same vertical level."),
    HALF_DROP("Half Drop", "Pattern repeats every other drop, shifted vertically by half a repeat."),
    RANDOM("Random", "Free match with no alignment required between adjacent strips.");

    val hasHalfDropWarning: Boolean
        get() = this == HALF_DROP
}
