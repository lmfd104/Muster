package com.lmfd.warboss.ui.unit

import com.lmfd.warboss.domain.model.UnitSummary

sealed interface UnitListUiState {
    data object Loading : UnitListUiState
    data class Success(val units: List<UnitSummary>) : UnitListUiState
}
