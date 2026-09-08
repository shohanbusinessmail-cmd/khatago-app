package com.shohan.khatago.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shohan.khatago.domain.CurrencyOption
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.domain.RecordCalculator
import com.shohan.khatago.domain.RecordType
import com.shohan.khatago.ui.Canvas
import com.shohan.khatago.ui.Danger
import com.shohan.khatago.ui.Emerald
import com.shohan.khatago.ui.EmeraldDark
import com.shohan.khatago.ui.Ink
import com.shohan.khatago.ui.Line
import com.shohan.khatago.ui.MainViewModel
import com.shohan.khatago.ui.Muted
import com.shohan.khatago.ui.Success
import com.shohan.khatago.ui.Warning
import com.shohan.khatago.ui.components.AppCard
import com.shohan.khatago.ui.components.BrandMark
import com.shohan.khatago.ui.components.EmptyState
import com.shohan.khatago.ui.components.MoneyStat
import com.shohan.khatago.ui.components.RecordRow
import com.shohan.khatago.ui.components.SectionTitle
import com.shohan.khatago.util.asCompactMoney
import com.shohan.khatago.util.asMoney
import java.time.LocalDate

@Composable
fun HomeScreen(viewModel: MainViewModel, onAdd: (RecordType) -> Unit, onOpenRecords: () -> Unit) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val currency = CurrencyOption.find(settings.currencyCode)
    val name = settings.name.ifBlank { "there" }
    val greeting = when (java.time.LocalTime.now().hour) { in 5..11 -> "Good morning"; in 12..17 -> "Good afternoon"; else -> "Good evening" }
    val upcoming = records.filter { it.type.isObligation && it.remainingMinor > 0 && it.dueDateEpochDay != null }.sortedBy { it.dueDateEpochDay }.take(3)
    val recent = records.sortedByDescending { it.createdAt }.take(4)

    LazyColumn(Modifier.fillMaxSize().background(Canvas), contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("$greeting, $name", style = MaterialTheme.typography.headlineSmall, color = Ink); Text("Here’s your financial overview.", color = Muted, modifier = Modifier.padding(top = 3.dp)) }
                BrandMark()
            }
        }
        item {
            Box(Modifier.fillMaxWidth().background(EmeraldDark, RoundedCornerShape(27.dp)).padding(21.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total outstanding", color = Color(0xFFC7EEDB)); Icon(Icons.Default.MoreHoriz, null, tint = Color(0xFFC7EEDB)) }
                    Text(summary.outstandingMinor.asMoney(currency), color = Color.White, style = MaterialTheme.typography.displayLarge)
                    Text("What you still owe across credits, loans and borrowed money", color = Color(0xFFB6DACA), style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth().padding(top = 3.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HeroStat("Paid", summary.paidMinor, currency)
                        HeroStat("Owed to me", summary.owedToMeMinor, currency)
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                MetricCard("Due soon", "${summary.dueSoonCount} payments", summary.dueSoonMinor.asCompactMoney(currency), Warning, Modifier.weight(1f))
                MetricCard("Overdue", "${summary.overdueCount} payments", summary.overdueMinor.asCompactMoney(currency), Danger, Modifier.weight(1f))
            }
        }
        item {
            SectionTitle("Quick actions")
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickAction("Shop", Icons.Default.ShoppingCart, RecordType.SHOP_CREDIT, onAdd)
                QuickAction("Loan", Icons.Default.AccountBalance, RecordType.LOAN, onAdd)
                QuickAction("EMI", Icons.Default.CreditCard, RecordType.EMI, onAdd)
                QuickAction("Borrow", Icons.Default.Handshake, RecordType.BORROWED, onAdd)
                QuickAction("Lend", Icons.Default.AttachMoney, RecordType.LENT, onAdd)
            }
            Row(Modifier.fillMaxWidth().padding(top = 11.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickAction("Income", Icons.Default.TrendingUp, RecordType.INCOME, onAdd)
                QuickAction("Expense", Icons.Default.TrendingDown, RecordType.EXPENSE, onAdd)
                QuickAction("More", Icons.Default.Add, null, { onAdd(RecordType.EXPENSE) })
                Spacer(Modifier.size(52.dp)); Spacer(Modifier.size(52.dp))
            }
        }
        item {
            AppCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("Cash flow", style = MaterialTheme.typography.titleLarge); Text("Recorded income and expenses", color = Muted, style = MaterialTheme.typography.bodySmall) }
                    Text(if (summary.netFlowMinor >= 0) "+${summary.netFlowMinor.asCompactMoney(currency)}" else summary.netFlowMinor.asCompactMoney(currency), color = if (summary.netFlowMinor >= 0) Success else Danger, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(14.dp))
                if (summary.incomeMinor == 0L && summary.expenseMinor == 0L) Text("No cash flow recorded yet", color = Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 20.dp))
                else MiniCashFlow(summary.incomeMinor, summary.expenseMinor)
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) { LegendDot("Income", Emerald); LegendDot("Expenses", Color(0xFFF19B80)) }
            }
        }
        item { SectionTitle("Upcoming payments", if (upcoming.isNotEmpty()) "View all" else null, onOpenRecords) }
        if (upcoming.isEmpty()) item { AppCard(Modifier.fillMaxWidth()) { EmptyState("Nothing due yet", "Your upcoming obligations will appear here.", "Add a record", { onAdd(RecordType.SHOP_CREDIT) }) } }
        else items(upcoming, key = { it.id }) { record -> AppCard(Modifier.fillMaxWidth()) { RecordRow(record, currency, showStatus = true) } }
        item { SectionTitle("Recent activity", if (recent.isNotEmpty()) "See all" else null, onOpenRecords) }
        if (recent.isEmpty()) item { AppCard(Modifier.fillMaxWidth()) { EmptyState("Start your record", "Add your first entry and keep every financial detail in one place.", "Add record", { onAdd(RecordType.EXPENSE) }) } }
        else items(recent, key = { it.id }) { record -> RecordRow(record, currency) }
    }
}

