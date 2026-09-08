package com.shohan.khatago.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shohan.khatago.domain.CurrencyOption
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.domain.Payment
import com.shohan.khatago.domain.RecordCalculator
import com.shohan.khatago.domain.RecordStatus
import com.shohan.khatago.ui.Canvas
import com.shohan.khatago.ui.Danger
import com.shohan.khatago.ui.Emerald
import com.shohan.khatago.ui.MainViewModel
import com.shohan.khatago.ui.Muted
import com.shohan.khatago.ui.components.AppCard
import com.shohan.khatago.ui.components.EmptyState
import com.shohan.khatago.ui.components.RecordRow
import com.shohan.khatago.ui.components.SectionTitle
import com.shohan.khatago.util.asDate
import com.shohan.khatago.util.asMoney

@Composable
fun PaymentsScreen(viewModel: MainViewModel, onRecordPayment: () -> Unit, onDeletePayment: (Payment) -> Unit) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currency = CurrencyOption.find(settings.currencyCode)
    var tab by remember { mutableIntStateOf(0) }
    var deleteTarget by remember { mutableStateOf<Payment?>(null) }
    val recordMap = records.associateBy { it.id }
    val obligations = records.filter { it.type.isObligation && it.remainingMinor > 0 }
    val overdue = obligations.filter { RecordCalculator.status(it) == RecordStatus.OVERDUE }
    val upcoming = obligations.filter { val s = RecordCalculator.status(it); s == RecordStatus.DUE_TODAY || s == RecordStatus.DUE_SOON || s == RecordStatus.UPCOMING }
    val history = payments.sortedByDescending { it.paidOnEpochDay }
    LazyColumn(Modifier.fillMaxSize().background(Canvas), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Payments", style = MaterialTheme.typography.headlineSmall); Text("Stay ahead of every due date.", color = Color(0xFF708078), modifier = Modifier.padding(top = 4.dp)) }
        item { Button(onClick = onRecordPayment, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Emerald)) { Text("Record a payment") } }
        item {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(tab == 0, { tab = 0 }, label = { Text("Payment history") }) }
                item { FilterChip(tab == 1, { tab = 1 }, label = { Text("Upcoming ${if (upcoming.isNotEmpty()) "• ${upcoming.size}" else ""}") }) }
                item { FilterChip(tab == 2, { tab = 2 }, label = { Text("Overdue ${if (overdue.isNotEmpty()) "• ${overdue.size}" else ""}") }) }
            }
        }
        item { SectionTitle(when (tab) { 1 -> "Due soon"; 2 -> "Needs attention"; else -> "Payment history" }) }
        when (tab) {
            1 -> if (upcoming.isEmpty()) item { AppCard { EmptyState("Nothing due yet", "Your upcoming obligations will appear here.") } } else items(upcoming, key = { it.id }) { record -> AppCard { RecordRow(record, currency) } }
            2 -> if (overdue.isEmpty()) item { AppCard { EmptyState("Nothing overdue", "A clear slate. Keep it up.") } } else items(overdue, key = { it.id }) { record -> AppCard { RecordRow(record, currency) } }
            else -> if (history.isEmpty()) item { AppCard { EmptyState("No payments yet", "Record a partial or final payment to start your history.") } } else items(history, key = { it.id }) { payment -> PaymentRow(payment, recordMap[payment.recordId], currency, { deleteTarget = payment }) }
        }
    }
    deleteTarget?.let { payment ->
        AlertDialog(onDismissRequest = { deleteTarget = null }, title = { Text("Delete this payment?") }, text = { Text("The payment will be removed and the linked balance will be recalculated. This does not delete the original record.") }, confirmButton = { Button(onClick = { onDeletePayment(payment); deleteTarget = null }, colors = ButtonDefaults.buttonColors(containerColor = Danger)) { Text("Delete payment") } }, dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } })
    }
}

@Composable
private fun PaymentRow(payment: Payment, record: FinancialRecord?, currency: CurrencyOption, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(record?.title ?: "Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(listOfNotNull(record?.counterparty?.takeIf { it.isNotBlank() }, payment.method.takeIf { it.isNotBlank() }).joinToString(" • "), color = Muted, style = MaterialTheme.typography.bodySmall)
            Text(payment.paidOnEpochDay.asDate(), color = Muted, style = MaterialTheme.typography.labelSmall)
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text(payment.amountMinor.asMoney(currency), color = Emerald, fontWeight = FontWeight.Bold)
            TextButton(onClick = onDelete, contentPadding = PaddingValues(0.dp)) { Text("Delete", color = Danger, style = MaterialTheme.typography.labelSmall) }
        }
    }
}
