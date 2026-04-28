package com.example.budgethero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.budgethero.data.session.SessionManager
import com.example.budgethero.ui.screens.AppNavigation
import com.example.budgethero.ui.screens.LoginScreen
import com.example.budgethero.ui.theme.BudgetHeroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)

        setContent {
            BudgetHeroTheme {
                var isLoggedIn by remember {
                    mutableStateOf(sessionManager.isLoggedIn())
                }

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
