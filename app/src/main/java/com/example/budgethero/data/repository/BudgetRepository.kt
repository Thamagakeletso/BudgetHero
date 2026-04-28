package com.example.budgethero.data.repository

import com.example.budgethero.data.database.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository acts as the single source of truth for all data.
 * ViewModels never talk directly to DAOs — they go through here.
 */
class BudgetRepository(private val db: BudgetDatabase) {

    // ── User ──────────────────────────────────────────────

    // Returns false if username is already taken
    suspend fun register(username: String, password: String): Boolean {
        val existing = db.userDao().getUserByUsername(username)
        if (existing != null) return false
        db.userDao().insertUser(User(username = username, password = password))
        return true
    }

    // Returns User object if login succeeds, null if it fails
    suspend fun login(username: String, password: String): User? {
        return db.userDao().login(username, password)
    }

    // ── Categories ────────────────────────────────────────

    fun getCategories(userId: Int): Flow<List<Category>> =
        db.categoryDao().getCategoriesByUser(userId)

    suspend fun getCategoriesOnce(userId: Int): List<Category> =
        db.categoryDao().getCategoriesByUserOnce(userId)

    suspend fun addCategory(name: String, userId: Int) {
        db.categoryDao().insertCategory(
            Category(name = name, userId = userId)
        )
    }

    suspend fun deleteCategory(category: Category) {
        db.categoryDao().deleteCategory(category)
    }

    // ── Expenses ──────────────────────────────────────────

    fun getExpenses(userId: Int): Flow<List<Expense>> =
        db.expenseDao().getExpensesByUser(userId)

    fun getExpensesByDateRange(
        userId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<Expense>> =
        db.expenseDao().getExpensesByDateRange(userId, startDate, endDate)

    suspend fun addExpense(expense: Expense) {
        db.expenseDao().insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        db.expenseDao().deleteExpense(expense)
    }

    suspend fun getSpendingByCategory(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<CategorySpending> =
        db.expenseDao().getSpendingByCategory(userId, startDate, endDate)

    // ── Monthly Goals ─────────────────────────────────────

    suspend fun setMonthlyGoal(
        userId: Int,
        month: String,
        min: Double,
        max: Double
    ) {
        db.monthlyGoalDao().setGoal(
            MonthlyGoal(
                userId = userId,
                month = month,
                minGoal = min,
                maxGoal = max
            )
        )
    }

    suspend fun getMonthlyGoal(userId: Int, month: String) =
        db.monthlyGoalDao().getGoalForMonth(userId, month)

    fun getAllGoals(userId: Int): Flow<List<MonthlyGoal>> =
        db.monthlyGoalDao().getAllGoals(userId)
}

