package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.QuboParams
import com.example.quantum.ExactEigensolver
import com.example.quantum.LinearAlgebra
import com.example.quantum.MarkowitzOptimizer
import com.example.quantum.QaoaSimulator
import com.example.quantum.QuboEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Q-Fin Pro", appName)
    }

    @Test
    fun `test exact eigensolver and QAOA approximation ratio`() {
        val n = 3
        val mu = doubleArrayOf(0.20, 0.25, 0.15)
        val vols = doubleArrayOf(0.22, 0.28, 0.18)
        val sigma = LinearAlgebra.buildCovarianceMatrix(vols, 0.30)
        val params = QuboParams(nAssets = n, budgetB = 2, riskAversionQ = 1.0, penaltyMultiplierP = 2.0)

        val qubo = QuboEngine(n, mu, sigma, params)
        val exact = ExactEigensolver(qubo).solve()

        // Valid budget check
        assertEquals(2, Integer.bitCount(exact.exactOptimalIndex))

        val qaoa = QaoaSimulator(qubo, circuitDepthP = 2).run(maxIterations = 20)
        assertTrue(qaoa.approximationRatio >= 0.5)
    }

    @Test
    fun `test transaction cost modeling and net metrics`() {
        val tickers = listOf("NVDA", "AAPL", "MSFT")
        val mu = doubleArrayOf(0.35, 0.20, 0.22)
        val vols = doubleArrayOf(0.40, 0.22, 0.24)
        val sigma = LinearAlgebra.buildCovarianceMatrix(vols, 0.35)

        val opt = MarkowitzOptimizer(
            tickers = tickers,
            mu = mu,
            sigma = sigma,
            riskAversionQ = 1.0,
            brokerageFeeBps = 15.0,
            slippageBps = 20.0,
            initialWeights = doubleArrayOf(0.5, 0.5, 0.0)
        )
        val res = opt.optimize()

        assertNotNull(res.costMetrics)
        val cost = res.costMetrics!!
        assertTrue(cost.turnoverRate > 0.0)
        assertTrue(cost.totalCost > 0.0)
        assertTrue(cost.netExpectedReturn < cost.grossExpectedReturn)
        assertTrue(cost.netSharpe < cost.grossSharpe)
    }
}
