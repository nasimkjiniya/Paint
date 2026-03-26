package com.example.shapepaint.model

data class ProjectDetails(
    val projectId: Long,
    val title: String,
    val artistName: String,
    val backgroundImagePath: String?,
    val updatedAt: Long,
    val shapes: List<DrawableShape>,
    val strokes: List<DrawableStroke>
)
