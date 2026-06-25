package com.lmfd.warboss.domain.model

data class ArmyListEntry(
    val id: String,
    val armyListId: String,
    val unitId: String,
    val unitName: String,
    val unitPoints: Int,
    val quantity: Int,
)
