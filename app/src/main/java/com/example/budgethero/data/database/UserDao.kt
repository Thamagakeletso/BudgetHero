package com.example.budgethero.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// DAO = Data Access Object — defines all database operations for users
@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User): Long

    // Returns user if credentials match, null if login fails
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    // Check if username already exists before registering
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
}

