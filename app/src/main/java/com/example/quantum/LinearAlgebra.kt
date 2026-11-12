package com.example.quantum

import kotlin.math.max
import kotlin.math.sqrt

object LinearAlgebra {

    /**
     * Cholesky Decomposition: Computes lower triangular matrix L such that A = L * L^T.
     * Incorporates adaptive Tikhonov jitter regularization on diagonal if necessary.
     */
    fun choleskyDecomposition(matrix: Array<DoubleArray>, jitter: Double = 1e-6): Array<DoubleArray> {
        val n = matrix.size
        val l = Array(n) { DoubleArray(n) }

        // Clone and add diagonal jitter for positive definiteness
        val a = Array(n) { i ->
            DoubleArray(n) { j ->
                if (i == j) matrix[i][j] + jitter else matrix[i][j]
            }
        }

        for (i in 0 until n) {
            for (j in 0..i) {
                var sum = 0.0
                for (k in 0 until j) {
                    sum += l[i][k] * l[j][k]
                }
                if (i == j) {
                    val diff = a[i][i] - sum
                    l[i][j] = if (diff > 0.0) sqrt(diff) else sqrt(max(diff, 1e-8))
                } else {
                    l[i][j] = if (l[j][j] > 1e-12) (a[i][j] - sum) / l[j][j] else 0.0
                }
            }
        }
        return l
    }

    /**
     * Multiplies lower triangular matrix L with vector z.
     */
    fun multiplyLowerTriangular(l: Array<DoubleArray>, z: DoubleArray): DoubleArray {
        val n = z.size
        val result = DoubleArray(n)
        for (i in 0 until n) {
            var sum = 0.0
            for (j in 0..i) {
                sum += l[i][j] * z[j]
            }
            result[i] = sum
        }
        return result
    }

    /**
     * Computes quadratic form: x^T * A * x
     */
    fun quadraticForm(x: DoubleArray, a: Array<DoubleArray>): Double {
        val n = x.size
        var sum = 0.0
        for (i in 0 until n) {
            if (x[i] == 0.0) continue
            for (j in 0 until n) {
                if (x[j] == 0.0) continue
                sum += x[i] * a[i][j] * x[j]
            }
        }
        return sum
    }

    /**
     * Vector dot product: u . v
     */
    fun dotProduct(u: DoubleArray, v: DoubleArray): Double {
        var sum = 0.0
        for (i in u.indices) {
            sum += u[i] * v[i]
        }
        return sum
    }

    /**
     * Builds covariance matrix from volatilities and correlation factor.
     */
    fun buildCovarianceMatrix(vols: DoubleArray, baseCorrelation: Double = 0.35): Array<DoubleArray> {
        val n = vols.size
        return Array(n) { i ->
            DoubleArray(n) { j ->
                if (i == j) {
                    vols[i] * vols[i]
                } else {
                    baseCorrelation * vols[i] * vols[j]
                }
            }
        }
    }
}
