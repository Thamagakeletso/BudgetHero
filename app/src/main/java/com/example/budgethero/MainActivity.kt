package com.example.budgethero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.budgethero.data.session.SessionManager
import com.example.budgethero.ui.screens.AppNavigation
import com.example.budgethero.ui.screens.LoginScreen
import com.example.budgethero.ui.theme.BudgetHeroTheme
import com.example.budgethero.ui.theme.LocalDarkMode

/**
 * Main entry point of the BudgetHero application.
 * Handles session management and dark mode state.
 * Reference: Android Activity documentation
 * (developer.android.com/reference/android/app/Activity)
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
// comments
        val sessionManager = SessionManager(this)

        setContent {
            // Dark mode state — shared across entire app
            val darkModeState = remember { mutableStateOf(false) }
            var isLoggedIn by remember {
                mutableStateOf(sessionManager.isLoggedIn())
            }

            // Provide dark mode state to all composables
            CompositionLocalProvider(LocalDarkMode provides darkModeState) {
                BudgetHeroTheme(darkTheme = darkModeState.value) {
                    if (isLoggedIn) {
                        AppNavigation(
                            onLogout = {
                                sessionManager.clearSession()
                                isLoggedIn = false
                            }
                        )
                    } else {
                        LoginScreen(
                            onLoginSuccess = {
                                isLoggedIn = true
                            }
                        )
                    }
                }
            }
        }
    }
}