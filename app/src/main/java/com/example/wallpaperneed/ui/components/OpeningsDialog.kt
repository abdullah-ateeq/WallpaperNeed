package com.example.wallpaperneed.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wallpaperneed.model.LengthUnit
import com.example.wallpaperneed.model.Opening
import com.example.wallpaperneed.model.OpeningType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpeningsDialog(
    initialOpening: Opening? = null,
    defaultType: OpeningType = OpeningType.DOOR,
    unit: LengthUnit,
    onDismiss: () -> Unit,
    onConfirm: (Opening) -> Unit
) {
    var name by remember {
        mutableStateOf(
            initialOpening?.name ?: when (defaultType) {
                OpeningType.DOOR -> "Main Door"
                OpeningType.WINDOW -> "Window"
                OpeningType.ARCH -> "Archway"
                OpeningType.CABINET -> "Built-in Cabinet"
                OpeningType.OTHER -> "Opening"
            }
        )
    }

    var type by remember { mutableStateOf(initialOpening?.type ?: defaultType) }
    var widthMm by remember {
        mutableDoubleStateOf(
            initialOpening?.widthMm ?: when (defaultType) {
                OpeningType.DOOR -> 900.0  // 90 cm
                OpeningType.WINDOW -> 1500.0 // 150 cm
                else -> 1000.0
            }
        )
    }
    var heightMm by remember {
        mutableDoubleStateOf(
            initialOpening?.heightMm ?: when (defaultType) {
                OpeningType.DOOR -> 2100.0  // 210 cm
                OpeningType.WINDOW -> 1200.0 // 120 cm
                else -> 2000.0
            }
        )
    }
    var quantity by remember { mutableIntStateOf(initialOpening?.quantity ?: 1) }

    var typeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialOpening == null) "Add Opening" else "Edit Opening",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type selector
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = type.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Opening Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        OpeningType.entries.forEach { opType ->
                            DropdownMenuItem(
                                text = { Text(opType.displayName) },
                                onClick = {
                                    type = opType
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Opening Name / Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Width
                DimensionInputField(
                    label = "Opening Width",
                    valueMm = widthMm,
                    unit = unit,
                    onValueChangeMm = { widthMm = it }
                )

                // Height
                DimensionInputField(
                    label = "Opening Height",
                    valueMm = heightMm,
                    unit = unit,
                    onValueChangeMm = { heightMm = it }
                )

                // Quantity
                OutlinedTextField(
                    value = quantity.toString(),
                    onValueChange = { text ->
                        val parsed = text.toIntOrNull()
                        if (parsed != null && parsed >= 1) {
                            quantity = parsed
                        }
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Calculated Area Preview
                val singleAreaM2 = (widthMm * heightMm) / 1_000_000.0
                val totalAreaM2 = singleAreaM2 * quantity
                Text(
                    text = "Total Deducted Area: ${String.format(java.util.Locale.US, "%.2f", totalAreaM2)} m²",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val opening = (initialOpening ?: Opening()).copy(
                        name = name.ifBlank { type.displayName },
                        type = type,
                        widthMm = widthMm,
                        heightMm = heightMm,
                        quantity = quantity
                    )
                    onConfirm(opening)
                }
            ) {
                Text(if (initialOpening == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
