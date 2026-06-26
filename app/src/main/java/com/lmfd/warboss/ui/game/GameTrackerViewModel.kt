package com.lmfd.warboss.ui.game

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.domain.usecase.SaveGameResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
data class RoundScore(
    val cpGained: Int = 0,
    val cpSpent: Int = 0,
    val primaryPts: Int = 0,
    val secondaryPts: Int = 0,
    val extraPts: Int = 0,
) : Parcelable

@Parcelize
data class GameTrackerState(
    val playerRounds: List<RoundScore> = List(5) { RoundScore() },
    val opponentRounds: List<RoundScore> = List(5) { RoundScore() },
    val currentRound: Int = 0,
    val myFactionName: String = "",
    val opponentFactionName: String = "",
) : Parcelable {
    val playerTotal: Int get() = playerRounds.sumOf { it.primaryPts + it.secondaryPts + it.extraPts }
    val opponentTotal: Int get() = opponentRounds.sumOf { it.primaryPts + it.secondaryPts + it.extraPts }
    val playerPrimaryTotal: Int get() = playerRounds.sumOf { it.primaryPts }
    val playerSecondaryTotal: Int get() = playerRounds.sumOf { it.secondaryPts }
    val opponentPrimaryTotal: Int get() = opponentRounds.sumOf { it.primaryPts }
    val opponentSecondaryTotal: Int get() = opponentRounds.sumOf { it.secondaryPts }
    val playerCpNet: Int get() = playerRounds.sumOf { it.cpGained } - playerRounds.sumOf { it.cpSpent }
    val opponentCpNet: Int get() = opponentRounds.sumOf { it.cpGained } - opponentRounds.sumOf { it.cpSpent }
}

sealed interface SaveGameState {
    data object Idle : SaveGameState
    data object Saving : SaveGameState
    data object Saved : SaveGameState
    data class Error(val message: String) : SaveGameState
}

@HiltViewModel
class GameTrackerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val saveGameResult: SaveGameResultUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        savedStateHandle.get<GameTrackerState>("game_state") ?: GameTrackerState()
    )
    val state: StateFlow<GameTrackerState> = _state.asStateFlow()

    private val _saveState = MutableStateFlow<SaveGameState>(SaveGameState.Idle)
    val saveState: StateFlow<SaveGameState> = _saveState.asStateFlow()

    init {
        viewModelScope.launch {
            _state.collect { savedStateHandle["game_state"] = it }
        }
    }

    fun setRound(round: Int) = _state.update { it.copy(currentRound = round.coerceIn(0, 4)) }

    fun resetGame() = _state.update { GameTrackerState() }

    fun setMyFaction(name: String) = _state.update { it.copy(myFactionName = name) }
    fun setOpponentFaction(name: String) = _state.update { it.copy(opponentFactionName = name) }

    fun saveGame(armyListId: String? = null) {
        val s = _state.value
        _saveState.value = SaveGameState.Saving
        viewModelScope.launch {
            try {
                saveGameResult(
                    armyListId = armyListId,
                    myFactionName = s.myFactionName.ifBlank { "Unknown" },
                    opponentFactionName = s.opponentFactionName.ifBlank { "Unknown" },
                    playerScore = s.playerTotal,
                    opponentScore = s.opponentTotal,
                )
                _saveState.value = SaveGameState.Saved
            } catch (e: Exception) {
                _saveState.value = SaveGameState.Error(e.message ?: "Failed to save")
            }
        }
    }

    fun dismissSaveResult() { _saveState.value = SaveGameState.Idle }

    fun adjustPlayerCpGained(delta: Int) = updatePlayer { r -> r.copy(cpGained = maxOf(0, r.cpGained + delta)) }
    fun adjustPlayerCpSpent(delta: Int) = updatePlayer { r -> r.copy(cpSpent = maxOf(0, r.cpSpent + delta)) }
    fun adjustPlayerPrimary(delta: Int) = updatePlayer { r -> r.copy(primaryPts = maxOf(0, r.primaryPts + delta)) }
    fun adjustPlayerSecondary(delta: Int) = updatePlayer { r -> r.copy(secondaryPts = maxOf(0, r.secondaryPts + delta)) }
    fun adjustPlayerExtra(delta: Int) = updatePlayer { r -> r.copy(extraPts = maxOf(0, r.extraPts + delta)) }

    fun adjustOpponentCpGained(delta: Int) = updateOpponent { r -> r.copy(cpGained = maxOf(0, r.cpGained + delta)) }
    fun adjustOpponentCpSpent(delta: Int) = updateOpponent { r -> r.copy(cpSpent = maxOf(0, r.cpSpent + delta)) }
    fun adjustOpponentPrimary(delta: Int) = updateOpponent { r -> r.copy(primaryPts = maxOf(0, r.primaryPts + delta)) }
    fun adjustOpponentSecondary(delta: Int) = updateOpponent { r -> r.copy(secondaryPts = maxOf(0, r.secondaryPts + delta)) }
    fun adjustOpponentExtra(delta: Int) = updateOpponent { r -> r.copy(extraPts = maxOf(0, r.extraPts + delta)) }

    private fun updatePlayer(transform: (RoundScore) -> RoundScore) = _state.update { s ->
        val rounds = s.playerRounds.toMutableList()
        rounds[s.currentRound] = transform(rounds[s.currentRound])
        s.copy(playerRounds = rounds)
    }

    private fun updateOpponent(transform: (RoundScore) -> RoundScore) = _state.update { s ->
        val rounds = s.opponentRounds.toMutableList()
        rounds[s.currentRound] = transform(rounds[s.currentRound])
        s.copy(opponentRounds = rounds)
    }
}
