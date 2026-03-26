package com.example.shapepaint.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shapepaint.data.repository.SettingsRepository
import com.example.shapepaint.model.UserSettings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            artistName = settingsRepository.defaultArtistName,
            defaultSize = settingsRepository.defaultSize
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.value = SettingsUiState(
                    artistName = settings.artistName,
                    showGrid = settings.showGrid,
                    showShapeLabels = settings.showShapeLabels,
                    defaultSize = settings.defaultSize
                )
            }
        }
    }

    fun updateArtistName(value: String) {
        _uiState.update { it.copy(artistName = value) }
    }

    fun updateShowGrid(value: Boolean) {
        _uiState.update { it.copy(showGrid = value) }
    }

    fun updateShowShapeLabels(value: Boolean) {
        _uiState.update { it.copy(showShapeLabels = value) }
    }

    fun updateDefaultSize(value: Int) {
        _uiState.update { it.copy(defaultSize = value) }
    }

    fun save() {
        viewModelScope.launch {
            val current = _uiState.value
            settingsRepository.save(
                UserSettings(
                    artistName = current.artistName,
                    showGrid = current.showGrid,
                    showShapeLabels = current.showShapeLabels,
                    defaultSize = current.defaultSize
                )
            )
            _events.emit(SettingsEvent.Saved)
        }
    }
}

sealed interface SettingsEvent {
    data object Saved : SettingsEvent
}
