package com.lmfd.warboss.domain.model

data class ArmyList(
    val id: String,
    val name: String,
    val factionId: String,
    val factionName: String,
    val pointsTotal: Int,
    val pointsLimit: Int,
    val unitCount: Int,
)
