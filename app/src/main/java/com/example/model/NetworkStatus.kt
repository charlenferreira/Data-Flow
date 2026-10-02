package com.example.model

data class NetworkStatus(
    val isConnected: Boolean = true,
    val connectionType: ConnectionType = ConnectionType.MOBILE_5G,
    val networkName: String = "Rede Móvel 5G",
    val signalStrengthDbm: Int = -82,
    val currentDownloadSpeedKbps: Float = 0f,
    val currentUploadSpeedKbps: Float = 0f,
    val isMetered: Boolean = true,
    val isVpnActive: Boolean = false
)

enum class ConnectionType(val displayName: String, val shortBadge: String) {
    MOBILE_5G("5G Ultra Rápido", "5G SA"),
    MOBILE_4G("4G LTE Avançado", "4G LTE"),
    MOBILE_3G("3G / HSPA+", "3G"),
    WIFI("Wi-Fi de Alta Velocidade", "Wi-Fi"),
    OFFLINE("Sem Conexão", "OFF")
}

data class SpeedTestLiveState(
    val isRunning: Boolean = false,
    val phase: SpeedTestPhase = SpeedTestPhase.IDLE,
    val currentMbps: Float = 0f,
    val pingMs: Int = 0,
    val jitterMs: Int = 0,
    val finalDownloadMbps: Float = 0f,
    val finalUploadMbps: Float = 0f,
    val testProgress: Float = 0f,
    val qualityVerdict: String = "Aguardando teste",
    val throttleRiskDetected: Boolean = false
)

enum class SpeedTestPhase(val label: String) {
    IDLE("Pronto para testar"),
    PING("Medindo Latência (Ping)..."),
    DOWNLOAD("Testando Velocidade de Download..."),
    UPLOAD("Testando Velocidade de Upload..."),
    FINISHED("Teste Concluído")
}
