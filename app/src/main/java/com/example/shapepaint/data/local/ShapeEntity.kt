package com.example.shapepaint.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shapes",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["projectId"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class ShapeEntity(
    @PrimaryKey(autoGenerate = true) val shapeId: Long = 0,
    val projectId: Long,
    val type: String,
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val colorHex: String,
    val drawOrder: Int
)
