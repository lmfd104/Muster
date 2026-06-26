package com.lmfd.warboss.domain.model

data class GameResult(
    val id: String,
    val armyListId: String?,
    val myFactionName: String,
    val opponentFactionName: String,
    val playerScore: Int,
    val opponentScore: Int,
    val didWin: Boolean,
    val playedAt: Long,
)
