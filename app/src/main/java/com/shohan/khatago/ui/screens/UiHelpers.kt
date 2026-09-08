package com.shohan.khatago.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shohan.khatago.domain.CurrencyOption
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.ui.components.RecordRow

@Composable
fun RecordList(records: List<FinancialRecord>, currency: CurrencyOption, onRecordClick: (FinancialRecord) -> Unit = {}) {
    LazyColumn {
        items(records, key = { it.id }) { record -> RecordRow(record, currency, { onRecordClick(record) }) }
        item { Spacer(Modifier.height(18.dp)) }
    }
}
