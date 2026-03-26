package com.example.shapepaint.ui.gallery

import com.example.shapepaint.model.ProjectSummary

data class GalleryUiState(
    val projects: List<ProjectSummary> = emptyList()
)
