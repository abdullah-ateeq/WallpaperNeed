package com.example.wallpaperneed.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallpaperneed.model.AppThemeMode
import com.example.wallpaperneed.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    // SAF Document Launchers for TXT, CSV, JSON
    var exportPayload by remember { mutableStateOf("") }
    var exportTypeLabel by remember { mutableStateOf("") }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(exportPayload.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "$exportTypeLabel exported successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Preferences, precision, display & offline export",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. APPEARANCE
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = "Appearance", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "APPEARANCE",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Theme Mode
                        Text(text = "App Theme", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AppThemeMode.entries.forEach { mode ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.updateThemeMode(mode) }
                                ) {
                                    RadioButton(
                                        selected = settings.themeMode == mode,
                                        onClick = { viewModel.updateThemeMode(mode) }
                                    )
                                    Text(text = mode.displayName, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Dynamic Color
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Dynamic Color (Material You)", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Adapts to device wallpaper on Android 12+", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Switch(
                                checked = settings.dynamicColor,
                                onCheckedChange = { viewModel.updateDynamicColor(it) }
                            )
                        }
                    }
                }
            }

            // 2. PRECISION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = "Precision", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DISPLAY PRECISION",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Decimal Places (Full precision is always kept internally):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(0, 1, 2, 3, 4).forEach { places ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.updateDecimalPlaces(places) }
                                ) {
                                    RadioButton(
                                        selected = settings.decimalPlaces == places,
                                        onClick = { viewModel.updateDecimalPlaces(places) }
                                    )
                                    Text(text = "$places", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                                }
                            }
                        }
                    }
                }
            }

            // 3. DISPLAY TOGGLES
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = "Display", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DISPLAY SECTIONS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        SettingSwitchRow(
                            title = "Show Room Top-View Diagram",
                            description = "Technical proportional layout preserving aspect ratio",
                            checked = settings.showRoomDiagram,
                            onCheckedChange = { viewModel.updateSettingToggle(showRoomDiagram = it) }
                        )

                        SettingSwitchRow(
                            title = "Show Roll Diagram",
                            description = "Strip illustrations with drop cuts and leftover material",
                            checked = settings.showRollDiagram,
                            onCheckedChange = { viewModel.updateSettingToggle(showRollDiagram = it) }
                        )

                        SettingSwitchRow(
                            title = "Show Cutting Plan",
                            description = "Drop numbering, pattern repeat derivations, and warnings",
                            checked = settings.showCuttingPlan,
                            onCheckedChange = { viewModel.updateSettingToggle(showCuttingPlan = it) }
                        )

                        SettingSwitchRow(
                            title = "Show Calculation Breakdown",
                            description = "Step-by-step mathematical formulas and proof",
                            checked = settings.showCalculationBreakdown,
                            onCheckedChange = { viewModel.updateSettingToggle(showBreakdown = it) }
                        )

                        SettingSwitchRow(
                            title = "Show Opening Area Deductions",
                            description = "Surface area deductions for doors and windows",
                            checked = settings.showOpeningArea,
                            onCheckedChange = { viewModel.updateSettingToggle(showOpeningArea = it) }
                        )

                        SettingSwitchRow(
                            title = "Show Purchase Excess Metric",
                            description = "Purchased material excess beyond net wall area",
                            checked = settings.showPurchaseExcess,
                            onCheckedChange = { viewModel.updateSettingToggle(showPurchaseExcess = it) }
                        )
                    }
                }
            }

            // 4. BEHAVIOR
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "APP BEHAVIOR",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
                            color = MaterialTheme.colorScheme.primary
                        )

                        SettingSwitchRow(
                            title = "Live Instant Calculation",
                            description = "Automatically update results as numbers change",
                            checked = settings.liveCalculation,
                            onCheckedChange = { viewModel.updateSettingToggle(liveCalculation = it) }
                        )

                        SettingSwitchRow(
                            title = "Restore Last Calculation",
                            description = "Resume where you left off when opening the app",
                            checked = settings.restoreLastCalculation,
                            onCheckedChange = { viewModel.updateSettingToggle(restoreLastCalculation = it) }
                        )

                        SettingSwitchRow(
                            title = "Confirm Deletions",
                            description = "Prompt before deleting history calculations or walls",
                            checked = settings.confirmDelete,
                            onCheckedChange = { viewModel.updateSettingToggle(confirmDelete = it) }
                        )
                    }
                }
            }

            // 5. STORAGE ACCESS FRAMEWORK EXPORT (TXT, CSV, JSON)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = "Export", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OFFLINE DOCUMENT EXPORT (SAF)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Export via Android Storage Access Framework. No broad storage permissions required.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    exportPayload = viewModel.getExportTxt()
                                    exportTypeLabel = "TXT Report"
                                    createDocLauncher.launch("WallpaperNeed_Project.txt")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("TXT")
                            }

                            OutlinedButton(
                                onClick = {
                                    exportPayload = viewModel.getExportCsv()
                                    exportTypeLabel = "CSV Spreadsheet"
                                    createDocLauncher.launch("WallpaperNeed_Project.csv")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CSV")
                            }

                            OutlinedButton(
                                onClick = {
                                    exportPayload = viewModel.getExportJson()
                                    exportTypeLabel = "JSON Data"
                                    createDocLauncher.launch("WallpaperNeed_Project.json")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("JSON")
                            }
                        }
                    }
                }
            }

            // 6. RESET SETTINGS
            item {
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Settings to Defaults",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings?") },
            text = { Text("This will restore default unit, trim presets, precision, and theme preferences. Your saved calculation history will NOT be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSettingsToDefault()
                        showResetDialog = false
                        Toast.makeText(context, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
