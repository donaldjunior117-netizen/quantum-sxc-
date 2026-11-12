package com.example.data.model

data class TransactionCostModel(
    val brokerageFeeBps: Double = 15.0, // basis points (15 bps = 0.15%)
    val slippageBps: Double = 20.0, // basis points (20 bps = 0.20%)
    val initialHoldings: Map<String, Double> = emptyMap() // ticker -> weight (sum <= 1.0)
) {
    val totalRate: Double
        get() = (brokerageFeeBps + slippageBps) / 10000.0
}

data class TransactionCostMetrics(
    val turnoverRate: Double, // 0.5 * sum |w_target - w_init|
    val brokerageFeeCost: Double, // turnover * brokerage_rate
    val slippageCost: Double, // turnover * slippage_rate
    val totalCost: Double, // brokerage + slippage
    val totalCostBps: Double, // in basis points
    val grossExpectedReturn: Double,
    val netExpectedReturn: Double, // gross - totalCost
    val grossSharpe: Double,
    val netSharpe: Double,
    val grossVar99: Double,
    val netVar99: Double, // grossVar99 + totalCost
    val grossCvar99: Double,
    val netCvar99: Double // grossCvar99 + totalCost
)

data class QuboParams(
    val nAssets: Int,
    val budgetB: Int, // Number of assets to select
    val riskAversionQ: Double, // q parameter in objective
    val penaltyMultiplierP: Double, // P for (sum x_i - B)^2 constraint
    val brokerageFeeBps: Double = 15.0,
    val slippageBps: Double = 20.0,
    val initialWeights: DoubleArray = DoubleArray(nAssets) { 0.0 }
) {
    val totalCostRate: Double
        get() = (brokerageFeeBps + slippageBps) / 10000.0
}

data class BitstringSample(
    val bitstring: String,
    val probability: Double,
    val energy: Double,
    val isValidBudget: Boolean
)

data class QaoaRunResult(
    val optimalBitstring: String,
    val selectedTickers: List<String>,
    val groundStateEnergy: Double,
    val exactMinimumEnergy: Double,
    val exactOptimalBitstring: String,
    val approximationRatio: Double, // E_qaoa / E_exact
    val bitstringProbabilities: List<BitstringSample>,
    val convergenceTrace: List<Pair<Int, Double>>, // iteration -> energy
    val optimalGammas: List<Double>,
    val optimalBetas: List<Double>,
    val circuitDepthP: Int,
    val costMetrics: TransactionCostMetrics? = null,
    val executionTimeMs: Long
)

data class MarkowitzResult(
    val weights: Map<String, Double>, // ticker -> continuous weight
    val expectedReturn: Double,
    val volatility: Double,
    val sharpeRatio: Double,
    val costMetrics: TransactionCostMetrics? = null
)
