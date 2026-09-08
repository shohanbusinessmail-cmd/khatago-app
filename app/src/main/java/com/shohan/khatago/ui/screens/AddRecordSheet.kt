package com.shohan.khatago.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.domain.RecordType
import com.shohan.khatago.ui.Canvas
import com.shohan.khatago.ui.Emerald
import com.shohan.khatago.ui.EmeraldDark
import com.shohan.khatago.ui.Line
import com.shohan.khatago.ui.MainViewModel
import com.shohan.khatago.ui.components.AppCard
import com.shohan.khatago.ui.components.RecordRow
import java.time.LocalDate

private data class ActionOption(val type: RecordType?, val label: String, val description: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val payment: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordSheet(viewModel: MainViewModel, preset: RecordType?, onDismiss: () -> Unit) {
    val records by viewModel.records.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currency = com.shohan.khatago.domain.CurrencyOption.find(settings.currencyCode)
    var type by remember { mutableStateOf(preset) }
    var paymentMode by remember { mutableStateOf(false) }
    var paymentRecord by remember { mutableStateOf<FinancialRecord?>(null) }
    var title by remember { mutableStateOf("") }
    var counterparty by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var dueText by remember { mutableStateOf("") }
    var installment by remember { mutableStateOf("") }
    var installmentCount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Canvas) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (paymentMode) "Record a payment" else if (type == null) "Add a record" else "New ${type!!.label}", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onDismiss) { Text("Close") }
            }
            formError?.let { Text(it, color = Color(0xFFC74747), style = MaterialTheme.typography.bodySmall) }
            if (type == null && !paymentMode) {
                Text("Choose what you want to track", color = Color(0xFF5E6E65))
                val options = listOf(
                    ActionOption(RecordType.SHOP_CREDIT, "Shop credit", "A purchase you will pay later", Icons.Default.ShoppingCart),
                    ActionOption(RecordType.LOAN, "Loan", "Bank or NGO repayment", Icons.Default.AccountBalance),
                    ActionOption(RecordType.EMI, "EMI purchase", "A product paid in installments", Icons.Default.CreditCard),
                    ActionOption(RecordType.BORROWED, "Borrow money", "Money you owe to someone", Icons.Default.Handshake),
                    ActionOption(RecordType.LENT, "Lend money", "Money someone owes you", Icons.Default.AttachMoney),
                    ActionOption(RecordType.INCOME, "Income", "Salary, business or other income", Icons.Default.TrendingUp),
                    ActionOption(RecordType.EXPENSE, "Expense", "Everyday spending", Icons.Default.TrendingDown),
                    ActionOption(null, "Payment", "Reduce an existing balance", Icons.Default.Payments, true)
                )
                options.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { option ->
                            AppCard(Modifier.weight(1f), onClick = { if (option.payment) paymentMode = true else type = option.type }) {
                                Icon(option.icon, null, tint = Emerald, modifier = Modifier.size(27.dp))
                                Spacer(Modifier.height(10.dp)); Text(option.label, fontWeight = FontWeight.Bold)
                                Text(option.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF68776F), modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            } else if (paymentMode) {
                Text("Select an open balance", color = Color(0xFF5E6E65))
                if (records.none { it.type.isObligation && it.remainingMinor > 0 }) Text("There are no unpaid records yet. Add a credit, loan or lending record first.", color = Color(0xFF5E6E65))
                records.filter { it.type.isObligation && it.remainingMinor > 0 }.forEach { record ->
                    AppCard(Modifier.fillMaxWidth(), onClick = { paymentRecord = record }) { RecordRow(record, currency, showStatus = false) }
                }
                paymentRecord?.let { selected ->
                    Text("Payment for ${selected.title}", style = MaterialTheme.typography.titleMedium, color = EmeraldDark)
                    AmountField(amount, { amount = it }, "Payment amount")
                    OutlinedTextField(dateText, { dateText = it }, label = { Text("Payment date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(paymentMethod, { paymentMethod = it }, label = { Text("Payment method (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(reference, { reference = it }, label = { Text("Reference (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(notes, { notes = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    Button(onClick = {
                        val parsedDate = runCatching { LocalDate.parse(dateText) }.getOrNull()
                        if (parsedDate == null) formError = "Use a valid date in YYYY-MM-DD format."
                        else { viewModel.recordPayment(selected, amount, parsedDate, paymentMethod, reference, notes); onDismiss() }
                    }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Emerald)) { Text("Save payment", fontWeight = FontWeight.Bold) }
                }
            } else {
                val selectedType = type!!
                Text(selectedType.description, color = Color(0xFF5E6E65))
                OutlinedTextField(title, { title = it }, label = { Text(if (selectedType == RecordType.EXPENSE) "What was it for?" else "Name / description") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(counterparty, { counterparty = it }, label = { Text(if (selectedType == RecordType.SHOP_CREDIT) "Shop name" else if (selectedType == RecordType.LOAN) "Institution" else if (selectedType == RecordType.INCOME) "Source (optional)" else "Person / provider (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                AmountField(amount, { amount = it }, when (selectedType) { RecordType.INCOME -> "Income amount"; RecordType.EXPENSE -> "Expense amount"; else -> "Total amount" })
                OutlinedTextField(category, { category = it }, label = { Text("Category (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(dateText, { dateText = it }, label = { Text("Date YYYY-MM-DD") }, singleLine = true, modifier = Modifier.weight(1f))
                    if (selectedType.isObligation) OutlinedTextField(dueText, { dueText = it }, label = { Text("Due date") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                if (selectedType == RecordType.LOAN || selectedType == RecordType.EMI) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AmountField(installment, { installment = it }, "Installment", Modifier.weight(1f))
                        OutlinedTextField(installmentCount, { installmentCount = it }, label = { Text("No. of payments") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                    }
                }
                if (selectedType == RecordType.SHOP_CREDIT || selectedType == RecordType.BORROWED || selectedType == RecordType.LENT) OutlinedTextField(phone, { phone = it }, label = { Text("Phone (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(paymentMethod, { paymentMethod = it }, label = { Text("Payment method (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Button(onClick = {
                    val parsedDate = runCatching { LocalDate.parse(dateText) }.getOrNull()
                    val parsedDue = dueText.trim().takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    if (parsedDate == null) formError = "Use a valid date in YYYY-MM-DD format."
                    else if (dueText.isNotBlank() && parsedDue == null) formError = "Use a valid due date in YYYY-MM-DD format."
                    else {
                        viewModel.addRecord(selectedType, title, counterparty, category, amount, parsedDate, parsedDue, installment.takeIf { selectedType == RecordType.LOAN || selectedType == RecordType.EMI }, installmentCount.takeIf { selectedType == RecordType.LOAN || selectedType == RecordType.EMI }, notes, paymentMethod, phone)
                        onDismiss()
                    }
                }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Emerald)) { Text("Save ${selectedType.label}", fontWeight = FontWeight.Bold) }
                Text("You can add partial payments later. KhataGo will never silently overpay a balance.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF68776F))
            }
        }
    }
}

@Composable
private fun AmountField(value: String, onValue: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(value, onValue, label = { Text(label) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = modifier.fillMaxWidth())
}
