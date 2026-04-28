package com.example.budgethero.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgethero.data.database.BudgetDatabase
import com.example.budgethero.data.database.Category
import com.example.budgethero.data.repository.BudgetRepository
import com.example.budgethero.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BudgetRepository(BudgetDatabase.getDatabase(application))
    private val sessionManager = SessionManager(application)
    private val userId = sessionManager.getUserId()

    // StateFlow that the UI observes for live category updates
    val categories: StateFlow<List<Category>> = repository
        .getCategories(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Error message state
    var errorMessage = androidx.compose.runtime.mutableStateOf("")

    fun addCategory(name: String) {
        // Validate input
        if (name.isBlank()) {
            errorMessage.value = "Category name cannot be empty"
            return
        }
        if (name.length < 2) {
            errorMessage.value = "Category name too short"
            return
        }
        // Check for duplicate
        if (categories.value.any { it.name.equals(name.trim(), ignoreCase = true) }) {
            errorMessage.value = "Category already exists"
            return
        }
        errorMessage.value = ""
        viewModelScope.launch {
            repository.addCategory(name.trim(), userId)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}

