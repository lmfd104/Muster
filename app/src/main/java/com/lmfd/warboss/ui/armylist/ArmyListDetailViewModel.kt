package com.lmfd.warboss.ui.armylist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.domain.usecase.GetArmyListDetailUseCase
import com.lmfd.warboss.domain.usecase.RemoveEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArmyListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getArmyListDetail: GetArmyListDetailUseCase,
    private val removeEntry: RemoveEntryUseCase,
) : ViewModel() {

    private val listId: String = checkNotNull(savedStateHandle["listId"])

    val uiState: StateFlow<ArmyListDetailUiState> = getArmyListDetail(listId)
        .map { (list, entries) ->
            if (list != null) ArmyListDetailUiState.Success(list, entries)
            else ArmyListDetailUiState.NotFound
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArmyListDetailUiState.Loading)

    fun removeEntry(entryId: String) {
        viewModelScope.launch { removeEntry.invoke(entryId) }
    }
}
