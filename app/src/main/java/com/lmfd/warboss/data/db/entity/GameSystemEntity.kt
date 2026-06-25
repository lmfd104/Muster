package com.lmfd.warboss.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_system")
data class GameSystemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val revision: Int,
    val bsVersion: String,
)
