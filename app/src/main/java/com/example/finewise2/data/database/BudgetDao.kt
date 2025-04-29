package com.example.finewise2.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.finewise2.data.model.Budget
import java.util.Date

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY startDate DESC")
    fun getAllBudgets(): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE category = :category AND startDate <= :date AND endDate >= :date")
    fun getCurrentBudgetByCategory(category: String?, date: Date): LiveData<Budget?>

    @Query("SELECT * FROM budgets WHERE startDate <= :date AND endDate >= :date")
    fun getCurrentBudgets(date: Date): LiveData<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getBudgetById(id: Long): LiveData<Budget?>
} 