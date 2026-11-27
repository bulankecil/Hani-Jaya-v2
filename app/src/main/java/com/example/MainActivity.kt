package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.repository.FinanceRepository
import com.example.data.security.SecurityPreferences
import com.example.ui.HomeViewModel
import com.example.ui.WalletViewModel
import com.example.ui.components.FloatingPillNavBar
import com.example.ui.components.HomeTab
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.ComingSoonTabScreen
import com.example.ui.screens.PinLoginScreen
import com.example.ui.screens.WalletHubScreen
import com.example.ui.theme.HaniJayaTheme

object NavRoutes {
    const val HOME = "home"
    const val PIN_LOGIN = "pin_login"
    const val WALLET_HUB = "wallet_hub"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.parseColor("#1B3A7D"))
        )

        val database = AppDatabase.getInstance(applicationContext)
        val repository = FinanceRepository(database.transactionDao())
        val securityPreferences = SecurityPreferences(applicationContext)

        setContent {
            HaniJayaTheme {
                HaniJayaApp(
                    repository = repository,
                    securityPreferences = securityPreferences
                )
            }
        }
    }
}

@Composable
fun HaniJayaApp(
    repository: FinanceRepository,
    securityPreferences: SecurityPreferences
) {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(repository)
    )
    val walletViewModel: WalletViewModel = viewModel(
        factory = WalletViewModel.Factory(repository, securityPreferences)
    )

    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(HomeTab.BERANDA) }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(NavRoutes.HOME) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    HomeTab.BERANDA -> {
                        BerandaScreen(
                            uiState = homeState,
                            onWalletClick = {
                                walletViewModel.lockWallet()
                                navController.navigate(NavRoutes.PIN_LOGIN) {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateTab = { tab -> selectedTab = tab },
                            onDeleteTransaction = { id ->
                                homeViewModel.deleteTransaction(id)
                            }
                        )
                    }
                    else -> {
                        ComingSoonTabScreen(
                            tab = selectedTab,
                            onReturnHome = { selectedTab = HomeTab.BERANDA }
                        )
                    }
                }

                FloatingPillNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab -> selectedTab = tab },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        composable(NavRoutes.PIN_LOGIN) {
            LaunchedEffect(walletState.isUnlocked) {
                if (walletState.isUnlocked) {
                    navController.navigate(NavRoutes.WALLET_HUB) {
                        popUpTo(NavRoutes.PIN_LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            PinLoginScreen(
                uiState = walletState,
                onDigitPressed = walletViewModel::onDigitPressed,
                onBackspacePressed = walletViewModel::onBackspacePressed,
                onClearPressed = walletViewModel::onClearPin,
                onBack = {
                    walletViewModel.lockWallet()
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.WALLET_HUB) {
            WalletHubScreen(
                uiState = walletState,
                onExportToUri = walletViewModel::exportBackup,
                onImportFromUri = walletViewModel::prepareImportBackup,
                onConfirmRestore = walletViewModel::confirmImportBackup,
                onDismissRestorePreview = walletViewModel::dismissImportPreview,
                onUpdatePin = walletViewModel::updatePin,
                onClearStatusMessage = walletViewModel::clearStatusMessage,
                onClearPinMessage = walletViewModel::clearPinChangeMessage,
                onLockAndBack = {
                    walletViewModel.lockWallet()
                    navController.popBackStack()
                }
            )
        }
    }
}
