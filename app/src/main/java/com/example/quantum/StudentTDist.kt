package com.example.quantum

import java.util.Random
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt

class StudentTDist(private val random: Random = Random()) {

    /**
     * Standard Normal N(0, 1) using Box-Muller transform
     */
    fun nextGaussian(): Double {
        var u1 = random.nextDouble()
        while (u1 <= 1e-12) {
            u1 = random.nextDouble()
        }
        val u2 = random.nextDouble()
        return sqrt(-2.0 * ln(u1)) * cos(2.0 * Math.PI * u2)
    }

    /**
     * Marsaglia and Tsang method for Gamma(alpha, 1.0) with alpha > 0
     */
    fun nextGamma(alpha: Double): Double {
        if (alpha < 1.0) {
            // Johnk's generator or boost via scaling
            var u = random.nextDouble()
            while (u <= 1e-12) u = random.nextDouble()
            return nextGamma(alpha + 1.0) * Math.pow(u, 1.0 / alpha)
        }
        val d = alpha - 1.0 / 3.0
        val c = 1.0 / sqrt(9.0 * d)
        while (true) {
            val z = nextGaussian()
            val v = (1.0 + c * z) * (1.0 + c * z) * (1.0 + c * z)
            if (v <= 0.0) continue
            var u = random.nextDouble()
            while (u <= 1e-12) u = random.nextDouble()
            if (u < 1.0 - 0.0331 * z * z * z * z) {
                return d * v
            }
            if (ln(u) < 0.5 * z * z + d * (1.0 - v + ln(v))) {
                return d * v
            }
        }
    }

    /**
     * Chi-Squared random variable with nu degrees of freedom.
     * ChiSquare(nu) = 2.0 * Gamma(nu / 2.0)
     */
    fun nextChiSquare(nu: Double): Double {
        val safeNu = if (nu < 2.1) 2.1 else nu
        return 2.0 * nextGamma(safeNu / 2.0)
    }

    /**
     * Draws a univariate Student-t variate with nu degrees of freedom:
     * T = Z / sqrt(W / nu), where Z ~ N(0, 1), W ~ ChiSquare(nu)
     */
    fun nextStudentT(nu: Double): Double {
        val z = nextGaussian()
        val w = nextChiSquare(nu)
        val denom = sqrt(w / nu)
        return z / (if (denom > 1e-8) denom else 1e-8)
    }

    /**
     * Draws an N-dimensional correlated Student-t vector:
     * X = mu + sqrt(nu / W) * (L * Z)
     * where L is the Cholesky factor of the covariance matrix, Z ~ N_N(0, I), and W ~ ChiSquare(nu).
     */
    fun nextCorrelatedStudentTVector(
        mu: DoubleArray,
        choleskyL: Array<DoubleArray>,
        nu: Double
    ): DoubleArray {
        val n = mu.size
        val z = DoubleArray(n) { nextGaussian() }
        val lz = LinearAlgebra.multiplyLowerTriangular(choleskyL, z)
        val w = nextChiSquare(nu)
        val scale = sqrt(nu / (if (w > 1e-8) w else 1e-8))

        val result = DoubleArray(n)
        for (i in 0 until n) {
            result[i] = mu[i] + scale * lz[i]
        }
        return result
    }
}
