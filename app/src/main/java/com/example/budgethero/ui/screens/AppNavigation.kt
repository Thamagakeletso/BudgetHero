package com.example.budgethero.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.budgethero.ui.theme.BrandGreen
import com.example.budgethero.ui.theme.BrandGreenLight

// Each screen in the bottom nav bar
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Expenses : Screen("expenses", "Expenses", Icons.Default.ReceiptLong)
    object AddExpense : Screen("add_expense", "Add", Icons.Default.Add)
    object Categories : Screen("categories", "Categories", Icons.Default.Category)
    object Goals : Screen("goals", "Goals", Icons.Default.TrackChanges)
    object Report : Screen("report", "Report", Icons.Default.BarChart)
}

private val bottomNavItems = listOf(
    Screen.Home,
    Screen.Expenses,
    Screen.AddExpense,
    Screen.Categories,
    Screen.Goals,
    Screen.Report
)

@Composable
fun AppNavigation(onLogout: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            // Make the Add button stand out
                            if (screen is Screen.AddExpense) {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.label,
                                    tint = BrandGreen
                                )
                            } else {
                                Icon(screen.icon, contentDescription = screen.label)
                            }
                        },
                        label = { Text(screen.label) },
                        selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = BrandGreenLight
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home screen
            composable(Screen.Home.route) {
                HomeScreen()
            }

            // Expense list with date filter
            composable(Screen.Expenses.route) {
                ExpenseListScreen()
            }

            // Add new expense
            composable(Screen.AddExpense.route) {
                AddExpenseScreen(
                    onExpenseSaved = {
                        navController.navigate(Screen.Expenses.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            // Manage categories
            composable(Screen.Categories.route) {
                CategoriesScreen()
            }

            // Set monthly goals
            composable(Screen.Goals.route) {
                GoalsScreen()
            }

            // Spending report by category
            composable(Screen.Report.route) {
                ReportScreen()
            }
        }
    }
}