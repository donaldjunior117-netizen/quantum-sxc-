package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PortfolioEntity
import com.example.data.local.PortfolioRepository
import com.example.data.model.AssetSpec
import com.example.data.model.MarkowitzResult
import com.example.data.model.QaeBenchmarkResult
import com.example.data.model.QaoaRunResult
import com.example.data.model.QuboParams
import com.example.data.model.RiskEngineResult
import com.example.quantum.LinearAlgebra
import com.example.quantum.MarkowitzOptimizer
import com.example.quantum.PythonScriptGenerator
import com.example.quantum.QaeEngine
import com.example.quantum.QaoaSimulator
import com.example.quantum.QuboEngine
import com.example.quantum.RiskEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QFinUiState(
    val assets: List<AssetSpec> = emptyList(),
    val budgetB: Int = 3,
    val riskAversionQ: Double = 1.0,
    val circuitDepthP: Int = 2,
    val forecastHorizonDays: Int = 90,
    val simulationPaths: Int = 5000,
    val brokerageFeeBps: Double = 15.0, // basis points (0.15%)
    val slippageBps: Double = 20.0, // basis points (0.20%)
    val initialHoldingTickers: Set<String> = setOf("NVDA", "AAPL"), // current portfolio holdings
    val isSimulating: Boolean = false,
    val activeTab: Int = 0,
    val qaoaResult: QaoaRunResult? = null,
    val markowitzResult: MarkowitzResult? = null,
    val riskResult: RiskEngineResult? = null,
    val qaeResult: QaeBenchmarkResult? = null,
    val targetQaeEpsilon: Double = 0.005,
    val statusMessage: String = "Quantum Engine Idle"
)

class QFinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PortfolioRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PortfolioRepository(db.portfolioDao())
    }

    val savedPortfolios: StateFlow<List<PortfolioEntity>> = repository.allPortfolios
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(
        QFinUiState(
            assets = getInitialUniverse()
        )
    )
    val uiState: StateFlow<QFinUiState> = _uiState.asStateFlow()

    init {
        runOptimization()
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    fun setBudgetB(b: Int) {
        val selectedCount = _uiState.value.assets.count { it.isSelected }
        val safeB = b.coerceIn(1, maxOf(1, selectedCount))
        _uiState.value = _uiState.value.copy(budgetB = safeB)
        runOptimization()
    }

    fun setRiskAversionQ(q: Double) {
        _uiState.value = _uiState.value.copy(riskAversionQ = q)
        runOptimization()
    }

    fun setCircuitDepthP(p: Int) {
        _uiState.value = _uiState.value.copy(circuitDepthP = p.coerceIn(1, 5))
        runOptimization()
    }

    fun setForecastHorizonDays(days: Int) {
        _uiState.value = _uiState.value.copy(forecastHorizonDays = days)
        runOptimization()
    }

    fun setBrokerageFeeBps(bps: Double) {
        _uiState.value = _uiState.value.copy(brokerageFeeBps = bps.coerceIn(0.0, 100.0))
        runOptimization()
    }

    fun setSlippageBps(bps: Double) {
        _uiState.value = _uiState.value.copy(slippageBps = bps.coerceIn(0.0, 200.0))
        runOptimization()
    }

    fun toggleInitialHolding(ticker: String) {
        val current = _uiState.value.initialHoldingTickers
        val updated = if (current.contains(ticker)) current - ticker else current + ticker
        _uiState.value = _uiState.value.copy(initialHoldingTickers = updated)
        runOptimization()
    }

    fun setInitialHoldingsCash() {
        _uiState.value = _uiState.value.copy(initialHoldingTickers = emptySet())
        runOptimization()
    }

    fun updateSentimentDrift(ticker: String, delta: Double) {
        val updated = _uiState.value.assets.map {
            if (it.ticker == ticker) it.copy(sentimentDrift = delta.coerceIn(-0.05, 0.05)) else it
        }
        _uiState.value = _uiState.value.copy(assets = updated)
        runOptimization()
    }

    fun toggleAssetSelection(ticker: String) {
        val updated = _uiState.value.assets.map {
            if (it.ticker == ticker) it.copy(isSelected = !it.isSelected) else it
        }
        val activeCount = updated.count { it.isSelected }
        val safeBudget = if (_uiState.value.budgetB > activeCount && activeCount > 0) activeCount else _uiState.value.budgetB
        _uiState.value = _uiState.value.copy(assets = updated, budgetB = maxOf(1, safeBudget))
        runOptimization()
    }

    fun addCustomAsset(ticker: String, name: String, sector: String, ret: Double, vol: Double, nu: Double) {
        val cleanTicker = ticker.trim().uppercase()
        if (cleanTicker.isEmpty()) return
        val newAsset = AssetSpec(
            ticker = cleanTicker,
            name = if (name.isNotBlank()) name.trim() else cleanTicker,
            sector = if (sector.isNotBlank()) sector.trim() else "Custom Equities",
            baseAnnualReturn = ret,
            annualVolatility = vol,
            studentTDegreesOfFreedom = nu,
            isSelected = true
        )
        val updated = _uiState.value.assets + newAsset
        _uiState.value = _uiState.value.copy(assets = updated)
        runOptimization()
    }

    fun setQaeTargetEpsilon(eps: Double) {
        _uiState.value = _uiState.value.copy(targetQaeEpsilon = eps)
        val qaeRes = QaeEngine.computeScalingBenchmark(
            eventProbability = 0.05,
            targetEpsilon = eps
        )
        _uiState.value = _uiState.value.copy(qaeResult = qaeRes)
    }

    fun runOptimization() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSimulating = true,
                statusMessage = "Simulating QAOA with Transaction Costs & Slippage..."
            )

            withContext(Dispatchers.Default) {
                val state = _uiState.value
                val selectedAssets = state.assets.filter { it.isSelected }
                if (selectedAssets.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isSimulating = false, statusMessage = "No assets selected")
                    return@withContext
                }

                val n = selectedAssets.size
                val budget = minOf(state.budgetB, n)
                val mu = DoubleArray(n) { selectedAssets[it].effectiveExpectedReturn }
                val vols = DoubleArray(n) { selectedAssets[it].annualVolatility }
                val sigma = LinearAlgebra.buildCovarianceMatrix(vols, baseCorrelation = 0.35)

                // Initial weights for rebalancing cost modeling
                val holdingsCount = state.initialHoldingTickers.count { ticker ->
                    selectedAssets.any { it.ticker == ticker }
                }
                val initialWeights = DoubleArray(n) { i ->
                    if (holdingsCount > 0 && state.initialHoldingTickers.contains(selectedAssets[i].ticker)) {
                        1.0 / holdingsCount
                    } else {
                        0.0
                    }
                }

                // 1. Build QUBO with Transaction Cost & Slippage Friction
                val quboParams = QuboParams(
                    nAssets = n,
                    budgetB = budget,
                    riskAversionQ = state.riskAversionQ,
                    penaltyMultiplierP = 2.5,
                    brokerageFeeBps = state.brokerageFeeBps,
                    slippageBps = state.slippageBps,
                    initialWeights = initialWeights
                )
                val quboEngine = QuboEngine(n, mu, sigma, quboParams)

                // 2. Solve QAOA with Net Cost Penalty
                val qaoaSimulator = QaoaSimulator(quboEngine, circuitDepthP = state.circuitDepthP)
                val qaoaRes = qaoaSimulator.run(maxIterations = 35)

                // Map selected tickers
                val selectedTickers = mutableListOf<String>()
                for (i in 0 until n) {
                    if (((qaoaRes.optimalBitstring.getOrNull(i) ?: '0') == '1')) {
                        selectedTickers.add(selectedAssets[i].ticker)
                    }
                }
                val mappedQaoaRes = qaoaRes.copy(selectedTickers = selectedTickers)

                // 3. Classical Markowitz Mean-Variance with Transaction Cost Penalty
                val markowitzOpt = MarkowitzOptimizer(
                    tickers = selectedAssets.map { it.ticker },
                    mu = mu,
                    sigma = sigma,
                    riskAversionQ = state.riskAversionQ,
                    brokerageFeeBps = state.brokerageFeeBps,
                    slippageBps = state.slippageBps,
                    initialWeights = initialWeights
                )
                val markowitzRes = markowitzOpt.optimize()

                // 4. Correlated Student-t Monte Carlo Risk Engine with Upfront Cost Drag
                val basketWeights = DoubleArray(n) { i ->
                    if (selectedTickers.contains(selectedAssets[i].ticker)) 1.0 / maxOf(1, selectedTickers.size) else 0.0
                }
                val avgNu = selectedAssets.map { it.studentTDegreesOfFreedom }.average()
                val totalCostDrag = qaoaRes.costMetrics?.totalCost ?: 0.0
                val riskEngine = RiskEngine(basketWeights, mu, sigma, nuDegreesOfFreedom = avgNu)
                val riskRes = riskEngine.simulate(
                    horizonDays = state.forecastHorizonDays,
                    numPaths = state.simulationPaths,
                    transactionCostDrag = totalCostDrag
                )

                // 5. QAE Query Complexity Benchmark
                val qaeRes = QaeEngine.computeScalingBenchmark(
                    eventProbability = 0.05,
                    targetEpsilon = state.targetQaeEpsilon
                )

                _uiState.value = _uiState.value.copy(
                    isSimulating = false,
                    qaoaResult = mappedQaoaRes,
                    markowitzResult = markowitzRes,
                    riskResult = riskRes,
                    qaeResult = qaeRes,
                    statusMessage = "Optimization & Net Rebalancing Analysis Complete"
                )
            }
        }
    }

    fun saveCurrentPortfolio(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val qaoa = state.qaoaResult ?: return@launch
            val risk = state.riskResult ?: return@launch

            val record = PortfolioEntity(
                name = if (name.isNotBlank()) name.trim() else "Portfolio_${System.currentTimeMillis() % 10000}",
                selectedTickersJson = qaoa.selectedTickers.joinToString(","),
                budgetB = state.budgetB,
                riskAversionQ = state.riskAversionQ,
                expectedReturn = risk.netExpectedReturnPeriod,
                annualVolatility = state.markowitzResult?.volatility ?: 0.20,
                sharpeRatio = qaoa.costMetrics?.netSharpe ?: 1.0,
                var99 = risk.netVar99,
                approximationRatio = qaoa.approximationRatio
            )
            repository.insertPortfolio(record)
        }
    }

    fun deletePortfolio(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePortfolio(id)
        }
    }

    fun generatePythonExport(): String {
        val state = _uiState.value
        val selectedAssets = state.assets.filter { it.isSelected }
        val holdingsCount = state.initialHoldingTickers.count { ticker ->
            selectedAssets.any { it.ticker == ticker }
        }
        val initialWeights = DoubleArray(selectedAssets.size) { i ->
            if (holdingsCount > 0 && state.initialHoldingTickers.contains(selectedAssets[i].ticker)) {
                1.0 / holdingsCount
            } else {
                0.0
            }
        }
        val quboParams = QuboParams(
            nAssets = selectedAssets.size,
            budgetB = state.budgetB,
            riskAversionQ = state.riskAversionQ,
            penaltyMultiplierP = 2.5,
            brokerageFeeBps = state.brokerageFeeBps,
            slippageBps = state.slippageBps,
            initialWeights = initialWeights
        )
        return PythonScriptGenerator.generateFullPythonQiskitScript(selectedAssets, quboParams)
    }

    private fun getInitialUniverse(): List<AssetSpec> {
        return listOf(
            AssetSpec("NVDA", "NVIDIA Corp.", "Semiconductors / AI", 0.38, 0.44, studentTDegreesOfFreedom = 4.0),
            AssetSpec("AAPL", "Apple Inc.", "Consumer Tech", 0.18, 0.22, studentTDegreesOfFreedom = 4.8),
            AssetSpec("MSFT", "Microsoft Corp.", "Enterprise Cloud / AI", 0.21, 0.24, studentTDegreesOfFreedom = 4.5),
            AssetSpec("GOOGL", "Alphabet Inc.", "Digital Advertising / AI", 0.23, 0.27, studentTDegreesOfFreedom = 4.2),
            AssetSpec("AMZN", "Amazon.com Inc.", "E-Commerce / Cloud", 0.25, 0.29, studentTDegreesOfFreedom = 4.3),
            AssetSpec("RELIANCE.NS", "Reliance Industries", "Energy & Telecom", 0.17, 0.23, studentTDegreesOfFreedom = 4.6),
            AssetSpec("TCS.NS", "Tata Consultancy", "IT Services", 0.16, 0.20, studentTDegreesOfFreedom = 5.0),
            AssetSpec("HDFCBANK.NS", "HDFC Bank Ltd.", "Financial Services", 0.15, 0.21, studentTDegreesOfFreedom = 4.7)
        )
    }
}
