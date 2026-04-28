package com.example.budgethero.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents one expense entry with all required fields
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val categoryId: Int,
    val description: String,
    val amount: Double,
    val date: String,       // format: "yyyy-MM-dd"
    val startTime: String,  // format: "HH:mm"
    val endTime: String,    // format: "HH:mm"
    val photoPath: String?  // null if no photo attached
)