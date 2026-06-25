package com.lmfd.warboss.ui.unit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.domain.usecase.GetUnitDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class UnitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getUnitDetail: GetUnitDetailUseCase,
) : ViewModel() {

    private val unitId: String = checkNotNull(savedStateHandle["unitId"])

    val uiState: StateFlow<UnitDetailUiState> = flow {
        val detail = getUnitDetail(unitId)
        emit(if (detail != null) UnitDetailUiState.Success(detail) else UnitDetailUiState.NotFound)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UnitDetailUiState.Loading)
}
