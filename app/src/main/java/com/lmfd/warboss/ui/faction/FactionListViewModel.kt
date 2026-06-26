package com.lmfd.warboss.ui.faction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.domain.usecase.GetFactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FactionListViewModel @Inject constructor(
    getFactions: GetFactionsUseCase,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<FactionListUiState> = combine(
        getFactions(),
        _searchQuery,
    ) { factions, query ->
        val filtered = if (query.isBlank()) factions
        else factions.filter { it.name.contains(query, ignoreCase = true) }
        FactionListUiState.Success(filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FactionListUiState.Loading)

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
}
