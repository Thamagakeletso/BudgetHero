package com.example.budgethero.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgethero.ui.theme.*
import com.example.budgethero.ui.viewmodels.GoalsViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * GoalsScreen allows users to set minimum and maximum monthly spending goals.
 * Uses GoalsViewModel following MVVM pattern — no direct repository calls.
 * Reference: Android MVVM Guide (developer.android.com/topic/architecture)
 */
@Composable
fun GoalsScreen(
    goalsViewModel: GoalsViewModel = viewModel()
) {
    val currentMonth = SimpleDateFormat(
        "yyyy-MM", Locale.getDefault()
    ).format(Date())

    // Collect state from ViewModel
    val currentGoal by goalsViewModel.currentGoal.collectAsState()
    val saveMessage by goalsViewModel.saveMessage.collectAsState()

    // Form state
    var minGoal by remember { mutableStateOf("") }
    var maxGoal by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    // Load goal when screen opens
    LaunchedEffect(Unit) {
        Log.d("GoalsScreen", "Screen opened, loading goal for month: $currentMonth")
        goalsViewModel.loadGoal(currentMonth)
    }

    // Pre-fill fields when goal loads
    LaunchedEffect(currentGoal) {
        currentGoal?.let {
            minGoal = it.minGoal.toString()
            maxGoal = it.maxGoal.toString()
        }
    }

    // Handle save result from ViewModel
    LaunchedEffect(saveMessage) {
        when {
            saveMessage == "SUCCESS" -> {
                successMessage = "Goals saved successfully!"
                errorMessage = ""
                goalsViewModel.clearMessage()
            }
            saveMessage != null -> {
                errorMessage = saveMessage!!
                successMessage = ""
                goalsViewModel.clearMessage()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Bar ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(16.dp)
        ) {
            Text(
                "Monthly Goals",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // ── Current Month Card ────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Current Month",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Text(
                            currentMonth,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Existing Goal Display ─────────────────────
            if (currentGoal != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandGreenLight
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Current Goals",
                            fontWeight = FontWeight.Bold,
                            color = BrandGreenDark
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Minimum",
                                    fontSize = 12.sp,
                                    color = BrandGreenDark
                                )
                                Text(
                                    "R${"%.2f".format(currentGoal!!.minGoal)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = BrandGreen
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Maximum",
                                    fontSize = 12.sp,
                                    color = BrandGreenDark
                                )
                                Text(
                                    "R${"%.2f".format(currentGoal!!.maxGoal)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Set Goals Form ────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (currentGoal != null) "Update Goals" else "Set Goals",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(16.dp))

                    // Minimum goal field
                    OutlinedTextField(
                        value = minGoal,
                        onValueChange = {
                            minGoal = it
                            errorMessage = ""
                            successMessage = ""
                        },
                        label = { Text("Minimum Goal (R)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.TrendingDown,
                                null,
                                tint = BrandGreen
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        supportingText = {
                            Text(
                                "Minimum amount you want to spend",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    // Maximum goal field
                    OutlinedTextField(
                        value = maxGoal,
                        onValueChange = {
                            maxGoal = it
                            errorMessage = ""
                            successMessage = ""
                        },
                        label = { Text("Maximum Goal (R)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.TrendingUp,
                                null,
                                tint = ExpenseRed
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        supportingText = {
                            Text(
                                "Maximum amount you want to spend",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    )

                    // Error message
                    if (errorMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            errorMessage,
                            color = ExpenseRed,
                            fontSize = 13.sp
                        )
                    }

                    // Success message
                    if (successMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            successMessage,
                            color = IncomeGreen,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Save button
                    Button(
                        onClick = {
                            Log.d("GoalsScreen", "Save button tapped")
                            goalsViewModel.saveGoal(
                                currentMonth, minGoal, maxGoal
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen
                        )
                    ) {
                        Icon(
                            Icons.Default.Save,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (currentGoal != null) "Update Goals"
                            else "Save Goals",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}