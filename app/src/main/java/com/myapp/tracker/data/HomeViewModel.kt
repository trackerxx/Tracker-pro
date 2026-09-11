package com.myapp.tracker.data

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Everything the Home screen shows, already computed for the currently
 * active fund + chart range. Recomputed any time state changes.
 */
data class HomeUiState(
    val userName: String = "User",
    val today: LocalDate = LocalDate.now(),
    val funds: List<Fund> = emptyList(),
    val activeFundId: String = "",
    val fundsExpanded: Boolean = false,
    val chartRange: ChartRange = ChartRange.MONTH,
    val lastActivityText: String = "No recent activity yet",
    val debts: List<Debt> = emptyList()
) {
    val activeFund: Fund? get() = funds.find { it.id == activeFundId }
}

class HomeViewModel : ViewModel() {

    // ---- seed / sample data (stand-in for what will later come from local storage) ----
    private val sampleFunds = listOf(
        Fund("cash", "Monthly Cash", startingBalance = 15000.0, colorHex = 0xFFB91C1C),
        Fund("savings", "Savings", startingBalance = 42000.0, colorHex = 0xFF16A34A),
        Fund("card", "Bank Card", startingBalance = 8000.0, colorHex = 0xFF2563EB)
    )

    private val today = LocalDate.now()

    private val sampleTransactions = listOf(
        Transaction("t1", "cash", "Groceries", "Food", 850.0, TxType.EXPENSE, today.minusDays(1)),
        Transaction("t2", "cash", "Bus fare", "Transport", 60.0, TxType.EXPENSE, today.minusDays(2)),
        Transaction("t3", "cash", "Freelance gig", "Income", 3000.0, TxType.INCOME, today.minusDays(3)),
        Transaction("t4", "cash", "Coffee", "Food", 180.0, TxType.EXPENSE, today.minusDays(4)),
        Transaction("t5", "cash", "Mobile recharge", "Utilities", 300.0, TxType.EXPENSE, today.minusDays(6)),
        Transaction("t6", "cash", "Movie night", "Entertainment", 500.0, TxType.EXPENSE, today.minusDays(10)),
        Transaction("t7", "savings", "Interest", "Income", 120.0, TxType.INCOME, today.minusDays(5)),
        Transaction("t8", "card", "Electric bill", "Utilities", 1200.0, TxType.EXPENSE, today.minusDays(2))
    )

    private val sampleDebts = listOf(
        Debt("d1", "Rahim", 2000.0, DebtType.OWE),
        Debt("d2", "Karim", 1500.0, DebtType.OWED),
        Debt("d3", "Sadia", 500.0, DebtType.OWED)
    )

    private val _uiState = MutableStateFlow(
        HomeUiState(
            userName = "Amr",
            today = today,
            funds = sampleFunds,
            activeFundId = sampleFunds.first().id,
            lastActivityText = "Groceries · ৳850.00 spent",
            debts = sampleDebts
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState

    private val transactions = sampleTransactions

    // ---- actions the UI can trigger — this is the "real logic" behind the screen ----

    fun toggleFundsExpanded() {
        _uiState.update { it.copy(fundsExpanded = !it.fundsExpanded) }
    }

    fun switchActiveFund(fundId: String) {
        _uiState.update { it.copy(activeFundId = fundId, fundsExpanded = false) }
    }

    fun setChartRange(range: ChartRange) {
        _uiState.update { it.copy(chartRange = range) }
    }

    // ---- derived numbers, recomputed from current state + transactions ----

    fun remainingBalance(state: HomeUiState): Double {
        val fund = state.activeFund ?: return 0.0
        val txForFund = transactions.filter { it.fundId == fund.id }
        val net = txForFund.sumOf { if (it.type == TxType.INCOME) it.amount else -it.amount }
        return fund.startingBalance + net
    }

    fun spentFromActiveFund(state: HomeUiState): Double {
        val fund = state.activeFund ?: return 0.0
        return transactions
            .filter { it.fundId == fund.id && it.type == TxType.EXPENSE }
            .sumOf { it.amount }
    }

    fun totalOwe(state: HomeUiState): Double =
        state.debts.filter { it.type == DebtType.OWE }.sumOf { it.amount }

    fun totalOwed(state: HomeUiState): Double =
        state.debts.filter { it.type == DebtType.OWED }.sumOf { it.amount }

    /** Category breakdown for the active fund, filtered to the selected week/month range. */
    fun categoryBreakdown(state: HomeUiState): List<CategorySlice> {
        val fund = state.activeFund ?: return emptyList()
        val cutoffDays = if (state.chartRange == ChartRange.WEEK) 7L else 30L
        val cutoff = state.today.minus(cutoffDays, ChronoUnit.DAYS)

        val relevant = transactions.filter {
            it.fundId == fund.id && it.type == TxType.EXPENSE && !it.date.isBefore(cutoff)
        }
        val total = relevant.sumOf { it.amount }
        if (total <= 0.0) return emptyList()

        return relevant
            .groupBy { it.category }
            .map { (category, txs) ->
                val amount = txs.sumOf { it.amount }
                CategorySlice(category, amount, (amount / total).toFloat())
            }
            .sortedByDescending { it.amount }
    }
}
