package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.setGlobalCurrencySymbol
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PlanningScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {

  private val viewModel: FinanceViewModel by viewModels {
    FinanceViewModelFactory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val themeAccent by viewModel.themeAccent.collectAsStateWithLifecycle()
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      val isOnboarded by viewModel.isOnboarded.collectAsStateWithLifecycle()
      val userCurrencySymbol by viewModel.userCurrencySymbol.collectAsStateWithLifecycle()
      var showSettings by remember { mutableStateOf(false) }

      LaunchedEffect(userCurrencySymbol) {
        setGlobalCurrencySymbol(userCurrencySymbol)
      }

      val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
      }

      MyApplicationTheme(darkTheme = darkTheme, themeAccent = themeAccent) {
        if (!isOnboarded) {
          OnboardingScreen(viewModel = viewModel)
        } else if (showSettings) {
          SettingsScreen(
            viewModel = viewModel,
            onDismiss = { showSettings = false }
          )
        } else {
          var currentTab by remember { mutableStateOf(0) }

          Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
              NavigationBar(
                modifier = Modifier.testTag("bottom_nav")
              ) {
                NavigationBarItem(
                  selected = currentTab == 0,
                  onClick = { currentTab = 0 },
                  icon = { Icon(Icons.Default.Home, contentDescription = "Resumen") },
                  label = { Text("Resumen") },
                  modifier = Modifier.testTag("nav_resumen")
                )
                NavigationBarItem(
                  selected = currentTab == 1,
                  onClick = { currentTab = 1 },
                  icon = { Icon(Icons.Default.List, contentDescription = "Movimientos") },
                  label = { Text("Movimientos") },
                  modifier = Modifier.testTag("nav_movimientos")
                )
                NavigationBarItem(
                  selected = currentTab == 2,
                  onClick = { currentTab = 2 },
                  icon = { Icon(Icons.Default.PieChart, contentDescription = "Presupuestos") },
                  label = { Text("Presupuestos") },
                  modifier = Modifier.testTag("nav_presupuestos")
                )
                NavigationBarItem(
                  selected = currentTab == 3,
                  onClick = { currentTab = 3 },
                  icon = { Icon(Icons.Default.Flag, contentDescription = "Planificación") },
                  label = { Text("Planificación") },
                  modifier = Modifier.testTag("nav_planificacion")
                )
              }
            }
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            ) {
              when (currentTab) {
                0 -> DashboardScreen(
                  viewModel = viewModel,
                  onNavigateToTransactions = { currentTab = 1 },
                  onNavigateToBudgets = { currentTab = 2 },
                  onProfileClick = { showSettings = true }
                )
                1 -> TransactionsScreen(
                  viewModel = viewModel
                )
                2 -> BudgetsScreen(
                  viewModel = viewModel
                )
                3 -> PlanningScreen(
                  viewModel = viewModel
                )
              }
            }
          }
        }
      }
    }
  }
}

