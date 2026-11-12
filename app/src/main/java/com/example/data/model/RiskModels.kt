package com.example.data.model

data class RiskEngineResult(
    val horizonDays: Int,
    val simulationPaths: Int,
    val var95: Double, // 95% Value-at-Risk (e.g. 0.14 -> 14% max loss)
    val var99: Double, // 99% Value-at-Risk
    val cvar95: Double, // 95% Conditional VaR / Expected Shortfall
    val cvar99: Double, // 99% Conditional VaR / Expected Shortfall
    val expectedReturnPeriod: Double,
    val medianReturnPeriod: Double,
    val maxDrawdown: Double,
    val empiricalLossHistBins: List<Pair<Double, Int>>, // loss threshold -> frequency
    val trajectoryPaths: List<List<Double>>, // sample normalized price paths [1.0, ...]
    val degreesOfFreedomNu: Double,
    val transactionCostDrag: Double = 0.0,
    val netVar95: Double = var95 + transactionCostDrag,
    val netVar99: Double = var99 + transactionCostDrag,
    val netCvar95: Double = cvar95 + transactionCostDrag,
    val netCvar99: Double = cvar99 + transactionCostDrag,
    val netExpectedReturnPeriod: Double = expectedReturnPeriod - transactionCostDrag
)

data class QaePoint(
    val epsilon: Double,
    val classicalSamples: Long,
    val quantumQueries: Long,
    val theoreticalSpeedup: Double
)

data class QaeBenchmarkResult(
    val points: List<QaePoint>,
    val targetEpsilon: Double,
    val currentClassicalQueries: Long,
    val currentQuantumQueries: Long,
    val speedupRatio: Double
)
