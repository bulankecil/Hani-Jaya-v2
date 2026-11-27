package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
import com.example.ui.CnfCalculationViewModel
import com.example.ui.HomeViewModel
import com.example.ui.WalletViewModel
import com.example.ui.components.FloatingPillNavBar
import com.example.ui.components.HomeTab
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.CnfCalculationScreen
import com.example.ui.screens.ComingSoonTabScreen
import com.example.ui.screens.PinLoginScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WalletHubScreen
import com.example.ui.theme.HaniJayaTheme
import kotlinx.coroutines.launch

object NavRoutes {
    const val SPLASH = "splash"
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
        val securityPreferences = SecurityPreferences(applicationContext)
        val repository = FinanceRepository(
            transactionDao = database.transactionDao(),
            cnfOfferDao = database.cnfOfferDao(),
            securityPreferences = securityPreferences
        )

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
    val cnfCalculationViewModel: CnfCalculationViewModel = viewModel(
        factory = CnfCalculationViewModel.Factory(repository)
    )

    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()
    val cnfCalculationState by cnfCalculationViewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.BERANDA) }
    val berandaListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val cnfCalculationListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val tabStateHolder = rememberSaveableStateHolder()
    val coroutineScope = rememberCoroutineScope()

    fun selectTab(tab: HomeTab) {
        if (tab == selectedTab) {
            coroutineScope.launch {
                when (tab) {
                    HomeTab.BERANDA -> if (berandaListState.firstVisibleItemIndex > 0 || berandaListState.firstVisibleItemScrollOffset > 0) {
                        berandaListState.animateScrollToItem(0)
                    }
                    HomeTab.CNF_CALCULATION -> if (cnfCalculationListState.firstVisibleItemIndex > 0 || cnfCalculationListState.firstVisibleItemScrollOffset > 0) {
                        cnfCalculationListState.animateScrollToItem(0)
                    }
                    else -> Unit
                }
            }
        } else {
            selectedTab = tab
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> fullWidth / 6 }
            ) + fadeIn(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> -fullWidth / 8 }
            ) + fadeOut(
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> -fullWidth / 6 }
            ) + fadeIn(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> fullWidth / 8 }
            ) + fadeOut(
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        composable(
            route = NavRoutes.SPLASH,
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            }
        ) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = NavRoutes.HOME,
            enterTransition = {
                if (initialState.destination.route == NavRoutes.SPLASH) {
                    fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
                } else {
                    null
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val movingRight = targetState.ordinal >= initialState.ordinal
                        (slideInHorizontally(
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> if (movingRight) fullWidth / 5 else -fullWidth / 5 }
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                        )) togetherWith (slideOutHorizontally(
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> if (movingRight) -fullWidth / 6 else fullWidth / 6 }
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                        )) using SizeTransform(clip = false)
                    },
                    label = "home_tab_content",
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    tabStateHolder.SaveableStateProvider(key = tab.name) {
                        when (tab) {
                            HomeTab.BERANDA -> {
                                BerandaScreen(
                                    uiState = homeState,
                                    onWalletClick = {
                                        walletViewModel.lockWallet()
                                        navController.navigate(NavRoutes.PIN_LOGIN) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateTab = ::selectTab,
                                    onDeleteTransaction = { id ->
                                        homeViewModel.deleteTransaction(id)
                                    },
                                    listState = berandaListState
                                )
                            }
                            HomeTab.CNF_CALCULATION -> {
                                CnfCalculationScreen(
                                    uiState = cnfCalculationState,
                                    onToggleExpand = cnfCalculationViewModel::toggleCardExpanded,
                                    onToggleCurrency = cnfCalculationViewModel::toggleCurrencyMode,
                                    onCreateOffer = cnfCalculationViewModel::createOffer,
                                    onUpdateOffer = cnfCalculationViewModel::updateOfferBreakdown,
                                    onDeleteOffer = cnfCalculationViewModel::deleteOffer,
                                    onExportOfferCsv = cnfCalculationViewModel::exportOfferCsv,
                                    onClearFeedback = cnfCalculationViewModel::clearFeedbackMessage,
                                    onReturnHome = { selectedTab = HomeTab.BERANDA },
                                    listState = cnfCalculationListState
                                )
                            }
                            else -> {
                                ComingSoonTabScreen(
                                    tab = tab,
                                    onReturnHome = { selectedTab = HomeTab.BERANDA }
                                )
                            }
                        }
                    }
                }

                FloatingPillNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = ::selectTab,
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
