package com.example.finewise2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.finewise2.data.database.AppDatabase
import com.example.finewise2.data.model.Transaction
import com.example.finewise2.data.model.TransactionType
import com.example.finewise2.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    val allTransactions: LiveData<List<Transaction>>

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        allTransactions = repository.allTransactions
    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    /*fun getTransactionsByType(type: TransactionType) = repository.getTransactionsByType(type)
    fun getTransactionsByCategory(category: String) = repository.getTransactionsByCategory(category)
    fun getTransactionsBetweenDates(start: Date, end: Date) = repository.getTransactionsBetweenDates(start, end)*/
}