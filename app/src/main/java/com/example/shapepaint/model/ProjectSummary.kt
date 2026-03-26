package com.example.shapepaint.model

data class ProjectSummary(
    val projectId: Long,
    val title: String,
    val artistName: String,
    val backgroundImagePath: String?,
    val updatedAt: Long,
    val shapeCount: Int
)
