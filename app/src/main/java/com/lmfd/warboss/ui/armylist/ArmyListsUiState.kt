package com.lmfd.warboss.ui.armylist

import com.lmfd.warboss.domain.model.ArmyList
import com.lmfd.warboss.domain.model.ArmyListEntry

sealed interface ArmyListsUiState {
    data object Loading : ArmyListsUiState
    data class Success(val lists: List<ArmyList>) : ArmyListsUiState
}

sealed interface ArmyListDetailUiState {
    data object Loading : ArmyListDetailUiState
    data object NotFound : ArmyListDetailUiState
    data class Success(val list: ArmyList, val entries: List<ArmyListEntry>) : ArmyListDetailUiState
}
