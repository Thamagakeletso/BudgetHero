package com.example.budgethero.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyGoalDao {

    // REPLACE means if a goal for this month exists, overwrite it
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setGoal(goal: MonthlyGoal)

    @Query("SELECT * FROM monthly_goals WHERE userId = :userId AND month = :month LIMIT 1")
    suspend fun getGoalForMonth(userId: Int, month: String): MonthlyGoal?

    @Query("SELECT * FROM monthly_goals WHERE userId = :userId ORDER BY month DESC")
    fun getAllGoals(userId: Int): Flow<List<MonthlyGoal>>
}

