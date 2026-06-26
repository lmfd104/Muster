package com.lmfd.warboss.ui.armylist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.data.billing.BillingManager
import com.lmfd.warboss.domain.usecase.AnalyzeArmyListUseCase
import com.lmfd.warboss.domain.usecase.GetArmyListDetailUseCase
import com.lmfd.warboss.domain.usecase.RemoveEntryUseCase
import com.lmfd.warboss.domain.usecase.UpdateEntryQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArmyListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getArmyListDetail: GetArmyListDetailUseCase,
    private val removeEntry: RemoveEntryUseCase,
    private val updateEntryQuantity: UpdateEntryQuantityUseCase,
    private val analyzeArmyList: AnalyzeArmyListUseCase,
    billingManager: BillingManager,
) : ViewModel() {

    private val listId: String = checkNotNull(savedStateHandle["listId"])

    val uiState: StateFlow<ArmyListDetailUiState> = getArmyListDetail(listId)
        .map { (list, entries) ->
            if (list != null) ArmyListDetailUiState.Success(list, entries)
            else ArmyListDetailUiState.NotFound
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArmyListDetailUiState.Loading)

    val isPro: StateFlow<Boolean> = billingManager.isPro
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _analysisState = MutableStateFlow<AiAnalysisUiState>(AiAnalysisUiState.Idle)
    val analysisState: StateFlow<AiAnalysisUiState> = _analysisState.asStateFlow()

    fun removeEntry(entryId: String) {
        viewModelScope.launch { removeEntry.invoke(entryId) }
    }

    fun incrementQuantity(entryId: String, currentQty: Int) {
        viewModelScope.launch { updateEntryQuantity(entryId, currentQty + 1) }
    }

    fun decrementQuantity(entryId: String, currentQty: Int) {
        if (currentQty > 1) viewModelScope.launch { updateEntryQuantity(entryId, currentQty - 1) }
    }

    fun analyzeList() {
        val current = uiState.value
        if (current !is ArmyListDetailUiState.Success) return
        if (current.entries.isEmpty()) return

        viewModelScope.launch {
            _analysisState.value = AiAnalysisUiState.Loading
            _analysisState.value = try {
                val analysis = analyzeArmyList(
                    factionName = current.list.factionName,
                    totalPoints = current.list.pointsTotal,
                    pointsLimit = current.list.pointsLimit,
                    entries = current.entries,
                )
                AiAnalysisUiState.Success(analysis)
            } catch (e: Exception) {
                AiAnalysisUiState.Error(e.message ?: "Analysis failed")
            }
        }
    }

    fun dismissAnalysis() {
        _analysisState.value = AiAnalysisUiState.Idle
    }
}
