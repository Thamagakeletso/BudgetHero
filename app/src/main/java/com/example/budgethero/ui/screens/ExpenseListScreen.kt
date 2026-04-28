package com.example.budgethero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.budgethero.data.database.Category
import com.example.budgethero.data.database.Expense
import com.example.budgethero.ui.theme.*
import com.example.budgethero.ui.viewmodels.ExpenseViewModel
import com.example.budgethero.ui.viewmodels.CategoryViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    expenseViewModel: ExpenseViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val categories by categoryViewModel.categories.collectAsState()
    val filteredExpenses by expenseViewModel.filteredExpenses.collectAsState()
    val allExpenses by expenseViewModel.expenses.collectAsState()

    // Date range filter state
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var isFiltered by remember { mutableStateOf(false) }

    // Date pickers
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Photo viewer
    var selectedPhotoPath by remember { mutableStateOf<String?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    // Show filtered or all expenses
    val displayedExpenses = if (isFiltered) filteredExpenses else allExpenses

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
                "Expense List",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // ── Date Filter Card ──────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Filter by Date Range",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Start date
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
                        // End date
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Apply filter button
                        Button(
                            onClick = {
                                if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                                    expenseViewModel.filterByDateRange(startDate, endDate)
                                    isFiltered = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                        ) {
                            Icon(Icons.Default.FilterList, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Apply", fontWeight = FontWeight.Bold)
                        }
                        // Clear filter button
                        OutlinedButton(
                            onClick = {
                                startDate = ""
                                endDate = ""
                                isFiltered = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Clear, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Expense Count ─────────────────────────────
            Text(
                text = "${displayedExpenses.size} expense(s) found",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Expense List ──────────────────────────────
            if (displayedExpenses.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Spacer(Modifier.height(8.dp))
                        Text("No expenses found", color = TextSecondary)
                        Text(
                            if (isFiltered) "Try a different date range"
                            else "Add your first expense",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(displayedExpenses, key = { it.id }) { expense ->
                        val category = categories.find { it.id == expense.categoryId }
                        ExpenseCard(
                            expense = expense,
                            category = category,
                            onPhotoClick = {
                                selectedPhotoPath = expense.photoPath
                                showPhotoDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // ── Start Date Picker Dialog ──────────────────────────
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        startDate = sdf.format(java.util.Date(millis))
                    }
                    showStartDatePicker = false
                }) { Text("OK", color = BrandGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = startDatePickerState) }
    }

    // ── End Date Picker Dialog ────────────────────────────
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let { millis ->
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        endDate = sdf.format(java.util.Date(millis))
                    }
                    showEndDatePicker = false
                }) { Text("OK", color = BrandGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = endDatePickerState) }
    }

    // ── Photo Viewer Dialog ───────────────────────────────
    if (showPhotoDialog && selectedPhotoPath != null) {
        Dialog(onDismissRequest = { showPhotoDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Expense Photo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    AsyncImage(
                        model = File(selectedPhotoPath!!),
                        contentDescription = "Expense photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showPhotoDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Close") }
                }
            }
        }
    }
}

// ── Single Expense Card ───────────────────────────────────
@Composable
fun ExpenseCard(
    expense: Expense,
    category: Category?,
    onPhotoClick: () -> Unit
) {
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
                // Description + category
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        expense.description,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        category?.name ?: "Unknown",
                        fontSize = 12.sp,
                        color = BrandGreen
                    )
                }
                // Amount
                Text(
                    "R${"%.2f".format(expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ExpenseRed
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            // Date and time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(expense.date, fontSize = 12.sp, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${expense.startTime} - ${expense.endTime}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Photo button if photo exists
            if (expense.photoPath != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onPhotoClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandGreen
                    )
                ) {
                    Icon(
                        Icons.Default.Image,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("View Photo", fontSize = 13.sp)
                }
            }
        }
    }
}

