package com.example.budgethero.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Helper class to hold category spending totals from grouped query
data class CategorySpending(
    val categoryId: Int,
    val total: Double
)

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getExpensesByUser(userId: Int): Flow<List<Expense>>

    // Filter expenses between two dates for the list view
    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getExpensesByDateRange(
        userId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<Expense>>

    // Sum expenses per category for the report screen
    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM expenses 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        GROUP BY categoryId
    """)
    suspend fun getSpendingByCategory(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<CategorySpending>
}
