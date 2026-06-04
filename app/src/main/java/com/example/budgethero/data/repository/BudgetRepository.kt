package com.example.budgethero.data.repository

import android.util.Log
import com.example.budgethero.data.database.*
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

/**
 * Repository acts as the single source of truth for all data operations.
 * ViewModels interact only with this class — never directly with DAOs.
 * Implements the Repository pattern from Android Architecture Guidelines.
 * Reference: Android Architecture Guide (developer.android.com/topic/architecture)
 */
class BudgetRepository(private val db: BudgetDatabase) {

    // ── User ──────────────────────────────────────────────

    /**
     * Hashes a plain text password using SHA-256 algorithm.
     * Ensures passwords are never stored as plain text in the database.
     * Reference: Java Security (docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html)
     * @param password The plain text password to hash
     * @return Hexadecimal string representation of the SHA-256 hash
     */
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Registers a new user after checking username availability.
     * Password is hashed with SHA-256 before storage for security.
     * @param username The desired username
     * @param password The plain text password (will be hashed)
     * @return true if registration succeeded, false if username is taken
     */
    suspend fun register(username: String, password: String): Boolean {
        Log.d("BudgetRepository", "Register attempt for username: $username")
        val existing = db.userDao().getUserByUsername(username)
        if (existing != null) {
            Log.w("BudgetRepository", "Registration failed - username already exists: $username")
            return false
        }
        // Hash password before saving to database
        val hashedPassword = hashPassword(password)
        db.userDao().insertUser(
            User(username = username, password = hashedPassword)
        )
        Log.d("BudgetRepository", "Registration successful for: $username")
        return true
    }

    /**
     * Validates login credentials against hashed password in database.
     * @param username The username to check
     * @param password The plain text password (will be hashed for comparison)
     * @return User object if credentials match, null if login fails
     */
    suspend fun login(username: String, password: String): User? {
        Log.d("BudgetRepository", "Login attempt for username: $username")
        val hashedPassword = hashPassword(password)
        val user = db.userDao().login(username, hashedPassword)
        Log.d("BudgetRepository", "Login result: ${if (user != null) "SUCCESS" else "FAILED"}")
        return user
    }

    // ── Categories ────────────────────────────────────────

    /**
     * Returns a Flow of categories for the given user.
     * Flow automatically emits new values when data changes.
     * @param userId The ID of the logged-in user
     */
    fun getCategories(userId: Int): Flow<List<Category>> {
        Log.d("BudgetRepository", "Getting categories for userId: $userId")
        return db.categoryDao().getCategoriesByUser(userId)
    }

    /**
     * One-time fetch of categories (not a Flow).
     * Used when a snapshot is needed, e.g. for dropdowns.
     * @param userId The ID of the logged-in user
     */
    suspend fun getCategoriesOnce(userId: Int): List<Category> {
        Log.d("BudgetRepository", "Getting categories once for userId: $userId")
        return db.categoryDao().getCategoriesByUserOnce(userId)
    }

    /**
     * Inserts a new category into the database.
     * @param name The category name
     * @param userId The ID of the user creating the category
     */
    suspend fun addCategory(name: String, userId: Int) {
        Log.d("BudgetRepository", "Adding category: $name for userId: $userId")
        db.categoryDao().insertCategory(
            Category(name = name, userId = userId)
        )
    }

    /**
     * Deletes a category from the database.
     * @param category The Category object to delete
     */
    suspend fun deleteCategory(category: Category) {
        Log.d("BudgetRepository", "Deleting category: ${category.name}")
        db.categoryDao().deleteCategory(category)
    }

    // ── Expenses ──────────────────────────────────────────

    /**
     * Returns a Flow of all expenses for the given user.
     * Ordered by date descending (newest first).
     * @param userId The ID of the logged-in user
     */
    fun getExpenses(userId: Int): Flow<List<Expense>> {
        Log.d("BudgetRepository", "Getting all expenses for userId: $userId")
        return db.expenseDao().getExpensesByUser(userId)
    }

    /**
     * Returns a Flow of expenses filtered by date range.
     * Used for the expense list screen date filter feature.
     * @param userId The ID of the logged-in user
     * @param startDate Start date in "yyyy-MM-dd" format
     * @param endDate End date in "yyyy-MM-dd" format
     */
    fun getExpensesByDateRange(
        userId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<Expense>> {
        Log.d("BudgetRepository", "Getting expenses from $startDate to $endDate")
        return db.expenseDao().getExpensesByDateRange(userId, startDate, endDate)
    }

    /**
     * Inserts a new expense into the database.
     * @param expense The Expense object to insert
     */
    suspend fun addExpense(expense: Expense) {
        Log.d("BudgetRepository", "Adding expense: ${expense.description}, amount: ${expense.amount}")
        db.expenseDao().insertExpense(expense)
    }

    /**
     * Deletes an expense from the database.
     * @param expense The Expense object to delete
     */
    suspend fun deleteExpense(expense: Expense) {
        Log.d("BudgetRepository", "Deleting expense: ${expense.description}")
        db.expenseDao().deleteExpense(expense)
    }

    /**
     * Returns total spending grouped by category for a date range.
     * Used by the Report screen to show category breakdowns.
     * @param userId The ID of the logged-in user
     * @param startDate Start date in "yyyy-MM-dd" format
     * @param endDate End date in "yyyy-MM-dd" format
     */
    suspend fun getSpendingByCategory(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<CategorySpending> {
        Log.d("BudgetRepository", "Getting spending by category from $startDate to $endDate")
        return db.expenseDao().getSpendingByCategory(userId, startDate, endDate)
    }

    // ── Monthly Goals ─────────────────────────────────────

    /**
     * Saves or updates the monthly spending goal.
     * Uses REPLACE conflict strategy so existing goals are overwritten.
     * @param userId The ID of the logged-in user
     * @param month The month in "yyyy-MM" format
     * @param min The minimum spending goal in Rands
     * @param max The maximum spending goal in Rands
     */
    suspend fun setMonthlyGoal(
        userId: Int,
        month: String,
        min: Double,
        max: Double
    ) {
        Log.d("BudgetRepository", "Setting goal for $month: min=R$min, max=R$max")
        db.monthlyGoalDao().setGoal(
            MonthlyGoal(
                userId = userId,
                month = month,
                minGoal = min,
                maxGoal = max
            )
        )
    }

    /**
     * Retrieves the monthly goal for a specific month.
     * Returns null if no goal has been set for that month.
     * @param userId The ID of the logged-in user
     * @param month The month in "yyyy-MM" format
     */
    suspend fun getMonthlyGoal(userId: Int, month: String): MonthlyGoal? {
        Log.d("BudgetRepository", "Getting goal for userId: $userId, month: $month")
        return db.monthlyGoalDao().getGoalForMonth(userId, month)
    }

    /**
     * Returns a Flow of all monthly goals for a user.
     * Ordered by month descending (most recent first).
     * @param userId The ID of the logged-in user
     */
    fun getAllGoals(userId: Int): Flow<List<MonthlyGoal>> {
        Log.d("BudgetRepository", "Getting all goals for userId: $userId")
        return db.monthlyGoalDao().getAllGoals(userId)
    }
}