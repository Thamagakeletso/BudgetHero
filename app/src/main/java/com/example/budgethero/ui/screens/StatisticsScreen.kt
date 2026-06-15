package com.example.budgethero.ui.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgethero.ui.theme.*
import com.example.budgethero.ui.viewmodels.CategoryViewModel
import com.example.budgethero.ui.viewmodels.ExpenseViewModel

/**
 * StatisticsScreen shows expense analytics including:
 * - Average, highest and lowest expense amounts
 * - Donut chart of spending by category
 * - Total expenses count
 * Custom feature added to exceed Part 3 requirements.
 * Reference: Android Canvas API
 * (developer.android.com/reference/android/graphics/Canvas)
 */
@Composable
fun StatisticsScreen(
    expenseViewModel: ExpenseViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val expenses by expenseViewModel.expenses.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    // Only calculate stats for expense entries (negative amounts)
    val expenseOnly = expenses // show ALL expenses in stats
    val amounts = expenseOnly.map { Math.abs(it.amount) }

    // Calculate statistics safely
    val average = if (amounts.isNotEmpty()) amounts.average() else 0.0
    val highest = if (amounts.isNotEmpty()) amounts.max() else 0.0
    val lowest = if (amounts.isNotEmpty()) amounts.min() else 0.0
    val total = amounts.sum()
    val count = expenses.size

    // Group expenses by category for donut chart
    // ✅ Group all expenses by category
    val categorySpending = expenses
        .groupBy { it.categoryId }
        .map { (catId, exps) ->
            val catName = categories
                .find { it.id == catId }?.name ?: "Unknown"
            Pair(catName, exps.sumOf { Math.abs(it.amount) })
        }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }

    // Colors for donut chart segments
    val chartColors = listOf(
        BrandGreen,
        Color(0xFF66BB6A),
        Color(0xFF26A69A),
        Color(0xFF42A5F5),
        Color(0xFFAB47BC),
        Color(0xFFEF5350),
        Color(0xFFFF7043),
        Color(0xFFFFCA28)
    )

    Log.d("StatisticsScreen",
        "Stats: avg=R$average, high=R$highest, low=R$lowest, count=$count")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // ── Top Bar ───────────────────────────────────────
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Text(
                    "Statistics",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        // ── No data message ───────────────────────────────
        if (expenses.isEmpty()) {
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
                        Text("📊", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No expenses yet",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Add expenses to see statistics",
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {

            // ── Stats Summary Cards ───────────────────────
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Expense Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Average",
                            value = "R${"%.2f".format(average)}",
                            icon = "📊",
                            color = BrandGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Highest",
                            value = "R${"%.2f".format(highest)}",
                            icon = "📈",
                            color = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Lowest",
                            value = "R${"%.2f".format(lowest)}",
                            icon = "📉",
                            color = IncomeGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Total",
                            value = "R${"%.2f".format(total)}",
                            icon = "💰",
                            color = BrandGreenDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── Donut Chart Card ──────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Spending by Category",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(16.dp))

                        if (categorySpending.isEmpty()) {
                            Text(
                                "Add categorised expenses to see chart",
                                color = MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        } else {
                            // Donut chart
                            DonutChart(
                                data = categorySpending,
                                colors = chartColors
                                    .take(categorySpending.size),
                                total = total,
                                size = 200.dp
                            )

                            Spacer(Modifier.height(16.dp))

                            // Legend
                            categorySpending.forEachIndexed { index, (name, amount) ->
                                val color = chartColors[
                                    index % chartColors.size
                                ]
                                val percentage = if (total > 0)
                                    (amount / total * 100).toInt()
                                else 0

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        name,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "R${"%.2f".format(amount)} ($percentage%)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── Expenses Count Card ───────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📋", fontSize = 32.sp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "Total Expenses Logged",
                                color = MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                            Text(
                                "$count expenses",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable stat card showing a single financial metric.
 * Supports dark mode via MaterialTheme colors.
 * @param title Label for the metric
 * @param value Formatted value to display
 * @param icon Emoji icon for visual representation
 * @param color Color for the value text
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color
            )
            Text(
                title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Custom donut chart drawn using Android Canvas API.
 * Each category gets a colored arc proportional to spending.
 * @param data List of category name and spending amount pairs
 * @param colors List of colors for each segment
 * @param total Total spending for percentage calculation
 * @param size Size of the chart in dp
 */
@Composable
fun DonutChart(
    data: List<Pair<String, Double>>,
    colors: List<Color>,
    total: Double,
    size: Dp = 200.dp
) {
    val strokeWidth = 50f
    var startAngle = -90f

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val topLeft = Offset(
                strokeWidth / 2,
                strokeWidth / 2
            )
            val arcSize = Size(
                canvasSize - strokeWidth,
                canvasSize - strokeWidth
            )

            data.forEachIndexed { index, (_, amount) ->
                val sweepAngle = if (total > 0)
                    (amount / total * 360f).toFloat()
                else 0f

                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle - 2f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
                startAngle += sweepAngle
            }
        }

        // Center label
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Total",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
                    .copy(alpha = 0.6f)
            )
            Text(
                "R${"%.0f".format(total)}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}