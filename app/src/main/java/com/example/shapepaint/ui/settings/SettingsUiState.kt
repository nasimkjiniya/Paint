package com.example.shapepaint.ui.settings

data class SettingsUiState(
    val artistName: String = "",
    val showGrid: Boolean = true,
    val showShapeLabels: Boolean = true,
    val defaultSize: Int = 0
)
