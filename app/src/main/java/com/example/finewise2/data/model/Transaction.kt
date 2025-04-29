package com.example.finewise2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val type: TransactionType,
    val description: String? = null
)

enum class TransactionType {
    INCOME,
    EXPENSE
} 