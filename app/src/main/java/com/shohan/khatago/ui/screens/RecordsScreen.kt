package com.shohan.khatago.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shohan.khatago.domain.CurrencyOption
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.domain.RecordCalculator
import com.shohan.khatago.domain.RecordType
import com.shohan.khatago.ui.Canvas
import com.shohan.khatago.ui.Danger
import com.shohan.khatago.ui.Emerald
import com.shohan.khatago.ui.MainViewModel
import com.shohan.khatago.ui.components.AppCard
import com.shohan.khatago.ui.components.EmptyState
import com.shohan.khatago.ui.components.RecordRow
import com.shohan.khatago.ui.components.SectionTitle
import com.shohan.khatago.ui.components.ThinProgress
import com.shohan.khatago.util.asMoney
import com.shohan.khatago.util.asDate

@Composable
fun RecordsScreen(viewModel: MainViewModel, onAdd: (RecordType) -> Unit) {
    val records by viewModel.visibleRecords.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selected by viewModel.selectedType.collectAsStateWithLifecycle()
    val currency = CurrencyOption.find(settings.currencyCode)
    var openRecord by remember { mutableStateOf<FinancialRecord?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().background(Canvas), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { Text("Records", style = MaterialTheme.typography.headlineSmall); Text("Everything you track, in one place.", color = Color(0xFF708078), modifier = Modifier.padding(top = 4.dp)) }
        item { ScreenSearch(query, viewModel::setQuery) }
        item {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected == null, { viewModel.setType(null) }, label = { Text("All") }) }
                items(RecordType.entries.size) { index -> val item = RecordType.entries[index]; FilterChip(selected == item, { viewModel.setType(item) }, label = { Text(item.label) }) }
            }
        }
        item { SectionTitle("${records.size} ${if (records.size == 1) "record" else "records"}") }
        if (records.isEmpty()) item { AppCard { EmptyState(if (query.isBlank()) "No records yet" else "No matches", if (query.isBlank()) "Add your first credit, payment, income or expense to get started." else "Try a different name, shop, person or category.", if (query.isBlank()) "Add a record" else null, if (query.isBlank()) ({ onAdd(RecordType.EXPENSE) }) else null) } }
        else items(records, key = { it.id }) { record -> RecordRow(record, currency, onClick = { openRecord = record }) }
    }

    openRecord?.let { record ->
        val status = RecordCalculator.status(record)
        AlertDialog(onDismissRequest = { openRecord = null }, title = { Text(record.title) }, text = {
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(listOfNotNull(record.counterparty.takeIf { it.isNotBlank() }, record.type.label).joinToString(" • "), color = Color(0xFF708078))
                Text("${record.amountMinor.asMoney(currency)} total", style = MaterialTheme.typography.titleLarge)
                if (record.type.isObligation) {
                    Text("${record.paidMinor.asMoney(currency)} paid  •  ${record.remainingMinor.asMoney(currency)} remaining")
                    ThinProgress(record.progress)
                    Text(status.label + (record.dueDateEpochDay?.let { " • due ${it.asDate()}" } ?: ""), color = if (status.name == "OVERDUE") Danger else Emerald)
                } else Text("Recorded on ${record.dateEpochDay.asDate()}")
                if (record.notes.isNotBlank()) Text(record.notes, color = Color(0xFF708078))
            }
        }, confirmButton = { TextButton(onClick = { openRecord = null }) { Text("Done") } }, dismissButton = { TextButton(onClick = { confirmDelete = true }) { Text("Delete", color = Danger) } })
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete this record?") }, text = { Text("The record and its payment history will be removed from this device.") }, confirmButton = { Button(onClick = { openRecord?.let(viewModel::deleteRecord); openRecord = null; confirmDelete = false }, colors = ButtonDefaults.buttonColors(containerColor = Danger)) { Text("Delete") } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
}
