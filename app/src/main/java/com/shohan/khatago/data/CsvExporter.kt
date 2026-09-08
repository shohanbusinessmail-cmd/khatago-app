package com.shohan.khatago.data

import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.util.asMoney
import com.shohan.khatago.domain.CurrencyOption

object CsvExporter {
    fun records(records: List<FinancialRecord>, currency: CurrencyOption): String = buildString {
        appendLine("id,type,title,counterparty,category,amount,paid,remaining,date,due_date,status")
        records.forEach { r ->
            appendLine(listOf(r.id, r.type.label, r.title, r.counterparty, r.category, r.amountMinor.asMoney(currency), r.paidMinor.asMoney(currency), r.remainingMinor.asMoney(currency), r.dateEpochDay, r.dueDateEpochDay ?: "", "").joinToString(",") { it.toString().csv() })
        }
    }
    private fun String.csv(): String = if (contains(',') || contains('"') || contains('\n')) "\"${replace("\"", "\"\"")}\"" else this
}
