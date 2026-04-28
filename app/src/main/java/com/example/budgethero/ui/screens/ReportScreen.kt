package com.example.budgethero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgethero.data.database.BudgetDatabase
import com.example.budgethero.data.database.Category
import com.example.budgethero.data.repository.BudgetRepository
import com.example.budgethero.data.session.SessionManager
import com.example.budgethero.ui.theme.*
import kotlinx.coroutines.launch

// Holds category name + total for display
data class CategoryTotal(
    val categoryName: String,
    val total: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = BudgetRepository(BudgetDatabase.getDatabase(context))
    val sessionManager = SessionManager(context)
    val userId = sessionManager.getUserId()

    // Date range state
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var categoryTotals by remember { mutableStateOf<List<CategoryTotal>>(emptyList()) }
    var totalSpent by remember { mutableStateOf(0.0) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    // Monthly goal
    var monthlyGoalMin by remember { mutableStateOf<Double?>(null) }
    var monthlyGoalMax by remember { mutableStateOf<Double?>(null) }

    // Date pickers
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Load categories once
    LaunchedEffect(Unit) {
        categories = repository.getCategoriesOnce(userId)
        val month = java.text.SimpleDateFormat(
            "yyyy-MM", java.util.Locale.getDefault()
        ).format(java.util.Date())
        val goal = repository.getMonthlyGoal(userId, month)
        monthlyGoalMin = goal?.minGoal
        monthlyGoalMax = goal?.maxGoal
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // ── Top Bar ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(16.dp)
        ) {
            Text(
                "Spending Report",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Date Range Filter ─────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Select Period",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("From") },
                                trailingIcon = {
                                    IconButton(onClick = { showStartDatePicker = true }) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            null,
                                            tint = BrandGreen
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("To") },
                                trailingIcon = {
                                    IconButton(onClick = { showEndDatePicker = true }) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            null,
                                            tint = BrandGreen
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                                    isLoading = true
                                    scope.launch {
                                        val spending = repository.getSpendingByCategory(
                                            userId, startDate, endDate
                                        )
                                        categoryTotals = spending.map { cs ->
                                            val cat = categories.find { it.id == cs.categoryId }
                                            CategoryTotal(
                                                categoryName = cat?.name ?: "Unknown",
                                                total = cs.total
                                            )
                                        }.sortedByDescending { it.total }
                                        totalSpent = spending.sumOf { it.total }
                                        isLoading = false
                                        hasSearched = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                        ) {
                            Icon(Icons.Default.BarChart, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Generate Report", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Loading ───────────────────────────────────
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandGreen)
                    }
                }
            }

            // ── Results ───────────────────────────────────
            if (hasSearched && !isLoading) {

                // Total spent summary card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGreen)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Total Spent",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                            Text(
                                "R${"%.2f".format(totalSpent)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                            Text(
                                "$startDate  →  $endDate",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Monthly goal status
                if (monthlyGoalMin != null && monthlyGoalMax != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    totalSpent < monthlyGoalMin!! -> BrandGreenLight
                                    totalSpent > monthlyGoalMax!! -> Color(0xFFFFEBEB)
                                    else -> BrandGreenLight
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when {
                                        totalSpent > monthlyGoalMax!! ->
                                            Icons.Default.Warning
                                        else ->
                                            Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        totalSpent > monthlyGoalMax!! -> ExpenseRed
                                        else -> BrandGreen
                                    },
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = when {
                                            totalSpent > monthlyGoalMax!! ->
                                                "Over maximum goal!"
                                            totalSpent < monthlyGoalMin!! ->
                                                "Under minimum goal"
                                            else -> "Within goal range ✓"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            totalSpent > monthlyGoalMax!! -> ExpenseRed
                                            else -> BrandGreenDark
                                        }
                                    )
                                    Text(
                                        "Goal: R${"%.2f".format(monthlyGoalMin)} - R${"%.2f".format(monthlyGoalMax)}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Category breakdown header
                item {
                    Text(
                        "Spending by Category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }

                if (categoryTotals.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No expenses in this period",
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    // Category total rows
                    items(categoryTotals) { ct ->
                        CategoryTotalCard(
                            categoryTotal = ct,
                            totalSpent = totalSpent
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // ── Start Date Picker ─────────────────────────────────
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let {
                        val sdf = java.text.SimpleDateFormat(
                            "yyyy-MM-dd", java.util.Locale.getDefault()
                        )
                        startDate = sdf.format(java.util.Date(it))
                    }
                    showStartDatePicker = false
                }) { Text("OK", color = BrandGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = startDatePickerState) }
    }

    // ── End Date Picker ───────────────────────────────────
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let {
                        val sdf = java.text.SimpleDateFormat(
                            "yyyy-MM-dd", java.util.Locale.getDefault()
                        )
                        endDate = sdf.format(java.util.Date(it))
                    }
                    showEndDatePicker = false
                }) { Text("OK", color = BrandGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = endDatePickerState) }
    }
}

// ── Category Total Card ───────────────────────────────────
@Composable
fun CategoryTotalCard(categoryTotal: CategoryTotal, totalSpent: Double) {
    val percentage = if (totalSpent > 0)
        (categoryTotal.total / totalSpent * 100).toFloat()
    else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category initial circle + name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(BrandGreenLight, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            categoryTotal.categoryName.first().uppercase(),
                            color = BrandGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        categoryTotal.categoryName,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "R${"%.2f".format(categoryTotal.total)}",
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                    Text(
                        "${"%.1f".format(percentage)}%",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar showing % of total
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = BrandGreen,
                trackColor = BrandGreenLight
            )
        }
    }
}
