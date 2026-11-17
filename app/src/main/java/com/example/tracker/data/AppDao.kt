package com.example.tracker.data

import androidx.room3.*

@Dao
interface AppDao {
    @Insert
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>

    @Insert
    suspend fun insertExpense(expense: Expense)


    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY id DESC")
    suspend fun getExpensesForUser(userId: Int): List<Expense>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Int): Expense?

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)


    @Insert
    suspend fun registerUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun loginUser(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
}