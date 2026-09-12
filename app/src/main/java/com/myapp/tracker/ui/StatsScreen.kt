package com.myapp.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myapp.tracker.data.CategorySlice
import com.myapp.tracker.data.LiabilityInsights
import com.myapp.tracker.data.PartnerAmount
import com.myapp.tracker.data.SpendingInsights
import com.myapp.tracker.data.StatsSubtab
import com.myapp.tracker.data.StatsViewModel
import com.myapp.tracker.data.TimeFilter
import com.myapp.tracker.data.Transaction
import com.myapp.tracker.ui.theme.*
import java.util.Locale

private fun money(amount: Double): String = "৳" + "%,.0f".format(Locale.US, amount)

// Slightly darker than AppBackground so section cards read as distinct
// panels, matching the reference app's bg-gray-200/50 tone.
private val StatsCardBg = Color(0xFFE9E9EC)

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                if (state.subtab == StatsSubtab.SPENDING) "Spending Analytics" else "Debts & Receivables Analytics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                if (state.subtab == StatsSubtab.SPENDING) "Deep insights into your spending patterns."
                else "Insights into your outstanding debts and receivables.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        StatsSubtabSwitcher(current = state.subtab, onSelect = { viewModel.switchSubtab(it) })

        when (state.subtab) {
            StatsSubtab.SPENDING -> SpendingSubtab(
                insights = state.spending,
                time = state.spendTime,
                onCycleTime = { viewModel.cycleSpendTime() }
            )
            StatsSubtab.DEBTS -> LiabilitySubtab(state.liability)
        }
    }
}

@Composable
private fun StatsSubtabSwitcher(current: StatsSubtab, onSelect: (StatsSubtab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PillBackground, RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StatsSubtabButton("Spending", current == StatsSubtab.SPENDING, Modifier.weight(1f)) {
            onSelect(StatsSubtab.SPENDING)
        }
        StatsSubtabButton("Debts & Receivables", current == StatsSubtab.DEBTS, Modifier.weight(1f)) {
            onSelect(StatsSubtab.DEBTS)
        }
    }
}

@Composable
private fun StatsSubtabButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
private fun SectionCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StatsCardBg, RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = BrandRed, modifier = Modifier.size(13.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        content()
    }
}

// ---------------- Spending Analytics ----------------

@Composable
private fun SpendingSubtab(insights: SpendingInsights?, time: TimeFilter, onCycleTime: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            FilterChip(
                Icons.Filled.CalendarMonth,
                if (time == TimeFilter.THIS_MONTH) "This Month" else "All Time",
                Modifier
            ) { onCycleTime() }
        }

        if (insights == null) {
            EmptyState("No spending statistics available for this range.")
        } else {
            OverviewCard(insights)
            TopCategoryCard(insights.topCategory)
            InsightsCard(insights)
        }
    }
}

@Composable
private fun OverviewCard(insights: SpendingInsights) {
    SectionCard(Icons.Filled.InsertChart, "Overview") {
        Row(modifier = Modifier.fillMaxWidth()) {
            OverviewStat("Total Spent", money(insights.totalSpent), BrandRed, Modifier.weight(1f))
            VerticalDivider()
            OverviewStat("Daily Avg", money(insights.dailyAvg), TextPrimary, Modifier.weight(1f))
            VerticalDivider()
            OverviewStat("Transactions", insights.txCount.toString(), TextPrimary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun OverviewStat(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
    }
}

@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0x40000000)))
}

