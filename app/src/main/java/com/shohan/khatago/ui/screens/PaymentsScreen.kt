package com.shohan.khatago.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shohan.khatago.domain.CurrencyOption
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.domain.RecordCalculator
import com.shohan.khatago.domain.RecordStatus
import com.shohan.khatago.domain.RecordType
import com.shohan.khatago.ui.Canvas
import com.shohan.khatago.ui.Danger
import com.shohan.khatago.ui.Emerald
import com.shohan.khatago.ui.MainViewModel
import com.shohan.khatago.ui.Warning
import com.shohan.khatago.ui.components.AppCard
import com.shohan.khatago.ui.components.EmptyState
import com.shohan.khatago.ui.components.RecordRow
import com.shohan.khatago.ui.components.SectionTitle

@Composable
fun PaymentsScreen(viewModel: MainViewModel, onRecordPayment: () -> Unit) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currency = CurrencyOption.find(settings.currencyCode)
    var tab by remember { mutableIntStateOf(0) }
    val obligations = records.filter { it.type.isObligation && it.remainingMinor > 0 }
    val overdue = obligations.filter { RecordCalculator.status(it) == RecordStatus.OVERDUE }
    val upcoming = obligations.filter { val s = RecordCalculator.status(it); s == RecordStatus.DUE_TODAY || s == RecordStatus.DUE_SOON || s == RecordStatus.UPCOMING }
    val paid = records.filter { it.type.isObligation && it.paidMinor > 0 }.sortedByDescending { it.updatedAt }
    val shown = when (tab) { 1 -> upcoming; 2 -> overdue; else -> paid }
    LazyColumn(Modifier.fillMaxSize().background(Canvas), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Payments", style = MaterialTheme.typography.headlineSmall); Text("Stay ahead of every due date.", color = Color(0xFF708078), modifier = Modifier.padding(top = 4.dp)) }
        item { Button(onClick = onRecordPayment, modifier = Modifier.fillMaxSize(), colors = ButtonDefaults.buttonColors(containerColor = Emerald)) { Text("Record a payment") } }
        item {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(tab == 0, { tab = 0 }, label = { Text("Recently paid") }) }
                item { FilterChip(tab == 1, { tab = 1 }, label = { Text("Upcoming ${if (upcoming.isNotEmpty()) "• ${upcoming.size}" else ""}") }) }
                item { FilterChip(tab == 2, { tab = 2 }, label = { Text("Overdue ${if (overdue.isNotEmpty()) "• ${overdue.size}" else ""}") }) }
            }
        }
        item { SectionTitle(when (tab) { 1 -> "Due soon"; 2 -> "Needs attention"; else -> "Payment history" }) }
        if (shown.isEmpty()) item { AppCard { EmptyState(if (tab == 2) "Nothing overdue" else "No payments here", if (tab == 2) "A clear slate. Keep it up." else "Your payment activity will appear here.") } }
        else items(shown, key = { it.id }) { record -> AppCard { RecordRow(record, currency, showStatus = true) } }
    }
}
