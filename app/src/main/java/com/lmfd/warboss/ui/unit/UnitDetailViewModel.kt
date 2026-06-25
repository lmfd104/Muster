package com.lmfd.warboss.ui.unit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.domain.model.ArmyList
import com.lmfd.warboss.domain.usecase.AddUnitToListUseCase
import com.lmfd.warboss.domain.usecase.CreateArmyListUseCase
import com.lmfd.warboss.domain.usecase.GetArmyListsUseCase
import com.lmfd.warboss.domain.usecase.GetUnitDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getUnitDetail: GetUnitDetailUseCase,
    getArmyListsUseCase: GetArmyListsUseCase,
    private val addUnitToListUseCase: AddUnitToListUseCase,
    private val createArmyListUseCase: CreateArmyListUseCase,
) : ViewModel() {

    private val unitId: String = checkNotNull(savedStateHandle["unitId"])

    val uiState: StateFlow<UnitDetailUiState> = flow {
        val detail = getUnitDetail(unitId)
        emit(if (detail != null) UnitDetailUiState.Success(detail) else UnitDetailUiState.NotFound)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UnitDetailUiState.Loading)

    val armyListsForFaction: StateFlow<List<ArmyList>> = combine(
        uiState,
        getArmyListsUseCase(),
    ) { state, lists ->
        if (state is UnitDetailUiState.Success) {
            lists.filter { it.factionId == state.detail.summary.factionId }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> = _showAddSheet.asStateFlow()

    private val _addResult = MutableStateFlow<AddToListResult?>(null)
    val addResult: StateFlow<AddToListResult?> = _addResult.asStateFlow()

    fun openAddSheet() { _showAddSheet.value = true }
    fun closeAddSheet() { _showAddSheet.value = false }
    fun clearAddResult() { _addResult.value = null }

    fun addToList(listId: String, listName: String) {
        viewModelScope.launch {
            runCatching { addUnitToListUseCase(listId, unitId) }
                .onSuccess {
                    _addResult.value = AddToListResult.Success(listName)
                    _showAddSheet.value = false
                }
                .onFailure { _addResult.value = AddToListResult.Error(it.message ?: "Failed to add") }
        }
    }

    fun createAndAdd(name: String, pointsLimit: Int) {
        val state = uiState.value as? UnitDetailUiState.Success ?: return
        val summary = state.detail.summary
        val factionName = state.detail.factionKeywords.firstOrNull() ?: summary.factionId
        viewModelScope.launch {
            runCatching {
                val listId = createArmyListUseCase(name, summary.factionId, factionName, pointsLimit)
                addUnitToListUseCase(listId, unitId)
                name
            }
                .onSuccess {
                    _addResult.value = AddToListResult.Success(it)
                    _showAddSheet.value = false
                }
                .onFailure { _addResult.value = AddToListResult.Error(it.message ?: "Failed to create list") }
        }
    }
}

sealed interface AddToListResult {
    data class Success(val listName: String) : AddToListResult
    data class Error(val message: String) : AddToListResult
}
