package com.lmfd.warboss.domain.model

data class UnitSummary(
    val id: String,
    val factionId: String,
    val name: String,
    val type: String,
    val points: Int,
    val minQuantity: Int,
    val maxQuantity: Int,
    val hasUnresolvableLinks: Boolean,
)
