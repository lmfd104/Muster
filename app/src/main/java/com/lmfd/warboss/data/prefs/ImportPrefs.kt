package com.lmfd.warboss.data.prefs

import android.content.SharedPreferences
import com.lmfd.warboss.data.bsdata.ImportStatus

class ImportPrefs(private val prefs: SharedPreferences) {

    fun initialStatus(): ImportStatus {
        val complete = prefs.getBoolean(KEY_COMPLETE, false)
        if (complete) return ImportStatus.Complete(
            factionCount = prefs.getInt(KEY_FACTION_COUNT, 0),
            skippedCount = prefs.getInt(KEY_SKIPPED_COUNT, 0),
        )
        val startedAt = prefs.getLong(KEY_STARTED_AT_MS, -1L)
        return if (startedAt >= 0) ImportStatus.Interrupted(startedAt) else ImportStatus.Idle
    }

    fun markImportStarted() {
        prefs.edit()
            .putBoolean(KEY_COMPLETE, false)
            .putLong(KEY_STARTED_AT_MS, System.currentTimeMillis())
            .apply()
    }

    fun markImportComplete(factionCount: Int, skippedCount: Int) {
        prefs.edit()
            .putBoolean(KEY_COMPLETE, true)
            .putInt(KEY_FACTION_COUNT, factionCount)
            .putInt(KEY_SKIPPED_COUNT, skippedCount)
            .apply()
    }

    fun markImportFailed() {
        prefs.edit()
            .putBoolean(KEY_COMPLETE, false)
            .apply()
    }

    fun reset() {
        prefs.edit()
            .remove(KEY_COMPLETE)
            .remove(KEY_STARTED_AT_MS)
            .remove(KEY_FACTION_COUNT)
            .remove(KEY_SKIPPED_COUNT)
            .apply()
    }

    companion object {
        private const val KEY_COMPLETE = "import_complete"
        private const val KEY_STARTED_AT_MS = "import_started_at_ms"
        private const val KEY_FACTION_COUNT = "import_faction_count"
        private const val KEY_SKIPPED_COUNT = "import_skipped_count"
    }
}
