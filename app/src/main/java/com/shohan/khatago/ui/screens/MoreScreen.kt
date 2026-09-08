package com.shohan.khatago.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shohan.khatago.data.CsvExporter
import com.shohan.khatago.domain.CurrencyOption
import com.shohan.khatago.ui.Canvas
import com.shohan.khatago.ui.Danger
import com.shohan.khatago.ui.Emerald
import com.shohan.khatago.ui.Ink
import com.shohan.khatago.ui.MainViewModel
import com.shohan.khatago.ui.Muted
import com.shohan.khatago.ui.components.AppCard
import com.shohan.khatago.ui.components.BrandMark
import com.shohan.khatago.ui.components.SectionTitle
import com.shohan.khatago.util.AppConstants
import kotlinx.coroutines.launch

@Composable
fun MoreScreen(viewModel: MainViewModel, onRequestNotifications: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val records by viewModel.records.collectAsStateWithLifecycle()
    val currency = CurrencyOption.find(settings.currencyCode)
    var showProfile by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var name by remember(settings.name) { mutableStateOf(settings.name) }

    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch { writeText(context, uri, viewModel.makeBackup()) { } }
    }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch { writeText(context, uri, CsvExporter.records(records, currency)) { } }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch { val raw = runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader -> readLimited(reader, 10_000_000) } }.getOrNull(); if (raw.isNullOrBlank()) viewModel.restoreBackup("{}"); else viewModel.restoreBackup(raw) }
    }

    LazyColumn(Modifier.fillMaxSize().background(Canvas), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("More", style = MaterialTheme.typography.headlineSmall); Text("Your settings and your data, always in your hands.", color = Muted, modifier = Modifier.padding(top = 4.dp)) }; BrandMark() } }
        item { AppCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) { BrandMark(Modifier.size(54.dp), 54); Column(Modifier.weight(1f)) { Text(settings.name.ifBlank { "Your profile" }, style = MaterialTheme.typography.titleLarge); Text("${currency.code} • Offline-first", color = Muted, style = MaterialTheme.typography.bodySmall) }; TextButton(onClick = { showProfile = true }) { Text("Edit") } } } }
        item { SectionTitle("Preferences") }
        item { SettingRow(Icons.Default.Notifications, "Notifications", "Local due-date reminders") { Switch(settings.notificationsEnabled, { if (it) onRequestNotifications(); viewModel.updateNotifications(it) }) } }
        item { SettingRow(Icons.Default.Lock, "Security", "App lock can be added without affecting your data") { Text("On device", color = Muted, style = MaterialTheme.typography.labelSmall) } }
        item { SectionTitle("Data management") }
        item { SettingRow(Icons.Default.Backup, "Create backup", "Export a complete JSON copy of your records") { IconButton(onClick = { backupLauncher.launch("khatago-backup.json") }) { Icon(Icons.Default.ChevronRight, "Create backup") } } }
        item { SettingRow(Icons.Default.Restore, "Restore backup", "Validate a backup before replacing local data") { IconButton(onClick = { restoreLauncher.launch(arrayOf("application/json", "text/plain")) }) { Icon(Icons.Default.ChevronRight, "Restore backup") } } }
        item { SettingRow(Icons.Default.Description, "Export CSV", "A spreadsheet-friendly export of all records") { IconButton(onClick = { csvLauncher.launch("khatago-records.csv") }) { Icon(Icons.Default.ChevronRight, "Export CSV") } } }
        item { SectionTitle("About KhataGo") }
        item { SettingRow(Icons.Default.Info, "About", "Version ${AppConstants.VERSION_NAME} • Created by Shohan Khan") { IconButton(onClick = { showAbout = true }) { Icon(Icons.Default.ChevronRight, "About") } } }
        item { SettingRow(Icons.Default.PrivacyTip, "Privacy", "Local storage, no tracking, no advertising") { IconButton(onClick = { showPrivacy = true }) { Icon(Icons.Default.ChevronRight, "Privacy") } } }
        item { SectionTitle("Danger zone") }
        item { SettingRow(Icons.Default.DeleteOutline, "Delete all data", "This cannot be undone", tint = Danger) { TextButton(onClick = { showDelete = true }) { Text("Delete", color = Danger) } } }
    }

    if (showProfile) AlertDialog(onDismissRequest = { showProfile = false }, title = { Text("Your profile") }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Text("Currency", fontWeight = FontWeight.SemiBold); androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) { items(CurrencyOption.supported.size) { index -> val option = CurrencyOption.supported[index]; androidx.compose.material3.FilterChip(settings.currencyCode == option.code, { viewModel.updateCurrency(option.code) }, label = { Text(option.code) }) } } } }, confirmButton = { Button(onClick = { viewModel.updateName(name); showProfile = false }, colors = ButtonDefaults.buttonColors(containerColor = Emerald)) { Text("Save") } }, dismissButton = { TextButton(onClick = { showProfile = false }) { Text("Cancel") } })
    if (showAbout) InfoDialog("About KhataGo", "KhataGo\n\nYour Money. Your Records. Your Control.\n\nCreated by Shohan Khan\nhelloiamshohan@gmail.com\n\nVersion ${AppConstants.VERSION_NAME}\n\nA free, offline-first personal finance record manager with no ads, subscriptions or tracking.\n\nBuilt with AndroidX, Jetpack Compose, Material 3, Room, DataStore and WorkManager.", { showAbout = false })
    if (showPrivacy) InfoDialog("Privacy", "Your financial records are stored locally on your device unless you explicitly export or share them. KhataGo does not require an account and does not include advertising or analytics tracking. Keep exported backups in a safe place because anyone with access to the file can read it.", { showPrivacy = false })
    if (showDelete) AlertDialog(onDismissRequest = { showDelete = false }, title = { Text("Delete all local data?") }, text = { Text("This permanently removes your records, payments and profile from this device. Export a backup first if you may need it.") }, confirmButton = { Button(onClick = { viewModel.deleteAllData(); showDelete = false }, colors = ButtonDefaults.buttonColors(containerColor = Danger)) { Text("Delete everything") } }, dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } })
}

@Composable private fun SettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, tint: Color = Emerald, trailing: @Composable () -> Unit) { Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) { Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold, color = if (tint == Danger) Danger else Ink); Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall) }; trailing() } }
@Composable private fun InfoDialog(title: String, message: String, onDismiss: () -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(message) }, confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }) }
private suspend fun writeText(context: Context, uri: android.net.Uri, text: String, after: () -> Unit) { runCatching { context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(text) } }.onSuccess { after() } }

private fun readLimited(reader: java.io.Reader, maxChars: Int): String? {
    val result = StringBuilder()
    val buffer = CharArray(8192)
    while (true) {
        val count = reader.read(buffer)
        if (count < 0) return result.toString()
        result.append(buffer, 0, count)
        if (result.length > maxChars) return null
    }
}
