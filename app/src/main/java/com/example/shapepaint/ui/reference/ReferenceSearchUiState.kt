package com.example.shapepaint.ui.reference

import com.example.shapepaint.model.ReferenceArtwork

data class ReferenceSearchUiState(
    val projectId: Long? = null,
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<ReferenceArtwork> = emptyList(),
    val emptyMessageRes: Int? = null,
    val importInFlightId: String? = null
)
