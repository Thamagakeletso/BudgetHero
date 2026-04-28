package com.example.budgethero.data.models

data class SavingsGoal(
    val id: String,
    val title: String,
    val description: String,
    val currentAmount: Double,
    val targetAmount: Double,
    val imageUrl: String
) {
    val progress: Float
        get() = (currentAmount / targetAmount)
            .toFloat().coerceIn(0f, 1f)

    val progressPercent: Int
        get() = (progress * 100).toInt()
}