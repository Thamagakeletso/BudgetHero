package com.example.budgethero.data.models

data class Transaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val category: TransactionCategory
)

enum class TransactionCategory {
    FOOD, TRANSPORT, INCOME,
    ENTERTAINMENT, HOUSING,
    ELECTRONICS, HEALTH, SHOPPING
}