@Composable
private fun TopCategoryCard(topCategory: CategorySlice) {
    SectionCard(Icons.Filled.TrendingUp, "Top Spending Category") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhiteTint, RoundedCornerShape(16.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(categoryIcon(topCategory.category), contentDescription = null, tint = BrandRed, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(topCategory.category, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandRed)
                    Text(
                        "${"%.1f".format(Locale.US, topCategory.percent * 100)}% of total spending",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandRedHover
                    )
                }
            }
            Text(money(topCategory.amount), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = BrandRed)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0x40000000), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(topCategory.percent.coerceIn(0.02f, 1f))
                    .height(8.dp)
                    .background(BrandRed, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun InsightsCard(insights: SpendingInsights) {
    SectionCard(Icons.Filled.Lightbulb, "Spending Insights") {
        InsightRow(
            icon = Icons.Filled.Receipt,
            title = "Biggest Expense",
            subtitle = biggestExpenseSubtitle(insights.biggestExpense),
            value = money(insights.biggestExpense.amount)
        )
        InsightRow(
            icon = Icons.Filled.CalendarMonth,
            title = "Most Active Day",
            subtitle = "${insights.mostActiveDayCount} transactions",
            value = insights.mostActiveDay
        )
        InsightRow(
            icon = Icons.Filled.Functions,
            title = "Avg. per Transaction",
            subtitle = "across ${insights.txCount} entries",
            value = money(insights.avgPerTx)
        )
        InsightRow(
            icon = Icons.Filled.Category,
            title = "Categories Used",
            subtitle = "unique spending categories",
            value = insights.categoriesUsedCount.toString()
        )
    }
}

private fun biggestExpenseSubtitle(tx: Transaction): String =
    if (tx.title.isNotBlank()) "${tx.title} (${tx.category})" else tx.category

@Composable
private fun InsightRow(icon: ImageVector, title: String, subtitle: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(subtitle, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextPrimary)
    }
}

// ---------------- Debts & Receivables (Liability) Analytics ----------------

@Composable
private fun LiabilitySubtab(liability: LiabilityInsights) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NetPositionCard(liability)
        SettlementProgressCard(liability)
        TopPartnersCard(liability)
    }
}

@Composable
private fun NetPositionCard(liability: LiabilityInsights) {
    val positive = liability.net >= 0
    val netColor = if (positive) BrandGreen else BrandRed
    SectionCard(if (positive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown, "Net Position") {
        Text(
            (if (positive) "+" else "-") + money(kotlin.math.abs(liability.net)),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = netColor
        )
        Text(
            when {
                liability.net > 0 -> "Others owe you more than you owe."
                liability.net < 0 -> "You owe more than others owe you."
                else -> "You are settled up."
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
        HorizontalDivider(color = Color(0x40000000))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("YOU OWE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(money(liability.totalOwe), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = BrandRed)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("YOU ARE OWED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(money(liability.totalOwed), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = BrandGreen)
            }
        }
    }
}

@Composable
private fun SettlementProgressCard(liability: LiabilityInsights) {
    SectionCard(Icons.Filled.CheckCircle, "Settlement Progress") {
        SettlementBar(
            label = "Debts Repaid",
            fraction = "${liability.debtsSettledCount}/${liability.debtsTotalCount} cleared (${liability.debtsRepaidPct.toInt()}%)",
            percent = liability.debtsRepaidPct,
            color = BrandRed
        )
        SettlementBar(
            label = "Receivables Collected",
            fraction = "${liability.receivablesSettledCount}/${liability.receivablesTotalCount} cleared (${liability.receivablesCollectedPct.toInt()}%)",
            percent = liability.receivablesCollectedPct,
            color = BrandGreen
        )
    }
}

@Composable
private fun SettlementBar(label: String, fraction: String, percent: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Text(fraction, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0x40000000), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                    .height(8.dp)
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun TopPartnersCard(liability: LiabilityInsights) {
    SectionCard(Icons.Filled.PeopleAlt, "Top Partners Breakdown") {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("TOP PEOPLE YOU OWE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            if (liability.topOwe.isEmpty()) {
                Text("No pending debts.", fontSize = 10.sp, color = TextMuted)
            } else {
                liability.topOwe.forEach { PartnerRow(it, BrandRed) }
            }
        }
        HorizontalDivider(color = Color(0x40000000))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("TOP PEOPLE WHO OWE YOU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            if (liability.topOwed.isEmpty()) {
                Text("No pending receivables.", fontSize = 10.sp, color = TextMuted)
            } else {
                liability.topOwed.forEach { PartnerRow(it, BrandGreen) }
            }
        }
    }
}

@Composable
private fun PartnerRow(partner: PartnerAmount, amountColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(partner.person, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(money(partner.amount), fontSize = 11.sp, fontWeight = FontWeight.Black, color = amountColor)
    }
}
