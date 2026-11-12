package com.example.quantum

import com.example.data.model.QuboParams
import kotlin.math.abs

class QuboEngine(
    val n: Int,
    val mu: DoubleArray,
    val sigma: Array<DoubleArray>,
    val params: QuboParams
) {
    val quboMatrix: Array<DoubleArray> = Array(n) { DoubleArray(n) }
    var constantOffset: Double = 0.0

    // Ising Hamiltonian coefficients: H = sum h_i Z_i + sum J_ij Z_i Z_j + offset
    val isingH: DoubleArray = DoubleArray(n)
    val isingJ: Array<DoubleArray> = Array(n) { DoubleArray(n) }
    var isingOffset: Double = 0.0

    init {
        buildQubo()
        convertToIsing()
    }

    private fun buildQubo() {
        val q = params.riskAversionQ
        val p = params.penaltyMultiplierP
        val b = params.budgetB
        val cTx = params.totalCostRate

        // Initial budget penalty offset: P * B^2
        var offset = p * b * b

        // Transaction cost: cTx * sum |x_i / B - w_init_i|
        // For x_i in {0, 1}:
        // C_0(i) = cTx * w_init_i  (when x_i = 0)
        // C_1(i) = cTx * |1/B - w_init_i|  (when x_i = 1)
        // Exactly g(x_i) = C_0(i) + (C_1(i) - C_0(i)) * x_i
        val invB = if (b > 0) 1.0 / b else 0.0

        for (i in 0 until n) {
            val wInit = if (i < params.initialWeights.size) params.initialWeights[i] else 0.0
            val c0 = cTx * wInit
            val c1 = cTx * abs(invB - wInit)
            val deltaQiiCost = c1 - c0
            offset += c0

            for (j in 0 until n) {
                if (i == j) {
                    quboMatrix[i][i] = q * sigma[i][i] - mu[i] + deltaQiiCost + p * (1.0 - 2.0 * b)
                } else {
                    quboMatrix[i][j] = q * sigma[i][j] + p
                }
            }
        }
        constantOffset = offset
    }

    private fun convertToIsing() {
        // x_i = (1 - Z_i) / 2
        var offset = constantOffset

        // Linear QUBO diagonal contributions
        for (i in 0 until n) {
            val qii = quboMatrix[i][i]
            offset += qii * 0.5
            isingH[i] -= qii * 0.5
        }

        // Quadratic QUBO off-diagonal contributions
        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val qij = quboMatrix[i][j] + quboMatrix[j][i]
                offset += qij * 0.25
                isingH[i] -= qij * 0.25
                isingH[j] -= qij * 0.25
                isingJ[i][j] = qij * 0.25
                isingJ[j][i] = qij * 0.25
            }
        }
        isingOffset = offset
    }

    /**
     * Evaluates exact QUBO objective for a binary vector x in {0, 1}^N
     * f(x) = q * x^T * Sigma * x - mu^T * x + TC(x) + P * (sum x_i - B)^2
     */
    fun evaluateBinaryVector(x: IntArray): Double {
        var sumX = 0
        val xDouble = DoubleArray(n) { i ->
            sumX += x[i]
            x[i].toDouble()
        }

        val riskTerm = params.riskAversionQ * LinearAlgebra.quadraticForm(xDouble, sigma)
        val returnTerm = LinearAlgebra.dotProduct(mu, xDouble)
        val budgetDiff = (sumX - params.budgetB).toDouble()
        val penaltyTerm = params.penaltyMultiplierP * budgetDiff * budgetDiff

        // Exact transaction cost: brokerage + slippage
        val invB = if (params.budgetB > 0) 1.0 / params.budgetB else 0.0
        var txCost = 0.0
        val cTx = params.totalCostRate
        for (i in 0 until n) {
            val wInit = if (i < params.initialWeights.size) params.initialWeights[i] else 0.0
            val targetW = if (x[i] == 1) invB else 0.0
            txCost += cTx * abs(targetW - wInit)
        }

        return riskTerm - returnTerm + txCost + penaltyTerm
    }

    fun evaluateStateIndex(stateIndex: Int): Double {
        val x = IntArray(n) { i -> (stateIndex shr (n - 1 - i)) and 1 }
        return evaluateBinaryVector(x)
    }

    fun stateToBitstring(stateIndex: Int): String {
        val sb = StringBuilder()
        for (i in 0 until n) {
            sb.append((stateIndex shr (n - 1 - i)) and 1)
        }
        return sb.toString()
    }
}
