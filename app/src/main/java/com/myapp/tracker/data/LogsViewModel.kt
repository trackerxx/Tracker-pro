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

enum class LogsSubtab { TRANSACTIONS, DEBTS, RECEIVABLES }
enum class SortOrder { NEWEST, OLDEST }
enum class TimeFilter { THIS_MONTH, ALL_TIME }
enum class PriceSort { NONE, HIGH_TO_LOW, LOW_TO_HIGH }
enum class DebtStatusFilter { PENDING, SETTLED }
enum class DueSort { NONE, SOONEST, LATEST }
enum class AmountSort { NONE, HIGH_TO_LOW, LOW_TO_HIGH }

private data class LogsLocalState(
    val subtab: LogsSubtab = LogsSubtab.TRANSACTIONS,
    // Transactions subtab
    val txSearch: String = "",
    val txSort: SortOrder = SortOrder.NEWEST,
    val txTime: TimeFilter = TimeFilter.THIS_MONTH,
    val txPrice: PriceSort = PriceSort.NONE,
    // Debts subtab
    val debtsSearch: String = "",
    val debtsStatus: DebtStatusFilter = DebtStatusFilter.PENDING,
    val debtsDueSort: DueSort = DueSort.NONE,
    val debtsAmountSort: AmountSort = AmountSort.NONE,
    // Receivables subtab
    val recvSearch: String = "",
    val recvStatus: DebtStatusFilter = DebtStatusFilter.PENDING,
    val recvDueSort: DueSort = DueSort.NONE,
    val recvAmountSort: AmountSort = AmountSort.NONE
)

data class LogsUiState(
    val subtab: LogsSubtab = LogsSubtab.TRANSACTIONS,
    val txSearch: String = "",
    val txSort: SortOrder = SortOrder.NEWEST,
    val txTime: TimeFilter = TimeFilter.THIS_MONTH,
    val txPrice: PriceSort = PriceSort.NONE,
    val txList: List<Transaction> = emptyList(),

    val debtsSearch: String = "",
    val debtsStatus: DebtStatusFilter = DebtStatusFilter.PENDING,
    val debtsDueSort: DueSort = DueSort.NONE,
    val debtsAmountSort: AmountSort = AmountSort.NONE,
    val debtsList: List<Debt> = emptyList(),
    val debtsTotalPending: Double = 0.0,

    val recvSearch: String = "",
    val recvStatus: DebtStatusFilter = DebtStatusFilter.PENDING,
    val recvDueSort: DueSort = DueSort.NONE,
    val recvAmountSort: AmountSort = AmountSort.NONE,
    val recvList: List<Debt> = emptyList(),
    val recvTotalPending: Double = 0.0
)

class LogsViewModel : ViewModel() {

    private val local = MutableStateFlow(LogsLocalState())
    private val today = LocalDate.now()

