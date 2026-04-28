package com.example.budgethero.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgethero.data.database.BudgetDatabase
import com.example.budgethero.data.repository.BudgetRepository
import com.example.budgethero.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Represents every possible state of the login/register screen
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BudgetRepository(
        BudgetDatabase.getDatabase(application)
    )
    val sessionManager = SessionManager(application)

    // StateFlow emits new values to the UI automatically
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(username: String, password: String) {
        // Validate inputs before hitting the database
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = repository.login(username.trim(), password.trim())
            if (user != null) {
                sessionManager.saveSession(user.id, user.username)
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Invalid username or password")
            }
        }
    }

    fun register(username: String, password: String, confirmPassword: String) {
        // Input validation
        when {
            username.isBlank() || password.isBlank() -> {
                _authState.value = AuthState.Error("Please fill in all fields")
                return
            }
            username.length < 3 -> {
                _authState.value = AuthState.Error("Username must be at least 3 characters")
                return
            }
            password.length < 4 -> {
                _authState.value = AuthState.Error("Password must be at least 4 characters")
                return
            }
            password != confirmPassword -> {
                _authState.value = AuthState.Error("Passwords do not match")
                return
            }
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val success = repository.register(username.trim(), password.trim())
            if (success) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Username already exists")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

