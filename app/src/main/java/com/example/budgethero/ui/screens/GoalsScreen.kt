package com.example.budgethero.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgethero.data.database.BudgetDatabase
import com.example.budgethero.data.repository.BudgetRepository
import com.example.budgethero.data.session.SessionManager
import com.example.budgethero.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GoalsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = BudgetRepository(BudgetDatabase.getDatabase(context))
    val sessionManager = SessionManager(context)
    val userId = sessionManager.getUserId()

    // Current month in yyyy-MM format
    val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    // Form state
    var minGoal by remember { mutableStateOf("") }
    var maxGoal by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var existingMin by remember { mutableStateOf<Double?>(null) }
    var existingMax by remember { mutableStateOf<Double?>(null) }

    // Load existing goal for this month
    LaunchedEffect(Unit) {
        val goal = repository.getMonthlyGoal(userId, currentMonth)
        if (goal != null) {
            existingMin = goal.minGoal
            existingMax = goal.maxGoal
            minGoal = goal.minGoal.toString()
            maxGoal = goal.maxGoal.toString()
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
            if (existingMin != null && existingMax != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGreenLight)
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Minimum", fontSize = 12.sp, color = BrandGreenDark)
                                Text(
                                    "R${"%.2f".format(existingMin)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = BrandGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Maximum", fontSize = 12.sp, color = BrandGreenDark)
                                Text(
                                    "R${"%.2f".format(existingMax)}",
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
                        if (existingMin != null) "Update Goals" else "Set Goals",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(16.dp))

                    // Min goal field
                    OutlinedTextField(
                        value = minGoal,
                        onValueChange = {
                            minGoal = it
                            errorMessage = ""
                        },
                        label = { Text("Minimum Goal (R)") },
                        leadingIcon = {
                            Icon(Icons.Default.TrendingDown, null, tint = BrandGreen)
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

                    // Max goal field
                    OutlinedTextField(
                        value = maxGoal,
                        onValueChange = {
                            maxGoal = it
                            errorMessage = ""
                        },
                        label = { Text("Maximum Goal (R)") },
                        leadingIcon = {
                            Icon(Icons.Default.TrendingUp, null, tint = ExpenseRed)
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

                    // Error / success messages
                    if (errorMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(errorMessage, color = ExpenseRed, fontSize = 13.sp)
                    }
                    if (successMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(successMessage, color = IncomeGreen, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Save button
                    Button(
                        onClick = {
                            errorMessage = ""
                            successMessage = ""
                            // Validation
                            val min = minGoal.toDoubleOrNull()
                            val max = maxGoal.toDoubleOrNull()
                            when {
                                minGoal.isBlank() || maxGoal.isBlank() ->
                                    errorMessage = "Please fill in both fields"
                                min == null || max == null ->
                                    errorMessage = "Please enter valid numbers"
                                min < 0 || max < 0 ->
                                    errorMessage = "Goals cannot be negative"
                                min >= max ->
                                    errorMessage = "Minimum must be less than maximum"
                                else -> {
                                    scope.launch {
                                        repository.setMonthlyGoal(
                                            userId, currentMonth, min, max
                                        )
                                        existingMin = min
                                        existingMax = max
                                        successMessage = "Goals saved successfully!"
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                    ) {
                        Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (existingMin != null) "Update Goals" else "Save Goals",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

