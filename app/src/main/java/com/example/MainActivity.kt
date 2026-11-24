package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.update.InAppUpdateController
import com.example.update.InAppUpdateUiState

class MainActivity : ComponentActivity() {
    private lateinit var inAppUpdateController: InAppUpdateController

    private val updateFlowLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        inAppUpdateController.onUpdateFlowResult(result.resultCode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        inAppUpdateController = InAppUpdateController(this).apply {
            attachLauncher(updateFlowLauncher)
        }

        setContent {
            val updateState by inAppUpdateController.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CoilAppNavigation(
                        updateState = updateState,
                        onCheckForUpdates = { inAppUpdateController.checkForUpdates(isManual = true) },
                        onStartUpdate = { inAppUpdateController.startFlexibleOrRecommendedUpdate() },
                        onDismissUpdateBanner = { inAppUpdateController.dismissAvailableNotification() },
                        onCompleteDownloadedUpdate = { inAppUpdateController.completeDownloadedUpdate() },
                        onClearTransientUpdateStatus = { inAppUpdateController.clearTransientStatus() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        inAppUpdateController.startObserving()
    }

    override fun onResume() {
        super.onResume()
        inAppUpdateController.checkForUpdates(isManual = false)
    }

    override fun onStop() {
        super.onStop()
        inAppUpdateController.stopObserving()
    }
}

@Composable
fun CoilAppNavigation(
    updateState: InAppUpdateUiState = InAppUpdateUiState.Idle,
    onCheckForUpdates: () -> Unit = {},
    onStartUpdate: () -> Unit = {},
    onDismissUpdateBanner: () -> Unit = {},
    onCompleteDownloadedUpdate: () -> Unit = {},
    onClearTransientUpdateStatus: () -> Unit = {}
) {
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
                },
                updateState = updateState,
                onStartUpdate = onStartUpdate,
                onDismissUpdateBanner = onDismissUpdateBanner,
                onCompleteDownloadedUpdate = onCompleteDownloadedUpdate
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                updateState = updateState,
                onCheckForUpdates = onCheckForUpdates,
                onStartUpdate = onStartUpdate,
                onCompleteDownloadedUpdate = onCompleteDownloadedUpdate,
                onClearTransientUpdateStatus = onClearTransientUpdateStatus
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
