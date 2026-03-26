package com.example.shapepaint.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE projectId = :projectId")
    suspend fun deleteProject(projectId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShapes(shapes: List<ShapeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrokes(strokes: List<StrokeEntity>)

    @Query("DELETE FROM shapes WHERE projectId = :projectId")
    suspend fun deleteShapesForProject(projectId: Long)

    @Query("DELETE FROM strokes WHERE projectId = :projectId")
    suspend fun deleteStrokesForProject(projectId: Long)

    @Transaction
    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    fun observeProject(projectId: Long): Flow<ProjectWithShapes?>

    @Query(
        """
        SELECT projects.projectId AS projectId,
               projects.title AS title,
               projects.artistName AS artistName,
               projects.backgroundImagePath AS backgroundImagePath,
               projects.updatedAt AS updatedAt,
               (
                   (SELECT COUNT(*) FROM shapes WHERE shapes.projectId = projects.projectId) +
                   (SELECT COUNT(*) FROM strokes WHERE strokes.projectId = projects.projectId)
               ) AS shapeCount
        FROM projects
        ORDER BY projects.updatedAt DESC
        """
    )
    fun observeProjectSummaries(): Flow<List<ProjectSummaryEntity>>

    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    suspend fun getProject(projectId: Long): ProjectEntity?
}
