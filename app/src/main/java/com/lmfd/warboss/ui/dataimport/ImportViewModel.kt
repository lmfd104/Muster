package com.lmfd.warboss.ui.dataimport

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.data.bsdata.BsDataRepository
import com.lmfd.warboss.data.bsdata.ImportStatus
import com.lmfd.warboss.domain.usecase.ImportRosterUseCase
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
class ImportViewModel @Inject constructor(
    private val repository: BsDataRepository,
    private val importRoster: ImportRosterUseCase,
) : ViewModel() {

    val uiState: StateFlow<ImportUiState> = repository.status
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ImportUiState.Idle)

    private val _rosterState = MutableStateFlow<RosterImportState>(RosterImportState.Idle)
    val rosterState: StateFlow<RosterImportState> = _rosterState.asStateFlow()

    fun startImport() = repository.startImport()
    fun cancelImport() = repository.cancelImport()

    fun importRosterFile(uri: Uri) {
        viewModelScope.launch {
            _rosterState.value = RosterImportState.Loading
            _rosterState.value = try {
                val result = importRoster(uri)
                RosterImportState.Success(result.listName, result.unitCount, result.listId)
            } catch (e: Exception) {
                RosterImportState.Error(e.message ?: "Failed to import roster")
            }
        }
    }

    fun dismissRosterResult() { _rosterState.value = RosterImportState.Idle }
}

private fun ImportStatus.toUiState(): ImportUiState = when (this) {
    ImportStatus.Idle -> ImportUiState.Idle
    is ImportStatus.Interrupted -> ImportUiState.Interrupted(startedAtMs)
    is ImportStatus.Downloading -> ImportUiState.Downloading(progress)
    is ImportStatus.Parsing -> ImportUiState.Parsing(factionName, progress)
    is ImportStatus.Complete -> ImportUiState.Complete(factionCount, skippedCount)
    is ImportStatus.Error -> ImportUiState.Error(message)
}
