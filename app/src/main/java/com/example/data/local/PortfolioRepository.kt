package com.example.data.local

import kotlinx.coroutines.flow.Flow

class PortfolioRepository(private val portfolioDao: PortfolioDao) {
    val allPortfolios: Flow<List<PortfolioEntity>> = portfolioDao.getAllPortfolios()

    suspend fun insertPortfolio(portfolio: PortfolioEntity): Long {
        return portfolioDao.insertPortfolio(portfolio)
    }

    suspend fun deletePortfolio(id: Long) {
        portfolioDao.deletePortfolioById(id)
    }

    suspend fun clearAll() {
        portfolioDao.clearAll()
    }
}
