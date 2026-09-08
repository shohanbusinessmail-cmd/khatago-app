package com.shohan.khatago.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScreenSearch(query: String, onQueryChanged: (String) -> Unit) {
    OutlinedTextField(query, onQueryChanged, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search records, people or shops") }, leadingIcon = { Icon(Icons.Default.Search, null) })
}

@Composable
fun FilterRow(content: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), content = { content() })
}
