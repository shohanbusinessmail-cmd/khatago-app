package com.shohan.khatago

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohan.khatago.data.ReminderScheduler
import com.shohan.khatago.ui.KhataGoApp
import com.shohan.khatago.ui.KhataGoTheme
import com.shohan.khatago.ui.MainViewModel
import com.shohan.khatago.ui.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ReminderScheduler.schedule(this)
        setContent { KhataGoRoot() }
    }
}

@Composable
private fun KhataGoRoot() {
    KhataGoTheme {
        val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application))
        KhataGoApp(viewModel)
    }
}
