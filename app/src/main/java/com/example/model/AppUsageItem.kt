package com.example.model

data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val totalBytes: Long,
    val foregroundBytes: Long,
    val backgroundBytes: Long,
    val wifiBytes: Long,
    val category: AppCategory = AppCategory.OTHER,
    val isBatteryDrainRisk: Boolean = false
) {
    val totalFormatted: String
        get() = formatBytes(totalBytes)

    val backgroundFormatted: String
        get() = formatBytes(backgroundBytes)

    val wifiFormatted: String
        get() = formatBytes(wifiBytes)

    val backgroundPercentage: Float
        get() = if (totalBytes > 0) (backgroundBytes.toFloat() / totalBytes) * 100f else 0f
}

enum class AppCategory(val label: String) {
    STREAMING("Streaming e Vídeo"),
    SOCIAL("Redes Sociais"),
    GAMING("Jogos Online"),
    BROWSING("Navegação"),
    DOWNLOADS("Downloads & Nuvem"),
    SYSTEM("Sistema & Serviços"),
    OTHER("Outros Aplicativos")
}

fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(java.util.Locale.US, "%.2f GB", gb)
        mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB", mb)
        kb >= 1.0 -> String.format(java.util.Locale.US, "%.0f KB", kb)
        else -> "$bytes B"
    }
}
