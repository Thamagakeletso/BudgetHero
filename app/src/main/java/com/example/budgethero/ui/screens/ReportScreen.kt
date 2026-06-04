package com.example.budgethero.ui.screens

import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgethero.ui.theme.*
import com.example.budgethero.ui.viewmodels.ReportViewModel

/**
 * Data class holding category name and total spending.
 * Used by both ReportScreen and ReportViewModel.
 */
data class CategoryTotal(
    val categoryName: String,
    val total: Double
)

/**
 * ReportScreen displays spending totals per category for a selected period.
 * Compares totals against monthly min/max goals.
 * Uses ReportViewModel following MVVM pattern.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    reportViewModel: ReportViewModel = viewModel()
) {
    // Collect state from ViewModel
    val categoryTotals by reportViewModel.categoryTotals.collectAsState()
    val totalSpent by reportViewModel.totalSpent.collectAsState()
    val isLoading by reportViewModel.isLoading.collectAsState()
    val hasSearched by reportViewModel.hasSearched.collectAsState()
    val currentGoal by reportViewModel.currentGoal.collectAsState()

    // Date range state
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    // Date pickers
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Log.d("ReportScreen", "Screen loaded")

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
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceWhite
                    ),
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
                                    IconButton(
                                        onClick = { showStartDatePicker = true }
                                    ) {
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
                                    IconButton(
                                        onClick = { showEndDatePicker = true }
                                    ) {
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
                                Log.d("ReportScreen", "Generate report tapped")
                                reportViewModel.generateReport(startDate, endDate)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandGreen
                            ),
                            enabled = startDate.isNotEmpty() && endDate.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.BarChart,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Generate Report",
                                fontWeight = FontWeight.Bold
                            )
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

                // Total spent card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandGreen
                        )
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

                // Goal status card
                if (currentGoal != null) {
                    item {
                        val isOverMax = totalSpent > currentGoal!!.maxGoal
                        val isUnderMin = totalSpent < currentGoal!!.minGoal

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOverMax)
                                    Color(0xFFFFEBEB)
                                else
                                    BrandGreenLight
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOverMax)
                                        Icons.Default.Warning
                                    else
                                        Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isOverMax) ExpenseRed else BrandGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = when {
                                            isOverMax -> "Over maximum goal!"
                                            isUnderMin -> "Under minimum goal"
                                            else -> "Within goal range ✓"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOverMax) ExpenseRed
                                        else BrandGreenDark
                                    )
                                    Text(
                                        "Goal: R${"%.2f".format(currentGoal!!.minGoal)}" +
                                                " - R${"%.2f".format(currentGoal!!.maxGoal)}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Category breakdown
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
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceWhite
                            )
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
                            "yyyy-MM-dd",
                            java.util.Locale.getDefault()
                        )
                        startDate = sdf.format(java.util.Date(it))
                    }
                    showStartDatePicker = false
                }) { Text("OK", color = BrandGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
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
                            "yyyy-MM-dd",
                            java.util.Locale.getDefault()
                        )
                        endDate = sdf.format(java.util.Date(it))
                    }
                    showEndDatePicker = false
                }) { Text("OK", color = BrandGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) { DatePicker(state = endDatePickerState) }
    }
}

/**
 * Card displaying a single category's spending total.
 * Shows percentage of total spending as a progress bar.
 */
@Composable
fun CategoryTotalCard(
    categoryTotal: CategoryTotal,
    totalSpent: Double
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                BrandGreenLight,
                                RoundedCornerShape(18.dp)
                            ),
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