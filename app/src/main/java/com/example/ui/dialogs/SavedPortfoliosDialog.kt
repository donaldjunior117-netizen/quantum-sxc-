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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.PortfolioEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedPortfoliosDialog(
    portfolios: List<PortfolioEntity>,
    onSaveCurrent: (String) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var showNamePrompt by remember { mutableStateOf(false) }
    var portfolioName by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("saved_portfolios_dialog"),
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
                            text = "SAVED QUANT PORTFOLIOS",
                            style = MaterialTheme.typography.titleMedium,
                            color = BenchmarkGold
                        )
                        Text(
                            text = "Audited Room Database Persistence",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_saved_dialog_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Save Current Button
                if (!showNamePrompt) {
                    Button(
                        onClick = { showNamePrompt = true },
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("save_current_portfolio_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BenchmarkGold.copy(alpha = 0.2f),
                            contentColor = BenchmarkGold
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SAVE CURRENT QUANT RUN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = portfolioName,
                            onValueChange = { portfolioName = it },
                            placeholder = { Text("Portfolio Name...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BenchmarkGold,
                                unfocusedBorderColor = TerminalBorder
                            ),
                            modifier = Modifier.weight(1f).testTag("input_portfolio_name")
                        )
                        Button(
                            onClick = {
                                onSaveCurrent(portfolioName)
                                portfolioName = ""
                                showNamePrompt = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BenchmarkGold, contentColor = Color(0xFF3F2E00)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("confirm_save_btn")
                        ) {
                            Text("Save")
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // List
                if (portfolios.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No saved portfolio runs yet", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                            Spacer(Modifier.height(4.dp))
                            Text("Tap 'Save Current Quant Run' to store your analysis", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(portfolios, key = { it.id }) { item ->
                            SavedPortfolioItem(
                                item = item,
                                formattedDate = dateFormat.format(Date(item.timestamp)),
                                onDelete = { onDelete(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedPortfolioItem(
    item: PortfolioEntity,
    formattedDate: String,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurface, RoundedCornerShape(8.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tickers: ${item.selectedTickersJson}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = QuantumCyan
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sharpe: ${String.format("%.2f", item.sharpeRatio)}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = BenchmarkGold)
                    Text("99% VaR: ${String.format("%.1f%%", item.var99 * 100)}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = RiskCrimson)
                    Text("Ratio: ${String.format("%.1f%%", item.approximationRatio * 100)}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = AlphaGreen)
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
            }
        }
    }
}
