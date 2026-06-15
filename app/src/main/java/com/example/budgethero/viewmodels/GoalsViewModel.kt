package com.example.budgethero.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgethero.data.database.BudgetDatabase
import com.example.budgethero.data.database.MonthlyGoal
import com.example.budgethero.data.repository.BudgetRepository
import com.example.budgethero.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Goals screen.
 * Handles all monthly goal operations through the repository.
 * Follows MVVM pattern - screen never calls repository directly.
 * Reference: Android ViewModel documentation
 * (developer.android.com/topic/libraries/architecture/viewmodel)
 */
class GoalsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BudgetRepository(
        BudgetDatabase.getDatabase(application)
    )
    private val sessionManager = SessionManager(application)
    private val userId = sessionManager.getUserId()

    private val _currentGoal = MutableStateFlow<MonthlyGoal?>(null)
    val currentGoal: StateFlow<MonthlyGoal?> = _currentGoal

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage

    /**
     * Loads the goal for the specified month from the database.
     * @param month The month in "yyyy-MM" format
     */
    fun loadGoal(month: String) {
        viewModelScope.launch {
            Log.d("GoalsViewModel", "Loading goal for month: $month")
            _currentGoal.value = repository.getMonthlyGoal(userId, month)
            Log.d("GoalsViewModel", "Goal loaded: ${_currentGoal.value}")
        }
    }

    /**
     * Saves or updates the monthly spending goal.
     * Validates that min is less than max before saving.
     * @param month The month in "yyyy-MM" format
     * @param minGoal The minimum spending goal as string
     * @param maxGoal The maximum spending goal as string
     */
    fun saveGoal(month: String, minGoal: String, maxGoal: String) {
        val min = minGoal.toDoubleOrNull()
        val max = maxGoal.toDoubleOrNull()

        // Input validation
        when {
            minGoal.isBlank() || maxGoal.isBlank() -> {
                _saveMessage.value = "Please fill in both fields"
                return
            }
            min == null || max == null -> {
                _saveMessage.value = "Please enter valid numbers"
                return
            }
            min < 0 || max < 0 -> {
                _saveMessage.value = "Goals cannot be negative"
                return
            }
            min >= max -> {
                _saveMessage.value = "Minimum must be less than maximum"
                return
            }
        }

        viewModelScope.launch {
            Log.d("GoalsViewModel", "Saving goal: min=$min, max=$max for month=$month")
            repository.setMonthlyGoal(userId, month, min!!, max!!)
            _currentGoal.value = repository.getMonthlyGoal(userId, month)
            _saveMessage.value = "SUCCESS"
            Log.d("GoalsViewModel", "Goal saved successfully")
        }
    }

    /**
     * Clears the save message after it has been displayed to the user.
     */
    fun clearMessage() {
        _saveMessage.value = null
    }
}