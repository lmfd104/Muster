package com.lmfd.warboss.ui.faction

import com.lmfd.warboss.domain.model.Faction

sealed interface FactionListUiState {
    data object Loading : FactionListUiState
    data class Success(val factions: List<Faction>) : FactionListUiState
}
