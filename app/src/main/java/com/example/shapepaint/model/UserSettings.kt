package com.example.shapepaint.model

data class UserSettings(
    val artistName: String = "",
    val showGrid: Boolean = true,
    val showShapeLabels: Boolean = true,
    val defaultSize: Int = 0
)
