package com.example.model

data class PlanSettings(
    val carrierName: String = "Claro Ilimitado Total",
    val isUnlimited: Boolean = true,
    val highSpeedFupLimitGB: Float = 50f, // Fair usage policy limit in GB before throttling
    val hasFupLimit: Boolean = true,
    val hotspotLimitGB: Float = 15f, // Hotspot / tethering quota in GB
    val billingCycleResetDay: Int = 1, // Day of month when plan resets
    val alertThresholdPercent: Int = 80, // Warn at 80% of FUP
    val notifyHotspotLimit: Boolean = true,
    val batterySaver5GMode: Boolean = true // Warn if background data on 5G is excessive
)

enum class OperatorPreset(
    val title: String,
    val defaultFupGB: Float,
    val defaultHotspotGB: Float,
    val description: String
) {
    CLARO_UNLIMITED(
        title = "Claro Ilimitado Total",
        defaultFupGB = 50f,
        defaultHotspotGB = 10f,
        description = "5G Máximo até 50GB; após reduz para 512Kbps. 10GB de Roteador."
    ),
    VIVO_POS(
        title = "Vivo Pós Família/Individual",
        defaultFupGB = 60f,
        defaultHotspotGB = 15f,
        description = "Franquia principal de alta velocidade com bônus e hotspot controlado."
    ),
    TIM_BLACK(
        title = "TIM Black Ilimitado",
        defaultFupGB = 50f,
        defaultHotspotGB = 12f,
        description = "Apps ilimitados (Redes/Vídeo) e franquia livre com FUP de rede."
    ),
    GENERIC_UNLIMITED(
        title = "Ilimitado Sem Redução",
        defaultFupGB = 150f,
        defaultHotspotGB = 20f,
        description = "Plano empresarial ou especial com franquia estendida."
    ),
    CUSTOM(
        title = "Plano Personalizado",
        defaultFupGB = 50f,
        defaultHotspotGB = 15f,
        description = "Ajuste os valores exatos da sua fatura e contrato."
    )
}
