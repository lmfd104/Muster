package com.lmfd.warboss.data.bsdata

sealed class ImportStatus {
    /** No import has ever been started */
    object Idle : ImportStatus()

    /** A prior import started but the process was killed before completion */
    data class Interrupted(val startedAtMs: Long) : ImportStatus()

    data class Downloading(val bytesRead: Long, val totalBytes: Long) : ImportStatus() {
        val progress: Float get() = if (totalBytes > 0) bytesRead.toFloat() / totalBytes else -1f
    }

    data class Parsing(val factionName: String, val index: Int, val total: Int) : ImportStatus() {
        val progress: Float get() = if (total > 0) index.toFloat() / total else 0f
    }

    data class Complete(val factionCount: Int, val skippedCount: Int) : ImportStatus()

    data class Error(val message: String, val cause: Throwable? = null) : ImportStatus()
}
