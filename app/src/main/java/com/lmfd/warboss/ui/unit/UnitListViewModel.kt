package com.lmfd.warboss.ui.unit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.domain.usecase.GetUnitsForFactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class UnitListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getUnitsForFaction: GetUnitsForFactionUseCase,
) : ViewModel() {

    val factionId: String = checkNotNull(savedStateHandle["factionId"])

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<UnitListUiState> = combine(
        getUnitsForFaction(factionId),
        _searchQuery,
    ) { units, query ->
        val filtered = if (query.isBlank()) units
        else units.filter { it.name.contains(query, ignoreCase = true) }
        UnitListUiState.Success(filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UnitListUiState.Loading)

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
}
