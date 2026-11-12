package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM saved_portfolios ORDER BY timestamp DESC")
    fun getAllPortfolios(): Flow<List<PortfolioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(portfolio: PortfolioEntity): Long

    @Query("DELETE FROM saved_portfolios WHERE id = :id")
    suspend fun deletePortfolioById(id: Long)

    @Query("DELETE FROM saved_portfolios")
    suspend fun clearAll()
}
