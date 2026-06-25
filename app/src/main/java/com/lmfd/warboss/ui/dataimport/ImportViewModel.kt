package com.lmfd.warboss.ui.dataimport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.data.bsdata.BsDataRepository
import com.lmfd.warboss.data.bsdata.ImportStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val repository: BsDataRepository,
) : ViewModel() {

    val uiState: StateFlow<ImportUiState> = repository.status
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ImportUiState.Idle)

    fun startImport() = repository.startImport()
    fun cancelImport() = repository.cancelImport()
}

private fun ImportStatus.toUiState(): ImportUiState = when (this) {
    ImportStatus.Idle -> ImportUiState.Idle
    is ImportStatus.Interrupted -> ImportUiState.Interrupted(startedAtMs)
    is ImportStatus.Downloading -> ImportUiState.Downloading(progress)
    is ImportStatus.Parsing -> ImportUiState.Parsing(factionName, progress)
    is ImportStatus.Complete -> ImportUiState.Complete(factionCount, skippedCount)
    is ImportStatus.Error -> ImportUiState.Error(message)
}
