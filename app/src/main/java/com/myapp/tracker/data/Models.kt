package com.myapp.tracker.data

import java.time.LocalDate

/** A "wallet" the user tracks money in — e.g. Monthly Cash, Savings, Bank Card. */
data class Fund(
    val id: String,
    val name: String,
    val startingBalance: Double,
    val colorHex: Long
)

enum class TxType { INCOME, EXPENSE }

data class Transaction(
    val id: String,
    val fundId: String,
    val title: String,
    val category: String,
    val amount: Double,
    val type: TxType,
    val date: LocalDate
)

enum class DebtType { OWE, OWED }

data class Debt(
    val id: String,
    val person: String,
    val amount: Double,
    val type: DebtType
)

enum class ChartRange { WEEK, MONTH }

/** One category's slice of spending, ready for the breakdown list + bar chart. */
data class CategorySlice(
    val category: String,
    val amount: Double,
    val percent: Float
)
