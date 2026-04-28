package com.example.budgethero.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgethero.data.database.BudgetDatabase
import com.example.budgethero.data.database.Category
import com.example.budgethero.data.database.Expense
import com.example.budgethero.data.repository.BudgetRepository
import com.example.budgethero.data.session.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BudgetRepository(BudgetDatabase.getDatabase(application))
    private val sessionManager = SessionManager(application)
    val userId = sessionManager.getUserId()

    // All expenses as live flow
    val expenses: StateFlow<List<Expense>> = repository
        .getExpenses(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Date range filtered expenses
    private val _filteredExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val filteredExpenses: StateFlow<List<Expense>> = _filteredExpenses

    // Categories for dropdown
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    // Save result feedback
    val saveResult = MutableStateFlow<String?>(null)

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories(userId).collect {
                _categories.value = it
            }
        }
    }

    fun addExpense(
        categoryId: Int,
        description: String,
        amount: String,
        date: String,
        startTime: String,
        endTime: String,
        photoPath: String?
    ) {
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
            saveResult.value = "SUCCESS"
        }
    }

    fun filterByDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            repository.getExpensesByDateRange(userId, startDate, endDate)
                .collect { _filteredExpenses.value = it }
        }
    }

    fun clearSaveResult() {
        saveResult.value = null
    }
}

