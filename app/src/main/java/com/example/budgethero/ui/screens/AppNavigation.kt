package com.example.budgethero.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.budgethero.ui.theme.BrandGreenLight
import com.example.budgethero.ui.theme.LocalDarkMode

/**
 * Sealed class defining all navigation routes in the app.
 * Using sealed class ensures type safety for navigation.
 * Reference: Navigation Compose
 * (developer.android.com/jetpack/compose/navigation)
 */
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Expenses : Screen("expenses", "Expenses", Icons.Default.Receipt)
    object AddExpense : Screen("add_expense", "Add", Icons.Default.Add)
    object Categories : Screen("categories", "Category", Icons.Default.Category)
    object Goals : Screen("goals", "Goals", Icons.Default.TrackChanges)
    object Report : Screen("report", "Report", Icons.Default.BarChart)
    object Achievements : Screen("achievements", "Badges", Icons.Default.EmojiEvents)
    object Statistics : Screen("statistics", "Stats", Icons.Default.PieChart)
}

// 5 items in bottom nav - optimized for Pixel 4 screen size
private val bottomNavItems = listOf(
    Screen.Home,
    Screen.Expenses,
    Screen.AddExpense,
    Screen.Categories,
    Screen.Goals
)

/**
 * Main navigation component with bottom navigation bar.
 * Handles routing between all screens in the app.
 * @param onLogout Callback to handle user logout
 */
@Composable
fun AppNavigation(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current

    // Debug: Check screen width on Pixel 4
    LaunchedEffect(Unit) {
        Log.d("AppNavigation", "Screen width DP: ${configuration.screenWidthDp}")
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier,
                tonalElevation = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                screen.label,
                                fontSize = 9.sp,  // Optimized for Pixel 4
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(
                                    navController.graph
                                        .findStartDestination().id
                                ) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = BrandGreenLight,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        alwaysShowLabel = true  // Ensures labels are always visible
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
            composable(Screen.Home.route) {
                HomeScreen(
                    onLogout = onLogout,
                    onNavigateToReport = {
                        navController.navigate(Screen.Report.route)
                    },
                    onNavigateToStats = {
                        navController.navigate(Screen.Statistics.route)
                    },
                    onNavigateToBadges = {
                        navController.navigate(Screen.Achievements.route)
                    },
                    onNavigateToAdd = {
                        navController.navigate(Screen.AddExpense.route)
                    },
                    onNavigateToGoals = {
                        navController.navigate(Screen.Goals.route)
                    }
                )
            }
            composable(Screen.Expenses.route) {
                ExpenseListScreen()
            }
            composable(Screen.AddExpense.route) {
                AddExpenseScreen(
                    onExpenseSaved = {
                        navController.navigate(Screen.Expenses.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
            composable(Screen.Categories.route) {
                CategoriesScreen()
            }
            composable(Screen.Goals.route) {
                GoalsScreen()
            }
            composable(Screen.Report.route) {
                ReportScreen()
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }
            composable(Screen.Achievements.route) {
                AchievementsScreen()
            }
        }
    }
}