package com.example.model

enum class BatteryPluggedSource(val label: String) {
    NONE("Bateria (Descarregando)"),
    AC("Carregador de Tomada (Turbo/PD)"),
    USB("Cabo USB (Computador/Padrão)"),
    WIRELESS("Carregamento por Indução (Qi)")
}

enum class BatteryHealthStatus(val label: String, val isWarning: Boolean) {
    GOOD("Excelente (Saudável)", false),
    OVERHEAT("Superaquecimento!", true),
    DEAD("Bateria Danificada", true),
    OVER_VOLTAGE("Sobretensão Detectada", true),
    COLD("Temperatura Muito Baixa", true),
    UNSPECIFIED_FAILURE("Falha Não Especificada", true),
    UNKNOWN("Desconhecido", false)
}

data class BatteryTelemetry(
    val levelPercent: Int = 85,
    val isCharging: Boolean = false,
    val pluggedSource: BatteryPluggedSource = BatteryPluggedSource.NONE,
    val currentNowMa: Long = -320L, // negative = discharging, positive = charging
    val voltageMv: Int = 4120, // in mV
    val temperatureCelsius: Float = 31.4f,
    val health: BatteryHealthStatus = BatteryHealthStatus.GOOD,
    val technology: String = "Li-poly",
    val designCapacityMah: Long = 5000L,
    val currentCapacityMah: Long = 4250L,
    val chargeRatePercentPerHour: Float = 24.5f,
    val estimatedMinutesRemaining: Int = 450, // e.g. 7.5 hours left
    val chargeAlarmEnabled: Boolean = true,
    val chargeAlarmThreshold: Int = 80 // AccuBattery recommendation
) {
    val voltageVolts: Float get() = voltageMv / 1000f
    val isCurrentCharging: Boolean get() = currentNowMa > 0
    val isTemperatureHigh: Boolean get() = temperatureCelsius >= 40.0f
    val powerWatts: Float get() = (kotlin.math.abs(currentNowMa) * voltageVolts) / 1000f
}
