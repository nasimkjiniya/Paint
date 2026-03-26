package com.example.shapepaint.ui.common

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shapepaint.data.repository.ProjectRepository
import com.example.shapepaint.data.repository.ReferenceRepository
import com.example.shapepaint.data.repository.SettingsRepository
import com.example.shapepaint.ui.editor.EditorViewModel
import com.example.shapepaint.ui.gallery.GalleryViewModel
import com.example.shapepaint.ui.reference.ReferenceSearchViewModel
import com.example.shapepaint.ui.settings.SettingsViewModel

class GalleryViewModelFactory(
    private val projectRepository: ProjectRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GalleryViewModel(projectRepository) as T
    }
}

class EditorViewModelFactory(
    private val projectRepository: ProjectRepository,
    private val settingsRepository: SettingsRepository,
    private val projectId: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditorViewModel(
            projectRepository = projectRepository,
            settingsRepository = settingsRepository,
            savedStateHandle = SavedStateHandle(mapOf(EditorViewModel.KEY_PROJECT_ID to projectId))
        ) as T
    }
}

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(settingsRepository) as T
    }
}

class ReferenceSearchViewModelFactory(
    private val referenceRepository: ReferenceRepository,
    private val projectRepository: ProjectRepository,
    private val projectId: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReferenceSearchViewModel(
            referenceRepository = referenceRepository,
            projectRepository = projectRepository,
            savedStateHandle = SavedStateHandle(mapOf(ReferenceSearchViewModel.KEY_PROJECT_ID to projectId))
        ) as T
    }
}
