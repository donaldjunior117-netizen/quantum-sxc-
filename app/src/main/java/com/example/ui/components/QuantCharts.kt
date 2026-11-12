package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BitstringSample
import com.example.data.model.QaePoint
import com.example.ui.theme.AlphaGreen
import com.example.ui.theme.BenchmarkGold
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.RiskCrimson
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

/**
 * Monte Carlo Student-t Fan Chart visualizing simulated portfolio price/return trajectories
 */
@Composable
fun FanChart(
    trajectories: List<List<Double>>,
    modifier: Modifier = Modifier
) {
    if (trajectories.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(TerminalSurface, RoundedCornerShape(8.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .testTag("fan_chart")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            val numSteps = trajectories[0].size
            if (numSteps < 2) return@Canvas

            var minVal = 100.0
            var maxVal = 100.0
            for (path in trajectories) {
                for (v in path) {
                    if (v < minVal) minVal = v
                    if (v > maxVal) maxVal = v
                }
            }
            val range = max(10.0, maxVal - minVal)

            fun xCoord(step: Int): Float = (step.toFloat() / (numSteps - 1)) * width
            fun yCoord(v: Double): Float = height - ((v - minVal) / range).toFloat() * height

            // Draw grid lines
            val gridColor = Color(0xFF1E293B)
            for (g in 0..4) {
                val y = height * (g / 4f)
                drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
            }

            // Draw baseline (100.0)
            val baseLineY = yCoord(100.0)
            drawLine(
                color = Color(0xFF475569),
                start = Offset(0f, baseLineY),
                end = Offset(width, baseLineY),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            // Draw paths
            trajectories.forEachIndexed { idx, path ->
                val pathObj = Path()
                pathObj.moveTo(xCoord(0), yCoord(path[0]))
                for (step in 1 until path.size) {
                    pathObj.lineTo(xCoord(step), yCoord(path[step]))
                }

                val strokeColor = when (idx % 3) {
                    0 -> QuantumCyan.copy(alpha = 0.55f)
                    1 -> BenchmarkGold.copy(alpha = 0.45f)
                    else -> AlphaGreen.copy(alpha = 0.35f)
                }

                drawPath(
                    path = pathObj,
                    color = strokeColor,
                    style = Stroke(width = if (idx == 0) 2.5f else 1.2f, cap = StrokeCap.Round)
                )
            }
        }

        // Legend overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color(0xCC070B14), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).background(QuantumCyan, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(4.dp))
            Text("Simulated Paths", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp)
        }
    }
}

/**
 * Empirical Loss Distribution Histogram with VaR and CVaR vertical markers
 */
@Composable
fun LossHistogramChart(
    bins: List<Pair<Double, Int>>,
    var95: Double,
    var99: Double,
    cvar99: Double,
    modifier: Modifier = Modifier
) {
    if (bins.isEmpty()) return

    val maxCount = bins.maxOfOrNull { it.second } ?: 1

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(TerminalSurface, RoundedCornerShape(8.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .testTag("loss_histogram_chart")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val numBins = bins.size
            val barWidth = width / numBins

            val minLoss = bins.first().first
            val maxLoss = bins.last().first
            val lossRange = max(0.01, maxLoss - minLoss)

            fun lossToX(loss: Double): Float {
                val clamped = min(maxLoss, max(minLoss, loss))
                return ((clamped - minLoss) / lossRange).toFloat() * width
            }

            // Draw histogram bars
            bins.forEachIndexed { idx, pair ->
                val barHeight = (pair.second.toFloat() / maxCount) * (height * 0.85f)
                val x = idx * barWidth
                val y = height - barHeight

                val isExtremeTail = pair.first >= var95
                val barColor = if (isExtremeTail) RiskCrimson.copy(alpha = 0.8f) else QuantumCyan.copy(alpha = 0.6f)

                drawRect(
                    color = barColor,
                    topLeft = Offset(x + 1f, y),
                    size = Size(max(1f, barWidth - 2f), barHeight)
                )
            }

            // 95% VaR Marker
            val x95 = lossToX(var95)
            drawLine(
                color = BenchmarkGold,
                start = Offset(x95, 0f),
                end = Offset(x95, height),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )

            // 99% VaR Marker
            val x99 = lossToX(var99)
            drawLine(
                color = RiskCrimson,
                start = Offset(x99, 0f),
                end = Offset(x99, height),
                strokeWidth = 2.5f
            )
        }

        // Legends
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color(0xCC070B14), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(BenchmarkGold, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(3.dp))
                Text("95% VaR: ${(var95 * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(RiskCrimson, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(3.dp))
                Text("99% VaR: ${(var99 * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp)
            }
        }
    }
}

/**
 * Top QAOA Bitstring Probabilities Bar Chart
 */
@Composable
fun BitstringBarChart(
    samples: List<BitstringSample>,
    modifier: Modifier = Modifier
) {
    if (samples.isEmpty()) return
    val topSamples = samples.take(6)
    val maxProb = topSamples.maxOfOrNull { it.probability } ?: 1.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalSurface, RoundedCornerShape(8.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .testTag("bitstring_bar_chart"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        topSamples.forEachIndexed { idx, sample ->
            val ratio = if (maxProb > 0) (sample.probability / maxProb).toFloat() else 0f
            val isTop = idx == 0

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "|${sample.bitstring}⟩",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isTop) FontWeight.Bold else FontWeight.Normal,
                            color = if (isTop) QuantumCyan else TextPrimary,
                            fontSize = 12.sp
                        )
                        if (sample.isValidBudget) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "VALID",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AlphaGreen,
                                modifier = Modifier
                                    .background(AlphaGreen.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = String.format("%.2f%% (E=%.2f)", sample.probability * 100, sample.energy),
                        fontFamily = FontFamily.Monospace,
                        color = if (isTop) QuantumCyan else TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .height(8.dp)
                            .background(
                                if (isTop) QuantumCyan else if (sample.isValidBudget) BenchmarkGold else Color(0xFF64748B),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
    }
}

/**
 * QAOA Energy Minimization Convergence Chart
 */
@Composable
fun ConvergenceChart(
    trace: List<Pair<Int, Double>>,
    exactGroundEnergy: Double,
    modifier: Modifier = Modifier
) {
    if (trace.size < 2) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(TerminalSurface, RoundedCornerShape(8.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .testTag("convergence_chart")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            val minE = min(exactGroundEnergy, trace.minOfOrNull { it.second } ?: 0.0)
            val maxE = trace.maxOfOrNull { it.second } ?: 1.0
            val range = max(0.1, maxE - minE)

            val maxIter = trace.last().first.toFloat()

            fun xCoord(iter: Int): Float = (iter / max(1f, maxIter)) * width
            fun yCoord(e: Double): Float = height - ((e - minE) / range).toFloat() * (height * 0.8f) - (height * 0.1f)

            // Grid lines
            for (g in 0..3) {
                val y = height * (g / 3f)
                drawLine(Color(0xFF1E293B), Offset(0f, y), Offset(width, y), strokeWidth = 1f)
            }

            // Exact Eigensolver baseline (Dashed gold)
            val exactY = yCoord(exactGroundEnergy)
            drawLine(
                color = BenchmarkGold,
                start = Offset(0f, exactY),
                end = Offset(width, exactY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
            )

            // QAOA trace path
            val path = Path()
            path.moveTo(xCoord(trace[0].first), yCoord(trace[0].second))
            for (i in 1 until trace.size) {
                path.lineTo(xCoord(trace[i].first), yCoord(trace[i].second))
            }

            drawPath(
                path = path,
                color = QuantumCyan,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // Trace point dots
            for (pt in trace) {
                drawCircle(
                    color = QuantumCyan,
                    radius = 3f,
                    center = Offset(xCoord(pt.first), yCoord(pt.second))
                )
            }
        }

        // Overlay Legend
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color(0xCC070B14), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(QuantumCyan, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(3.dp))
                Text("QAOA Trace", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(BenchmarkGold, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(3.dp))
                Text("Exact Ground", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp)
            }
        }
    }
}

/**
 * Log-scale Chart comparing Classical Monte Carlo O(1/eps^2) vs Quantum Amplitude Estimation O(1/eps)
 */
@Composable
fun QaeScalingChart(
    points: List<QaePoint>,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(TerminalSurface, RoundedCornerShape(8.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .testTag("qae_scaling_chart")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // Max query count in log10
            val maxLog = log10(points.maxOf { it.classicalSamples.toDouble() } * 1.5)
            val minLog = log10(max(10.0, points.minOf { it.quantumQueries.toDouble() } * 0.7))
            val logRange = max(1.0, maxLog - minLog)

            val nPts = points.size

            fun xCoord(idx: Int): Float = (idx.toFloat() / (nPts - 1)) * width
            fun yCoord(samples: Long): Float {
                val logVal = log10(max(1.0, samples.toDouble()))
                return height - ((logVal - minLog) / logRange).toFloat() * height
            }

            // Grid lines
            for (g in 0..4) {
                val y = height * (g / 4f)
                drawLine(Color(0xFF1E293B), Offset(0f, y), Offset(width, y), strokeWidth = 1f)
            }

            // Classical MC path (O(1/eps^2))
            val mcPath = Path()
            mcPath.moveTo(xCoord(0), yCoord(points[0].classicalSamples))
            for (i in 1 until nPts) {
                mcPath.lineTo(xCoord(i), yCoord(points[i].classicalSamples))
            }
            drawPath(
                path = mcPath,
                color = BenchmarkGold,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // QAE path (O(1/eps))
            val qaePath = Path()
            qaePath.moveTo(xCoord(0), yCoord(points[0].quantumQueries))
            for (i in 1 until nPts) {
                qaePath.lineTo(xCoord(i), yCoord(points[i].quantumQueries))
            }
            drawPath(
                path = qaePath,
                color = QuantumCyan,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }

        // Legend
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color(0xCC070B14), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(BenchmarkGold, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(3.dp))
                Text("Classical MC: O(1/ε²)", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(QuantumCyan, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(3.dp))
                Text("Quantum QAE: O(1/ε)", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp)
            }
        }
    }
}
