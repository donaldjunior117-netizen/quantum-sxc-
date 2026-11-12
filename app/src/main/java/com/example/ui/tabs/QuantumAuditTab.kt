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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.components.ConvergenceChart
import com.example.ui.components.QaeScalingChart
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
fun QuantumAuditTab(
    state: QFinUiState,
    onCircuitDepthChange: (Int) -> Unit,
    onQaeEpsilonChange: (Double) -> Unit,
    onOpenPythonExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qaoa = state.qaoaResult
    val qae = state.qaeResult

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: QAOA Energy Convergence & Approximation Ratio
        Card(
            modifier = Modifier.fillMaxWidth().testTag("qaoa_audit_header_card"),
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, QuantumCyan.copy(alpha = 0.4f)),
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
                            text = "QAOA GROUND STATE SOLVER AUDIT",
                            style = MaterialTheme.typography.titleSmall,
                            color = QuantumCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Circuit Depth: p=${state.circuitDepthP} • Execution Time: ${qaoa?.executionTimeMs ?: 0}ms",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    // Approximation Ratio Badge
                    val ratio = qaoa?.approximationRatio ?: 1.0
                    val badgeColor = if (ratio >= 0.90) AlphaGreen else if (ratio >= 0.75) BenchmarkGold else RiskCrimson
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "APPROX RATIO",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                            Text(
                                text = String.format("%.1f%%", ratio * 100),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Depth p chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Depth (reps p):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    (1..4).forEach { p ->
                        FilterChip(
                            selected = state.circuitDepthP == p,
                            onClick = { onCircuitDepthChange(p) },
                            label = { Text("p=$p", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = QuantumCyan.copy(alpha = 0.2f),
                                selectedLabelColor = QuantumCyan,
                                containerColor = Color(0xFF131D33),
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = state.circuitDepthP == p,
                                borderColor = if (state.circuitDepthP == p) QuantumCyan else TerminalBorder
                            ),
                            modifier = Modifier.testTag("circuit_depth_chip_$p")
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Energy Comparison Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnergyTile(
                        label = "QAOA Energy E",
                        value = String.format("%.4f", qaoa?.groundStateEnergy ?: 0.0),
                        subtext = "State |${qaoa?.optimalBitstring ?: ""}⟩",
                        color = QuantumCyan,
                        modifier = Modifier.weight(1f)
                    )
                    EnergyTile(
                        label = "Exact Minimum E",
                        value = String.format("%.4f", qaoa?.exactMinimumEnergy ?: 0.0),
                        subtext = "State |${qaoa?.exactOptimalBitstring ?: ""}⟩",
                        color = BenchmarkGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Convergence Trace Chart
        if (qaoa != null && qaoa.convergenceTrace.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ENERGY MINIMIZATION TRACE VS ITERATION",
                    style = MaterialTheme.typography.titleSmall,
                    color = QuantumCyan
                )
                ConvergenceChart(
                    trace = qaoa.convergenceTrace,
                    exactGroundEnergy = qaoa.exactMinimumEnergy
                )
            }
        }

        // Section 2: Iterative Quantum Amplitude Estimation (IQAE) Query Complexity
        Card(
            modifier = Modifier.fillMaxWidth().testTag("qae_benchmark_card"),
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BenchmarkGold.copy(alpha = 0.4f)),
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
                            text = "QUANTUM AMPLITUDE ESTIMATION (QAE)",
                            style = MaterialTheme.typography.titleSmall,
                            color = BenchmarkGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Quadratic Query Speedup: O(1/ε) vs O(1/ε²)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speedup",
                        tint = BenchmarkGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Target Precision Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target VaR Precision (ε)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("%.3f (%.1f%%)", state.targetQaeEpsilon, state.targetQaeEpsilon * 100),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = BenchmarkGold,
                        fontSize = 14.sp
                    )
                }

                Slider(
                    value = state.targetQaeEpsilon.toFloat(),
                    onValueChange = { onQaeEpsilonChange(it.toDouble()) },
                    valueRange = 0.001f..0.02f,
                    colors = SliderDefaults.colors(
                        thumbColor = BenchmarkGold,
                        activeTrackColor = BenchmarkGold,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("qae_epsilon_slider")
                )

                Spacer(Modifier.height(8.dp))

                // Query Comparison KPI Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnergyTile(
                        label = "Classical MC (Samples)",
                        value = "${qae?.currentClassicalQueries ?: 0}",
                        subtext = "O(1/ε²) Complexity",
                        color = BenchmarkGold,
                        modifier = Modifier.weight(1f)
                    )
                    EnergyTile(
                        label = "Quantum QAE (Queries)",
                        value = "${qae?.currentQuantumQueries ?: 0}",
                        subtext = "O(1/ε) Quadratic",
                        color = QuantumCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Speedup Multiplier Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AlphaGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, AlphaGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUANTUM ORACLE ADVANTAGE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = AlphaGreen
                        )
                        Text(
                            text = "${String.format("%.1f", qae?.speedupRatio ?: 1.0)}x Speedup",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AlphaGreen
                        )
                    }
                }
            }
        }

        // QAE Scaling Chart
        if (qae != null && qae.points.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "LOG-SCALE SCALING: MONTE CARLO VS QAE",
                    style = MaterialTheme.typography.titleSmall,
                    color = BenchmarkGold
                )
                QaeScalingChart(points = qae.points)
            }
        }

        // Python & Qiskit Script Export Button
        Button(
            onClick = onOpenPythonExport,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("export_python_script_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = QuantumCyan,
                contentColor = Color(0xFF00363C)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "EXPORT PYTHON & QISKIT 1.x CODE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun EnergyTile(
    label: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF131D33), RoundedCornerShape(8.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = color)
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(1.dp))
            Text(text = subtext, style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = TextMuted)
        }
    }
}
