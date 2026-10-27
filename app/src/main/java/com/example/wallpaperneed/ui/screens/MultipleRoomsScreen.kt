package com.example.wallpaperneed.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallpaperneed.calculator.WallpaperCalculator
import com.example.wallpaperneed.model.LengthUnit
import com.example.wallpaperneed.model.WallpaperProduct
import com.example.wallpaperneed.model.WallpaperProject
import com.example.wallpaperneed.model.WallpaperRoom
import com.example.wallpaperneed.ui.components.DimensionInputField
import com.example.wallpaperneed.viewmodel.WallpaperViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleRoomsScreen(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.project.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val rooms = project.rooms

    // Calculate individual room results & project total
    val roomCalculations = remember(rooms, project.rollCalculationMode, project.dropOptimizationMode) {
        rooms.map { room ->
            val roomProj = WallpaperProject(
                id = room.id,
                name = room.name,
                roomLengthMm = room.lengthMm,
                roomWidthMm = room.widthMm,
                wallHeightMm = room.heightMm,
                wallpaperProduct = room.wallpaperProduct,
                topTrimMm = room.topTrimMm,
                bottomTrimMm = room.bottomTrimMm
            )
            Pair(room, WallpaperCalculator.calculateProject(roomProj))
        }
    }

    val totalGrossAreaMm2 = roomCalculations.sumOf { it.second.grossWallAreaMm2 }
    val totalNetAreaMm2 = roomCalculations.sumOf { it.second.netWallAreaMm2 }
    val totalRolls = roomCalculations.sumOf { it.second.requiredRolls }

    // Group rolls by wallpaper product specification
    val rollsByProduct = remember(roomCalculations) {
        roomCalculations.groupBy { it.first.wallpaperProduct.name }
            .mapValues { entry -> entry.value.sumOf { it.second.requiredRolls } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Multiple Rooms",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Plan wallpaper rolls across distinct property rooms",
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
            // Add Room Action Button
            item {
                Button(
                    onClick = {
                        val newRoom = WallpaperRoom(
                            id = UUID.randomUUID().toString(),
                            name = "Room ${rooms.size + 1}",
                            lengthMm = 5000.0,
                            widthMm = 4000.0,
                            heightMm = 3000.0,
                            wallpaperProduct = project.wallpaperProduct
                        )
                        viewModel.addRoom(newRoom)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Room", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ ADD ROOM TO PROJECT",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Project Total Summary Card
            if (rooms.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "PROJECT SUMMARY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Total Rooms:", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "${rooms.size}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Gross Wall Area:", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = LengthUnit.formatArea(totalGrossAreaMm2, settings.decimalPlaces),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Net Wall Area:", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = LengthUnit.formatArea(totalNetAreaMm2, settings.decimalPlaces),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Wallpaper Rolls:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$totalRolls ROLLS",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // If different wallpaper specifications are used
                            if (rollsByProduct.size > 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Text(
                                    text = "ROLLS PER WALLPAPER SPECIFICATION:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.outline
                                )
                                rollsByProduct.forEach { (prodName, rollCount) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = prodName, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            text = "$rollCount rolls",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Rooms List
            if (rooms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = "Rooms",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No rooms added yet.",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Click '+ ADD ROOM TO PROJECT' above to calculate rolls for multiple spaces.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(roomCalculations) { (room, calcResult) ->
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = room.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Product: ${room.wallpaperProduct.name}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                Row {
                                    IconButton(onClick = { viewModel.duplicateRoom(room.id) }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Room")
                                    }
                                    IconButton(onClick = { viewModel.removeRoom(room.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            // Dimension inputs for this room
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DimensionInputField(
                                    label = "Length",
                                    valueMm = room.lengthMm,
                                    unit = project.lengthUnit,
                                    onValueChangeMm = { viewModel.updateRoom(room.copy(lengthMm = it)) },
                                    modifier = Modifier.weight(1f)
                                )
                                DimensionInputField(
                                    label = "Width",
                                    valueMm = room.widthMm,
                                    unit = project.lengthUnit,
                                    onValueChangeMm = { viewModel.updateRoom(room.copy(widthMm = it)) },
                                    modifier = Modifier.weight(1f)
                                )
                                DimensionInputField(
                                    label = "Height",
                                    valueMm = room.heightMm,
                                    unit = project.lengthUnit,
                                    onValueChangeMm = { viewModel.updateRoom(room.copy(heightMm = it)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Room Result Summary
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Wall Area: ${LengthUnit.formatArea(calcResult.grossWallAreaMm2, 1)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Drops: ${calcResult.requiredDrops}  |  Usable: ${calcResult.dropsPerRoll}/roll",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${calcResult.requiredRolls} ROLLS",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
