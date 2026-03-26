package com.example.shapepaint.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "strokes",
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
data class StrokeEntity(
    @PrimaryKey(autoGenerate = true) val strokeId: Long = 0,
    val projectId: Long,
    val colorHex: String,
    val strokeWidth: Float,
    val pointsData: String,
    val drawOrder: Int
)
