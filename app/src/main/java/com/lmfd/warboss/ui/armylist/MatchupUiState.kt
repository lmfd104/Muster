package com.lmfd.warboss.ui.armylist

import com.lmfd.warboss.domain.model.MatchupAnalysis

sealed interface MatchupUiState {
    data object Idle : MatchupUiState
    data object Loading : MatchupUiState
    data class Success(val analysis: MatchupAnalysis) : MatchupUiState
    data class Error(val message: String) : MatchupUiState
}
