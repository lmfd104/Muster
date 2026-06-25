package com.lmfd.warboss.domain.model

data class UnitProfile(
    val id: String,
    val name: String,
    val typeName: String,
    val characteristics: Map<String, String>,
)
