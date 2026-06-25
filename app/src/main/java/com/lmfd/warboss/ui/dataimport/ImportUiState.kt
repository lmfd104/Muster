package com.lmfd.warboss.ui.dataimport

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data class Interrupted(val startedAtMs: Long) : ImportUiState
    data class Downloading(val progress: Float) : ImportUiState   // -1f = indeterminate
    data class Parsing(val factionName: String, val progress: Float) : ImportUiState
    data class Complete(val factionCount: Int, val skippedCount: Int) : ImportUiState
    data class Error(val message: String) : ImportUiState
}
