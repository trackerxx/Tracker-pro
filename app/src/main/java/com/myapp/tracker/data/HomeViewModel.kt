package com.myapp.tracker.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** The bits of Home screen state that only Home cares about (not shared data). */
private data class HomeLocalState(
    val userName: String = "Amr",
    val today: LocalDate = LocalDate.now(),
    val activeFundId: String = AppData.funds.first().id,
    val fundsExpanded: Boolean = false,
    val chartRange: ChartRange = ChartRange.MONTH
)

/**
 * Everything the Home screen shows, already computed for the currently
 * active fund + chart range. Recomputed any time local state OR the
 * shared transaction/debt data changes.
 */
data class HomeUiState(
    val userName: String = "User",
    val today: LocalDate = LocalDate.now(),
    val funds: List<Fund> = emptyList(),
    val activeFundId: String = "",
    val fundsExpanded: Boolean = false,
    val chartRange: ChartRange = ChartRange.MONTH,
    val lastActivityText: String = "No recent activity yet",
    val transactions: List<Transaction> = emptyList(),
    val debts: List<Debt> = emptyList()
) {
    val activeFund: Fund? get() = funds.find { it.id == activeFundId }
}

class HomeViewModel : ViewModel() {

    private val local = MutableStateFlow(HomeLocalState())

    val uiState: StateFlow<HomeUiState> = combine(
        local, AppData.transactions, AppData.debts
    ) { local, txs, debts ->
        HomeUiState(
            userName = local.userName,
            today = local.today,
            funds = AppData.funds,
            activeFundId = local.activeFundId,
            fundsExpanded = local.fundsExpanded,
            chartRange = local.chartRange,
            lastActivityText = txs.maxByOrNull { it.date }
                ?.let { "${it.title} · ${moneyShort(it.amount)} ${if (it.type == TxType.INCOME) "received" else "spent"}" }
                ?: "No recent activity yet",
            transactions = txs,
            debts = debts
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState(funds = AppData.funds, activeFundId = AppData.funds.first().id)
    )

    // ---- actions the UI can trigger — this is the "real logic" behind the screen ----

    fun toggleFundsExpanded() {
        local.update { it.copy(fundsExpanded = !it.fundsExpanded) }
    }

    fun switchActiveFund(fundId: String) {
        local.update { it.copy(activeFundId = fundId, fundsExpanded = false) }
    }

    fun setChartRange(range: ChartRange) {
        local.update { it.copy(chartRange = range) }
    }

    // ---- derived numbers, recomputed from current state + transactions ----

    fun remainingBalance(state: HomeUiState): Double {
        val fund = state.activeFund ?: return 0.0
        val txForFund = state.transactions.filter { it.fundId == fund.id }
        val net = txForFund.sumOf { if (it.type == TxType.INCOME) it.amount else -it.amount }
        return fund.startingBalance + net
    }

    fun spentFromActiveFund(state: HomeUiState): Double {
        val fund = state.activeFund ?: return 0.0
        return state.transactions
            .filter { it.fundId == fund.id && it.type == TxType.EXPENSE }
            .sumOf { it.amount }
    }

    fun totalOwe(state: HomeUiState): Double =
        state.debts.filter { it.type == DebtType.OWE && it.status == DebtStatus.PENDING }.sumOf { it.amount }

    fun totalOwed(state: HomeUiState): Double =
        state.debts.filter { it.type == DebtType.OWED && it.status == DebtStatus.PENDING }.sumOf { it.amount }

    /** Category breakdown for the active fund, filtered to the selected week/month range. */
    fun categoryBreakdown(state: HomeUiState): List<CategorySlice> {
        val fund = state.activeFund ?: return emptyList()
        val cutoffDays = if (state.chartRange == ChartRange.WEEK) 7L else 30L
        val cutoff = state.today.minus(cutoffDays, ChronoUnit.DAYS)

        val relevant = state.transactions.filter {
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

private fun moneyShort(amount: Double): String = "৳" + "%,.0f".format(amount)
