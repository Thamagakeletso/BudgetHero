package com.example.budgethero.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents a spending category created by the user
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val userId: Int
)

