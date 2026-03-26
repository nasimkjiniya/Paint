package com.example.shapepaint.model

data class DrawableStroke(
    val id: Long,
    val points: List<StrokePoint>,
    val color: PaintColor,
    val strokeWidth: Float,
    val drawOrder: Int
)