    val uiState: StateFlow<LogsUiState> = combine(
        local, AppData.transactions, AppData.debts
    ) { l, txs, debts ->
        LogsUiState(
            subtab = l.subtab,
            txSearch = l.txSearch,
            txSort = l.txSort,
            txTime = l.txTime,
            txPrice = l.txPrice,
            txList = filterTransactions(txs, l),

            debtsSearch = l.debtsSearch,
            debtsStatus = l.debtsStatus,
            debtsDueSort = l.debtsDueSort,
            debtsAmountSort = l.debtsAmountSort,
            debtsList = filterDebts(debts, DebtType.OWE, l.debtsSearch, l.debtsStatus, l.debtsDueSort, l.debtsAmountSort),
            debtsTotalPending = debts.filter { it.type == DebtType.OWE && it.status == DebtStatus.PENDING }.sumOf { it.amount },

            recvSearch = l.recvSearch,
            recvStatus = l.recvStatus,
            recvDueSort = l.recvDueSort,
            recvAmountSort = l.recvAmountSort,
            recvList = filterDebts(debts, DebtType.OWED, l.recvSearch, l.recvStatus, l.recvDueSort, l.recvAmountSort),
            recvTotalPending = debts.filter { it.type == DebtType.OWED && it.status == DebtStatus.PENDING }.sumOf { it.amount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LogsUiState())

    // ---- actions ----

    fun switchSubtab(tab: LogsSubtab) = local.update { it.copy(subtab = tab) }

    fun setTxSearch(q: String) = local.update { it.copy(txSearch = q) }
    fun cycleTxSort() = local.update {
        it.copy(txSort = if (it.txSort == SortOrder.NEWEST) SortOrder.OLDEST else SortOrder.NEWEST)
    }
    fun cycleTxTime() = local.update {
        it.copy(txTime = if (it.txTime == TimeFilter.THIS_MONTH) TimeFilter.ALL_TIME else TimeFilter.THIS_MONTH)
    }
    fun cycleTxPrice() = local.update {
        val next = when (it.txPrice) {
            PriceSort.NONE -> PriceSort.HIGH_TO_LOW
            PriceSort.HIGH_TO_LOW -> PriceSort.LOW_TO_HIGH
            PriceSort.LOW_TO_HIGH -> PriceSort.NONE
        }
        it.copy(txPrice = next)
    }
    fun clearAllTransactions() = AppData.clearAllTransactions()

    fun setDebtsSearch(q: String) = local.update { it.copy(debtsSearch = q) }
    fun setDebtsStatusFilter(status: DebtStatusFilter) = local.update { it.copy(debtsStatus = status) }
    fun cycleDebtsDueSort() = local.update { it.copy(debtsDueSort = nextDueSort(it.debtsDueSort)) }
    fun cycleDebtsAmountSort() = local.update { it.copy(debtsAmountSort = nextAmountSort(it.debtsAmountSort)) }

    fun setRecvSearch(q: String) = local.update { it.copy(recvSearch = q) }
    fun setRecvStatusFilter(status: DebtStatusFilter) = local.update { it.copy(recvStatus = status) }
    fun cycleRecvDueSort() = local.update { it.copy(recvDueSort = nextDueSort(it.recvDueSort)) }
    fun cycleRecvAmountSort() = local.update { it.copy(recvAmountSort = nextAmountSort(it.recvAmountSort)) }

    fun toggleDebtStatus(debtId: String) = AppData.toggleDebtStatus(debtId)

    private fun nextDueSort(current: DueSort) = when (current) {
        DueSort.NONE -> DueSort.SOONEST
        DueSort.SOONEST -> DueSort.LATEST
        DueSort.LATEST -> DueSort.NONE
    }

    private fun nextAmountSort(current: AmountSort) = when (current) {
        AmountSort.NONE -> AmountSort.HIGH_TO_LOW
        AmountSort.HIGH_TO_LOW -> AmountSort.LOW_TO_HIGH
        AmountSort.LOW_TO_HIGH -> AmountSort.NONE
    }

    // ---- filtering ----

    private fun filterTransactions(all: List<Transaction>, l: LogsLocalState): List<Transaction> {
        var list = all

        if (l.txTime == TimeFilter.THIS_MONTH) {
            list = list.filter { it.date.month == today.month && it.date.year == today.year }
        }

        if (l.txSearch.isNotBlank()) {
            val q = l.txSearch.trim()
            list = list.filter { it.category.contains(q, ignoreCase = true) || it.title.contains(q, ignoreCase = true) }
        }

        list = when (l.txPrice) {
            PriceSort.NONE -> list
            PriceSort.HIGH_TO_LOW -> list.sortedByDescending { it.amount }
            PriceSort.LOW_TO_HIGH -> list.sortedBy { it.amount }
        }

        if (l.txPrice == PriceSort.NONE) {
            list = when (l.txSort) {
                SortOrder.NEWEST -> list.sortedByDescending { it.date }
                SortOrder.OLDEST -> list.sortedBy { it.date }
            }
        }

        return list
    }

    private fun filterDebts(
        all: List<Debt>,
        type: DebtType,
        search: String,
        status: DebtStatusFilter,
        dueSort: DueSort,
        amountSort: AmountSort
    ): List<Debt> {
        var list = all.filter { it.type == type }

        list = when (status) {
            DebtStatusFilter.PENDING -> list.filter { it.status == DebtStatus.PENDING }
            DebtStatusFilter.SETTLED -> list.filter { it.status == DebtStatus.SETTLED }
        }

        if (search.isNotBlank()) {
            val q = search.trim()
            list = list.filter { it.person.contains(q, ignoreCase = true) }
        }

        list = when (amountSort) {
            AmountSort.NONE -> list
            AmountSort.HIGH_TO_LOW -> list.sortedByDescending { it.amount }
            AmountSort.LOW_TO_HIGH -> list.sortedBy { it.amount }
        }

        if (amountSort == AmountSort.NONE) {
            list = when (dueSort) {
                DueSort.NONE -> list.sortedBy { it.dueDate }
                DueSort.SOONEST -> list.sortedBy { ChronoUnit.DAYS.between(today, it.dueDate) }
                DueSort.LATEST -> list.sortedByDescending { ChronoUnit.DAYS.between(today, it.dueDate) }
            }
        }

        return list
    }
}
