package com.example.finewise2.data.repository

import androidx.lifecycle.LiveData
import com.example.finewise2.data.database.TransactionDao
import com.example.finewise2.data.model.Transaction
import com.example.finewise2.data.model.TransactionType
import java.util.Date

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category)
    }

    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    fun getTotalAmountByTypeAndDateRange(
        type: TransactionType,
        startDate: Date,
        endDate: Date
    ): LiveData<Double> {
        return transactionDao.getTotalAmountByTypeAndDateRange(type, startDate, endDate)
    }

    fun getTotalAmountByCategoryAndDateRange(
        category: String,
        startDate: Date,
        endDate: Date
    ): LiveData<Double> {
        return transactionDao.getTotalAmountByCategoryAndDateRange(category, startDate, endDate)
    }
}