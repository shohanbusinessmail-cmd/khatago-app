package com.shohan.khatago.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shohan.khatago.R
import com.shohan.khatago.domain.CurrencyOption
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.domain.RecordCalculator
import com.shohan.khatago.domain.RecordStatus
import com.shohan.khatago.ui.Danger
import com.shohan.khatago.ui.Emerald
import com.shohan.khatago.ui.Ink
import com.shohan.khatago.ui.Line
import com.shohan.khatago.ui.Muted
import com.shohan.khatago.ui.Success
import com.shohan.khatago.ui.Warning
import com.shohan.khatago.util.asDate
import com.shohan.khatago.util.asMoney

@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Int = 54) {
    Image(painterResource(R.drawable.khatago_logo), "KhataGo logo", modifier.size(size.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
}

@Composable
fun AppCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
        content = { Box(Modifier.padding(18.dp)) { content() } }
    )
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Ink)
        if (action != null && onAction != null) Text(action, color = Emerald, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable(onClick = onAction))
    }
}

@Composable
fun StatusChip(status: RecordStatus) {
    val (background, foreground, icon) = when (status) {
        RecordStatus.PAID, RecordStatus.COMPLETED -> Triple(Color(0xFFE7F7EF), Success, Icons.Default.CheckCircle)
        RecordStatus.OVERDUE -> Triple(Color(0xFFFFECEA), Danger, Icons.Default.WarningAmber)
        RecordStatus.DUE_TODAY, RecordStatus.DUE_SOON -> Triple(Color(0xFFFFF3DB), Warning, Icons.Default.Event)
        else -> Triple(Color(0xFFEAF3F0), Emerald, Icons.Default.Event)
    }
    Surface(color = background, shape = RoundedCornerShape(50), modifier = Modifier.semantics { contentDescription = status.label }) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, null, tint = foreground, modifier = Modifier.size(14.dp))
            Text(status.label, color = foreground, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun RecordRow(record: FinancialRecord, currency: CurrencyOption, onClick: (() -> Unit)? = null, showStatus: Boolean = true) {
    val status = RecordCalculator.status(record)
    Row(
        modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(if (record.type.isObligation) Color(0xFFE7F5EF) else if (record.type.name == "EXPENSE") Color(0xFFFFEEE9) else Color(0xFFEAF3FF)), contentAlignment = Alignment.Center) {
            Text(record.type.label.take(1), color = if (record.type.isObligation) Emerald else Ink, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text(record.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
            Text(listOfNotNull(record.counterparty.takeIf { it.isNotBlank() }, record.type.label).joinToString(" • "), color = Muted, style = MaterialTheme.typography.bodySmall)
            if (record.dueDateEpochDay != null && record.type.isObligation) Text(RecordCalculator.dueLabel(record), color = if (status == RecordStatus.OVERDUE) Danger else Muted, style = MaterialTheme.typography.labelSmall)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(if (record.type.name == "EXPENSE") "−${record.amountMinor.asMoney(currency)}" else record.amountMinor.asMoney(currency), fontWeight = FontWeight.Bold, color = if (record.type.name == "EXPENSE") Danger else Ink)
            if (showStatus) StatusChip(status)
        }
        Icon(Icons.Default.ChevronRight, "Open ${record.title}", tint = Muted, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun EmptyState(title: String, message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Box(Modifier.size(68.dp).clip(CircleShape).background(Color(0xFFE6F6EE)), contentAlignment = Alignment.Center) { BrandMark(size = 47) }
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(message, color = Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 28.dp))
        if (actionLabel != null && onAction != null) Text(actionLabel, color = Emerald, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable(onClick = onAction).padding(8.dp))
    }
}

@Composable
fun MoneyStat(label: String, value: Long, currency: CurrencyOption, accent: Color = Ink, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
        Text(value.asMoney(currency), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
    }
}

@Composable
fun ThinProgress(progress: Float, tint: Color = Emerald) {
    androidx.compose.material3.LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp)), color = tint, trackColor = Color(0xFFE7EEE9))
}
