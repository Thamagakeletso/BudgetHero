package com.example.budgethero.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgethero.data.database.BudgetDatabase
import com.example.budgethero.data.database.Category
import com.example.budgethero.data.database.Expense
import com.example.budgethero.data.repository.BudgetRepository
import com.example.budgethero.data.session.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for expense-related screens.
 * Handles expense CRUD operations and date range filtering.
 * Follows MVVM pattern - screens never call repository directly.
 * Reference: Android ViewModel
 * (developer.android.com/topic/libraries/architecture/viewmodel)
 */
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BudgetRepository(
        BudgetDatabase.getDatabase(application)
    )
    private val sessionManager = SessionManager(application)
    val userId = sessionManager.getUserId()

    /**
     * All expenses for current user as live StateFlow.
     * Automatically updates UI when database changes.
     */
    val expenses: StateFlow<List<Expense>> = repository
        .getExpenses(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Categories for current user as live StateFlow.
     * Used for dropdown selection in AddExpenseScreen.
     */
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    /**
     * Date range filter state.
     * Empty strings mean no filter applied — show all expenses.
     */
    private val _startDate = MutableStateFlow("")
    private val _endDate = MutableStateFlow("")

    /**
     * Filtered expenses based on selected date range.
     * Uses flatMapLatest to cancel previous collectors — prevents memory leaks.
     * Reference: Kotlin Flow (kotlinlang.org/docs/flow.html)
     */
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        _startDate, _endDate
    ) { start, end ->
        Pair(start, end)
    }.flatMapLatest { (start, end) ->
        if (start.isEmpty() || end.isEmpty()) {
            repository.getExpenses(userId)
        } else {
            repository.getExpensesByDateRange(userId, start, end)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Save result feedback for AddExpenseScreen
    val saveResult = MutableStateFlow<String?>(null)

    init {
        loadCategories()
    }

    /**
     * Loads categories for the current user.
     * Used to populate category dropdown in AddExpenseScreen.
     */
    private fun loadCategories() {
        viewModelScope.launch {
            Log.d("ExpenseViewModel", "Loading categories for userId: $userId")
            repository.getCategories(userId).collect {
                _categories.value = it
                Log.d("ExpenseViewModel", "Loaded ${it.size} categories")
            }
        }
    }

    /**
     * Updates the date range filter.
     * Triggers automatic recomposition via StateFlow.
     * @param startDate Start date in "yyyy-MM-dd" format
     * @param endDate End date in "yyyy-MM-dd" format
     */
    fun filterByDateRange(startDate: String, endDate: String) {
        Log.d("ExpenseViewModel",
            "Filtering expenses from $startDate to $endDate")
        _startDate.value = startDate
        _endDate.value = endDate
    }

    /**
     * Clears the date range filter to show all expenses.
     */
    fun clearFilter() {
        Log.d("ExpenseViewModel", "Clearing date filter")
        _startDate.value = ""
        _endDate.value = ""
    }

    /**
     * Validates and saves a new expense to the database.
     * All validation happens here — not in the UI layer.
     */
    fun addExpense(
        categoryId: Int,
        description: String,
        amount: String,
        date: String,
        startTime: String,
        endTime: String,
        photoPath: String?
    ) {
        Log.d("ExpenseViewModel",
            "Adding expense: $description, amount: $amount")

        // Input validation
        when {
            description.isBlank() -> {
                saveResult.value = "Please enter a description"
                return
            }
            amount.isBlank() -> {
                saveResult.value = "Please enter an amount"
                return
            }
            amount.toDoubleOrNull() == null -> {
                saveResult.value = "Please enter a valid amount"
                return
            }
            amount.toDouble() <= 0 -> {
                saveResult.value = "Amount must be greater than zero"
                return
            }
            date.isBlank() -> {
                saveResult.value = "Please select a date"
                return
            }
            startTime.isBlank() || endTime.isBlank() -> {
                saveResult.value = "Please enter start and end times"
                return
            }
            categoryId == -1 -> {
                saveResult.value = "Please select a category"
                return
            }
        }

        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    userId = userId,
                    categoryId = categoryId,
                    description = description.trim(),
                    amount = amount.toDouble(),
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    photoPath = photoPath
                )
            )
            Log.d("ExpenseViewModel", "Expense saved successfully")
            saveResult.value = "SUCCESS"
        }
    }

    /**
     * Clears the save result after it has been handled by the UI.
     */
    fun clearSaveResult() {
        saveResult.value = null
    }
}