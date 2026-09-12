package com.myapp.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myapp.tracker.data.AmountSort
import com.myapp.tracker.data.Debt
import com.myapp.tracker.data.DebtStatus
import com.myapp.tracker.data.DebtStatusFilter
import com.myapp.tracker.data.DueSort
import com.myapp.tracker.data.LogsSubtab
import com.myapp.tracker.data.LogsUiState
import com.myapp.tracker.data.LogsViewModel
import com.myapp.tracker.data.PriceSort
import com.myapp.tracker.data.SortOrder
import com.myapp.tracker.data.TimeFilter
import com.myapp.tracker.data.Transaction
import com.myapp.tracker.data.TxType
import com.myapp.tracker.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private fun money(amount: Double): String = "৳" + "%,.2f".format(Locale.US, amount)

@Composable
fun LogsScreen(viewModel: LogsViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Ledger & Logs", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Filter, search, and review your logs.", fontSize = 12.sp, color = TextSecondary)
        }

        SubtabSwitcher(current = state.subtab, onSelect = { viewModel.switchSubtab(it) })

        when (state.subtab) {
            LogsSubtab.TRANSACTIONS -> TransactionsSubtab(state, viewModel)
            LogsSubtab.DEBTS -> DebtReceivableSubtab(
                title = "Total Owed",
                amountColor = BrandRed,
                searchPlaceholder = "Search by Person Name...",
                total = state.debtsTotalPending,
                status = state.debtsStatus,
                list = state.debtsList,
                search = state.debtsSearch,
                dueSort = state.debtsDueSort,
                amountSort = state.debtsAmountSort,
                onSearchChange = { viewModel.setDebtsSearch(it) },
                onStatusChange = { viewModel.setDebtsStatusFilter(it) },
                onDueSortClick = { viewModel.cycleDebtsDueSort() },
                onAmountSortClick = { viewModel.cycleDebtsAmountSort() },
                onToggleStatus = { viewModel.toggleDebtStatus(it) }
            )
            LogsSubtab.RECEIVABLES -> DebtReceivableSubtab(
                title = "Total Collectible",
                amountColor = BrandGreen,
                searchPlaceholder = "Search by Person Name...",
                total = state.recvTotalPending,
                status = state.recvStatus,
                list = state.recvList,
                search = state.recvSearch,
                dueSort = state.recvDueSort,
                amountSort = state.recvAmountSort,
                onSearchChange = { viewModel.setRecvSearch(it) },
                onStatusChange = { viewModel.setRecvStatusFilter(it) },
                onDueSortClick = { viewModel.cycleRecvDueSort() },
                onAmountSortClick = { viewModel.cycleRecvAmountSort() },
                onToggleStatus = { viewModel.toggleDebtStatus(it) }
            )
        }
    }
}

@Composable
private fun SubtabSwitcher(current: LogsSubtab, onSelect: (LogsSubtab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PillBackground, RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SubtabButton("Transactions", current == LogsSubtab.TRANSACTIONS, Modifier.weight(1f)) { onSelect(LogsSubtab.TRANSACTIONS) }
        SubtabButton("Debts", current == LogsSubtab.DEBTS, Modifier.weight(1f)) { onSelect(LogsSubtab.DEBTS) }
        SubtabButton("Receivables", current == LogsSubtab.RECEIVABLES, Modifier.weight(1f)) { onSelect(LogsSubtab.RECEIVABLES) }
    }
}

@Composable
private fun SubtabButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) BrandRed else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SearchField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PillBackground, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FilterChip(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0x22000000), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BrandRed, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ---------------- Transactions ----------------

@Composable
private fun TransactionsSubtab(state: LogsUiState, viewModel: LogsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SearchField(state.txSearch, "Search by Category...") { viewModel.setTxSearch(it) }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                Icons.Filled.Sort,
                if (state.txSort == SortOrder.NEWEST) "Newest" else "Oldest",
                Modifier.weight(1f)
            ) { viewModel.cycleTxSort() }
            FilterChip(
                Icons.Filled.CalendarMonth,
                if (state.txTime == TimeFilter.THIS_MONTH) "Month" else "All Time",
                Modifier.weight(1f)
            ) { viewModel.cycleTxTime() }
            FilterChip(
                Icons.Filled.AttachMoney,
                when (state.txPrice) {
                    PriceSort.NONE -> "All Amounts"
                    PriceSort.HIGH_TO_LOW -> "High \u2192 Low"
                    PriceSort.LOW_TO_HIGH -> "Low \u2192 High"
                },
                Modifier.weight(1f)
            ) { viewModel.cycleTxPrice() }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(PillBackground)
                    .clickable { viewModel.clearAllTransactions() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Clear history", tint = BrandRed, modifier = Modifier.size(15.dp))
            }
        }

        if (state.txList.isEmpty()) {
            EmptyState("No transactions found.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.txList.forEach { tx -> TransactionCard(tx) }
            }
        }
    }
}

