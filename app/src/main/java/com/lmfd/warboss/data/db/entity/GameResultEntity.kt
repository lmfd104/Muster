package com.lmfd.warboss.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_result")
data class GameResultEntity(
    @PrimaryKey val id: String,
    val armyListId: String?,
    val myFactionName: String,
    val opponentFactionName: String,
    val playerScore: Int,
    val opponentScore: Int,
    val didWin: Boolean,
    val playedAt: Long,
)
