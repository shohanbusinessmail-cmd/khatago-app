package com.shohan.khatago.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shohan.khatago.domain.CurrencyOption
import com.shohan.khatago.ui.Canvas
import com.shohan.khatago.ui.Emerald
import com.shohan.khatago.ui.EmeraldDark
import com.shohan.khatago.ui.MainViewModel
import com.shohan.khatago.ui.components.BrandMark

@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    var page by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(CurrencyOption.supported.first()) }
    Column(Modifier.fillMaxSize().background(Canvas).padding(horizontal = 26.dp, vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            BrandMark(size = 56)
            Text("Skip", color = Emerald, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { viewModel.completeOnboarding() }.padding(10.dp))
        }
        Spacer(Modifier.height(48.dp))
        when (page) {
            0 -> WelcomePage()
            1 -> PrivacyPage()
            else -> ProfilePage(name, { name = it }, currency, { currency = it })
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { repeat(3) { i -> androidx.compose.foundation.Canvas(Modifier.size(if (i == page) 25.dp else 7.dp, 7.dp)) { drawRoundRect(if (i == page) Emerald else Color(0xFFD3E2D9), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)) } } }
        Spacer(Modifier.height(22.dp))
        Button(onClick = {
            if (page < 2) page++ else { viewModel.updateName(name); viewModel.updateCurrency(currency.code); viewModel.completeOnboarding() }
        }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(containerColor = Emerald)) {
            Text(if (page == 2) "Start using KhataGo" else "Continue", fontWeight = FontWeight.Bold)
            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.padding(start = 9.dp))
        }
    }
}

@Composable private fun WelcomePage() {
    BrandMark(size = 170)
    Spacer(Modifier.height(30.dp))
    Text("Your money, in focus.", color = EmeraldDark, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
    Text("KhataGo brings credits, loans, payments, income and everyday spending into one calm, clear view.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color(0xFF5E6E65), style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
}

@Composable private fun PrivacyPage() {
    Icon(Icons.Default.Lock, null, tint = Emerald, modifier = Modifier.size(100.dp))
    Spacer(Modifier.height(28.dp))
    Text("Private by design.", color = EmeraldDark, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
    Text("Your records stay on this device. No account, ads, tracking or internet connection is needed for the things that matter.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color(0xFF5E6E65), style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
}

@Composable private fun ProfilePage(name: String, onName: (String) -> Unit, currency: CurrencyOption, onCurrency: (CurrencyOption) -> Unit) {
    Icon(Icons.Default.TrendingUp, null, tint = Emerald, modifier = Modifier.size(100.dp))
    Spacer(Modifier.height(24.dp))
    Text("Make it yours.", color = EmeraldDark, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Text("A name and currency are all you need to begin. You can change them anytime in More.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color(0xFF5E6E65))
    Spacer(Modifier.height(24.dp))
    OutlinedTextField(name, onName, label = { Text("Your name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(14.dp))
    Text("Preferred currency", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold)
    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CurrencyOption.supported.take(4).forEach { option ->
            androidx.compose.material3.FilterChip(selected = currency.code == option.code, onClick = { onCurrency(option) }, label = { Text(option.code) })
        }
    }
}
