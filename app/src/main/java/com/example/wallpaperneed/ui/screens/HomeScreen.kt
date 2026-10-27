package com.example.wallpaperneed.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import com.example.wallpaperneed.model.CalculationMode
import com.example.wallpaperneed.model.DropOptimizationMode
import com.example.wallpaperneed.model.LengthUnit
import com.example.wallpaperneed.model.Opening
import com.example.wallpaperneed.model.OpeningType
import com.example.wallpaperneed.model.PatternMatch
import com.example.wallpaperneed.model.RollCalculationMode
import com.example.wallpaperneed.model.WallpaperDirection
import com.example.wallpaperneed.model.WallpaperProduct
import com.example.wallpaperneed.ui.components.BreakdownCard
import com.example.wallpaperneed.ui.components.DimensionInputField
import com.example.wallpaperneed.ui.components.OpeningsDialog
import com.example.wallpaperneed.ui.components.PresetChipsRow
import com.example.wallpaperneed.ui.components.PresetCollections
import com.example.wallpaperneed.ui.components.ResultCard
import com.example.wallpaperneed.ui.components.RoomDiagramCanvas
import com.example.wallpaperneed.ui.components.SecondaryMetricsCard
import com.example.wallpaperneed.viewmodel.WallpaperViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WallpaperViewModel,
    onNavigateToCuttingPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val project by viewModel.project.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val result = project.result

    var showOpeningDialog by remember { mutableStateOf(false) }
    var selectedOpeningType by remember { mutableStateOf(OpeningType.DOOR) }
    var openingToEdit by remember { mutableStateOf<Opening?>(null) }

    var unitMenuExpanded by remember { mutableStateOf(false) }
    var patternMatchMenuExpanded by remember { mutableStateOf(false) }
    var productMenuExpanded by remember { mutableStateOf(false) }
    val productProfiles by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "WallpaperNeed",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Calculate wallpaper rolls for your room.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Unit selector in app bar
                    ExposedDropdownMenuBox(
                        expanded = unitMenuExpanded,
                        onExpandedChange = { unitMenuExpanded = !unitMenuExpanded }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .menuAnchor()
                                .padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = project.lengthUnit.symbol.uppercase(Locale.US),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (unitMenuExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Select Unit",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = unitMenuExpanded,
                            onDismissRequest = { unitMenuExpanded = false }
                        ) {
                            LengthUnit.entries.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.displayName} (${u.symbol})") },
                                    onClick = {
                                        viewModel.setLengthUnit(u)
                                        unitMenuExpanded = false
                                    }
                                )
                            }
                        }
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
            // 1. CALCULATION MODE PICKER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "CALCULATION MODE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val modes = CalculationMode.entries
                        ScrollableTabRow(
                            selectedTabIndex = modes.indexOf(project.calculationMode),
                            edgePadding = 0.dp,
                            divider = {}
                        ) {
                            modes.forEach { mode ->
                                Tab(
                                    selected = project.calculationMode == mode,
                                    onClick = { viewModel.setCalculationMode(mode) },
                                    text = {
                                        Text(
                                            text = mode.displayName,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (project.calculationMode == mode) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        Text(
                            text = project.calculationMode.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // 2. ROOM / WALL DIMENSIONS
            when (project.calculationMode) {
                CalculationMode.COMPLETE_ROOM -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "ROOM DIMENSIONS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                DimensionInputField(
                                    label = "Room Length",
                                    valueMm = project.roomLengthMm,
                                    unit = project.lengthUnit,
                                    onValueChangeMm = {
                                        viewModel.updateRoomDimensions(it, project.roomWidthMm, project.wallHeightMm)
                                    }
                                )

                                DimensionInputField(
                                    label = "Room Width",
                                    valueMm = project.roomWidthMm,
                                    unit = project.lengthUnit,
                                    onValueChangeMm = {
                                        viewModel.updateRoomDimensions(project.roomLengthMm, it, project.wallHeightMm)
                                    }
                                )

                                // Wall Height Mode
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { viewModel.setSameWallHeight(true) }
                                    ) {
                                        RadioButton(
                                            selected = project.sameWallHeight,
                                            onClick = { viewModel.setSameWallHeight(true) }
                                        )
                                        Text(text = "Same height", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { viewModel.setSameWallHeight(false) }
                                    ) {
                                        RadioButton(
                                            selected = !project.sameWallHeight,
                                            onClick = { viewModel.setSameWallHeight(false) }
                                        )
                                        Text(text = "Individual heights", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                if (project.sameWallHeight) {
                                    DimensionInputField(
                                        label = "Wall Height",
                                        valueMm = project.wallHeightMm,
                                        unit = project.lengthUnit,
                                        onValueChangeMm = {
                                            viewModel.updateRoomDimensions(project.roomLengthMm, project.roomWidthMm, it)
                                        }
                                    )
                                } else {
                                    // Individual heights for North, South, East, West
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Wall Heights",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        project.walls.forEach { wall ->
                                            DimensionInputField(
                                                label = "${wall.name} Height",
                                                valueMm = wall.heightMm,
                                                unit = project.lengthUnit,
                                                onValueChangeMm = { viewModel.updateWallHeight(wall.id, it) }
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                // Quick Computed Perimeter & Gross Wall Area Informational Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "WALL PERIMETER",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = LengthUnit.formatLengthMeters(result.perimeterMm, settings.decimalPlaces),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "GROSS WALL AREA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = LengthUnit.formatArea(result.grossWallAreaMm2, settings.decimalPlaces),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "informational surface area",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Top-view Room Diagram
                    if (settings.showRoomDiagram) {
                        item {
                            RoomDiagramCanvas(
                                lengthMm = project.roomLengthMm,
                                widthMm = project.roomWidthMm,
                                unit = project.lengthUnit,
                                openings = project.openings
                            )
                        }
                    }
                }

                CalculationMode.SINGLE_WALL -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "FEATURE / SINGLE WALL DIMENSIONS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                DimensionInputField(
                                    label = "Wall Width",
                                    valueMm = project.singleWallWidthMm,
                                    unit = project.lengthUnit,
                                    onValueChangeMm = {
                                        viewModel.updateSingleWallDimensions(it, project.singleWallHeightMm)
                                    }
                                )

                                DimensionInputField(
                                    label = "Wall Height",
                                    valueMm = project.singleWallHeightMm,
                                    unit = project.lengthUnit,
                                    onValueChangeMm = {
                                        viewModel.updateSingleWallDimensions(project.singleWallWidthMm, it)
                                    }
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "WALL COVERAGE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = project.lengthUnit.format(project.singleWallWidthMm, 0),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "GROSS WALL AREA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = LengthUnit.formatArea(result.grossWallAreaMm2, settings.decimalPlaces),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                CalculationMode.CUSTOM_WALLS -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "WALL SECTIONS (${project.walls.size})",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.1.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(
                                        onClick = {
                                            viewModel.addWall(
                                                com.example.wallpaperneed.model.WallpaperWall(
                                                    name = "Wall ${project.walls.size + 1}",
                                                    widthMm = 4000.0,
                                                    heightMm = 3000.0
                                                )
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Wall", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Wall", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                project.walls.forEachIndexed { idx, wall ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (wall.wallpaperEnabled)
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(
                                                        checked = wall.wallpaperEnabled,
                                                        onCheckedChange = { viewModel.toggleWallEnabled(wall.id, it) }
                                                    )
                                                    Text(
                                                        text = wall.name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                }
                                                if (project.walls.size > 1) {
                                                    IconButton(onClick = { viewModel.removeWall(wall.id) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete Wall", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }

                                            if (wall.wallpaperEnabled) {
                                                DimensionInputField(
                                                    label = "Width",
                                                    valueMm = wall.widthMm,
                                                    unit = project.lengthUnit,
                                                    onValueChangeMm = { viewModel.updateWall(wall.copy(widthMm = it)) }
                                                )
                                                DimensionInputField(
                                                    label = "Height",
                                                    valueMm = wall.heightMm,
                                                    unit = project.lengthUnit,
                                                    onValueChangeMm = { viewModel.updateWall(wall.copy(heightMm = it)) }
                                                )
                                            } else {
                                                Text(
                                                    text = "Wall excluded from wallpaper roll calculation.",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                CalculationMode.MULTIPLE_ROOMS -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "MULTIPLE ROOMS PLANNING",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Manage all individual rooms in the dedicated Multiple Rooms tab or click below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            // 3. WALLPAPER ROLL SETTINGS & PRESETS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WALLPAPER ROLL SPECIFICATIONS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Apply saved profile menu
                            if (productProfiles.isNotEmpty()) {
                                ExposedDropdownMenuBox(
                                    expanded = productMenuExpanded,
                                    onExpandedChange = { productMenuExpanded = !productMenuExpanded }
                                ) {
                                    Text(
                                        text = "Saved Profiles ▼",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .menuAnchor()
                                            .clickable { productMenuExpanded = true }
                                            .padding(4.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = productMenuExpanded,
                                        onDismissRequest = { productMenuExpanded = false }
                                    ) {
                                        productProfiles.forEach { profile ->
                                            DropdownMenuItem(
                                                text = { Text(profile.name) },
                                                onClick = {
                                                    viewModel.applyProductProfile(profile)
                                                    productMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Roll Width
                        Text(
                            text = "Common width presets:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        PresetChipsRow(
                            presets = PresetCollections.widthPresets(project.lengthUnit),
                            selectedValue = project.wallpaperProduct.rollWidthMm,
                            onSelect = {
                                viewModel.updateWallpaperRoll(it, project.wallpaperProduct.rollLengthMm)
                            },
                            isEqual = { a, b -> PresetCollections.isCloseMm(a, b) }
                        )

                        DimensionInputField(
                            label = "Roll Width",
                            valueMm = project.wallpaperProduct.rollWidthMm,
                            unit = project.lengthUnit,
                            onValueChangeMm = {
                                viewModel.updateWallpaperRoll(it, project.wallpaperProduct.rollLengthMm)
                            }
                        )

                        // Roll Length
                        Text(
                            text = "Common length presets:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        PresetChipsRow(
                            presets = PresetCollections.lengthPresets(project.lengthUnit),
                            selectedValue = project.wallpaperProduct.rollLengthMm,
                            onSelect = {
                                viewModel.updateWallpaperRoll(project.wallpaperProduct.rollWidthMm, it)
                            },
                            isEqual = { a, b -> PresetCollections.isCloseMm(a, b) }
                        )

                        DimensionInputField(
                            label = "Roll Length",
                            valueMm = project.wallpaperProduct.rollLengthMm,
                            unit = project.lengthUnit,
                            stepMm = 1000.0, // 1 m step
                            onValueChangeMm = {
                                viewModel.updateWallpaperRoll(project.wallpaperProduct.rollWidthMm, it)
                            }
                        )
                    }
                }
            }

            // 4. TRIMMING & FINISH ALLOWANCES
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "TRIMMING & INSTALLATION ALLOWANCES",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Top trimming allowance presets:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        PresetChipsRow(
                            presets = PresetCollections.trimPresets(project.lengthUnit),
                            selectedValue = project.topTrimMm,
                            onSelect = { viewModel.updateTrimming(it, project.bottomTrimMm) },
                            isEqual = { a, b -> PresetCollections.isCloseMm(a, b) }
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DimensionInputField(
                                label = "Top Trim",
                                valueMm = project.topTrimMm,
                                unit = project.lengthUnit,
                                allowZero = true,
                                onValueChangeMm = { viewModel.updateTrimming(it, project.bottomTrimMm) },
                                modifier = Modifier.weight(1f)
                            )
                            DimensionInputField(
                                label = "Bottom Trim",
                                valueMm = project.bottomTrimMm,
                                unit = project.lengthUnit,
                                allowZero = true,
                                onValueChangeMm = { viewModel.updateTrimming(project.topTrimMm, it) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Calculated Base Drop Length Callout
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "BASE DROP LENGTH",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = project.lengthUnit.format(result.baseDropLengthMm, 0),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 5. PATTERN REPEAT & PATTERN MATCHING
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PATTERN REPEAT & MATCHING",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Pattern repeat presets:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        PresetChipsRow(
                            presets = PresetCollections.repeatPresets(project.lengthUnit),
                            selectedValue = project.wallpaperProduct.patternRepeatMm,
                            onSelect = {
                                viewModel.updatePatternRepeat(it, project.wallpaperProduct.patternMatch)
                            },
                            isEqual = { a, b -> PresetCollections.isCloseMm(a, b) }
                        )

                        DimensionInputField(
                            label = "Pattern Repeat",
                            valueMm = project.wallpaperProduct.patternRepeatMm,
                            unit = project.lengthUnit,
                            allowZero = true,
                            onValueChangeMm = {
                                viewModel.updatePatternRepeat(it, project.wallpaperProduct.patternMatch)
                            }
                        )

                        // Pattern Match Dropdown
                        ExposedDropdownMenuBox(
                            expanded = patternMatchMenuExpanded,
                            onExpandedChange = { patternMatchMenuExpanded = !patternMatchMenuExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = project.wallpaperProduct.patternMatch.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pattern Match Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patternMatchMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = patternMatchMenuExpanded,
                                onDismissRequest = { patternMatchMenuExpanded = false }
                            ) {
                                PatternMatch.entries.forEach { match ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(match.displayName, fontWeight = FontWeight.Bold)
                                                Text(match.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                            }
                                        },
                                        onClick = {
                                            viewModel.updatePatternRepeat(project.wallpaperProduct.patternRepeatMm, match)
                                            patternMatchMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Half drop warning
                        if (project.wallpaperProduct.patternMatch == PatternMatch.HALF_DROP) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠ Half-drop pattern selected. Additional matching material may be required. Verify the manufacturer's pattern-repeat instructions.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Calculated Adjusted Drop Callout
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ADJUSTED DROP LENGTH",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (project.wallpaperProduct.patternRepeatMm > 0)
                                            "Rounded UP to repeat increment" else "Same as base drop",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Text(
                                    text = project.lengthUnit.format(result.adjustedDropLengthMm, 0),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 6. OPENINGS (DOORS & WINDOWS)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OPENINGS & DEDUCTIONS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row {
                                OutlinedButton(
                                    onClick = {
                                        selectedOpeningType = OpeningType.DOOR
                                        openingToEdit = null
                                        showOpeningDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.DoorFront, contentDescription = "Add Door", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Door", style = MaterialTheme.typography.labelSmall)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                OutlinedButton(
                                    onClick = {
                                        selectedOpeningType = OpeningType.WINDOW
                                        openingToEdit = null
                                        showOpeningDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Window, contentDescription = "Add Window", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Window", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Openings List
                        if (project.openings.isEmpty()) {
                            Text(
                                text = "No openings added. Wallpaper will cover the entire room perimeter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            project.openings.forEach { op ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "${op.name} (${op.type.displayName})",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            val wStr = project.lengthUnit.format(op.widthMm, 0)
                                            val hStr = project.lengthUnit.format(op.heightMm, 0)
                                            Text(
                                                text = "$wStr × $hStr × ${op.quantity} = ${LengthUnit.formatArea(op.totalAreaMm2, 2)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row {
                                            IconButton(onClick = {
                                                openingToEdit = op
                                                selectedOpeningType = op.type
                                                showOpeningDialog = true
                                            }) {
                                                Icon(Icons.Default.ExpandMore, contentDescription = "Edit")
                                            }
                                            IconButton(onClick = { viewModel.removeOpening(op.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Drop Optimization Mode Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Optimization:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.setDropOptimizationMode(DropOptimizationMode.CONSERVATIVE) }
                            ) {
                                RadioButton(
                                    selected = project.dropOptimizationMode == DropOptimizationMode.CONSERVATIVE,
                                    onClick = { viewModel.setDropOptimizationMode(DropOptimizationMode.CONSERVATIVE) }
                                )
                                Text("Conservative", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.setDropOptimizationMode(DropOptimizationMode.BASIC_OPENING_OPTIMIZATION) }
                            ) {
                                RadioButton(
                                    selected = project.dropOptimizationMode == DropOptimizationMode.BASIC_OPENING_OPTIMIZATION,
                                    onClick = { viewModel.setDropOptimizationMode(DropOptimizationMode.BASIC_OPENING_OPTIMIZATION) }
                                )
                                Text("Basic", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Text(
                            text = "Conservative mode bases drop count on complete wall coverage to prevent material shortages.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // 7. EXTRA ROLL ALLOWANCE
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "EXTRA ROLL ALLOWANCE (PURCHASE MARGIN)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        PresetChipsRow(
                            presets = PresetCollections.extraRollPresets,
                            selectedValue = project.extraRollAllowance,
                            onSelect = { viewModel.setExtraRollAllowance(it) }
                        )

                        Text(
                            text = "Recommended for pattern matching safety, future repairs, and installer trimming margins.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // 8. MAIN PROMINENT RESULT CARD
            item {
                ResultCard(
                    project = project,
                    result = result,
                    unit = project.lengthUnit,
                    decimalPlaces = settings.decimalPlaces,
                    onViewCuttingPlan = onNavigateToCuttingPlan
                )
            }

            // 9. SURFACE AREA VS PURCHASE AREA SECONDARY CARD
            item {
                SecondaryMetricsCard(
                    result = result,
                    decimalPlaces = settings.decimalPlaces,
                    showOpeningArea = settings.showOpeningArea,
                    showPurchaseExcess = settings.showPurchaseExcess
                )
            }

            // 10. STEP-BY-STEP CALCULATION BREAKDOWN
            if (settings.showCalculationBreakdown && result.breakdownSteps.isNotEmpty()) {
                item {
                    BreakdownCard(steps = result.breakdownSteps)
                }
            }

            // 11. ACTIONS (SAVE, SHARE, COPY)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveToHistory()
                            Toast.makeText(context, "Saved to History!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save")
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "WallpaperNeed Calculation")
                                putExtra(Intent.EXTRA_TEXT, viewModel.getShareText())
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Wallpaper Calculation"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("WallpaperNeed", viewModel.getShareText())
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Openings Add/Edit Dialog
    if (showOpeningDialog) {
        OpeningsDialog(
            initialOpening = openingToEdit,
            defaultType = selectedOpeningType,
            unit = project.lengthUnit,
            onDismiss = { showOpeningDialog = false },
            onConfirm = { opening ->
                if (openingToEdit == null) {
                    viewModel.addOpening(opening)
                } else {
                    viewModel.updateOpening(opening)
                }
                showOpeningDialog = false
            }
        )
    }
}
