package com.lmfd.warboss.ui.dataimport

sealed interface RosterImportState {
    data object Idle : RosterImportState
    data object Loading : RosterImportState
    data class Success(val listName: String, val unitCount: Int, val listId: String) : RosterImportState
    data class Error(val message: String) : RosterImportState
}
