package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.security.SecurityPreferences
import com.example.ui.WalletUiState
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CnfBlueSoft
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.NavyTopBar
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WalletIndigo

@Composable
fun PinLoginScreen(
    uiState: WalletUiState,
    onDigitPressed: (Char) -> Unit,
    onBackspacePressed: () -> Unit,
    onClearPressed: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
    ) {
        // Navy Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyTopBar)
                .statusBarsPadding()
                .height(8.dp)
        )

        // Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("pin_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back_button),
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.pin_screen_title),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CnfBlueSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = WalletIndigo,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.pin_prompt_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.pin_prompt_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    if (uiState.isDefaultPinActive) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CnfBlueSoft,
                            border = BorderStroke(1.dp, WalletIndigo.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = stringResource(R.string.pin_default_hint),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = WalletIndigo,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 6-Dot PIN Indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(SecurityPreferences.PIN_LENGTH) { index ->
                            val isFilled = index < uiState.pinInput.length
                            val dotColor = when {
                                uiState.pinError != null -> ExpenseRed.copy(alpha = 0.25f)
                                isFilled -> WalletIndigo
                                else -> CardSurface
                            }
                            val borderColor = when {
                                uiState.pinError != null -> ExpenseRed
                                isFilled -> WalletIndigo
                                else -> SubtleBorder
                            }
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                                    .border(1.5.dp, borderColor, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.height(20.dp), contentAlignment = Alignment.Center) {
                        if (uiState.pinError != null) {
                            Text(
                                text = uiState.pinError,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = ExpenseRed,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Numeric Keypad
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val rows = listOf(
                        listOf('1', '2', '3'),
                        listOf('4', '5', '6'),
                        listOf('7', '8', '9')
                    )
                    rows.forEach { rowDigits ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            rowDigits.forEach { digit ->
                                PinDigitKey(
                                    label = digit.toString(),
                                    onClick = { onDigitPressed(digit) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pin_key_$digit")
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            onClick = onClearPressed,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .testTag("pin_key_reset"),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Reset",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextSecondary
                                )
                            }
                        }

                        PinDigitKey(
                            label = "0",
                            onClick = { onDigitPressed('0') },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pin_key_0")
                        )

                        Surface(
                            onClick = onBackspacePressed,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .testTag("pin_key_backspace"),
                            shape = RoundedCornerShape(16.dp),
                            color = CardSurface,
                            border = BorderStroke(1.dp, SubtleBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                    contentDescription = "Hapus satu digit",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinDigitKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardSurface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )
        }
    }
}
