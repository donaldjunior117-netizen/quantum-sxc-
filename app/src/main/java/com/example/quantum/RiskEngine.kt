package com.example.quantum

import com.example.data.model.RiskEngineResult
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class RiskEngine(
    private val weights: DoubleArray,
    private val muAnnual: DoubleArray,
    private val sigmaAnnual: Array<DoubleArray>,
    private val nuDegreesOfFreedom: Double = 4.5
) {
    private val studentT = StudentTDist()

    fun simulate(
        horizonDays: Int = 90,
        numPaths: Int = 5000,
        transactionCostDrag: Double = 0.0
    ): RiskEngineResult {
        val n = weights.size
        val dt = horizonDays.toDouble() / 252.0
        val sqrtDt = sqrt(dt)

        // Period scaled parameters
        val periodMu = DoubleArray(n) { i -> muAnnual[i] * dt }
        val periodCholesky = LinearAlgebra.choleskyDecomposition(sigmaAnnual)
        for (i in 0 until n) {
            for (j in 0 until n) {
                periodCholesky[i][j] *= sqrtDt
            }
        }

        val portfolioLosses = DoubleArray(numPaths)
        var sumReturn = 0.0

        for (p in 0 until numPaths) {
            val assetReturns = studentT.nextCorrelatedStudentTVector(periodMu, periodCholesky, nuDegreesOfFreedom)
            val portReturn = LinearAlgebra.dotProduct(weights, assetReturns)
            sumReturn += portReturn
            portfolioLosses[p] = -portReturn // positive means loss
        }

        portfolioLosses.sort()

        val idx95 = min(numPaths - 1, max(0, (numPaths * 0.95).toInt()))
        val idx99 = min(numPaths - 1, max(0, (numPaths * 0.99).toInt()))

        val var95 = portfolioLosses[idx95]
        val var99 = portfolioLosses[idx99]

        // CVaR (Expected Shortfall) is the average of losses beyond the VaR cutoff
        var cvar95Sum = 0.0
        var cvar95Count = 0
        for (i in idx95 until numPaths) {
            cvar95Sum += portfolioLosses[i]
            cvar95Count++
        }
        val cvar95 = if (cvar95Count > 0) cvar95Sum / cvar95Count else var95

        var cvar99Sum = 0.0
        var cvar99Count = 0
        for (i in idx99 until numPaths) {
            cvar99Sum += portfolioLosses[i]
            cvar99Count++
        }
        val cvar99 = if (cvar99Count > 0) cvar99Sum / cvar99Count else var99

        val medianReturn = -portfolioLosses[numPaths / 2]
        val expectedReturn = sumReturn / numPaths

        // Max drawdown over the simulated paths
        val maxLoss = portfolioLosses[numPaths - 1]

        // Build loss distribution histogram (24 bins)
        val minLoss = portfolioLosses[0]
        val binWidth = (maxLoss - minLoss) / 24.0
        val bins = mutableListOf<Pair<Double, Int>>()
        if (binWidth > 1e-6) {
            val counts = IntArray(24)
            for (loss in portfolioLosses) {
                val bIdx = min(23, max(0, ((loss - minLoss) / binWidth).toInt()))
                counts[bIdx]++
            }
            for (b in 0 until 24) {
                val binCenter = minLoss + (b + 0.5) * binWidth
                bins.add(binCenter to counts[b])
            }
        }

        // Generate 12 multi-step trajectory paths for fan chart
        val trajectorySteps = 20
        val trajectoryPaths = mutableListOf<List<Double>>()
        val stepDt = dt / trajectorySteps
        val stepSqrtDt = sqrt(stepDt)
        val stepMu = DoubleArray(n) { i -> muAnnual[i] * stepDt }
        val stepCholesky = LinearAlgebra.choleskyDecomposition(sigmaAnnual)
        for (i in 0 until n) {
            for (j in 0 until n) {
                stepCholesky[i][j] *= stepSqrtDt
            }
        }

        for (pathIdx in 0 until 12) {
            val path = mutableListOf<Double>()
            var currentVal = 100.0 // Base index 100
            path.add(currentVal)
            for (step in 1..trajectorySteps) {
                val assetRet = studentT.nextCorrelatedStudentTVector(stepMu, stepCholesky, nuDegreesOfFreedom)
                val stepRet = LinearAlgebra.dotProduct(weights, assetRet)
                currentVal *= (1.0 + stepRet)
                path.add(currentVal)
            }
            trajectoryPaths.add(path)
        }

        return RiskEngineResult(
            horizonDays = horizonDays,
            simulationPaths = numPaths,
            var95 = var95,
            var99 = var99,
            cvar95 = cvar95,
            cvar99 = cvar99,
            expectedReturnPeriod = expectedReturn,
            medianReturnPeriod = medianReturn,
            maxDrawdown = maxLoss,
            empiricalLossHistBins = bins,
            trajectoryPaths = trajectoryPaths,
            degreesOfFreedomNu = nuDegreesOfFreedom,
            transactionCostDrag = transactionCostDrag
        )
    }
}
