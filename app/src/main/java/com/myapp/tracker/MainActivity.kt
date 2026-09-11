package com.myapp.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myapp.tracker.data.HomeViewModel
import com.myapp.tracker.ui.HomeScreen
import com.myapp.tracker.ui.theme.AppBackground
import com.myapp.tracker.ui.theme.TrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackerApp()
        }
    }
}

@Composable
fun TrackerApp() {
    TrackerTheme {
        Surface(color = AppBackground) {
            val viewModel: HomeViewModel = viewModel()
            HomeScreen(viewModel = viewModel)
        }
    }
}
