package com.example.quantum

data class ExactResult(
    val exactGroundEnergy: Double,
    val exactOptimalBitstring: String,
    val exactOptimalIndex: Int,
    val exactSelectedIndices: List<Int>,
    val maximumEnergy: Double,
    val validStatesCount: Int
)

class ExactEigensolver(private val quboEngine: QuboEngine) {

    fun solve(): ExactResult {
        val n = quboEngine.n
        val totalStates = 1 shl n

        var minEnergy = Double.MAX_VALUE
        var maxEnergy = -Double.MAX_VALUE
        var bestState = 0
        var validCount = 0

        for (s in 0 until totalStates) {
            val energy = quboEngine.evaluateStateIndex(s)
            val bitCount = Integer.bitCount(s)
            val isValid = bitCount == quboEngine.params.budgetB

            if (isValid) {
                validCount++
            }

            if (energy < minEnergy) {
                minEnergy = energy
                bestState = s
            }
            if (energy > maxEnergy) {
                maxEnergy = energy
            }
        }

        val selected = mutableListOf<Int>()
        for (i in 0 until n) {
            if (((bestState shr (n - 1 - i)) and 1) == 1) {
                selected.add(i)
            }
        }

        return ExactResult(
            exactGroundEnergy = minEnergy,
            exactOptimalBitstring = quboEngine.stateToBitstring(bestState),
            exactOptimalIndex = bestState,
            exactSelectedIndices = selected,
            maximumEnergy = maxEnergy,
            validStatesCount = validCount
        )
    }
}
