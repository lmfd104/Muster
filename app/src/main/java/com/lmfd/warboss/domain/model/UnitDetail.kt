package com.lmfd.warboss.domain.model

data class UnitDetail(
    val summary: UnitSummary,
    val profiles: List<UnitProfile>,
    val keywords: List<String>,
    val factionKeywords: List<String>,
    val categoryLinks: List<CategoryLink>,
)

data class CategoryLink(
    val categoryId: String,
    val categoryName: String,
    val isPrimary: Boolean,
)
