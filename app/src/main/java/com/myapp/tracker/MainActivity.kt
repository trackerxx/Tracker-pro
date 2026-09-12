package com.myapp.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myapp.tracker.data.HomeViewModel
import com.myapp.tracker.data.LogsViewModel
import com.myapp.tracker.ui.HomeScreen
import com.myapp.tracker.ui.LogsScreen
import com.myapp.tracker.ui.theme.AppBackground
import com.myapp.tracker.ui.theme.BrandRed
import com.myapp.tracker.ui.theme.TrackerTheme

private enum class AppTab(val label: String) { HOME("Home"), LOGS("Logs"), STATS("Stats"), PROFILE("Profile") }

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
        var currentTab by remember { mutableStateOf(AppTab.HOME) }

        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.HOME,
                        onClick = { currentTab = AppTab.HOME },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.LOGS,
                        onClick = { currentTab = AppTab.LOGS },
                        icon = { Icon(Icons.Filled.List, contentDescription = "Logs") },
                        label = { Text("Logs") },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.STATS,
                        onClick = { currentTab = AppTab.STATS },
                        icon = { Icon(Icons.Filled.BarChart, contentDescription = "Stats") },
                        label = { Text("Stats") },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.PROFILE,
                        onClick = { currentTab = AppTab.PROFILE },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = navColors()
                    )
                }
            }
        ) { innerPadding ->
            Surface(color = AppBackground, modifier = Modifier.padding(innerPadding)) {
                when (currentTab) {
                    AppTab.HOME -> {
                        val viewModel: HomeViewModel = viewModel()
                        HomeScreen(viewModel = viewModel)
                    }
                    AppTab.LOGS -> {
                        val viewModel: LogsViewModel = viewModel()
                        LogsScreen(viewModel = viewModel)
                    }
                    AppTab.STATS -> ComingSoonScreen("Stats")
                    AppTab.PROFILE -> ComingSoonScreen("Profile")
                }
            }
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = BrandRed,
    selectedTextColor = BrandRed,
    indicatorColor = Color(0x1AB91C1C)
)

@Composable
private fun ComingSoonScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("$name — coming soon", color = BrandRed)
    }
}
