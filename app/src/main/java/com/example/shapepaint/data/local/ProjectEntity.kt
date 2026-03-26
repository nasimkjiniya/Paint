package com.example.shapepaint.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val projectId: Long = 0,
    val title: String,
    val artistName: String,
    val backgroundImagePath: String?,
    val updatedAt: Long
)
