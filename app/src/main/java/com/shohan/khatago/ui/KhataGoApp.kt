package com.shohan.khatago.ui

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shohan.khatago.domain.RecordType
import com.shohan.khatago.ui.screens.AddRecordSheet
import com.shohan.khatago.ui.screens.AnalyticsScreen
import com.shohan.khatago.ui.screens.HomeScreen
import com.shohan.khatago.ui.screens.MoreScreen
import com.shohan.khatago.ui.screens.OnboardingScreen
import com.shohan.khatago.ui.screens.PaymentsScreen
import com.shohan.khatago.ui.screens.RecordsScreen

private data class NavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private val navItems = listOf(
    NavItem("home", "Home", Icons.Default.Home),
    NavItem("records", "Records", Icons.Default.CalendarMonth),
    NavItem("payments", "Payments", Icons.Default.Payments),
    NavItem("analytics", "Analytics", Icons.Default.Analytics),
    NavItem("more", "More", Icons.Default.MoreHoriz)
)

@Composable
fun KhataGoApp(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    if (!settings.onboardingComplete) OnboardingScreen(viewModel) else MainShell(viewModel)
}

@Composable
private fun MainShell(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: "home"
    var showAdd by remember { mutableStateOf(false) }
    var addPreset by remember { mutableStateOf<RecordType?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val message by viewModel.message.collectAsStateWithLifecycle()
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() } }

    Box(Modifier.fillMaxSize().background(Canvas)) {
        Scaffold(
            containerColor = Canvas,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                NavigationBar(containerColor = Color.White, modifier = Modifier.navigationBarsPadding()) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = current == item.route,
                            onClick = { navController.navigate(item.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                            icon = { Icon(item.icon, item.label) }, label = { Text(item.label) }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (current != "more") FloatingActionButton(onClick = { addPreset = null; showAdd = true }, containerColor = Emerald, contentColor = Color.White) { Icon(Icons.Default.Add, "Add record") }
            }
        ) { padding ->
            NavHost(navController, startDestination = "home", modifier = Modifier.padding(padding).statusBarsPadding()) {
                composable("home") { HomeScreen(viewModel, onAdd = { addPreset = it; showAdd = true }, onOpenRecords = { navController.navigate("records") }) }
                composable("records") { RecordsScreen(viewModel, onAdd = { addPreset = it; showAdd = true }) }
                composable("payments") { PaymentsScreen(viewModel, onRecordPayment = { addPreset = null; showAdd = true }, onDeletePayment = viewModel::deletePayment) }
                composable("analytics") { AnalyticsScreen(viewModel) }
                composable("more") { MoreScreen(viewModel, onRequestNotifications = { if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }) }
            }
        }
    }
    if (showAdd) AddRecordSheet(viewModel, preset = addPreset, onDismiss = { showAdd = false })
}
