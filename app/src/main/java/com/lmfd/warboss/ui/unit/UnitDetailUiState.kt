package com.lmfd.warboss.ui.unit

import com.lmfd.warboss.domain.model.UnitDetail

sealed interface UnitDetailUiState {
    data object Loading : UnitDetailUiState
    data object NotFound : UnitDetailUiState
    data class Success(val detail: UnitDetail) : UnitDetailUiState
}
