package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.SurfaceMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WalletIndigo
import com.example.ui.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfferBottomSheet(
    onDismiss: () -> Unit,
    onCreateOffer: (
        offerName: String,
        serialNumber: String,
        mediatorName: String,
        customerName: String,
        route: String,
        offerDateMillis: Long
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var offerName by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var mediatorName by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isReadyToSubmit = offerName.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .navigationBarsPadding()
        ) {
            // Top Header with Centered Title and Top-Right Close Button
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Buat Penawaran Baru",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 44.dp)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .minimumInteractiveComponentSize()
                        .testTag("create_offer_close_button")
                ) {
                    Surface(
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = CardSurface,
                        border = BorderStroke(1.2.dp, TextSecondary.copy(alpha = 0.55f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Tutup form",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Isi informasi penawaran. Breakdown biaya bisa diisi setelah dibuat.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            OfferInputField(
                label = "Nama Penawaran",
                value = offerName,
                onValueChange = {
                    offerName = it
                    errorMessage = null
                },
                placeholder = "Contoh: Penawaran Container 40HC",
                imeAction = ImeAction.Next,
                testTag = "input_offer_name"
            )

            Spacer(modifier = Modifier.height(14.dp))

            OfferInputField(
                label = "Serial Number",
                value = serialNumber,
                onValueChange = { serialNumber = it },
                placeholder = "CONTOH: SN-001",
                imeAction = ImeAction.Next,
                testTag = "input_serial_number"
            )

            Spacer(modifier = Modifier.height(14.dp))

            OfferInputField(
                label = "Nama Mediator",
                value = mediatorName,
                onValueChange = { mediatorName = it },
                placeholder = "Opsional",
                imeAction = ImeAction.Next,
                testTag = "input_mediator_name"
            )

            Spacer(modifier = Modifier.height(14.dp))

            OfferInputField(
                label = "Nama Customer",
                value = customerName,
                onValueChange = { customerName = it },
                placeholder = "Opsional",
                imeAction = ImeAction.Next,
                testTag = "input_customer_name"
            )

            Spacer(modifier = Modifier.height(14.dp))

            OfferInputField(
                label = "Rute",
                value = route,
                onValueChange = { route = it },
                placeholder = "Contoh: Semarang - Singapura",
                imeAction = ImeAction.Done,
                testTag = "input_offer_route"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tanggal Field with DatePicker Trigger
            Text(
                text = "Tanggal",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showDatePicker = true }
                    .testTag("input_offer_date"),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceMuted
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = CurrencyFormatter.formatDateSlashed(selectedDateMillis),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Pilih tanggal",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Bottom Action Row: "+ Buat" and "x Batal"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val cleanedName = offerName.trim()
                        if (cleanedName.isEmpty()) {
                            errorMessage = "Nama Penawaran wajib diisi."
                        } else {
                            onCreateOffer(
                                cleanedName,
                                serialNumber.trim(),
                                mediatorName.trim(),
                                customerName.trim(),
                                route.trim(),
                                selectedDateMillis
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1.45f)
                        .height(52.dp)
                        .testTag("create_offer_submit_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReadyToSubmit) {
                            WalletIndigo
                        } else {
                            WalletIndigo.copy(alpha = 0.55f)
                        },
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Buat",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Surface(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(0.95f)
                        .height(52.dp)
                        .testTag("create_offer_cancel_button"),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceMuted
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Batal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { chosen ->
                            selectedDateMillis = chosen
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih", color = WalletIndigo, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun OfferInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceMuted,
                unfocusedContainerColor = SurfaceMuted,
                focusedBorderColor = WalletIndigo.copy(alpha = 0.65f),
                unfocusedBorderColor = SubtleBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}
