package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.QFinViewModel
import com.example.ui.dialogs.PythonExportDialog
import com.example.ui.dialogs.SavedPortfoliosDialog
import com.example.ui.dialogs.UniverseEditorSheet
import com.example.ui.tabs.AllocationTab
import com.example.ui.tabs.QuantumAuditTab
import com.example.ui.tabs.RiskTab
import com.example.ui.theme.AlphaGreen
import com.example.ui.theme.BenchmarkGold
import com.example.ui.theme.QFinProTheme
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.RiskCrimson
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: QFinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QFinProTheme {
                QFinApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QFinApp(viewModel: QFinViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val savedPortfolios by viewModel.savedPortfolios.collectAsStateWithLifecycle()

    var showUniverseSheet by remember { mutableStateOf(false) }
    var showPythonExport by remember { mutableStateOf(false) }
    var showSavedPortfolios by remember { mutableStateOf(false) }

    // System back navigation handling
    BackHandler(enabled = state.activeTab != 0) {
        viewModel.setActiveTab(0)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurface)
                    .statusBarsPadding()
                    .border(androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Title and Qubit Symbol
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(QuantumCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, QuantumCyan, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ψ",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = QuantumCyan,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Q-FIN PRO",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(AlphaGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("v1.0", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = AlphaGreen)
                                }
                            }
                            Text(
                                text = "Hybrid Quantum-Classical Optimizer",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // Top Action Icons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showSavedPortfolios = true },
                            modifier = Modifier.testTag("saved_portfolios_action_btn")
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Saved Portfolios", tint = BenchmarkGold)
                        }

                        IconButton(
                            onClick = { showPythonExport = true },
                            modifier = Modifier.testTag("python_export_action_btn")
                        ) {
                            Icon(Icons.Default.Code, contentDescription = "Export Code", tint = QuantumCyan)
                        }

                        IconButton(
                            onClick = { viewModel.runOptimization() },
                            modifier = Modifier.testTag("recalculate_action_btn")
                        ) {
                            if (state.isSimulating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = QuantumCyan
                                )
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Re-run Simulation", tint = TextPrimary)
                            }
                        }
                    }
                }

                // Status Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF030712))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (state.isSimulating) BenchmarkGold else AlphaGreen, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = state.statusMessage,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = if (state.isSimulating) BenchmarkGold else TextSecondary
                        )
                    }
                    Text(
                        text = "QUBO: q=${String.format("%.1f", state.riskAversionQ)} B=${state.budgetB}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = QuantumCyan
                    )
                }

                AnimatedVisibility(visible = state.isSimulating) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = QuantumCyan,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                containerColor = TerminalSurface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = state.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = { Icon(Icons.Default.DonutLarge, contentDescription = "Allocation") },
                    label = { Text("Allocation", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = QuantumCyan,
                        selectedTextColor = QuantumCyan,
                        indicatorColor = QuantumCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_allocation_tab")
                )
                NavigationBarItem(
                    selected = state.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Tail-Risk") },
                    label = { Text("Tail-Risk", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RiskCrimson,
                        selectedTextColor = RiskCrimson,
                        indicatorColor = RiskCrimson.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_risk_tab")
                )
                NavigationBarItem(
                    selected = state.activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Quantum Audit") },
                    label = { Text("Quantum", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BenchmarkGold,
                        selectedTextColor = BenchmarkGold,
                        indicatorColor = BenchmarkGold.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_quantum_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TerminalBackground)
        ) {
            when (state.activeTab) {
                0 -> AllocationTab(
                    state = state,
                    onBudgetChange = { viewModel.setBudgetB(it) },
                    onRiskAversionChange = { viewModel.setRiskAversionQ(it) },
                    onBrokerageFeeChange = { viewModel.setBrokerageFeeBps(it) },
                    onSlippageChange = { viewModel.setSlippageBps(it) },
                    onToggleHolding = { viewModel.toggleInitialHolding(it) },
                    onResetHoldingsCash = { viewModel.setInitialHoldingsCash() },
                    onOpenUniverseSheet = { showUniverseSheet = true }
                )
                1 -> RiskTab(
                    state = state,
                    onHorizonChange = { viewModel.setForecastHorizonDays(it) }
                )
                2 -> QuantumAuditTab(
                    state = state,
                    onCircuitDepthChange = { viewModel.setCircuitDepthP(it) },
                    onQaeEpsilonChange = { viewModel.setQaeTargetEpsilon(it) },
                    onOpenPythonExport = { showPythonExport = true }
                )
            }
        }
    }

    // Modal Dialogs
    if (showUniverseSheet) {
        UniverseEditorSheet(
            assets = state.assets,
            onToggleSelect = { viewModel.toggleAssetSelection(it) },
            onSentimentChange = { ticker, delta -> viewModel.updateSentimentDrift(ticker, delta) },
            onAddAsset = { ticker, name, sector, ret, vol, nu ->
                viewModel.addCustomAsset(ticker, name, sector, ret, vol, nu)
            },
            onDismiss = { showUniverseSheet = false }
        )
    }

    if (showPythonExport) {
        PythonExportDialog(
            scriptContent = viewModel.generatePythonExport(),
            onDismiss = { showPythonExport = false }
        )
    }

    if (showSavedPortfolios) {
        SavedPortfoliosDialog(
            portfolios = savedPortfolios,
            onSaveCurrent = { name -> viewModel.saveCurrentPortfolio(name) },
            onDelete = { id -> viewModel.deletePortfolio(id) },
            onDismiss = { showSavedPortfolios = false }
        )
    }
}
