package com.example.budgethero.data.session

import android.content.Context

/**
 * SessionManager uses SharedPreferences to persist login state.
 * When app reopens, we check here if a user is already logged in.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("budget_session", Context.MODE_PRIVATE)

    // Save user details after successful login
    fun saveSession(userId: Int, username: String) {
        prefs.edit()
            .putInt("user_id", userId)
            .putString("username", username)
            .putBoolean("is_logged_in", true)
            .apply()
    }

    // Clear session on logout
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun getUserId(): Int = prefs.getInt("user_id", -1)

    fun getUsername(): String = prefs.getString("username", "") ?: ""
}

