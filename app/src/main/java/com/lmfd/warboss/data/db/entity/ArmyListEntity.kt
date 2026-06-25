package com.lmfd.warboss.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "army_list")
data class ArmyListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val factionId: String,
    val factionName: String,
    val pointsLimit: Int,
    val createdAt: Long,
)
