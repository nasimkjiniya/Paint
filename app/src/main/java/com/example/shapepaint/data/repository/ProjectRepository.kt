package com.example.shapepaint.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.shapepaint.R
import com.example.shapepaint.data.local.ProjectDao
import com.example.shapepaint.data.local.ProjectEntity
import com.example.shapepaint.data.local.ShapeEntity
import com.example.shapepaint.data.local.StrokeEntity
import com.example.shapepaint.model.DrawableShape
import com.example.shapepaint.model.DrawableStroke
import com.example.shapepaint.model.PaintColor
import com.example.shapepaint.model.ProjectDetails
import com.example.shapepaint.model.ProjectSummary
import com.example.shapepaint.model.ShapeType
import com.example.shapepaint.model.StrokePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

interface ProjectRepository {
    fun observeProjects(): Flow<List<ProjectSummary>>
    fun observeProject(projectId: Long): Flow<ProjectDetails?>
    suspend fun createProject(title: String? = null): Long
    suspend fun deleteProject(projectId: Long)
    suspend fun saveProject(
        projectId: Long,
        title: String,
        artistName: String,
        shapes: List<DrawableShape>,
        strokes: List<DrawableStroke>,
        backgroundImagePath: String?
    )
    suspend fun clearProject(projectId: Long)
    suspend fun importBackground(projectId: Long, bitmap: Bitmap): String
}

class DefaultProjectRepository(
    private val projectDao: ProjectDao,
    private val settingsRepository: SettingsRepository,
    private val appContext: Context
) : ProjectRepository {

    override fun observeProjects(): Flow<List<ProjectSummary>> {
        return projectDao.observeProjectSummaries().map { summaries ->
            summaries.map {
                ProjectSummary(
                    projectId = it.projectId,
                    title = it.title,
                    artistName = it.artistName,
                    backgroundImagePath = it.backgroundImagePath,
                    updatedAt = it.updatedAt,
                    shapeCount = it.shapeCount
                )
            }
        }
    }

    override fun observeProject(projectId: Long): Flow<ProjectDetails?> {
        return projectDao.observeProject(projectId).map { record ->
            record?.let {
                ProjectDetails(
                    projectId = it.project.projectId,
                    title = it.project.title,
                    artistName = it.project.artistName,
                    backgroundImagePath = it.project.backgroundImagePath,
                    updatedAt = it.project.updatedAt,
                    shapes = it.shapes.map { shape ->
                        DrawableShape(
                            id = shape.shapeId,
                            type = ShapeType.valueOf(shape.type),
                            centerX = shape.centerX,
                            centerY = shape.centerY,
                            width = shape.width,
                            height = shape.height,
                            color = PaintColor.fromHex(shape.colorHex),
                            drawOrder = shape.drawOrder
                        )
                    },
                    strokes = it.strokes.map { stroke ->
                        DrawableStroke(
                            id = stroke.strokeId,
                            points = decodePoints(stroke.pointsData),
                            color = PaintColor.fromHex(stroke.colorHex),
                            strokeWidth = stroke.strokeWidth,
                            drawOrder = stroke.drawOrder
                        )
                    }
                )
            }
        }
    }

    override suspend fun createProject(title: String?): Long = withContext(Dispatchers.IO) {
        val settings = settingsRepository.settings.first()
        val normalizedTitle = title?.trim().orEmpty()
        projectDao.insertProject(
            ProjectEntity(
                title = normalizedTitle.ifBlank {
                    appContext.getString(
                        R.string.default_project_title,
                        System.currentTimeMillis().toString().takeLast(4)
                    )
                },
                artistName = settings.artistName,
                backgroundImagePath = null,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteProject(projectId: Long) = withContext(Dispatchers.IO) {
        val existing = projectDao.getProject(projectId) ?: return@withContext
        existing.backgroundImagePath?.let { path ->
            runCatching { File(path).takeIf { it.exists() }?.delete() }
        }
        projectDao.deleteProject(projectId)
    }

    override suspend fun saveProject(
        projectId: Long,
        title: String,
        artistName: String,
        shapes: List<DrawableShape>,
        strokes: List<DrawableStroke>,
        backgroundImagePath: String?
    ) = withContext(Dispatchers.IO) {
        val existing = projectDao.getProject(projectId) ?: return@withContext
        projectDao.updateProject(
            existing.copy(
                title = title.trim().ifBlank { existing.title },
                artistName = artistName.trim().ifBlank { existing.artistName },
                backgroundImagePath = backgroundImagePath,
                updatedAt = System.currentTimeMillis()
            )
        )
        projectDao.deleteShapesForProject(projectId)
        projectDao.deleteStrokesForProject(projectId)
        if (shapes.isNotEmpty()) {
            projectDao.insertShapes(
                shapes.map { shape ->
                    ShapeEntity(
                        shapeId = if (shape.id > 0) shape.id else 0,
                        projectId = projectId,
                        type = shape.type.name,
                        centerX = shape.centerX,
                        centerY = shape.centerY,
                        width = shape.width,
                        height = shape.height,
                        colorHex = shape.color.hex,
                        drawOrder = shape.drawOrder
                    )
                }
            )
        }
        if (strokes.isNotEmpty()) {
            projectDao.insertStrokes(
                strokes.map { stroke ->
                    StrokeEntity(
                        strokeId = if (stroke.id > 0) stroke.id else 0,
                        projectId = projectId,
                        colorHex = stroke.color.hex,
                        strokeWidth = stroke.strokeWidth,
                        pointsData = encodePoints(stroke.points),
                        drawOrder = stroke.drawOrder
                    )
                }
            )
        }
    }

    override suspend fun clearProject(projectId: Long) = withContext(Dispatchers.IO) {
        val existing = projectDao.getProject(projectId) ?: return@withContext
        saveProject(
            projectId = projectId,
            title = existing.title,
            artistName = existing.artistName,
            shapes = emptyList(),
            strokes = emptyList(),
            backgroundImagePath = null
        )
    }

    override suspend fun importBackground(projectId: Long, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val outputDir = File(appContext.filesDir, "backgrounds").apply { mkdirs() }
        val outputFile = File(outputDir, "project_${projectId}_${System.currentTimeMillis()}.png")
        FileOutputStream(outputFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        outputFile.absolutePath
    }

    private fun encodePoints(points: List<StrokePoint>): String {
        return points.joinToString("|") { point -> "${point.x},${point.y}" }
    }

    private fun decodePoints(pointsData: String): List<StrokePoint> {
        if (pointsData.isBlank()) return emptyList()
        return pointsData.split("|").mapNotNull { token ->
            val parts = token.split(",")
            if (parts.size != 2) {
                null
            } else {
                val x = parts[0].toFloatOrNull()
                val y = parts[1].toFloatOrNull()
                if (x == null || y == null) null else StrokePoint(x, y)
            }
        }
    }
}
