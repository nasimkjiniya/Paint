package com.example.shapepaint.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectWithShapes(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "projectId",
        entityColumn = "projectId"
    )
    val shapes: List<ShapeEntity>,
    @Relation(
        parentColumn = "projectId",
        entityColumn = "projectId"
    )
    val strokes: List<StrokeEntity>
)
