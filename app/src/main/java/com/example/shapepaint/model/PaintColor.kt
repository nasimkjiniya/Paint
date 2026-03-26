package com.example.shapepaint.model

import android.graphics.Color

enum class PaintColor(
    val hex: String
) {
    RED("#D1495B"),
    GREEN("#4F772D"),
    BLUE("#2E86AB"),
    ORANGE("#F28F3B"),
    PURPLE("#7B5EA7"),
    CHARCOAL("#3D405B");

    val colorInt: Int
        get() = Color.parseColor(hex)

    companion object {
        fun fromHex(hex: String): PaintColor = entries.firstOrNull { it.hex == hex } ?: RED
    }
}
