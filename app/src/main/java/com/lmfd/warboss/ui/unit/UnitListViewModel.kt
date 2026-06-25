package com.lmfd.warboss.ui.unit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.domain.usecase.GetUnitsForFactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class UnitListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getUnitsForFaction: GetUnitsForFactionUseCase,
) : ViewModel() {

    val factionId: String = checkNotNull(savedStateHandle["factionId"])

    val uiState: StateFlow<UnitListUiState> = getUnitsForFaction(factionId)
        .map { units -> UnitListUiState.Success(units) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UnitListUiState.Loading)
}
