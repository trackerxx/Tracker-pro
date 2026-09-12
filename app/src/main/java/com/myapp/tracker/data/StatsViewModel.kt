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

enum class StatsSubtab { SPENDING, DEBTS }

/** One row in the "Top Partners Breakdown" lists (who you owe / who owes you). */
data class PartnerAmount(val person: String, val amount: Double)

/** Everything the Spending subtab shows. Null when there's no spending in scope. */
data class SpendingInsights(
    val totalSpent: Double,
    val dailyAvg: Double,
    val txCount: Int,
    val topCategory: CategorySlice,
    val biggestExpense: Transaction,
    val mostActiveDay: String,
    val mostActiveDayCount: Int,
    val avgPerTx: Double,
    val categoriesUsedCount: Int,
    /** Top 6 categories by spend, for the bar chart. */
    val categoryBars: List<CategorySlice>
)

/** Everything the Debts & Receivables subtab shows. Always present, even with zero debts. */
data class LiabilityInsights(
    val totalOwe: Double,
    val totalOwed: Double,
    val net: Double,
    val debtsSettledCount: Int,
    val debtsTotalCount: Int,
    val debtsRepaidPct: Float,
    val receivablesSettledCount: Int,
    val receivablesTotalCount: Int,
    val receivablesCollectedPct: Float,
    val topOwe: List<PartnerAmount>,
    val topOwed: List<PartnerAmount>
)

private data class StatsLocalState(
    val subtab: StatsSubtab = StatsSubtab.SPENDING,
    // Mirrors the same THIS_MONTH / ALL_TIME toggle Logs uses for
    // Transactions, since Spending Analytics is scoped the same way.
    val spendTime: TimeFilter = TimeFilter.THIS_MONTH
)

data class StatsUiState(
    val subtab: StatsSubtab = StatsSubtab.SPENDING,
    val spendTime: TimeFilter = TimeFilter.THIS_MONTH,
    val spending: SpendingInsights? = null,
    val liability: LiabilityInsights = LiabilityInsights(
        totalOwe = 0.0, totalOwed = 0.0, net = 0.0,
        debtsSettledCount = 0, debtsTotalCount = 0, debtsRepaidPct = 0f,
        receivablesSettledCount = 0, receivablesTotalCount = 0, receivablesCollectedPct = 0f,
        topOwe = emptyList(), topOwed = emptyList()
    )
)

class StatsViewModel : ViewModel() {

    private val local = MutableStateFlow(StatsLocalState())
    private val today = LocalDate.now()

    val uiState: StateFlow<StatsUiState> = combine(
        local, AppData.transactions, AppData.debts
    ) { l, txs, debts ->
        StatsUiState(
            subtab = l.subtab,
            spendTime = l.spendTime,
            spending = computeSpendingInsights(txs, l.spendTime),
            liability = computeLiabilityInsights(debts)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    fun switchSubtab(tab: StatsSubtab) = local.update { it.copy(subtab = tab) }

    fun cycleSpendTime() = local.update {
        it.copy(spendTime = if (it.spendTime == TimeFilter.THIS_MONTH) TimeFilter.ALL_TIME else TimeFilter.THIS_MONTH)
    }

    // ---- Spending Analytics ----

    private fun computeSpendingInsights(all: List<Transaction>, time: TimeFilter): SpendingInsights? {
        var filtered = all.filter { it.type == TxType.EXPENSE }
        if (time == TimeFilter.THIS_MONTH) {
            filtered = filtered.filter { it.date.month == today.month && it.date.year == today.year }
        }
        if (filtered.isEmpty()) return null

        val totalSpent = filtered.sumOf { it.amount }
        val txCount = filtered.size

        val minDate = filtered.minOf { it.date }
        val maxDate = filtered.maxOf { it.date }
        val daySpan = maxOf(1, ChronoUnit.DAYS.between(minDate, maxDate).toInt() + 1)
        val dailyAvg = totalSpent / daySpan

        val groups = groupByCategory(filtered, totalSpent)
        val topCategory = groups.first()

        val biggest = filtered.maxByOrNull { it.amount }!!

        val dayCounts = filtered.groupingBy { it.date.dayOfWeek }.eachCount()
        val mostActiveDayEntry = dayCounts.entries.maxByOrNull { it.value }
        val mostActiveDay = mostActiveDayEntry?.key?.name
            ?.lowercase()
            ?.replaceFirstChar { it.uppercase() }
            ?: "—"
        val mostActiveDayCount = mostActiveDayEntry?.value ?: 0

        val avgPerTx = totalSpent / txCount

        return SpendingInsights(
            totalSpent = totalSpent,
            dailyAvg = dailyAvg,
            txCount = txCount,
            topCategory = topCategory,
            biggestExpense = biggest,
            mostActiveDay = mostActiveDay,
            mostActiveDayCount = mostActiveDayCount,
            avgPerTx = avgPerTx,
            categoriesUsedCount = groups.size,
            categoryBars = groups.take(6)
        )
    }

    private fun groupByCategory(transactions: List<Transaction>, total: Double): List<CategorySlice> {
        return transactions
            .groupBy { it.category }
            .map { (category, txs) ->
                val amount = txs.sumOf { it.amount }
                CategorySlice(category, amount, if (total > 0) (amount / total).toFloat() else 0f)
            }
            .sortedByDescending { it.amount }
    }

    // ---- Debts & Receivables (Liability) Analytics ----

    private fun computeLiabilityInsights(debts: List<Debt>): LiabilityInsights {
        val allDebts = debts.filter { it.type == DebtType.OWE }
        val allReceivables = debts.filter { it.type == DebtType.OWED }
        val pendingDebts = allDebts.filter { it.status == DebtStatus.PENDING }
        val pendingReceivables = allReceivables.filter { it.status == DebtStatus.PENDING }

        val totalOwe = pendingDebts.sumOf { it.amount }
        val totalOwed = pendingReceivables.sumOf { it.amount }
        val net = totalOwed - totalOwe

        val debtsSettledCount = allDebts.count { it.status == DebtStatus.SETTLED }
        val receivablesSettledCount = allReceivables.count { it.status == DebtStatus.SETTLED }
        val debtsRepaidPct = if (allDebts.isNotEmpty()) debtsSettledCount * 100f / allDebts.size else 0f
        val receivablesCollectedPct = if (allReceivables.isNotEmpty()) receivablesSettledCount * 100f / allReceivables.size else 0f

        return LiabilityInsights(
            totalOwe = totalOwe,
            totalOwed = totalOwed,
            net = net,
            debtsSettledCount = debtsSettledCount,
            debtsTotalCount = allDebts.size,
            debtsRepaidPct = debtsRepaidPct,
            receivablesSettledCount = receivablesSettledCount,
            receivablesTotalCount = allReceivables.size,
            receivablesCollectedPct = receivablesCollectedPct,
            topOwe = topPartners(pendingDebts),
            topOwed = topPartners(pendingReceivables)
        )
    }

    private fun topPartners(entries: List<Debt>): List<PartnerAmount> {
        return entries
            .groupBy { it.person }
            .map { (person, list) -> PartnerAmount(person, list.sumOf { it.amount }) }
            .sortedByDescending { it.amount }
    }
}
