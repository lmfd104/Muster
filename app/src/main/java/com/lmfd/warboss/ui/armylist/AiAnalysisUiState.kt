package com.lmfd.warboss.ui.armylist

import com.lmfd.warboss.domain.model.ListAnalysis

sealed interface AiAnalysisUiState {
    data object Idle : AiAnalysisUiState
    data object Loading : AiAnalysisUiState
    data class Success(val analysis: ListAnalysis) : AiAnalysisUiState
    data class Error(val message: String) : AiAnalysisUiState
}
