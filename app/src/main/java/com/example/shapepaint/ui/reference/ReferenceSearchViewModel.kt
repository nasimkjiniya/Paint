package com.example.shapepaint.ui.reference

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shapepaint.R
import com.example.shapepaint.data.repository.ProjectRepository
import com.example.shapepaint.data.repository.ReferenceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReferenceSearchViewModel(
    private val referenceRepository: ReferenceRepository,
    private val projectRepository: ProjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ReferenceSearchUiState(projectId = savedStateHandle.get<Long>(KEY_PROJECT_ID))
    )
    val uiState: StateFlow<ReferenceSearchUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReferenceSearchEvent>()
    val events: SharedFlow<ReferenceSearchEvent> = _events.asSharedFlow()

    fun updateQuery(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    results = emptyList(),
                    emptyMessageRes = R.string.reference_empty_start
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    results = emptyList(),
                    emptyMessageRes = null
                )
            }
            val results = runCatching { referenceRepository.searchArtworks(query) }.getOrElse {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = emptyList(),
                        emptyMessageRes = R.string.reference_error
                    )
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    results = results,
                    emptyMessageRes = if (results.isEmpty()) R.string.reference_no_results else null
                )
            }
        }
    }

    fun importReference(objectId: String, imageUrl: String) {
        viewModelScope.launch {
            val projectId = _uiState.value.projectId ?: return@launch
            _uiState.update { it.copy(importInFlightId = objectId) }
            val bitmap = referenceRepository.downloadBitmap(imageUrl)
            if (bitmap == null) {
                _uiState.update { it.copy(importInFlightId = null) }
                _events.emit(ReferenceSearchEvent.ImportFailed)
                return@launch
            }
            projectRepository.importBackground(projectId, bitmap)
            _uiState.update { it.copy(importInFlightId = null) }
            _events.emit(ReferenceSearchEvent.ReferenceImported)
        }
    }

    companion object {
        const val KEY_PROJECT_ID = "project_id"
    }
}

sealed interface ReferenceSearchEvent {
    data object ReferenceImported : ReferenceSearchEvent
    data object ImportFailed : ReferenceSearchEvent
}
