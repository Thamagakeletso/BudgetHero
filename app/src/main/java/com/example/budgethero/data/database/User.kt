package com.example.budgethero.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents a user account in the database
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String
)