@Composable private fun RowScope.HeroStat(label: String, value: Long, currency: CurrencyOption) { Column(Modifier.weight(1f)) { Text(label, color = Color(0xFFA4CDBA), style = MaterialTheme.typography.bodySmall); Text(value.asCompactMoney(currency), color = Color.White, fontWeight = FontWeight.Bold) } }
@Composable private fun MetricCard(title: String, sub: String, amount: String, color: Color, modifier: Modifier) { AppCard(modifier) { Text(title, color = color, fontWeight = FontWeight.Bold); Text(sub, color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 5.dp)); Text(amount, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 9.dp)) } }
@Composable private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, type: RecordType?, onAdd: (RecordType) -> Unit) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.size(54.dp).then(if (type != null) Modifier else Modifier)) { Box(Modifier.size(47.dp).background(Color.White, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) { androidx.compose.material3.IconButton(onClick = { if (type != null) onAdd(type) }) { Icon(icon, label, tint = Emerald) } }; Text(label, style = MaterialTheme.typography.labelSmall, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
@Composable private fun LegendDot(label: String, color: Color) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Box(Modifier.size(8.dp).background(color, androidx.compose.foundation.shape.CircleShape)); Text(label, color = Muted, style = MaterialTheme.typography.labelSmall) } }
@Composable private fun MiniCashFlow(income: Long, expense: Long) { val max = maxOf(income, expense, 1L).toFloat(); Row(Modifier.fillMaxWidth().height(80.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) { repeat(6) { i -> val inH = (income / max * (34 + i * 5)).coerceIn(7f, 68f); val outH = (expense / max * (22 + (5 - i) * 5)).coerceIn(7f, 55f); Column(Modifier.weight(1f), verticalArrangement = Arrangement.Bottom) { Box(Modifier.fillMaxWidth().height(inH.dp).background(Emerald, RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))); Spacer(Modifier.height(4.dp)); Box(Modifier.fillMaxWidth().height(outH.dp).background(Color(0xFFF19B80), RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))) } } } }
