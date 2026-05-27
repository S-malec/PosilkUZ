package com.example.posilkuz.ui.pantry

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.posilkuz.R
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * Dialog umożliwiający dodanie produktu do spiżarni poprzez zeskanowanie lub ręczne wpisanie kodu kreskowego.
 *
 * Udostępnia dwie ścieżki wprowadzania kodu:
 * - skanowanie kamery przy użyciu Google ML Kit ([GmsBarcodeScanning]),
 * - ręczne wpisanie cyfr kodu kreskowego w rozwijalnym polu tekstowym.
 *
 * Po pomyślnym odczycie kodu wywołuje [onBarcodeScanned] i zamyka dialog.
 *
 * @param onDismiss wywołanie zwrotne zamknięcia dialogu bez akcji
 * @param onBarcodeScanned wywołanie zwrotne z odczytanym kodem kreskowym jako parametrem
 */
@Composable
fun BarcodeScannerDialog(
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    var manualBarcode by remember { mutableStateOf("") }
    var isManualInputVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scanner = remember { GmsBarcodeScanning.getClient(context as Activity) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.add_product)) },
        text = {
            Column {
                Button(
                    onClick = {
                        scanner.startScan()
                            .addOnSuccessListener { barcode ->
                                barcode.rawValue?.let {
                                    onBarcodeScanned(it)
                                    onDismiss()
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.height(8.dp))
                    Text(text = stringResource(R.string.scan_barcode))
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isManualInputVisible) {
                    TextButton(
                        onClick = { isManualInputVisible = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.height(8.dp))
                        Text(text = stringResource(R.string.enter_barcode_manually))
                    }
                }

                AnimatedVisibility(visible = isManualInputVisible) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualBarcode,
                            onValueChange = { manualBarcode = it },
                            label = { Text(text = stringResource(R.string.enter_barcode_digits)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (manualBarcode.isNotBlank()) {
                                    onBarcodeScanned(manualBarcode)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = manualBarcode.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(text = stringResource(R.string.confirm_barcode))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancel)) }
        }
    )
}
