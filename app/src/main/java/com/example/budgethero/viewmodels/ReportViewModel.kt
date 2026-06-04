package com.example.budgethero.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgethero.data.database.BudgetDatabase
import com.example.budgethero.data.database.Category
import com.example.budgethero.data.database.MonthlyGoal
import com.example.budgethero.data.repository.BudgetRepository
import com.example.budgethero.data.session.SessionManager
import com.example.budgethero.ui.screens.CategoryTotal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Report screen.
 * Handles spending report generation and category total calculations.
 * Follows MVVM pattern — screen never calls repository directly.
 */
class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BudgetRepository(
        BudgetDatabase.getDatabase(application)
    )
    private val sessionManager = SessionManager(application)
    private val userId = sessionManager.getUserId()

    private val _categoryTotals = MutableStateFlow<List<CategoryTotal>>(emptyList())
    val categoryTotals: StateFlow<List<CategoryTotal>> = _categoryTotals

    private val _totalSpent = MutableStateFlow(0.0)
    val totalSpent: StateFlow<Double> = _totalSpent

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched

    private val _currentGoal = MutableStateFlow<MonthlyGoal?>(null)
    val currentGoal: StateFlow<MonthlyGoal?> = _currentGoal

    private val _categories = MutableStateFlow<List<Category>>(emptyList())

    /**
     * Initializes the ViewModel by loading categories and current goal.
     */
    init {
        loadCategories()
        loadCurrentGoal()
    }

    /**
     * Loads all categories for the current user.
     */
    private fun loadCategories() {
        viewModelScope.launch {
            Log.d("ReportViewModel", "Loading categories for userId: $userId")
            repository.getCategories(userId).collect {
                _categories.value = it
            }
        }
    }

    /**
     * Loads the monthly goal for the current month.
     */
    private fun loadCurrentGoal() {
        viewModelScope.launch {
            val month = java.text.SimpleDateFormat(
                "yyyy-MM", java.util.Locale.getDefault()
            ).format(java.util.Date())
            Log.d("ReportViewModel", "Loading goal for month: $month")
            _currentGoal.value = repository.getMonthlyGoal(userId, month)
        }
    }

    /**
     * Generates a spending report for the given date range.
     * Groups expenses by category and calculates totals.
     * @param startDate Start date in "yyyy-MM-dd" format
     * @param endDate End date in "yyyy-MM-dd" format
     */
    fun generateReport(startDate: String, endDate: String) {
        if (startDate.isEmpty() || endDate.isEmpty()) return

        viewModelScope.launch {
            Log.d("ReportViewModel", "Generating report from $startDate to $endDate")
            _isLoading.value = true

            val spending = repository.getSpendingByCategory(
                userId, startDate, endDate
            )

            _categoryTotals.value = spending.map { cs ->
                val cat = _categories.value.find { it.id == cs.categoryId }
                CategoryTotal(
                    categoryName = cat?.name ?: "Unknown",
                    total = cs.total
                )
            }.sortedByDescending { it.total }

            _totalSpent.value = spending.sumOf { it.total }
            _isLoading.value = false
            _hasSearched.value = true

            Log.d("ReportViewModel", "Report generated: ${_categoryTotals.value.size} categories, total: R${_totalSpent.value}")
        }
    }
}