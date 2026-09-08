package com.shohan.khatago.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shohan.khatago.domain.CurrencyOption
import com.shohan.khatago.domain.RecordType
import com.shohan.khatago.ui.Canvas
import com.shohan.khatago.ui.Danger
import com.shohan.khatago.ui.Emerald
import com.shohan.khatago.ui.Ink
import com.shohan.khatago.ui.MainViewModel
import com.shohan.khatago.ui.Muted
import com.shohan.khatago.ui.Success
import com.shohan.khatago.ui.components.AppCard
import com.shohan.khatago.ui.components.MoneyStat
import com.shohan.khatago.ui.components.SectionTitle
import com.shohan.khatago.util.asCompactMoney
import com.shohan.khatago.util.asMoney
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(viewModel: MainViewModel) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val currency = CurrencyOption.find(settings.currencyCode)
    var range by remember { mutableIntStateOf(1) }
    val days = listOf(7, 30, 90, 365)[range]
    val from = LocalDate.now().minusDays((days - 1).toLong()).toEpochDay().toInt()
    val window = records.filter { it.dateEpochDay >= from }
    val income = window.filter { it.type == RecordType.INCOME }.sumOf { it.amountMinor }
    val expense = window.filter { it.type == RecordType.EXPENSE }.sumOf { it.amountMinor }
    val expenseGroups = window.filter { it.type == RecordType.EXPENSE }.groupBy { it.category.ifBlank { "Other" } }.mapValues { it.value.sumOf { r -> r.amountMinor } }.toList().sortedByDescending { it.second }
    val topCategory = expenseGroups.firstOrNull()
    LazyColumn(Modifier.fillMaxSize().background(Canvas), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Analytics", style = MaterialTheme.typography.headlineSmall); Text("A clearer view of your financial patterns.", color = Muted, modifier = Modifier.padding(top = 4.dp)) }
        item { androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("7 days", "30 days", "3 months", "1 year").forEachIndexed { i, label -> item { FilterChip(range == i, { range = i }, label = { Text(label) }) } } } }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppCard(Modifier.weight(1f)) { MoneyStat("Income", income, currency, Success) }
                AppCard(Modifier.weight(1f)) { MoneyStat("Expenses", expense, currency, Danger) }
            }
        }
        item {
            AppCard(Modifier.fillMaxWidth()) {
                Text("Income vs expense", style = MaterialTheme.typography.titleLarge)
                Text("Selected period", color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 3.dp))
                Spacer(Modifier.height(16.dp)); if (income == 0L && expense == 0L) Text("No income or expense records in this period.", color = Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 30.dp)) else CashFlowChart(income, expense)
                Row(Modifier.fillMaxWidth().padding(top = 13.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Net flow", color = Muted); Text((income - expense).asMoney(currency), color = if (income >= expense) Success else Danger, style = MaterialTheme.typography.titleMedium) }
            }
        }
        item {
            AppCard(Modifier.fillMaxWidth()) {
                Text("Outstanding snapshot", style = MaterialTheme.typography.titleLarge)
                Text("Current balances by what the money represents", color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 3.dp))
                Spacer(Modifier.height(16.dp)); OutstandingBars(records, currency)
            }
        }
        item { SectionTitle("Local insights") }
        item {
            AppCard(Modifier.fillMaxWidth()) {
                Insight("Your current outstanding balance is ${summary.outstandingMinor.asCompactMoney(currency)}.")
                if (summary.overdueCount > 0) Insight("${summary.overdueCount} obligation${if (summary.overdueCount == 1) " is" else "s are"} overdue. Review the Payments tab.") else Insight("No obligations are overdue right now.")
                if (topCategory != null) Insight("${topCategory.first} is your highest expense category for this period.") else Insight("Add expenses to see category insights here.")
                Text("These are simple summaries of your stored records, not professional financial advice.", color = Muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}

@Composable private fun Insight(text: String) { Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.CheckCircle, null, tint = Emerald, modifier = Modifier.size(18.dp)); Text(text, color = Ink, style = MaterialTheme.typography.bodyMedium) } }
@Composable private fun CashFlowChart(income: Long, expense: Long) { val max = maxOf(income, expense, 1L).toFloat(); Canvas(Modifier.fillMaxWidth().height(130.dp)) { val base = size.height - 15; val barWidth = size.width / 5f; drawRoundRect(Color(0xFFE5F4EC), Offset(barWidth * 1.1f, base - size.height * (income / max) * .8f), androidx.compose.ui.geometry.Size(barWidth * .72f, size.height * (income / max) * .8f), androidx.compose.ui.geometry.CornerRadius(14f)); drawRoundRect(Color(0xFFFFDED3), Offset(barWidth * 2.45f, base - size.height * (expense / max) * .8f), androidx.compose.ui.geometry.Size(barWidth * .72f, size.height * (expense / max) * .8f), androidx.compose.ui.geometry.CornerRadius(14f)); drawLine(Emerald, Offset(barWidth * 1.46f, base - 3), Offset(barWidth * 1.46f, base - size.height * (income / max) * .8f), 5f, StrokeCap.Round); drawLine(Color(0xFFF19B80), Offset(barWidth * 2.81f, base - 3), Offset(barWidth * 2.81f, base - size.height * (expense / max) * .8f), 5f, StrokeCap.Round) } }
@Composable private fun OutstandingBars(records: List<com.shohan.khatago.domain.FinancialRecord>, currency: CurrencyOption) { val types = listOf(RecordType.SHOP_CREDIT, RecordType.LOAN, RecordType.EMI, RecordType.BORROWED, RecordType.LENT); Column(verticalArrangement = Arrangement.spacedBy(13.dp)) { types.forEach { type -> val amount = records.filter { it.type == type }.sumOf { it.remainingMinor }; val max = records.filter { it.type.isObligation }.sumOf { it.remainingMinor }.coerceAtLeast(1L); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(type.label, Modifier.size(115.dp), color = Muted, style = MaterialTheme.typography.bodySmall); androidx.compose.material3.LinearProgressIndicator(progress = { (amount.toDouble() / max).toFloat() }, modifier = Modifier.weight(1f).height(8.dp), color = if (type == RecordType.LENT) Success else Emerald, trackColor = Color(0xFFE7EEE9)); Text(amount.asCompactMoney(currency), Modifier.padding(start = 10.dp), style = MaterialTheme.typography.labelMedium) } } } }
