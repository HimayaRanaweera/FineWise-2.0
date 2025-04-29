package com.example.finewise2.data.repository
/*
import androidx.lifecycle.LiveData
import com.example.finewise2.data.database.CategoryDao
import com.example.finewise2.data.model.Category
import com.example.finewise2.data.model.TransactionType

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: TransactionType): LiveData<List<Category>> {
        return categoryDao.getCategoriesByType(type)
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)
    }
} */