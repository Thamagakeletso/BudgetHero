package com.example.budgethero.data.achievements

import android.content.Context
import android.util.Log

/**
 * Represents a single achievement/badge in the gamification system.
 * @param id Unique identifier for the achievement
 * @param title Display name of the achievement
 * @param description What the user did to earn it
 * @param emoji Visual representation of the badge
 * @param isUnlocked Whether the user has earned this achievement
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean = false
)

/**
 * Manages the badge/achievement system for BudgetHero gamification.
 * Tracks user progress and unlocks achievements based on app usage.
 * Achievements are persisted using SharedPreferences.
 * Reference: Android SharedPreferences
 * (developer.android.com/training/data-storage/shared-preferences)
 */
class AchievementManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "budget_achievements", Context.MODE_PRIVATE
    )

    // All available achievements
    val allAchievements = listOf(
        Achievement(
            id = "first_expense",
            title = "First Step!",
            description = "Logged your first expense",
            emoji = "🏆"
        ),
        Achievement(
            id = "five_expenses",
            title = "Getting Started",
            description = "Logged 5 expenses",
            emoji = "⭐"
        ),
        Achievement(
            id = "ten_expenses",
            title = "Expense Tracker",
            description = "Logged 10 expenses",
            emoji = "📊"
        ),
        Achievement(
            id = "first_category",
            title = "Organiser",
            description = "Created your first category",
            emoji = "📁"
        ),
        Achievement(
            id = "budget_master",
            title = "Budget Master",
            description = "Stayed within your monthly goals",
            emoji = "💰"
        ),
        Achievement(
            id = "goal_setter",
            title = "Goal Setter",
            description = "Set your first monthly goal",
            emoji = "🎯"
        ),
        Achievement(
            id = "seven_day_streak",
            title = "7-Day Streak",
            description = "Logged expenses 7 days in a row",
            emoji = "🔥"
        ),
        Achievement(
            id = "saver",
            title = "Super Saver",
            description = "Stayed under minimum goal",
            emoji = "💎"
        )
    )

    /**
     * Returns list of all achievements with their current unlock status.
     */
    fun getAchievements(): List<Achievement> {
        return allAchievements.map { achievement ->
            achievement.copy(
                isUnlocked = prefs.getBoolean(achievement.id, false)
            )
        }
    }

    /**
     * Unlocks an achievement by its ID.
     * Does nothing if already unlocked.
     * @param achievementId The ID of the achievement to unlock
     * @return true if newly unlocked, false if already unlocked
     */
    fun unlockAchievement(achievementId: String): Boolean {
        if (prefs.getBoolean(achievementId, false)) return false
        Log.d("AchievementManager", "Achievement unlocked: $achievementId")
        prefs.edit().putBoolean(achievementId, true).apply()
        return true
    }

    /**
     * Checks and unlocks achievements based on expense count.
     * @param expenseCount Total number of expenses logged
     */
    fun checkExpenseAchievements(expenseCount: Int) {
        Log.d("AchievementManager", "Checking expense achievements for count: $expenseCount")
        if (expenseCount >= 1) unlockAchievement("first_expense")
        if (expenseCount >= 5) unlockAchievement("five_expenses")
        if (expenseCount >= 10) unlockAchievement("ten_expenses")
    }

    /**
     * Checks and unlocks achievements based on category count.
     * @param categoryCount Total number of categories created
     */
    fun checkCategoryAchievements(categoryCount: Int) {
        Log.d("AchievementManager", "Checking category achievements for count: $categoryCount")
        if (categoryCount >= 1) unlockAchievement("first_category")
    }

    /**
     * Checks and unlocks goal-related achievements.
     * @param totalSpent Total amount spent this month
     * @param minGoal Minimum monthly spending goal
     * @param maxGoal Maximum monthly spending goal
     */
    fun checkGoalAchievements(
        totalSpent: Double,
        minGoal: Double,
        maxGoal: Double
    ) {
        Log.d("AchievementManager",
            "Checking goal achievements: spent=$totalSpent, min=$minGoal, max=$maxGoal")
        unlockAchievement("goal_setter")
        if (totalSpent in minGoal..maxGoal) {
            unlockAchievement("budget_master")
        }
        if (totalSpent < minGoal) {
            unlockAchievement("saver")
        }
    }

    /**
     * Checks streak achievements based on consecutive days.
     * @param streakDays Number of consecutive days with expenses
     */
    fun checkStreakAchievements(streakDays: Int) {
        Log.d("AchievementManager", "Checking streak achievements: $streakDays days")
        if (streakDays >= 7) unlockAchievement("seven_day_streak")
    }

    /**
     * Returns count of unlocked achievements.
     */
    fun getUnlockedCount(): Int {
        return getAchievements().count { it.isUnlocked }
    }

    /**
     * Resets all achievements - used for testing only.
     */
    fun resetAll() {
        prefs.edit().clear().apply()
        Log.d("AchievementManager", "All achievements reset")
    }
}