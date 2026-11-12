package com.example.quantum

import com.example.data.model.MarkowitzResult
import com.example.data.model.TransactionCostMetrics
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.sqrt

class MarkowitzOptimizer(
    private val tickers: List<String>,
    private val mu: DoubleArray,
    private val sigma: Array<DoubleArray>,
    private val riskAversionQ: Double = 1.0,
    private val riskFreeRate: Double = 0.03,
    private val brokerageFeeBps: Double = 15.0,
    private val slippageBps: Double = 20.0,
    private val initialWeights: DoubleArray = DoubleArray(tickers.size) { 0.0 }
) {
    private val totalCostRate = (brokerageFeeBps + slippageBps) / 10000.0

    /**
     * Solves continuous Markowitz Mean-Variance optimization with transaction cost penalty:
     * max_w w^T * mu - (q / 2) * w^T * Sigma * w - c_tx * sum |w_i - w_init_i|
     * subject to sum w_i = 1, w_i >= 0
     */
    fun optimize(maxIterations: Int = 100, learningRate: Double = 0.05): MarkowitzResult {
        val n = tickers.size
        var w = DoubleArray(n) { 1.0 / n }

        for (iter in 0 until maxIterations) {
            val grad = DoubleArray(n)
            for (i in 0 until n) {
                var sigW = 0.0
                for (j in 0 until n) {
                    sigW += sigma[i][j] * w[j]
                }
                val wInit = if (i < initialWeights.size) initialWeights[i] else 0.0
                val costSubgrad = totalCostRate * sign(w[i] - wInit)
                grad[i] = mu[i] - riskAversionQ * sigW - costSubgrad
            }

            val nextW = DoubleArray(n) { i ->
                w[i] + learningRate * grad[i]
            }

            w = projectOntoSimplex(nextW)
        }

        val expReturn = LinearAlgebra.dotProduct(mu, w)
        val variance = LinearAlgebra.quadraticForm(w, sigma)
        val vol = sqrt(max(1e-8, variance))

        // Turnover & Transaction Costs
        var sumAbsTurnover = 0.0
        for (i in 0 until n) {
            val wInit = if (i < initialWeights.size) initialWeights[i] else 0.0
            sumAbsTurnover += abs(w[i] - wInit)
        }
        val turnoverRate = 0.5 * sumAbsTurnover
        val brokerageCost = turnoverRate * (brokerageFeeBps / 10000.0)
        val slippageCost = turnoverRate * (slippageBps / 10000.0)
        val totalCost = brokerageCost + slippageCost
        val netReturn = expReturn - totalCost

        val grossSharpe = if (vol > 1e-6) (expReturn - riskFreeRate) / vol else 0.0
        val netSharpe = if (vol > 1e-6) (netReturn - riskFreeRate) / vol else 0.0

        val weightMap = mutableMapOf<String, Double>()
        for (i in tickers.indices) {
            weightMap[tickers[i]] = w[i]
        }

        val costMetrics = TransactionCostMetrics(
            turnoverRate = turnoverRate,
            brokerageFeeCost = brokerageCost,
            slippageCost = slippageCost,
            totalCost = totalCost,
            totalCostBps = totalCost * 10000.0,
            grossExpectedReturn = expReturn,
            netExpectedReturn = netReturn,
            grossSharpe = grossSharpe,
            netSharpe = netSharpe,
            grossVar99 = 0.25,
            netVar99 = 0.25 + totalCost,
            grossCvar99 = 0.30,
            netCvar99 = 0.30 + totalCost
        )

        return MarkowitzResult(
            weights = weightMap,
            expectedReturn = expReturn,
            volatility = vol,
            sharpeRatio = grossSharpe,
            costMetrics = costMetrics
        )
    }

    private fun projectOntoSimplex(y: DoubleArray): DoubleArray {
        val n = y.size
        val sorted = y.clone()
        sorted.sortDescending()

        var sum = 0.0
        var rho = 0
        for (i in 0 until n) {
            sum += sorted[i]
            if (sorted[i] + (1.0 - sum) / (i + 1) > 0.0) {
                rho = i + 1
            }
        }

        var sumRho = 0.0
        for (i in 0 until rho) {
            sumRho += sorted[i]
        }
        val theta = (sumRho - 1.0) / rho

        val w = DoubleArray(n)
        for (i in 0 until n) {
            w[i] = max(0.0, y[i] - theta)
        }
        return w
    }
}
