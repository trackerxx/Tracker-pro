package com.myapp.tracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myapp.tracker.data.ChartRange
import com.myapp.tracker.data.Fund
import com.myapp.tracker.data.HomeViewModel
import com.myapp.tracker.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun money(amount: Double): String = "৳" + "%,.2f".format(Locale.US, amount)
private fun moneyShort(amount: Double): String = "৳" + "%,.0f".format(Locale.US, amount)

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        HeaderRow(userName = state.userName, today = state.today)

        BalanceCard(
            state = state,
            balance = viewModel.remainingBalance(state),
            spent = viewModel.spentFromActiveFund(state),
            onToggleExpand = { viewModel.toggleFundsExpanded() },
            onSwitchFund = { viewModel.switchActiveFund(it) }
        )

        UpdateButton(text = state.lastActivityText)

        DebtsCard(
            owe = viewModel.totalOwe(state),
            owed = viewModel.totalOwed(state)
        )

        CategoryBreakdownCard(
            fundName = state.activeFund?.name ?: "",
            range = state.chartRange,
            slices = viewModel.categoryBreakdown(state),
            onRangeChange = { viewModel.setChartRange(it) }
        )
    }
}

@Composable
private fun HeaderRow(userName: String, today: java.time.LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "WELCOME BACK",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Text(
                userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .background(PillBackground, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = BrandRed, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    today.format(DateTimeFormatter.ofPattern("EEE, d MMM")),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PillBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = BrandGreen)
            }
        }
    }
}

@Composable
private fun BalanceCard(
    state: com.myapp.tracker.data.HomeUiState,
    balance: Double,
    spent: Double,
    onToggleExpand: () -> Unit,
    onSwitchFund: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhiteTint, RoundedCornerShape(24.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "REMAINING BALANCE  ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        state.activeFund?.name ?: "",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandRed
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    money(balance),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(PillBackground, CircleShape)
                    .clickable { onToggleExpand() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Expand funds",
                    modifier = Modifier.rotate(if (state.fundsExpanded) 180f else 0f)
                )
            }
        }

        AnimatedVisibility(
            visible = state.fundsExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .fillMaxWidth()
            ) {
                HorizontalDivider(color = Color(0x33000000))
                Spacer(Modifier.height(10.dp))
                Text(
                    "TAP FUND TO SWITCH",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(Modifier.height(8.dp))
                state.funds.forEach { fund ->
                    FundRow(
                        fund = fund,
                        isActive = fund.id == state.activeFundId,
                        onClick = { onSwitchFund(fund.id) }
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row {
            Text(money(spent), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandRed)
            Spacer(Modifier.width(4.dp))
            Text("spent from this fund", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun FundRow(fund: Fund, isActive: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) Color(0x1AB91C1C) else Color.White)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) BrandRed else Color(0x22000000),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color(fund.colorHex), CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Text(fund.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text(moneyShort(fund.startingBalance), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
    }
}

@Composable
private fun UpdateButton(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PillBackground, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .background(Color.Black, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).background(BrandRed, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text("UPDATE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun DebtsCard(owe: Double, owed: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhiteTint, RoundedCornerShape(24.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Handshake, contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "DEBTS & RECEIVABLES OVERVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandRed
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("YOU OWE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(moneyShort(owe), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = BrandRed)
            }
            Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0x22000000)))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("YOU ARE OWED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(moneyShort(owed), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = BrandGreen)
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    fundName: String,
    range: ChartRange,
    slices: List<com.myapp.tracker.data.CategorySlice>,
    onRangeChange: (ChartRange) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black, RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.InsertChart, contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Category Breakdown", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(fundName, color = Color(0xFF9CA3AF), fontSize = 10.sp)
                }
            }
            Row(
                modifier = Modifier
                    .background(Color(0xFF27272A), RoundedCornerShape(50))
                    .padding(3.dp)
            ) {
                RangePill("WEEK", selected = range == ChartRange.WEEK) { onRangeChange(ChartRange.WEEK) }
                RangePill("MONTH", selected = range == ChartRange.MONTH) { onRangeChange(ChartRange.MONTH) }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (slices.isEmpty()) {
            Text(
                "No spending in this range yet.",
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                slices.forEach { slice ->
                    CategoryBar(slice)
                }
            }
        }
    }
}

@Composable
private fun RangePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) BrandRed else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CategoryBar(slice: com.myapp.tracker.data.CategorySlice) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(slice.category, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(moneyShort(slice.amount), color = Color(0xFF9CA3AF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFF27272A), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(slice.percent.coerceIn(0.02f, 1f))
                    .height(6.dp)
                    .background(BrandRed, RoundedCornerShape(50))
            )
        }
    }
}
