package com.example.shapepaint.model

data class DrawableShape(
    val id: Long,
    val type: ShapeType,
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val color: PaintColor,
    val drawOrder: Int
)