private fun categoryIcon(category: String): ImageVector = when (category) {
    "Food" -> Icons.Filled.Restaurant
    "Transport" -> Icons.Filled.DirectionsBus
    "Utilities" -> Icons.Filled.Bolt
    "Entertainment" -> Icons.Filled.Movie
    "Income" -> Icons.Filled.TrendingUp
    else -> Icons.Filled.Receipt
}

@Composable
private fun TransactionCard(tx: Transaction) {
    val isIncoming = tx.type == TxType.INCOME
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(PillBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(categoryIcon(tx.category), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.category, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(tx.title, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                tx.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                fontSize = 10.sp,
                color = TextMuted
            )
        }
        Text(
            (if (isIncoming) "+" else "-") + money(tx.amount),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isIncoming) BrandGreen else BrandRed
        )
    }
}

// ---------------- Debts / Receivables ----------------

@Composable
private fun DebtReceivableSubtab(
    title: String,
    amountColor: Color,
    searchPlaceholder: String,
    total: Double,
    status: DebtStatusFilter,
    list: List<Debt>,
    search: String,
    dueSort: DueSort,
    amountSort: AmountSort,
    onSearchChange: (String) -> Unit,
    onStatusChange: (DebtStatusFilter) -> Unit,
    onDueSortClick: () -> Unit,
    onAmountSortClick: () -> Unit,
    onToggleStatus: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Text(
                "${title.uppercase()} (PENDING)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9CA3AF)
            )
            Spacer(Modifier.height(4.dp))
            Text(money(total), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = amountColor)
        }

        SearchField(search, searchPlaceholder, onSearchChange)

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            StatusToggle("Pending", Icons.Filled.Schedule, status == DebtStatusFilter.PENDING, Modifier.weight(1f)) {
                onStatusChange(DebtStatusFilter.PENDING)
            }
            StatusToggle("Settled", Icons.Filled.Handshake, status == DebtStatusFilter.SETTLED, Modifier.weight(1f)) {
                onStatusChange(DebtStatusFilter.SETTLED)
            }
            FilterChip(
                Icons.Filled.Flag,
                when (dueSort) {
                    DueSort.NONE -> "Priority"
                    DueSort.SOONEST -> "Soonest"
                    DueSort.LATEST -> "Latest"
                },
                Modifier.weight(1f)
            ) { onDueSortClick() }
            FilterChip(
                Icons.Filled.AttachMoney,
                when (amountSort) {
                    AmountSort.NONE -> "All Amounts"
                    AmountSort.HIGH_TO_LOW -> "High \u2192 Low"
                    AmountSort.LOW_TO_HIGH -> "Low \u2192 High"
                },
                Modifier.weight(1f)
            ) { onAmountSortClick() }
        }

        if (list.isEmpty()) {
            EmptyState("Nothing here yet.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                list.forEach { debt -> DebtCard(debt, amountColor, onToggleStatus) }
            }
        }
    }
}

@Composable
private fun StatusToggle(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) BrandRed else Color.White)
            .border(1.dp, if (selected) BrandRed else Color(0x22000000), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) Color.White else BrandRed, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else TextSecondary)
    }
}

@Composable
private fun DebtCard(debt: Debt, amountColor: Color, onToggleStatus: (String) -> Unit) {
    val today = LocalDate.now()
    val daysLeft = ChronoUnit.DAYS.between(today, debt.dueDate)
    val dueLabel = when {
        debt.status == DebtStatus.SETTLED -> "Settled"
        daysLeft < 0 -> "${-daysLeft} days overdue"
        daysLeft == 0L -> "Due today"
        else -> "Due in $daysLeft days"
    }
    val dueColor = when {
        debt.status == DebtStatus.SETTLED -> TextMuted
        daysLeft < 0 -> BrandRed
        else -> TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(debt.person, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(dueLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dueColor)
        }
        Text(money(debt.amount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = amountColor)
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (debt.status == DebtStatus.SETTLED) Color(0x1A16A34A) else PillBackground)
                .clickable { onToggleStatus(debt.id) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = if (debt.status == DebtStatus.SETTLED) "Mark as pending" else "Mark as settled",
                tint = if (debt.status == DebtStatus.SETTLED) BrandGreen else TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 12.sp, color = TextMuted)
    }
}
