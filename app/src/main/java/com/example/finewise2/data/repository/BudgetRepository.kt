package com.example.finewise2.data.repository

/*import androidx.lifecycle.LiveData
import com.example.finewise2.data.database.BudgetDao
import com.example.finewise2.data.model.Budget
import java.util.Date

class BudgetRepository(private val budgetDao: BudgetDao) {

    val allBudgets: LiveData<List<Budget>> = budgetDao.getAllBudgets()

    fun getCurrentBudgetByCategory(category: String?, date: Date): LiveData<Budget?> {
        return budgetDao.getCurrentBudgetByCategory(category, date)
    }

    fun getCurrentBudgets(date: Date): LiveData<List<Budget>> {
        return budgetDao.getCurrentBudgets(date)
    }

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    fun getBudgetById(id: Long): LiveData<Budget?> {
        return budgetDao.getBudgetById(id)
    }
} */