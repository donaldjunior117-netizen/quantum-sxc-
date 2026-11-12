package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.QFinUiState
import com.example.ui.components.FanChart
import com.example.ui.components.LossHistogramChart
import com.example.ui.theme.AlphaGreen
import com.example.ui.theme.BenchmarkGold
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.RiskCrimson
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RiskTab(
    state: QFinUiState,
    onHorizonChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val risk = state.riskResult

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
        Card(
            modifier = Modifier.fillMaxWidth().testTag("risk_header_card"),
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FAT-TAIL STUDENT-T STRESS ENGINE",
                            style = MaterialTheme.typography.titleSmall,
                            color = RiskCrimson,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${state.simulationPaths} Correlated Cholesky Paths • Horizon: ${state.forecastHorizonDays} Days",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(RiskCrimson.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(1.dp, RiskCrimson.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ν = ${String.format("%.1f", risk?.degreesOfFreedomNu ?: 4.5)} df",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RiskCrimson,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // VaR & CVaR KPI Matrix (Net of Transaction Cost Drag)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RiskCard(
                title = "95% VaR (Net)",
                value = String.format("%.2f%%", (risk?.netVar95 ?: 0.0) * 100),
                subtext = "Gross: ${String.format("%.2f%%", (risk?.var95 ?: 0.0) * 100)}",
                color = BenchmarkGold,
                modifier = Modifier.weight(1f)
            )
            RiskCard(
                title = "99% VaR (Net)",
                value = String.format("%.2f%%", (risk?.netVar99 ?: 0.0) * 100),
                subtext = "Gross: ${String.format("%.2f%%", (risk?.var99 ?: 0.0) * 100)}",
                color = RiskCrimson,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RiskCard(
                title = "95% CVaR (Net)",
                value = String.format("%.2f%%", (risk?.netCvar95 ?: 0.0) * 100),
                subtext = "Friction drag: ${String.format("+%.1f bps", (risk?.transactionCostDrag ?: 0.0) * 10000.0)}",
                color = BenchmarkGold,
                modifier = Modifier.weight(1f)
            )
            RiskCard(
                title = "99% CVaR (Net)",
                value = String.format("%.2f%%", (risk?.netCvar99 ?: 0.0) * 100),
                subtext = "Worst 1% net loss mean",
                color = RiskCrimson,
                modifier = Modifier.weight(1f)
            )
        }

        // Horizon Adjustment Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Forecast Horizon Window",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${state.forecastHorizonDays} Trading Days",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = QuantumCyan,
                        fontSize = 14.sp
                    )
                }
                Slider(
                    value = state.forecastHorizonDays.toFloat(),
                    onValueChange = { onHorizonChange(it.toInt()) },
                    valueRange = 15f..252f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = QuantumCyan,
                        activeTrackColor = QuantumCyan,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("horizon_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("15d (Bi-weekly)", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                    Text("90d (Quarterly)", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                    Text("252d (Annual)", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                }
            }
        }

        // Fan Chart (Trajectory Simulation)
        if (risk != null && risk.trajectoryPaths.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SIMULATED TRAJECTORY FAN (CORRELATED T-PATHS)",
                        style = MaterialTheme.typography.titleSmall,
                        color = QuantumCyan
                    )
                    Text(
                        text = "Base=100.0",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                FanChart(trajectories = risk.trajectoryPaths)
            }
        }

        // Loss Histogram with VaR markers
        if (risk != null && risk.empiricalLossHistBins.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "EMPIRICAL LOSS DISTRIBUTION & TAIL-RISK CUTOFFS",
                    style = MaterialTheme.typography.titleSmall,
                    color = RiskCrimson
                )
                LossHistogramChart(
                    bins = risk.empiricalLossHistBins,
                    var95 = risk.var95,
                    var99 = risk.var99,
                    cvar99 = risk.cvar99
                )
            }
        }

        // Quant Leptokurtosis Advisory Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "QUANT NOTE: WHY STUDENT-T INSTEAD OF GAUSSIAN?",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Standard Gaussian models severely underestimate crash probability because normal distributions decay exponentially (e^(-x^2)). The Student-t distribution features polynomial heavy tails with finite degrees of freedom (ν=4.5), capturing simultaneous market shocks, volatility clustering, and systemic liquidity dry-ups.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun RiskCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = color
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 9.sp,
                color = TextMuted
            )
        }
    }
}
