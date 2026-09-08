package com.shohan.khatago.util

import com.shohan.khatago.domain.CurrencyOption
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun Long.asMoney(currency: CurrencyOption): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "${currency.symbol}${formatter.format(this / 100.0)}"
}

fun Long.asCompactMoney(currency: CurrencyOption): String {
    val value = this / 100.0
    return when {
        value >= 1_000_000 -> "${currency.symbol}${"%.1f".format(Locale.US, value / 1_000_000)}M"
        value >= 1_000 -> "${currency.symbol}${"%.1f".format(Locale.US, value / 1_000)}K"
        else -> asMoney(currency)
    }
}

fun Int.asDate(): String = LocalDate.ofEpochDay(toLong()).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
fun LocalDate.asDate(): String = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
fun String.capitalized(): String = lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
