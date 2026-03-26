package com.example.shapepaint.ui.editor

import com.example.shapepaint.model.DrawableShape
import com.example.shapepaint.model.DrawableStroke
import com.example.shapepaint.model.PaintColor
import com.example.shapepaint.model.ShapeType

data class EditorUiState(
    val projectId: Long? = null,
    val title: String = "",
    val artistName: String = "",
    val selectedShape: ShapeType = ShapeType.SQUARE,
    val selectedColor: PaintColor = PaintColor.RED,
    val uniformSize: Float = 140f,
    val width: Float = 200f,
    val height: Float = 140f,
    val strokeWidth: Float = 12f,
    val shapes: List<DrawableShape> = emptyList(),
    val strokes: List<DrawableStroke> = emptyList(),
    val showGrid: Boolean = true,
    val showLabels: Boolean = true,
    val backgroundImagePath: String? = null,
    val toolsExpanded: Boolean = true,
    val hasUnsavedChanges: Boolean = false
)
