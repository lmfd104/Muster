package com.lmfd.warboss.domain.model

data class MatchupAnalysis(
    val matchupRating: Int,
    val summary: String,
    val threats: List<String>,
    val counterStrategies: List<String>,
    val unitRecommendations: List<String>,
    val historySummary: String,
)
