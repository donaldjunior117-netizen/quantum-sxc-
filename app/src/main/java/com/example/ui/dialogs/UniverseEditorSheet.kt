package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AssetSpec
import com.example.ui.theme.AlphaGreen
import com.example.ui.theme.BenchmarkGold
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.RiskCrimson
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun UniverseEditorSheet(
    assets: List<AssetSpec>,
    onToggleSelect: (String) -> Unit,
    onSentimentChange: (String, Double) -> Unit,
    onAddAsset: (String, String, String, Double, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("universe_editor_sheet"),
            shape = RoundedCornerShape(16.dp),
            color = TerminalBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "UNIVERSE & AI SENTIMENT DRIFT",
                            style = MaterialTheme.typography.titleMedium,
                            color = QuantumCyan,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Adjust macro/news drift Δμ ∈ [-5%, +5%] per ticker",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_universe_sheet_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${assets.count { it.isSelected }} of ${assets.size} Assets Active",
                        fontFamily = FontFamily.Monospace,
                        color = BenchmarkGold,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = QuantumCyan.copy(alpha = 0.2f), contentColor = QuantumCyan),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp).testTag("add_custom_ticker_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Ticker", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Asset List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(assets, key = { it.ticker }) { asset ->
                        AssetEditorItem(
                            asset = asset,
                            onToggle = { onToggleSelect(asset.ticker) },
                            onDriftChange = { onSentimentChange(asset.ticker, it) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("apply_universe_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = QuantumCyan, contentColor = Color(0xFF00363C)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("APPLY & RECALCULATE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAddDialog) {
        AddAssetDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { ticker, name, sector, ret, vol, nu ->
                onAddAsset(ticker, name, sector, ret, vol, nu)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AssetEditorItem(
    asset: AssetSpec,
    onToggle: () -> Unit,
    onDriftChange: (Double) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurface, RoundedCornerShape(8.dp))
            .border(1.dp, if (asset.isSelected) QuantumCyan.copy(alpha = 0.4f) else TerminalBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = asset.isSelected,
                        onCheckedChange = { onToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = QuantumCyan,
                            uncheckedColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = asset.ticker,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (asset.isSelected) QuantumCyan else TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${asset.name} • ${asset.sector}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("μ=%.1f%%  σ=%.1f%%", asset.effectiveExpectedReturn * 100, asset.annualVolatility * 100),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "ν=${String.format("%.1f", asset.studentTDegreesOfFreedom)} df",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = BenchmarkGold
                    )
                }
            }

            if (asset.isSelected) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Sentiment Overlay (Δμ):",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = String.format("%+.1f%%", asset.sentimentDrift * 100),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (asset.sentimentDrift > 0) AlphaGreen else if (asset.sentimentDrift < 0) RiskCrimson else TextMuted,
                        fontSize = 11.sp
                    )
                }
                Slider(
                    value = asset.sentimentDrift.toFloat(),
                    onValueChange = { onDriftChange(it.toDouble()) },
                    valueRange = -0.05f..0.05f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (asset.sentimentDrift >= 0) AlphaGreen else RiskCrimson,
                        activeTrackColor = if (asset.sentimentDrift >= 0) AlphaGreen else RiskCrimson,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.fillMaxWidth().height(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AddAssetDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Double, Double, Double) -> Unit
) {
    var ticker by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var sector by remember { mutableStateOf("Equities") }
    var retStr by remember { mutableStateOf("20") }
    var volStr by remember { mutableStateOf("25") }
    var nuStr by remember { mutableStateOf("4.5") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().testTag("add_asset_dialog"),
            shape = RoundedCornerShape(12.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, QuantumCyan.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ADD CANDIDATE TICKER", style = MaterialTheme.typography.titleMedium, color = QuantumCyan)

                OutlinedTextField(
                    value = ticker,
                    onValueChange = { ticker = it.uppercase() },
                    label = { Text("Ticker Symbol (e.g. META)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = QuantumCyan,
                        unfocusedBorderColor = TerminalBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_ticker")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = QuantumCyan,
                        unfocusedBorderColor = TerminalBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = retStr,
                        onValueChange = { retStr = it },
                        label = { Text("Exp Return %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = volStr,
                        onValueChange = { volStr = it },
                        label = { Text("Volatility %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = nuStr,
                    onValueChange = { nuStr = it },
                    label = { Text("Student-t nu (df)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val r = (retStr.toDoubleOrNull() ?: 20.0) / 100.0
                            val v = (volStr.toDoubleOrNull() ?: 25.0) / 100.0
                            val nu = nuStr.toDoubleOrNull() ?: 4.5
                            onAdd(ticker, name, sector, r, v, nu)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = QuantumCyan, contentColor = Color(0xFF00363C)),
                        enabled = ticker.isNotBlank(),
                        modifier = Modifier.testTag("submit_add_ticker_btn")
                    ) {
                        Text("Add Asset")
                    }
                }
            }
        }
    }
}
