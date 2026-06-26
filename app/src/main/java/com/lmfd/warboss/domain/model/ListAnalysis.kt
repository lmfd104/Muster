package com.lmfd.warboss.domain.model

data class ListAnalysis(
    val rating: Int,
    val tier: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val suggestions: List<String>,
    val caveat: String,
)
