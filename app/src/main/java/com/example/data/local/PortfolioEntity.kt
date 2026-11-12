package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_portfolios")
data class PortfolioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val selectedTickersJson: String, // comma-separated tickers
    val budgetB: Int,
    val riskAversionQ: Double,
    val expectedReturn: Double,
    val annualVolatility: Double,
    val sharpeRatio: Double,
    val var99: Double,
    val approximationRatio: Double
)
