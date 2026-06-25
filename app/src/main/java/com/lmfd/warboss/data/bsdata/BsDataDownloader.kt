package com.lmfd.warboss.data.bsdata

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BsDataDownloader @Inject constructor(
    private val client: OkHttpClient,
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val REPO_API_URL = "https://api.github.com/repos/BSData/wh40k-10e"
        private const val ARCHIVE_URL_TEMPLATE = "https://github.com/BSData/wh40k-10e/archive/refs/heads/%s.zip"
        private const val DEFAULT_BRANCH = "main"
        private const val DEST_FILENAME = "bsdata_import.zip"
    }

    /**
     * Downloads the BSData wh40k-10e archive to cacheDir.
     * @param onProgress callback with (bytesRead, totalBytes); totalBytes=-1 if unknown.
     * @return the downloaded File (in cacheDir).
     */
    fun download(onProgress: (bytesRead: Long, totalBytes: Long) -> Unit): File {
        val branch = resolveDefaultBranch()
        val url = ARCHIVE_URL_TEMPLATE.format(branch)

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/zip")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw ImportException("Download failed: HTTP ${response.code} from $url")
        }

        val body = response.body ?: throw ImportException("Empty response body from $url")
        val total = body.contentLength()
        val destFile = File(context.cacheDir, DEST_FILENAME)

        body.byteStream().buffered().use { input ->
            destFile.outputStream().buffered().use { output ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead = 0L
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    bytesRead += read
                    onProgress(bytesRead, total)
                }
            }
        }

        return destFile
    }

    private fun resolveDefaultBranch(): String {
        return try {
            val request = Request.Builder()
                .url(REPO_API_URL)
                .header("Accept", "application/vnd.github+json")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return DEFAULT_BRANCH
            val body = response.body?.string() ?: return DEFAULT_BRANCH
            // Parse "default_branch" without a full JSON library
            val match = Regex(""""default_branch"\s*:\s*"([^"]+)"""").find(body)
            match?.groupValues?.getOrNull(1) ?: DEFAULT_BRANCH
        } catch (_: Exception) {
            DEFAULT_BRANCH
        }
    }
}

class ImportException(message: String, cause: Throwable? = null) : Exception(message, cause)
