package com.example.finewise2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val startDate: Date,
    val endDate: Date,
    val category: String? = null, // If null, it's a total budget
    val warningThreshold: Double = 0.8 // 80% of budget by default
) 