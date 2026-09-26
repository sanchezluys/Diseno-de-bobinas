package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.nav.Screen
import com.example.ui.screen.design.CoilViewModel
import com.example.ui.screen.design.DesignScreen
import com.example.ui.screen.formulas.FormulasScreen
import com.example.ui.screen.settings.SettingsScreen
import com.example.ui.screen.settings.SettingsViewModel
import com.example.ui.screen.wirespecs.WireSpecsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CoilAppNavigation()
                }
            }
        }
    }
}

@Composable
fun CoilAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Design.route
    ) {
        composable(Screen.Design.route) {
            val coilViewModel: CoilViewModel = viewModel()
            DesignScreen(
                viewModel = coilViewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToFormulas = {
                    navController.navigate(Screen.Formulas.route)
                },
                onNavigateToWireSpecs = {
                    navController.navigate(Screen.WireSpecs.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Formulas.route) {
            FormulasScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.WireSpecs.route) {
            WireSpecsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
