package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.BitstringBarChart
import com.example.ui.theme.AlphaGreen
import com.example.ui.theme.BenchmarkGold
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.RiskCrimson
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllocationTab(
    state: QFinUiState,
    onBudgetChange: (Int) -> Unit,
    onRiskAversionChange: (Double) -> Unit,
    onBrokerageFeeChange: (Double) -> Unit,
    onSlippageChange: (Double) -> Unit,
    onToggleHolding: (String) -> Unit,
    onResetHoldingsCash: () -> Unit,
    onOpenUniverseSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qaoa = state.qaoaResult
    val markowitz = state.markowitzResult
    val risk = state.riskResult
    val cost = qaoa?.costMetrics

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Summary Header Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("allocation_header_card"),
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
                            text = "QAOA COMBINATORIAL ALLOCATION",
                            style = MaterialTheme.typography.titleSmall,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Discrete Target: Select ${state.budgetB} of ${state.assets.count { it.isSelected }} Assets",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    IconButton(
                        onClick = onOpenUniverseSheet,
                        modifier = Modifier
                            .background(QuantumCyan.copy(alpha = 0.15f), CircleShape)
                            .size(36.dp)
                            .testTag("open_universe_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Tune Universe & Sentiment",
                            tint = QuantumCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Optimal QAOA Asset Basket Chips
                Text(
                    text = "OPTIMAL BASKET |${qaoa?.optimalBitstring ?: ""}⟩",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeTickers = qaoa?.selectedTickers ?: emptyList()
                    if (activeTickers.isEmpty()) {
                        Text("No assets selected in quantum solution", style = MaterialTheme.typography.bodySmall)
                    } else {
                        activeTickers.forEach { ticker ->
                            val asset = state.assets.find { it.ticker == ticker }
                            val effRet = asset?.effectiveExpectedReturn ?: 0.0
                            val sentiment = asset?.sentimentDrift ?: 0.0
                            val isHeld = state.initialHoldingTickers.contains(ticker)

                            Box(
                                modifier = Modifier
                                    .background(QuantumCyan.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .border(1.dp, QuantumCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ticker,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = QuantumCyan,
                                        fontSize = 13.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = String.format("%.1f%%", effRet * 100),
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                    if (isHeld) {
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "HELD",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = BenchmarkGold,
                                            modifier = Modifier
                                                .background(BenchmarkGold.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                                                .padding(horizontal = 3.dp, vertical = 1.dp)
                                        )
                                    }
                                    if (sentiment != 0.0) {
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = if (sentiment > 0) "+AI" else "-AI",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = if (sentiment > 0) AlphaGreen else RiskCrimson
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transaction Cost & Rebalancing Slippage Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("transaction_cost_card"),
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PriceChange, contentDescription = null, tint = BenchmarkGold, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TRANSACTION COSTS & SLIPPAGE FRICTIONS",
                                style = MaterialTheme.typography.titleSmall,
                                color = BenchmarkGold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Total Friction: ${String.format("%.1f", state.brokerageFeeBps + state.slippageBps)} bps (${String.format("%.2f%%", (state.brokerageFeeBps + state.slippageBps) / 100.0)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                    Button(
                        onClick = onResetHoldingsCash,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.initialHoldingTickers.isEmpty()) QuantumCyan.copy(alpha = 0.2f) else TerminalSurfaceVariant,
                            contentColor = if (state.initialHoldingTickers.isEmpty()) QuantumCyan else TextSecondary
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(28.dp).testTag("cash_toggle_btn")
                    ) {
                        Text(if (state.initialHoldingTickers.isEmpty()) "Cash Mode" else "Reset to Cash", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Current Holdings (Rebalance Source) Selection Chips
                Text(
                    text = "CURRENT HOLDINGS (Select held tickers to model rebalance turnover):",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.assets.filter { it.isSelected }.forEach { asset ->
                        val isHeld = state.initialHoldingTickers.contains(asset.ticker)
                        FilterChip(
                            selected = isHeld,
                            onClick = { onToggleHolding(asset.ticker) },
                            label = { Text(asset.ticker, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BenchmarkGold.copy(alpha = 0.2f),
                                selectedLabelColor = BenchmarkGold,
                                containerColor = Color(0xFF131D33),
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isHeld,
                                borderColor = if (isHeld) BenchmarkGold else TerminalBorder
                            ),
                            modifier = Modifier.testTag("holding_chip_${asset.ticker}")
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Brokerage Fee Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Brokerage Fee Rate",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${state.brokerageFeeBps.toInt()} bps (${String.format("%.2f%%", state.brokerageFeeBps / 100.0)})",
                        fontFamily = FontFamily.Monospace,
                        color = BenchmarkGold,
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = state.brokerageFeeBps.toFloat(),
                    onValueChange = { onBrokerageFeeChange(it.toDouble()) },
                    valueRange = 0f..50f,
                    colors = SliderDefaults.colors(
                        thumbColor = BenchmarkGold,
                        activeTrackColor = BenchmarkGold,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.fillMaxWidth().height(26.dp).testTag("brokerage_fee_slider")
                )

                Spacer(Modifier.height(6.dp))

                // Slippage Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Market Impact / Slippage",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${state.slippageBps.toInt()} bps (${String.format("%.2f%%", state.slippageBps / 100.0)})",
                        fontFamily = FontFamily.Monospace,
                        color = RiskCrimson,
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = state.slippageBps.toFloat(),
                    onValueChange = { onSlippageChange(it.toDouble()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = RiskCrimson,
                        activeTrackColor = RiskCrimson,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.fillMaxWidth().height(26.dp).testTag("slippage_slider")
                )

                Spacer(Modifier.height(10.dp))

                // Rebalancing Drag Breakdown Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                        .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rebalancing Turnover:", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            Text(
                                String.format("%.1f%%", (cost?.turnoverRate ?: 0.5) * 100),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Brokerage Drag:", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            Text(
                                String.format("-%.2f bps (-%.3f%%)", (cost?.brokerageFeeCost ?: 0.0) * 10000.0, (cost?.brokerageFeeCost ?: 0.0) * 100),
                                fontFamily = FontFamily.Monospace,
                                color = BenchmarkGold,
                                fontSize = 11.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Slippage Impact Drag:", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            Text(
                                String.format("-%.2f bps (-%.3f%%)", (cost?.slippageCost ?: 0.0) * 10000.0, (cost?.slippageCost ?: 0.0) * 100),
                                fontFamily = FontFamily.Monospace,
                                color = RiskCrimson,
                                fontSize = 11.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Net Rebalancing Friction:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(
                                String.format("-%.1f bps (-%.2f%%)", cost?.totalCostBps ?: 0.0, (cost?.totalCost ?: 0.0) * 100),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = RiskCrimson,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Side-by-side KPI Cards (QAOA Discrete vs Markowitz Classical) with Gross vs Net Metrics
        Text(
            text = "NET-OF-COST PERFORMANCE BENCHMARK",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // QAOA Discrete Card
            Card(
                modifier = Modifier.weight(1f).testTag("qaoa_kpi_card"),
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, QuantumCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QAOA (Discrete)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = QuantumCyan,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Net Model",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = AlphaGreen
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    KpiRow("Gross Return", String.format("%.1f%%", (cost?.grossExpectedReturn ?: (risk?.expectedReturnPeriod ?: 0.0)) * 100), TextSecondary)
                    KpiRow("Net Return", String.format("%.1f%%", (cost?.netExpectedReturn ?: (risk?.netExpectedReturnPeriod ?: 0.0)) * 100), AlphaGreen)
                    KpiRow("Volatility (σ)", String.format("%.1f%%", (markowitz?.volatility ?: 0.22) * 100), TextPrimary)
                    KpiRow("Gross Sharpe", String.format("%.2f", cost?.grossSharpe ?: (markowitz?.sharpeRatio ?: 1.0)), TextSecondary)
                    KpiRow("Net Sharpe", String.format("%.2f", cost?.netSharpe ?: 1.0), BenchmarkGold)
                    KpiRow("Net 99% CVaR", String.format("%.1f%%", (risk?.netCvar99 ?: 0.25) * 100), RiskCrimson)
                }
            }

            // Markowitz Continuous Card
            Card(
                modifier = Modifier.weight(1f).testTag("markowitz_kpi_card"),
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BenchmarkGold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                val mCost = markowitz?.costMetrics
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Markowitz (Cont.)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = BenchmarkGold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Simplex",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    KpiRow("Gross Return", String.format("%.1f%%", (mCost?.grossExpectedReturn ?: (markowitz?.expectedReturn ?: 0.0)) * 100), TextSecondary)
                    KpiRow("Net Return", String.format("%.1f%%", (mCost?.netExpectedReturn ?: 0.0) * 100), AlphaGreen)
                    KpiRow("Volatility (σ)", String.format("%.1f%%", (markowitz?.volatility ?: 0.0) * 100), TextPrimary)
                    KpiRow("Gross Sharpe", String.format("%.2f", markowitz?.sharpeRatio ?: 0.0), TextSecondary)
                    KpiRow("Net Sharpe", String.format("%.2f", mCost?.netSharpe ?: 0.0), BenchmarkGold)
                    KpiRow("Turnover", String.format("%.1f%%", (mCost?.turnoverRate ?: 0.0) * 100), TextMuted)
                }
            }
        }

        // Quick Optimization Controls (Budget B & Risk Aversion q)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Budget B Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Target Portfolio Budget (B Assets)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Hard constraint: ∑ x_i = B",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (state.budgetB > 1) onBudgetChange(state.budgetB - 1) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(TerminalSurfaceVariant, CircleShape)
                                .testTag("budget_decrease_btn")
                        ) {
                            Icon(Icons.Default.Remove, "Decrease Budget", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${state.budgetB}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = QuantumCyan,
                            fontSize = 16.sp
                        )
                        IconButton(
                            onClick = {
                                val maxB = state.assets.count { it.isSelected }
                                if (state.budgetB < maxB) onBudgetChange(state.budgetB + 1)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(TerminalSurfaceVariant, CircleShape)
                                .testTag("budget_increase_btn")
                        ) {
                            Icon(Icons.Default.Add, "Increase Budget", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Risk Aversion q Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Risk Aversion Factor (q)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("%.2f", state.riskAversionQ),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = BenchmarkGold,
                        fontSize = 14.sp
                    )
                }
                Slider(
                    value = state.riskAversionQ.toFloat(),
                    onValueChange = { onRiskAversionChange(it.toDouble()) },
                    valueRange = 0.1f..4.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = BenchmarkGold,
                        activeTrackColor = BenchmarkGold,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("risk_aversion_slider")
                )
            }
        }

        // Continuous Markowitz Weights Breakdown
        if (markowitz != null && markowitz.weights.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MARKOWITZ CONTINUOUS ALLOCATION WEIGHTS",
                        style = MaterialTheme.typography.titleSmall,
                        color = BenchmarkGold
                    )
                    Spacer(Modifier.height(10.dp))
                    markowitz.weights.entries.sortedByDescending { it.value }.forEach { (ticker, weight) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ticker,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(6.dp)
                                        .background(Color(0xFF1E293B), RoundedCornerShape(3.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(weight.toFloat().coerceIn(0f, 1f))
                                            .height(6.dp)
                                            .background(BenchmarkGold, RoundedCornerShape(3.dp))
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = String.format("%4.1f%%", weight * 100),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // QAOA Sampled Bitstring Distribution
        if (qaoa != null && qaoa.bitstringProbabilities.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QAOA GROUND STATE PROBABILITY DISTRIBUTION",
                        style = MaterialTheme.typography.titleSmall,
                        color = QuantumCyan
                    )
                    Text(
                        text = "p=${qaoa.circuitDepthP} reps",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                BitstringBarChart(samples = qaoa.bitstringProbabilities)
            }
        }
    }
}

@Composable
private fun KpiRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            fontSize = 12.sp
        )
    }
}
