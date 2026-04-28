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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgethero.data.models.Transaction
import com.example.budgethero.data.models.TransactionCategory
import com.example.budgethero.ui.theme.*

// ── Sample Data ───────────────────────────────────────────
private val sampleTransactions = listOf(
    Transaction(
        id = "1",
        title = "Whole Foods Market",
        subtitle = "Groceries • Today, 10:45 AM",
        amount = -84.20,
        category = TransactionCategory.FOOD
    ),
    Transaction(
        id = "2",
        title = "Uber Trip",
        subtitle = "Transport • Yesterday",
        amount = -24.50,
        category = TransactionCategory.TRANSPORT
    ),
    Transaction(
        id = "3",
        title = "Salary Deposit",
        subtitle = "Income • 2 days ago",
        amount = 4200.0,
        category = TransactionCategory.INCOME
    ),
    Transaction(
        id = "4",
        title = "Netflix Premium",
        subtitle = "Entertainment • 3 days ago",
        amount = -15.99,
        category = TransactionCategory.ENTERTAINMENT
    )
)

// ── Root Screen Composable ────────────────────────────────
@Composable
fun HomeScreen() {
    // LazyColumn = efficient scrollable list
    // Only renders what's visible on screen — like RecyclerView
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Each item{} is one non-repeating section
        item { HomeTopBar() }
        item { BalanceCard(balance = 12450.00) }
        item { BudgetProgressCard(spent = 2275.0, total = 3500.0) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { ActionButtonsRow() }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { RecentTransactionsHeader() }

        // items() repeats for each element in the list
        items(sampleTransactions, key = { it.id }) { transaction ->
            TransactionRow(transaction = transaction)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = CardBorder,
                thickness = 0.5.dp
            )
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────
@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo + app name side by side
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

        // Bell icon with a small red dot badge
        BadgedBox(badge = { Badge() }) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = TextSecondary
            )
        }
    }
}

// ── Balance Card ──────────────────────────────────────────
// Big green card at the top showing total balance
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
                text = "$${"%.2f".format(balance)}",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Budget Progress Card ──────────────────────────────────
// Shows how much of the monthly budget has been spent
@Composable
fun BudgetProgressCard(spent: Double, total: Double) {
    // Calculate progress as a value between 0.0 and 1.0
    val progress = (spent / total).toFloat().coerceIn(0f, 1f)
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
            // Label
            Text(
                text = "MONTHLY BUDGET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Spent / Total and percentage on same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$${"%.2f".format(spent)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = " / ${"%.0f".format(total)}",
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

            // Green progress bar
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
                text = "You have $${"%.0f".format(remaining)} remaining for this month.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

// ── Action Buttons Row ────────────────────────────────────
// Three buttons: Add Expense (filled), Transfer, Set Budget (outlined)
@Composable
fun ActionButtonsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionButton(
            label = "ADD EXPENSE",
            icon = Icons.Default.Add,
            isPrimary = true,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            label = "TRANSFER",
            icon = Icons.Default.SwapHoriz,
            isPrimary = false,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            label = "SET BUDGET",
            icon = Icons.Default.Settings,
            isPrimary = false,
            modifier = Modifier.weight(1f)
        )
    }
}

// Single button — primary = green filled, secondary = outlined
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
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = {},
            modifier = modifier.height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextPrimary
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Recent Transactions Header ────────────────────────────
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
            Text(
                text = "View All",
                color = BrandGreen,
                fontSize = 13.sp
            )
        }
    }
}

// ── Single Transaction Row ────────────────────────────────
// Icon | Title + subtitle | Amount
@Composable
fun TransactionRow(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular icon — color coded by category
        TransactionIcon(category = transaction.category)

        Spacer(modifier = Modifier.width(12.dp))

        // Title and subtitle take all remaining space
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            Text(
                text = transaction.subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Amount — green if income, red if expense
        val isIncome = transaction.amount > 0
        Text(
            text = if (isIncome)
                "+$${"%.2f".format(transaction.amount)}"
            else
                "-$${"%.2f".format(Math.abs(transaction.amount))}",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (isIncome) IncomeGreen else ExpenseRed
        )
    }
}

// ── Category Icon ─────────────────────────────────────────
// Maps each TransactionCategory to an icon inside a green circle
@Composable
fun TransactionIcon(category: TransactionCategory) {
    val icon: ImageVector = when (category) {
        TransactionCategory.FOOD          -> Icons.Default.Restaurant
        TransactionCategory.TRANSPORT     -> Icons.Default.DirectionsCar
        TransactionCategory.INCOME        -> Icons.Default.Work
        TransactionCategory.ENTERTAINMENT -> Icons.Default.PlayCircle
        TransactionCategory.HOUSING       -> Icons.Default.Home
        TransactionCategory.ELECTRONICS   -> Icons.Default.Devices
        TransactionCategory.HEALTH        -> Icons.Default.FitnessCenter
        TransactionCategory.SHOPPING      -> Icons.Default.ShoppingBag
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(BrandGreenLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.name,
            tint = BrandGreen,
            modifier = Modifier.size(22.dp)
        )
    }
}