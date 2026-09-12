package com.myapp.tracker.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/**
 * Stand-in for local persistence (Room comes later). One shared source of
 * truth so Home's balance/debts totals and the Logs screen always agree.
 */
object AppData {

    private val today = LocalDate.now()

    val funds: List<Fund> = listOf(
        Fund("cash", "Monthly Cash", startingBalance = 15000.0, colorHex = 0xFFB91C1C),
        Fund("savings", "Savings", startingBalance = 42000.0, colorHex = 0xFF16A34A),
        Fund("card", "Bank Card", startingBalance = 8000.0, colorHex = 0xFF2563EB)
    )

    private val _transactions = MutableStateFlow(
        listOf(
            Transaction("t1", "cash", "Groceries", "Food", 850.0, TxType.EXPENSE, today.minusDays(1)),
            Transaction("t2", "cash", "Bus fare", "Transport", 60.0, TxType.EXPENSE, today.minusDays(2)),
            Transaction("t3", "cash", "Freelance gig", "Income", 3000.0, TxType.INCOME, today.minusDays(3)),
            Transaction("t4", "cash", "Coffee", "Food", 180.0, TxType.EXPENSE, today.minusDays(4)),
            Transaction("t5", "cash", "Mobile recharge", "Utilities", 300.0, TxType.EXPENSE, today.minusDays(6)),
            Transaction("t6", "cash", "Movie night", "Entertainment", 500.0, TxType.EXPENSE, today.minusDays(10)),
            Transaction("t7", "savings", "Interest", "Income", 120.0, TxType.INCOME, today.minusDays(5)),
            Transaction("t8", "card", "Electric bill", "Utilities", 1200.0, TxType.EXPENSE, today.minusDays(2))
        )
    )
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _debts = MutableStateFlow(
        listOf(
            Debt("d1", "Rahim", 2000.0, DebtType.OWE, dueDate = today.plusDays(5)),
            Debt("d2", "Karim", 1500.0, DebtType.OWED, dueDate = today.plusDays(2)),
            Debt("d3", "Sadia", 500.0, DebtType.OWED, dueDate = today.plusDays(12)),
            Debt("d4", "Habib", 1000.0, DebtType.OWE, dueDate = today.minusDays(3), status = DebtStatus.SETTLED),
            Debt("d5", "Nadia", 2500.0, DebtType.OWED, dueDate = today.minusDays(10), status = DebtStatus.SETTLED)
        )
    )
    val debts: StateFlow<List<Debt>> = _debts

    fun toggleDebtStatus(debtId: String) {
        _debts.update { list ->
            list.map {
                if (it.id == debtId) {
                    it.copy(status = if (it.status == DebtStatus.PENDING) DebtStatus.SETTLED else DebtStatus.PENDING)
                } else it
            }
        }
    }

    fun clearAllTransactions() {
        _transactions.update { emptyList() }
    }
}
