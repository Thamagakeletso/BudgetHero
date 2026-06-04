package com.example.budgethero.ui.screens

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
import com.example.budgethero.data.database.Expense
import com.example.budgethero.data.database.Category
import com.example.budgethero.data.models.TransactionCategory
import com.example.budgethero.ui.theme.*
import com.example.budgethero.ui.viewmodels.ExpenseViewModel
import com.example.budgethero.ui.viewmodels.CategoryViewModel
import android.util.Log

/**
 * HomeScreen displays the user's financial summary.
 * Shows real balance from RoomDB, budget progress, and recent expenses.
 * Reference: Android Jetpack Compose documentation (developer.android.com/jetpack/compose)
 */
@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    expenseViewModel: ExpenseViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    // Collect real data from database
    val expenses by expenseViewModel.expenses.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    // Calculate real totals from database
    val totalIncome = expenses.filter { it.amount > 0 }.sumOf { it.amount }
    val totalExpenses = expenses.filter { it.amount < 0 }.sumOf { Math.abs(it.amount) }
    val totalBalance = totalIncome - totalExpenses

    // Monthly spending for progress bar
    val currentMonth = java.text.SimpleDateFormat(
        "yyyy-MM", java.util.Locale.getDefault()
    ).format(java.util.Date())

    val monthlyExpenses = expenses.filter { expense ->
        expense.date.startsWith(currentMonth) && expense.amount < 0
    }.sumOf { Math.abs(it.amount) }

    // Show only last 5 expenses on home screen
    val recentExpenses = expenses.take(5)

    // Log screen load for debugging
    Log.d("HomeScreen", "Loaded ${expenses.size} expenses, balance: R$totalBalance")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item { HomeTopBar(onLogout = onLogout) }

        item {
            BalanceCard(balance = totalBalance)
        }

        item {
            BudgetProgressCard(
                spent = monthlyExpenses,
                total = if (monthlyExpenses > 0) monthlyExpenses * 1.5 else 3500.0
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { ActionButtonsRow() }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { RecentTransactionsHeader() }

        if (recentExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
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
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            "Tap Add to log your first expense",
                            color = TextSecondary,
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
                    color = CardBorder,
                    thickness = 0.5.dp
                )
            }
        }
    }
}

/**
 * Top bar with BudgetHero logo and logout button.
 */
@Composable
fun HomeTopBar(onLogout: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
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
                color = TextPrimary
            )
        }
        Row {
            BadgedBox(badge = { Badge() }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = TextSecondary
                )
            }
        }
    }
}

/**
 * Balance card showing total balance in Rands.
 * Balance is calculated from real RoomDB expense data.
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
            // ✅ Fixed: uses R instead of $
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
 * Progress card showing monthly spending vs budget.
 * Uses real expense data from RoomDB.
 */
@Composable
fun BudgetProgressCard(spent: Double, total: Double) {
    val progress = if (total > 0) (spent / total).toFloat().coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()
    val remaining = total - spent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MONTHLY BUDGET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    // ✅ Fixed: R instead of $
                    Text(
                        text = "R${"%.2f".format(spent)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = " / R${"%.0f".format(total)}",
                        fontSize = 14.sp,
                        color = TextSecondary
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
            // ✅ Fixed: R instead of $
            Text(
                text = "You have R${"%.0f".format(remaining)} remaining for this month.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

/**
 * Action buttons row — Add Expense, Transfer, Set Budget.
 */
@Composable
fun ActionButtonsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionButton("ADD EXPENSE", Icons.Default.Add, true, Modifier.weight(1f))
        ActionButton("TRANSFER", Icons.Default.SwapHoriz, false, Modifier.weight(1f))
        ActionButton("SET BUDGET", Icons.Default.Settings, false, Modifier.weight(1f))
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
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        OutlinedButton(
            onClick = {},
            modifier = modifier.height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
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
            color = TextPrimary
        )
        TextButton(onClick = {}) {
            Text("View All", color = BrandGreen, fontSize = 13.sp)
        }
    }
}

/**
 * Single transaction row showing real expense data from RoomDB.
 * Displays category name, amount in Rands, and date.
 */
@Composable
fun RealTransactionRow(expense: Expense, category: Category?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon circle
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
                color = TextPrimary
            )
            Text(
                text = "${category?.name ?: "Unknown"} • ${expense.date}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // ✅ Fixed: R instead of $
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