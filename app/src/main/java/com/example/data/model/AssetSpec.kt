package com.example.data.model

data class AssetSpec(
    val ticker: String,
    val name: String,
    val sector: String,
    val baseAnnualReturn: Double, // e.g. 0.22 for 22%
    val annualVolatility: Double, // e.g. 0.28 for 28%
    val sentimentDrift: Double = 0.0, // AI/macro news drift in [-0.05, +0.05]
    val studentTDegreesOfFreedom: Double = 4.5, // nu for fat tails (empirical leptokurtosis)
    val isSelected: Boolean = true
) {
    val effectiveExpectedReturn: Double
        get() = baseAnnualReturn + sentimentDrift
}
