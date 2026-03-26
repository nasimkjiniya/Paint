package com.example.shapepaint.data.local

data class ProjectSummaryEntity(
    val projectId: Long,
    val title: String,
    val artistName: String,
    val backgroundImagePath: String?,
    val updatedAt: Long,
    val shapeCount: Int
)
