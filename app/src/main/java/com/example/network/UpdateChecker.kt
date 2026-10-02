package com.example.network

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AppReleaseInfo(
    val tagName: String,
    val title: String,
    val changelog: String,
    val releasePageUrl: String,
    val directApkDownloadUrl: String?,
    val isNewerVersion: Boolean
)

class UpdateChecker {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val releaseApiUrl =
        "https://api.github.com/repos/charlenferreira/consumo-de-dados/releases/latest"

    suspend fun checkForUpdates(): Result<AppReleaseInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(releaseApiUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "DataPulse-Android-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Código HTTP ${response.code}: Não foi possível consultar o GitHub Releases")
                    )
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Resposta vazia da API do GitHub"))

                val json = JSONObject(responseBody)
                val tagName = json.optString("tag_name", "").trim()
                val title = json.optString("name", tagName)
                val changelog = json.optString("body", "Melhorias de desempenho e correções.")
                val releasePageUrl = json.optString(
                    "html_url",
                    "https://github.com/charlenferreira/consumo-de-dados/releases"
                )

                // Search for direct .apk asset
                var apkUrl: String? = null
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        val downloadUrl = asset.optString("browser_download_url", "")
                        if (name.endsWith(".apk", ignoreCase = true) && downloadUrl.isNotBlank()) {
                            apkUrl = downloadUrl
                            break
                        }
                    }
                }

                val currentVersion = BuildConfig.VERSION_NAME
                val isNewer = isRemoteVersionNewer(remoteTag = tagName, currentVersion = currentVersion)

                Result.success(
                    AppReleaseInfo(
                        tagName = tagName,
                        title = title,
                        changelog = changelog,
                        releasePageUrl = releasePageUrl,
                        directApkDownloadUrl = apkUrl ?: releasePageUrl,
                        isNewerVersion = isNewer
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isRemoteVersionNewer(remoteTag: String, currentVersion: String): Boolean {
        val cleanRemote = remoteTag.removePrefix("v").removePrefix("V").trim()
        val cleanCurrent = currentVersion.removePrefix("v").removePrefix("V").trim()

        if (cleanRemote.isBlank()) return false
        if (cleanRemote == cleanCurrent) return false

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLength) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }

        // If numeric parts are identical, fall back to string inequality
        return cleanRemote != cleanCurrent
    }
}
