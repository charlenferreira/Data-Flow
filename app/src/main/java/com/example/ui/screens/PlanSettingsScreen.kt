package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import com.example.BuildConfig
import com.example.model.OperatorPreset
import com.example.model.PlanSettings
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.NeonCyan

@Composable
fun PlanSettingsScreen(
    planSettings: PlanSettings,
    onUpdatePlanSettings: (PlanSettings) -> Unit,
    onSelectPreset: (OperatorPreset) -> Unit,
    onCheckUpdates: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Text(
            text = "Configuração do Plano",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Personalize o monitoramento para o seu contrato e operadora",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Operators
        Text(
            text = "Operadoras & Planos Pré-configurados",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(OperatorPreset.entries.toList()) { preset ->
                val isSelected = planSettings.carrierName == preset.title
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else CardDark
                    ),
                    modifier = Modifier
                        .width(200.dp)
                        .border(
                            1.dp,
                            if (isSelected) NeonCyan else CardBorderDark,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectPreset(preset) }
                        .testTag("preset_${preset.name.lowercase()}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onBackground
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.description,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FUP Limit Slider
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Franquia Alta Velocidade (FUP)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "${planSettings.highSpeedFupLimitGB.toInt()} GB",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Limite em que a operadora garante 5G pleno antes de reduzir para ~512Kbps.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = planSettings.highSpeedFupLimitGB,
                    onValueChange = { onUpdatePlanSettings(planSettings.copy(highSpeedFupLimitGB = it)) },
                    valueRange = 10f..300f,
                    steps = 28,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("fup_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hotspot Limit Slider
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = null,
                            tint = CyberIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cota de Roteador / Hotspot",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "${planSettings.hotspotLimitGB.toInt()} GB",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberIndigo
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A maioria dos planos ilimitados corta o roteamento após 10 a 20 GB.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = planSettings.hotspotLimitGB,
                    onValueChange = { onUpdatePlanSettings(planSettings.copy(hotspotLimitGB = it)) },
                    valueRange = 5f..50f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberIndigo,
                        activeTrackColor = CyberIndigo
                    ),
                    modifier = Modifier.testTag("hotspot_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Billing Cycle Reset Day
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dia de Fechamento da Fatura",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Dia ${planSettings.billingCycleResetDay}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "O contador de alta velocidade zera neste dia a cada mês.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = planSettings.billingCycleResetDay.toFloat(),
                    onValueChange = { onUpdatePlanSettings(planSettings.copy(billingCycleResetDay = it.toInt())) },
                    valueRange = 1f..31f,
                    steps = 29,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Alert Toggles
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Alertas Inteligentes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alerta aos 80% do Limite",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Avisa antes da redução de velocidade acontecer",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = planSettings.alertThresholdPercent == 80,
                        onCheckedChange = {
                            onUpdatePlanSettings(planSettings.copy(alertThresholdPercent = if (it) 80 else 95))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Proteção de Bateria 5G",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Detecta apps consumindo tráfego em segundo plano",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = planSettings.batterySaver5GMode,
                        onCheckedChange = {
                            onUpdatePlanSettings(planSettings.copy(batterySaver5GMode = it))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GitHub Repository & Update Checker Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Repositório & Atualizações",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "O Data Pulse verifica automaticamente novas versões publicadas no repositório GitHub oficial: charlenferreira/consumo-de-dados.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCheckUpdates,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("check_github_updates_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verificar Agora", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/charlenferreira/consumo-de-dados")
                                )
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Abrir GitHub", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FAQ Section Answering the User's exact prompt
        Text(
            text = "Dúvidas Frequentes: Internet Ilimitada",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        FaqCard(
            question = "Ele reduz a internet após bater o limite?",
            answer = "Quem reduz a velocidade é a sua OPERADORA através da Política de Uso Justo (FUP), e não este aplicativo. O app não corta sua conexão: ele audita seu tráfego, calcula sua meta diária de GB e alerta antes que sua operadora rebaixe sua velocidade de 300 Mbps para 512 Kbps."
        )

        Spacer(modifier = Modifier.height(8.dp))

        FaqCard(
            question = "Por que meu plano tem limite se é 'ilimitado'?",
            answer = "Operadoras utilizam a Política de Uso Justo (FUP - Fair Usage Policy). A navegação continua livre e sem cobrança por megabyte adicional, porém a velocidade é reduzida após a cota contratual de alta velocidade."
        )

        Spacer(modifier = Modifier.height(8.dp))

        FaqCard(
            question = "Como o 5G ilimitado afeta a bateria do meu celular?",
            answer = "O rádio 5G consome até 40% mais energia que o 4G ao manter transmissões ativas. Se apps como Instagram ou Google Fotos fazem uploads pesados em segundo plano, seu telefone descarrega rapidamente e esquenta."
        )

        Spacer(modifier = Modifier.height(8.dp))

        FaqCard(
            question = "Posso usar meu celular como roteador livremente?",
            answer = "Não necessariamente. Quase todas as operadoras separam os dados consumidos no próprio aparelho dos dados transmitidos via Roteador Wi-Fi (Tethering), bloqueando ou cobrando excedente quando atinge a cota de hotspot."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FaqCard(question: String, answer: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = question,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = answer,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
