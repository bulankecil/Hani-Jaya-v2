package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.security.SecurityPreferences
import com.example.ui.WalletUiState
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CnfBlueSoft
import com.example.ui.theme.CnfBlueStart
import com.example.ui.theme.CnfEmeraldSoft
import com.example.ui.theme.CnfEmeraldStart
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.NavyTopBar
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WalletIndigo
import com.example.ui.util.CurrencyFormatter

@Composable
fun WalletHubScreen(
    uiState: WalletUiState,
    onExportToUri: (android.content.Context, android.net.Uri) -> Unit,
    onImportFromUri: (android.content.Context, android.net.Uri) -> Unit,
    onConfirmRestore: (Boolean) -> Unit,
    onDismissRestorePreview: () -> Unit,
    onUpdatePin: (String, String, String) -> Unit,
    onClearStatusMessage: () -> Unit,
    onClearPinMessage: () -> Unit,
    onLockAndBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onLockAndBack)

    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportToUri(context, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportFromUri(context, uri)
        }
    }

    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyTopBar)
                .statusBarsPadding()
                .height(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onLockAndBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("wallet_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button),
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.wallet_hub_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }

            TextButton(
                onClick = onLockAndBack,
                modifier = Modifier.testTag("wallet_lock_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = WalletIndigo
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kunci", color = WalletIndigo, fontWeight = FontWeight.SemiBold)
            }
        }

        if (uiState.isBusy) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = WalletIndigo
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Banner if present
                if (uiState.statusMessage != null) {
                    item(key = "status_banner") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (uiState.isErrorStatus) {
                                ExpenseRed.copy(alpha = 0.1f)
                            } else {
                                CnfEmeraldSoft
                            },
                            border = BorderStroke(
                                1.dp,
                                if (uiState.isErrorStatus) ExpenseRed.copy(alpha = 0.3f)
                                else IncomeGreen.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.statusMessage,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (uiState.isErrorStatus) ExpenseRed else IncomeGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onClearStatusMessage) {
                                    Text("Tutup")
                                }
                            }
                        }
                    }
                }

                // Offline Local Storage Status Card
                item(key = "storage_summary") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CnfBlueSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudOff,
                                        contentDescription = null,
                                        tint = CnfBlueStart,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Database Lokal Offline",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Tanpa akun • Tersimpan aman di perangkat",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            HorizontalDivider(color = SubtleBorder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Catatan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${uiState.totalTransactions} transaksi",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Saldo Bersih Saat Ini",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatRupiah(uiState.netBalance),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (uiState.netBalance >= 0L) IncomeGreen else ExpenseRed
                                    )
                                }
                            }
                        }
                    }
                }

                // Export & Import JSON Section
                item(key = "backup_section") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Ekspor & Impor Data (JSON)",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "Cadangkan seluruh catatan keuangan ke file JSON portabel atau pulihkan database dari file backup sebelumnya.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Button(
                                onClick = {
                                    exportLauncher.launch(CurrencyFormatter.formatBackupFileName())
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("export_json_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WalletIndigo,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ekspor Backup ke JSON",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("import_json_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CnfEmeraldStart.copy(alpha = 0.45f))
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FileUpload,
                                    contentDescription = null,
                                    tint = CnfEmeraldStart,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Impor Data dari JSON",
                                    color = CnfEmeraldStart,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Change Security PIN Section
                item(key = "pin_settings_section") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CnfEmeraldSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Key,
                                        contentDescription = null,
                                        tint = CnfEmeraldStart,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Ubah PIN Keamanan",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (uiState.isDefaultPinActive) {
                                            "Saat ini masih menggunakan PIN default (123456)"
                                        } else {
                                            "PIN khusus 6 digit aktif"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = currentPin,
                                onValueChange = { input ->
                                    if (input.length <= SecurityPreferences.PIN_LENGTH) {
                                        currentPin = input.filter { it.isDigit() }
                                        onClearPinMessage()
                                    }
                                },
                                label = { Text("PIN Saat Ini (6 digit)") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_current_pin")
                            )

                            OutlinedTextField(
                                value = newPin,
                                onValueChange = { input ->
                                    if (input.length <= SecurityPreferences.PIN_LENGTH) {
                                        newPin = input.filter { it.isDigit() }
                                        onClearPinMessage()
                                    }
                                },
                                label = { Text("PIN Baru (6 digit)") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_new_pin")
                            )

                            OutlinedTextField(
                                value = confirmPin,
                                onValueChange = { input ->
                                    if (input.length <= SecurityPreferences.PIN_LENGTH) {
                                        confirmPin = input.filter { it.isDigit() }
                                        onClearPinMessage()
                                    }
                                },
                                label = { Text("Konfirmasi PIN Baru") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_confirm_pin")
                            )

                            if (uiState.pinChangeMessage != null) {
                                Text(
                                    text = uiState.pinChangeMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (uiState.isPinChangeSuccess) IncomeGreen else ExpenseRed
                                )
                            }

                            Button(
                                onClick = {
                                    onUpdatePin(currentPin, newPin, confirmPin)
                                    currentPin = ""
                                    newPin = ""
                                    confirmPin = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_new_pin_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavyTopBar,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Simpan PIN Baru", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.pendingRestoreBackup?.let { backup ->
        AlertDialog(
            onDismissRequest = onDismissRestorePreview,
            title = { Text("Konfirmasi Impor JSON") },
            text = {
                Text(
                    "Ditemukan ${backup.transactions.size} transaksi dan ${backup.cnfOffers.size} penawaran CnF dari backup " +
                        "(${CurrencyFormatter.formatDateTime(backup.exportedAt)}). " +
                        "Pilih metode pemulihan ke database lokal:"
                )
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { onConfirmRestore(false) }
                    ) {
                        Text("Gabungkan", color = WalletIndigo)
                    }
                    Button(
                        onClick = { onConfirmRestore(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = WalletIndigo)
                    ) {
                        Text("Ganti Semua")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRestorePreview) {
                    Text("Batal")
                }
            }
        )
    }
}
