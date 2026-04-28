package com.example.budgethero.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Stores the min and max spending goals per month per user
@Entity(tableName = "monthly_goals")
data class MonthlyGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val month: String,    // format: "yyyy-MM"
    val minGoal: Double,
    val maxGoal: Double
)

