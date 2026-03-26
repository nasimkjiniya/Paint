package com.example.shapepaint.ui.editor

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shapepaint.data.repository.ProjectRepository
import com.example.shapepaint.data.repository.SettingsRepository
import com.example.shapepaint.model.DrawableShape
import com.example.shapepaint.model.DrawableStroke
import com.example.shapepaint.model.PaintColor
import com.example.shapepaint.model.ShapeType
import com.example.shapepaint.model.StrokePoint
import kotlin.math.abs
import kotlin.math.hypot
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditorViewModel(
    private val projectRepository: ProjectRepository,
    private val settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditorEvent>()
    val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    private var nextShapeId = 1L
    private var nextStrokeId = 1L
    private var nextDrawOrder = 1
    private var savedSnapshot: EditorContentSnapshot? = null

    init {
        val initialProjectId = savedStateHandle.get<Long?>(KEY_PROJECT_ID)
        val expanded = savedStateHandle.get<Boolean>(KEY_TOOLS_EXPANDED) ?: true
        _uiState.update { withUnsavedFlag(it.copy(toolsExpanded = expanded)) }
        if (initialProjectId == null) {
            viewModelScope.launch {
                val projectId = projectRepository.createProject()
                savedStateHandle[KEY_PROJECT_ID] = projectId
                observeProject(projectId)
            }
        } else {
            observeProject(initialProjectId)
        }

        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update { current ->
                    withUnsavedFlag(current.copy(
                        artistName = settings.artistName,
                        showGrid = settings.showGrid,
                        showLabels = settings.showShapeLabels,
                        uniformSize = if (current.shapes.isEmpty()) settings.defaultSize.toFloat() else current.uniformSize,
                        width = if (current.shapes.isEmpty()) settings.defaultSize.toFloat() * 1.3f else current.width,
                        height = if (current.shapes.isEmpty()) settings.defaultSize.toFloat() else current.height,
                        strokeWidth = if (current.strokes.isEmpty()) 12f else current.strokeWidth
                    ))
                }
            }
        }
    }

    private fun observeProject(projectId: Long) {
        _uiState.update { withUnsavedFlag(it.copy(projectId = projectId)) }
        viewModelScope.launch {
            projectRepository.observeProject(projectId).filterNotNull().collect { project ->
                nextShapeId = (project.shapes.maxOfOrNull { it.id } ?: 0L) + 1L
                nextStrokeId = (project.strokes.maxOfOrNull { it.id } ?: 0L) + 1L
                nextDrawOrder = (
                    listOf(
                        project.shapes.maxOfOrNull { it.drawOrder } ?: 0,
                        project.strokes.maxOfOrNull { it.drawOrder } ?: 0
                    ).maxOrNull() ?: 0
                ) + 1
                savedSnapshot = EditorContentSnapshot(
                    title = project.title.trim(),
                    shapes = project.shapes,
                    strokes = project.strokes,
                    backgroundImagePath = project.backgroundImagePath
                )
                _uiState.update { current ->
                    current.copy(
                        projectId = project.projectId,
                        title = project.title,
                        artistName = current.artistName.ifBlank { project.artistName },
                        shapes = project.shapes,
                        strokes = project.strokes,
                        backgroundImagePath = project.backgroundImagePath,
                        hasUnsavedChanges = false
                    )
                }
            }
        }
    }

    fun selectShape(shapeType: ShapeType) {
        _uiState.update { withUnsavedFlag(it.copy(selectedShape = shapeType)) }
    }

    fun selectColor(color: PaintColor) {
        _uiState.update { withUnsavedFlag(it.copy(selectedColor = color)) }
    }

    fun updateUniformSize(value: Float) {
        _uiState.update { withUnsavedFlag(it.copy(uniformSize = value)) }
    }

    fun updateWidth(value: Float) {
        _uiState.update { withUnsavedFlag(it.copy(width = value)) }
    }

    fun updateHeight(value: Float) {
        _uiState.update { withUnsavedFlag(it.copy(height = value)) }
    }

    fun updateTitle(value: String) {
        _uiState.update { withUnsavedFlag(it.copy(title = value)) }
    }

    fun updateStrokeWidth(value: Float) {
        _uiState.update { withUnsavedFlag(it.copy(strokeWidth = value)) }
    }

    fun toggleTools() {
        val expanded = !_uiState.value.toolsExpanded
        savedStateHandle[KEY_TOOLS_EXPANDED] = expanded
        _uiState.update { withUnsavedFlag(it.copy(toolsExpanded = expanded)) }
    }

    fun addShapeAt(x: Float, y: Float) {
        _uiState.update { current ->
            if (current.selectedShape == ShapeType.FREEHAND || current.selectedShape == ShapeType.ERASER) {
                return@update current
            }
            val isLocked = current.selectedShape.lockAspectRatio
            val width = if (isLocked) current.uniformSize else current.width
            val height = if (isLocked) current.uniformSize else current.height
            withUnsavedFlag(current.copy(
                shapes = current.shapes + DrawableShape(
                    id = nextShapeId++,
                    type = current.selectedShape,
                    centerX = x,
                    centerY = y,
                    width = width,
                    height = height,
                    color = current.selectedColor,
                    drawOrder = nextDrawOrder++
                )
            ))
        }
    }

    fun addStroke(points: List<StrokePoint>) {
        if (points.size < 2) return
        _uiState.update { current ->
            if (current.selectedShape != ShapeType.FREEHAND) return@update current
            withUnsavedFlag(current.copy(
                strokes = current.strokes + DrawableStroke(
                    id = nextStrokeId++,
                    points = points,
                    color = current.selectedColor,
                    strokeWidth = current.strokeWidth,
                    drawOrder = nextDrawOrder++
                )
            ))
        }
    }

    fun eraseAt(x: Float, y: Float) {
        _uiState.update { current ->
            if (current.selectedShape != ShapeType.ERASER) return@update current
            val radius = current.strokeWidth * 1.5f
            val shapeHit = current.shapes
                .filter { containsPoint(it, x, y, radius) }
                .maxByOrNull { it.drawOrder }
            val strokeHit = current.strokes
                .filter { containsPoint(it, x, y, radius) }
                .maxByOrNull { it.drawOrder }

            when {
                shapeHit == null && strokeHit == null -> current
                shapeHit != null && (strokeHit == null || shapeHit.drawOrder >= strokeHit.drawOrder) -> {
                    withUnsavedFlag(current.copy(shapes = current.shapes.filterNot { it.id == shapeHit.id }))
                }

                else -> {
                    withUnsavedFlag(current.copy(strokes = current.strokes.filterNot { it.id == strokeHit?.id }))
                }
            }
        }
    }

    fun undoLastShape() {
        _uiState.update { current ->
            val lastShapeOrder = current.shapes.maxOfOrNull { it.drawOrder } ?: Int.MIN_VALUE
            val lastStrokeOrder = current.strokes.maxOfOrNull { it.drawOrder } ?: Int.MIN_VALUE
            when {
                lastShapeOrder == Int.MIN_VALUE && lastStrokeOrder == Int.MIN_VALUE -> current
                lastShapeOrder >= lastStrokeOrder -> withUnsavedFlag(current.copy(shapes = current.shapes.filterNot { it.drawOrder == lastShapeOrder }))
                else -> withUnsavedFlag(current.copy(strokes = current.strokes.filterNot { it.drawOrder == lastStrokeOrder }))
            }
        }
    }

    fun clearCanvas() {
        _uiState.update { current ->
            withUnsavedFlag(current.copy(shapes = emptyList(), strokes = emptyList(), backgroundImagePath = null))
        }
    }

    fun saveProject() {
        viewModelScope.launch {
            val current = _uiState.value
            val projectId = current.projectId ?: return@launch
            projectRepository.saveProject(
                projectId = projectId,
                title = current.title,
                artistName = current.artistName,
                shapes = current.shapes,
                strokes = current.strokes,
                backgroundImagePath = current.backgroundImagePath
            )
            _events.emit(EditorEvent.ProjectSaved(projectId))
        }
    }

    fun importBackground(bitmap: Bitmap) {
        viewModelScope.launch {
            val projectId = _uiState.value.projectId ?: return@launch
            val path = projectRepository.importBackground(projectId, bitmap)
            _uiState.update { withUnsavedFlag(it.copy(backgroundImagePath = path)) }
            _events.emit(EditorEvent.BackgroundImported)
        }
    }

    fun requestExport() {
        viewModelScope.launch {
            val projectId = _uiState.value.projectId ?: return@launch
            _events.emit(EditorEvent.ExportRequested(projectId))
        }
    }

    fun deleteProject() {
        viewModelScope.launch {
            val projectId = _uiState.value.projectId ?: return@launch
            projectRepository.deleteProject(projectId)
            _events.emit(EditorEvent.ProjectDeleted)
        }
    }

    companion object {
        const val KEY_PROJECT_ID = "project_id"
        const val KEY_TOOLS_EXPANDED = "tools_expanded"
    }

    private fun withUnsavedFlag(state: EditorUiState): EditorUiState {
        val snapshot = savedSnapshot
        val hasUnsavedChanges = snapshot != null && snapshot != EditorContentSnapshot(
            title = state.title.trim(),
            shapes = state.shapes,
            strokes = state.strokes,
            backgroundImagePath = state.backgroundImagePath
        )
        return state.copy(hasUnsavedChanges = hasUnsavedChanges)
    }

    private data class EditorContentSnapshot(
        val title: String,
        val shapes: List<DrawableShape>,
        val strokes: List<DrawableStroke>,
        val backgroundImagePath: String?
    )

    private fun containsPoint(shape: DrawableShape, x: Float, y: Float, radius: Float): Boolean {
        return when (shape.type) {
            ShapeType.SQUARE, ShapeType.RECTANGLE -> {
                x >= shape.centerX - shape.width / 2f - radius &&
                    x <= shape.centerX + shape.width / 2f + radius &&
                    y >= shape.centerY - shape.height / 2f - radius &&
                    y <= shape.centerY + shape.height / 2f + radius
            }

            ShapeType.CIRCLE -> {
                hypot(x - shape.centerX, y - shape.centerY) <= (shape.width / 2f) + radius
            }

            ShapeType.OVAL -> {
                val normalizedX = (x - shape.centerX) / ((shape.width / 2f) + radius)
                val normalizedY = (y - shape.centerY) / ((shape.height / 2f) + radius)
                (normalizedX * normalizedX) + (normalizedY * normalizedY) <= 1f
            }

            ShapeType.TRIANGLE -> {
                x >= shape.centerX - shape.width / 2f - radius &&
                    x <= shape.centerX + shape.width / 2f + radius &&
                    y >= shape.centerY - shape.height / 2f - radius &&
                    y <= shape.centerY + shape.height / 2f + radius
            }

            ShapeType.FREEHAND, ShapeType.ERASER -> false
        }
    }

    private fun containsPoint(stroke: DrawableStroke, x: Float, y: Float, radius: Float): Boolean {
        return stroke.points.any { point ->
            abs(point.x - x) <= radius && abs(point.y - y) <= radius
        }
    }
}

sealed interface EditorEvent {
    data class ProjectSaved(val projectId: Long) : EditorEvent
    data class ExportRequested(val projectId: Long) : EditorEvent
    data object BackgroundImported : EditorEvent
    data object ProjectDeleted : EditorEvent
}
