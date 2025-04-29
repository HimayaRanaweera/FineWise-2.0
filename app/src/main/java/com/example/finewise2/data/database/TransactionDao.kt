package com.example.finewise2.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.finewise2.data.model.Transaction
import com.example.finewise2.data.model.TransactionType
import java.util.Date

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    fun getTotalAmountByTypeAndDateRange(type: TransactionType, startDate: Date, endDate: Date): LiveData<Double>

    @Query("SELECT SUM(amount) FROM transactions WHERE category = :category AND date BETWEEN :startDate AND :endDate")
    fun getTotalAmountByCategoryAndDateRange(category: String, startDate: Date, endDate: Date): LiveData<Double>
} 