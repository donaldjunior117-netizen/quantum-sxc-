package com.example.quantum

import com.example.data.model.QaeBenchmarkResult
import com.example.data.model.QaePoint
import kotlin.math.ln
import kotlin.math.max

object QaeEngine {

    /**
     * Computes the theoretical and simulated query complexity comparison
     * between Classical Monte Carlo O(1/eps^2) and Quantum Amplitude Estimation O(1/eps)
     */
    fun computeScalingBenchmark(
        eventProbability: Double = 0.05, // e.g. 5% tail probability for 95% VaR
        confidenceLevel: Double = 0.99,
        targetEpsilon: Double = 0.005
    ): QaeBenchmarkResult {
        val alpha = 1.0 - confidenceLevel
        val zAlpha = 2.576 // 99% two-sided normal quantile
        val p = max(0.01, minOf(0.99, eventProbability))
        val variance = p * (1.0 - p)

        // Precision levels to evaluate
        val epsilons = listOf(0.05, 0.02, 0.01, 0.005, 0.002, 0.001)
        val points = mutableListOf<QaePoint>()

        for (eps in epsilons) {
            // Classical Monte Carlo: N_MC = z^2 * Var / eps^2
            val mcSamples = ((zAlpha * zAlpha * variance) / (eps * eps)).toLong()

            // Quantum Amplitude Estimation (IQAE): M_QAE = ceil( (pi / (4 * eps)) * ln(2 / alpha) )
            val qaeQueries = (Math.ceil((Math.PI / (4.0 * eps)) * ln(2.0 / alpha))).toLong()

            val speedup = if (qaeQueries > 0) mcSamples.toDouble() / qaeQueries else 1.0

            points.add(
                QaePoint(
                    epsilon = eps,
                    classicalSamples = mcSamples,
                    quantumQueries = qaeQueries,
                    theoreticalSpeedup = speedup
                )
            )
        }

        // Active target point
        val activeMc = ((zAlpha * zAlpha * variance) / (targetEpsilon * targetEpsilon)).toLong()
        val activeQae = (Math.ceil((Math.PI / (4.0 * targetEpsilon)) * ln(2.0 / alpha))).toLong()
        val activeSpeedup = if (activeQae > 0) activeMc.toDouble() / activeQae else 1.0

        return QaeBenchmarkResult(
            points = points,
            targetEpsilon = targetEpsilon,
            currentClassicalQueries = activeMc,
            currentQuantumQueries = activeQae,
            speedupRatio = activeSpeedup
        )
    }
}
