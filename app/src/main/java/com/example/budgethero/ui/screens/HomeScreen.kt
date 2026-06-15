package com.example.budgethero.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgethero.data.database.Category
import com.example.budgethero.data.database.Expense
import com.example.budgethero.ui.theme.*
import com.example.budgethero.ui.viewmodels.CategoryViewModel
import com.example.budgethero.ui.viewmodels.ExpenseViewModel

/**
 * HomeScreen displays the user's financial summary.
 * Shows real balance from RoomDB, budget progress, and recent expenses.
 * Reference: Android Jetpack Compose
 * (developer.android.com/jetpack/compose)
 */
@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToBadges: () -> Unit = {},
    onNavigateToAdd: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    expenseViewModel: ExpenseViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
){
    // Collect real data from RoomDB
    val expenses by expenseViewModel.expenses.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    // Calculate real balance from database
    val totalIncome = expenses.filter { it.amount > 0 }.sumOf { it.amount }
    val totalExpenses = expenses
        .filter { it.amount < 0 }
        .sumOf { Math.abs(it.amount) }
    val totalBalance = totalIncome - totalExpenses

    // Monthly spending
    val currentMonth = java.text.SimpleDateFormat(
        "yyyy-MM", java.util.Locale.getDefault()
    ).format(java.util.Date())

    val monthlySpent = expenses.filter { expense ->
        expense.date.startsWith(currentMonth) && expense.amount < 0
    }.sumOf { Math.abs(it.amount) }

    val recentExpenses = expenses.take(5)

    Log.d("HomeScreen",
        "Loaded ${expenses.size} expenses, balance: R$totalBalance")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item { HomeTopBar(onLogout = onLogout) }

        item { BalanceCard(balance = totalBalance) }

        item {
            BudgetProgressCard(
                spent = monthlySpent,
                total = if (monthlySpent > 0) monthlySpent * 1.5
                else 3500.0
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            ActionButtonsRow(
                onAddExpense = onNavigateToAdd,
                onViewGoals = onNavigateToGoals,
                onViewReport = onNavigateToReport
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Quick access cards row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAccessCard(
                    title = "Report",
                    icon = Icons.Default.BarChart,
                    onClick = onNavigateToReport,
                    modifier = Modifier.weight(1f)
                )
                QuickAccessCard(
                    title = "Stats",
                    icon = Icons.Default.PieChart,
                    onClick = onNavigateToStats,
                    modifier = Modifier.weight(1f)
                )
                QuickAccessCard(
                    title = "Badges",
                    icon = Icons.Default.EmojiEvents,
                    onClick = onNavigateToBadges,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item { RecentTransactionsHeader() }

        if (recentExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            null,
                            tint = BrandGreenMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No expenses yet",
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.6f)
                        )
                        Text(
                            "Tap Add to log your first expense",
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(recentExpenses, key = { it.id }) { expense ->
                val category = categories.find { it.id == expense.categoryId }
                RealTransactionRow(
                    expense = expense,
                    category = category
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

/**
 * Quick access card for navigating to Report, Stats and Badges.
 */
@Composable
fun QuickAccessCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandGreenLight
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = BrandGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                title,
                fontSize = 11.sp,
                color = BrandGreenDark,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Top bar with BudgetHero logo, dark mode toggle and logout button.
 * Dark mode uses MaterialTheme.colorScheme for proper theming.
 */
@Composable
fun HomeTopBar(onLogout: () -> Unit = {}) {
    // Get dark mode state from CompositionLocal
    val darkModeState = LocalDarkMode.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Logo",
                    tint = BrandGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BudgetHero",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Dark mode toggle button
                IconButton(onClick = {
                    darkModeState.value = !darkModeState.value
                    Log.d("HomeScreen",
                        "Dark mode: ${darkModeState.value}")
                }) {
                    Icon(
                        imageVector = if (darkModeState.value)
                            Icons.Default.LightMode
                        else
                            Icons.Default.DarkMode,
                        contentDescription = "Toggle dark mode",
                        tint = if (darkModeState.value)
                            Color.Yellow
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                // Logout button
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Balance card showing total balance calculated from RoomDB.
 * Uses MaterialTheme colors for dark mode support.
 */
@Composable
fun BalanceCard(balance: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 24.dp)
        ) {
            Text(
                text = "Total Balance",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "R${"%.2f".format(balance)}",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Budget progress card showing monthly spending vs budget.
 * Adapts to dark mode using MaterialTheme colors.
 */
@Composable
fun BudgetProgressCard(spent: Double, total: Double) {
    val progress = if (total > 0)
        (spent / total).toFloat().coerceIn(0f, 1f)
    else 0f
    val percent = (progress * 100).toInt()
    val remaining = total - spent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MONTHLY BUDGET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "R${"%.2f".format(spent)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " / R${"%.0f".format(total)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                            .copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "$percent%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BrandGreen
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = BrandGreen,
                trackColor = BrandGreenLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You have R${"%.0f".format(remaining)} remaining.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Action buttons row — Add Expense, Transfer, Set Budget.
 */
@Composable
fun ActionButtonsRow(
    onAddExpense: () -> Unit = {},
    onViewGoals: () -> Unit = {},
    onViewReport: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Add Expense — navigates to Add screen
        Button(
            onClick = onAddExpense,
            modifier = Modifier
                .weight(1f)
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandGreen
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "ADD EXPENSE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Goals button — navigates to Goals screen
        OutlinedButton(
            onClick = onViewGoals,
            modifier = Modifier
                .weight(1f)
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.TrackChanges,
                    contentDescription = "Goals",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "GOALS",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Report button — navigates to Report screen
        OutlinedButton(
            onClick = onViewReport,
            modifier = Modifier
                .weight(1f)
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "Report",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "REPORT",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Composable
fun ActionButton(
    label: String,
    icon: ImageVector,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPrimary) {
        Button(
            onClick = {},
            modifier = modifier.height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandGreen
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    icon,
                    contentDescription = label,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        OutlinedButton(
            onClick = {},
            modifier = modifier.height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    icon,
                    contentDescription = label,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RecentTransactionsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recent Transactions",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = {}) {
            Text("View All", color = BrandGreen, fontSize = 13.sp)
        }
    }
}

/**
 * Single transaction row showing real expense data from RoomDB.
 * Uses MaterialTheme colors for dark mode compatibility.
 */
@Composable
fun RealTransactionRow(expense: Expense, category: Category?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BrandGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category?.name?.first()?.uppercase() ?: "?",
                color = BrandGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.description,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${category?.name ?: "Unknown"} • ${expense.date}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        val isIncome = expense.amount > 0
        Text(
            text = if (isIncome)
                "+R${"%.2f".format(expense.amount)}"
            else
                "-R${"%.2f".format(Math.abs(expense.amount))}",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (isIncome) IncomeGreen else ExpenseRed
        )
    }
}