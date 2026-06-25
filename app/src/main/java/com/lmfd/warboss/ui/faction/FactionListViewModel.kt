package com.lmfd.warboss.ui.faction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.domain.usecase.GetFactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FactionListViewModel @Inject constructor(
    getFactions: GetFactionsUseCase,
) : ViewModel() {

    val uiState: StateFlow<FactionListUiState> = getFactions()
        .map { factions -> FactionListUiState.Success(factions) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FactionListUiState.Loading)
}
