package com.lmfd.warboss.data.ai

import com.lmfd.warboss.domain.model.ArmyListEntry
import com.lmfd.warboss.domain.model.GameResult
import com.lmfd.warboss.domain.model.ListAnalysis
import com.lmfd.warboss.domain.model.MatchupAnalysis
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiAnalysisService @Inject constructor(private val client: OkHttpClient) {

    companion object {
        private const val BASE_URL = "https://muster-rate-my-list.mjambogames.workers.dev"
        private const val RATE_ENDPOINT = "$BASE_URL/rate"
        private const val MATCHUP_ENDPOINT = "$BASE_URL/matchup"
    }

    fun analyzeList(
        factionName: String,
        totalPoints: Int,
        pointsLimit: Int,
        entries: List<ArmyListEntry>,
    ): ListAnalysis {
        val unitsArray = JSONArray().apply {
            entries.forEach { e ->
                put(JSONObject().apply {
                    put("name", e.unitName)
                    put("quantity", e.quantity)
                    put("unitPoints", e.unitPoints)
                })
            }
        }
        val requestBody = JSONObject().apply {
            put("factionName", factionName)
            put("totalPoints", totalPoints)
            put("pointsLimit", pointsLimit)
            put("units", unitsArray)
        }

        val request = Request.Builder()
            .url(RATE_ENDPOINT)
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("AI service returned HTTP ${response.code}")
        }

        val json = JSONObject(response.body!!.string())
        return ListAnalysis(
            rating = json.getInt("rating"),
            tier = json.getString("tier"),
            strengths = json.getJSONArray("strengths").toStringList(),
            weaknesses = json.getJSONArray("weaknesses").toStringList(),
            suggestions = json.getJSONArray("suggestions").toStringList(),
            caveat = json.optString("caveat", ""),
        )
    }

    fun analyzeMatchup(
        myFactionName: String,
        opponentFactionName: String,
        totalPoints: Int,
        pointsLimit: Int,
        entries: List<ArmyListEntry>,
        recentGames: List<GameResult>,
    ): MatchupAnalysis {
        val unitsArray = JSONArray().apply {
            entries.forEach { e ->
                put(JSONObject().apply {
                    put("name", e.unitName)
                    put("quantity", e.quantity)
                    put("unitPoints", e.unitPoints)
                })
            }
        }
        val historyArray = JSONArray().apply {
            recentGames.forEach { g ->
                put(JSONObject().apply {
                    put("opponentFaction", g.opponentFactionName)
                    put("playerScore", g.playerScore)
                    put("opponentScore", g.opponentScore)
                    put("didWin", g.didWin)
                })
            }
        }
        val requestBody = JSONObject().apply {
            put("myFactionName", myFactionName)
            put("opponentFactionName", opponentFactionName)
            put("totalPoints", totalPoints)
            put("pointsLimit", pointsLimit)
            put("units", unitsArray)
            put("recentGames", historyArray)
        }

        val request = Request.Builder()
            .url(MATCHUP_ENDPOINT)
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("AI service returned HTTP ${response.code}")
        }

        val json = JSONObject(response.body!!.string())
        return MatchupAnalysis(
            matchupRating = json.getInt("matchupRating"),
            summary = json.getString("summary"),
            threats = json.getJSONArray("threats").toStringList(),
            counterStrategies = json.getJSONArray("counterStrategies").toStringList(),
            unitRecommendations = json.getJSONArray("unitRecommendations").toStringList(),
            historySummary = json.optString("historySummary", ""),
        )
    }

    private fun JSONArray.toStringList(): List<String> = (0 until length()).map { getString(it) }
}
