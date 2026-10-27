package com.example.wallpaperneed.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallpaperneed.calculator.WallpaperCalculator
import com.example.wallpaperneed.data.WallpaperPreferencesRepository
import com.example.wallpaperneed.model.AppThemeMode
import com.example.wallpaperneed.model.CalculationMode
import com.example.wallpaperneed.model.DropOptimizationMode
import com.example.wallpaperneed.model.LengthUnit
import com.example.wallpaperneed.model.Opening
import com.example.wallpaperneed.model.PatternMatch
import com.example.wallpaperneed.model.RollCalculationMode
import com.example.wallpaperneed.model.SettingsState
import com.example.wallpaperneed.model.WallpaperDirection
import com.example.wallpaperneed.model.WallpaperHistory
import com.example.wallpaperneed.model.WallpaperProduct
import com.example.wallpaperneed.model.WallpaperProject
import com.example.wallpaperneed.model.WallpaperRoom
import com.example.wallpaperneed.model.WallpaperWall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WallpaperPreferencesRepository(application)

    private val _settings = MutableStateFlow(repository.getSettings())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    private val _history = MutableStateFlow(repository.getHistory())
    val history: StateFlow<List<WallpaperHistory>> = _history.asStateFlow()

    private val _products = MutableStateFlow(repository.getProductProfiles())
    val products: StateFlow<List<WallpaperProduct>> = _products.asStateFlow()

    private val _project = MutableStateFlow(createInitialProject())
    val project: StateFlow<WallpaperProject> = _project.asStateFlow()

    init {
        // If restore last calculation is enabled and exists, load it
        if (_settings.value.restoreLastCalculation) {
            val lastProj = repository.getLastProject()
            if (lastProj != null) {
                _project.value = lastProj
            }
        }
        recalculate()
    }

    private fun createInitialProject(): WallpaperProject {
        val st = _settings.value
        val initialProduct = WallpaperProduct(
            id = "default_product",
            name = "Standard Wallpaper",
            rollWidthMm = st.defaultRollWidthMm,
            rollLengthMm = st.defaultRollLengthMm,
            patternRepeatMm = st.defaultPatternRepeatMm,
            patternMatch = PatternMatch.NONE
        )

        return WallpaperProject(
            name = "Living Room",
            lengthUnit = st.defaultUnit,
            rollCalculationMode = st.defaultRollCalculationMode,
            topTrimMm = st.defaultTopTrimMm,
            bottomTrimMm = st.defaultBottomTrimMm,
            wallpaperProduct = initialProduct,
            roomLengthMm = 5000.0,
            roomWidthMm = 4000.0,
            wallHeightMm = 3000.0,
            walls = listOf(
                WallpaperWall(id = "wall_north", name = "North Wall", widthMm = 5000.0, heightMm = 3000.0),
                WallpaperWall(id = "wall_east", name = "East Wall", widthMm = 4000.0, heightMm = 3000.0),
                WallpaperWall(id = "wall_south", name = "South Wall", widthMm = 5000.0, heightMm = 3000.0),
                WallpaperWall(id = "wall_west", name = "West Wall", widthMm = 4000.0, heightMm = 3000.0)
            )
        )
    }

    private fun recalculate() {
        _project.update { current ->
            val result = WallpaperCalculator.calculateProject(current)
            val updated = current.copy(result = result)
            repository.saveLastProject(updated)
            updated
        }
    }

    // --- PROJECT MUTATIONS ---

    fun updateProjectName(name: String) {
        _project.update { it.copy(name = name) }
        recalculate()
    }

    fun setCalculationMode(mode: CalculationMode) {
        _project.update { it.copy(calculationMode = mode) }
        recalculate()
    }

    fun setRollCalculationMode(mode: RollCalculationMode) {
        _project.update { it.copy(rollCalculationMode = mode) }
        recalculate()
    }

    fun setDropOptimizationMode(mode: DropOptimizationMode) {
        _project.update { it.copy(dropOptimizationMode = mode) }
        recalculate()
    }

    fun setLengthUnit(unit: LengthUnit) {
        _project.update { it.copy(lengthUnit = unit) }
    }

    fun updateRoomDimensions(lengthMm: Double, widthMm: Double, heightMm: Double) {
        _project.update { current ->
            val updatedWalls = if (current.sameWallHeight) {
                listOf(
                    WallpaperWall(id = "wall_north", name = "North Wall", widthMm = lengthMm, heightMm = heightMm),
                    WallpaperWall(id = "wall_east", name = "East Wall", widthMm = widthMm, heightMm = heightMm),
                    WallpaperWall(id = "wall_south", name = "South Wall", widthMm = lengthMm, heightMm = heightMm),
                    WallpaperWall(id = "wall_west", name = "West Wall", widthMm = widthMm, heightMm = heightMm)
                )
            } else {
                current.walls
            }
            current.copy(
                roomLengthMm = lengthMm,
                roomWidthMm = widthMm,
                wallHeightMm = heightMm,
                walls = updatedWalls
            )
        }
        recalculate()
    }

    fun setSameWallHeight(same: Boolean) {
        _project.update { current ->
            val updatedWalls = if (same) {
                current.walls.map { it.copy(heightMm = current.wallHeightMm) }
            } else {
                current.walls
            }
            current.copy(sameWallHeight = same, walls = updatedWalls)
        }
        recalculate()
    }

    fun updateWallHeight(wallId: String, heightMm: Double) {
        _project.update { current ->
            val updatedWalls = current.walls.map { wall ->
                if (wall.id == wallId) wall.copy(heightMm = heightMm) else wall
            }
            current.copy(walls = updatedWalls)
        }
        recalculate()
    }

    fun updateSingleWallDimensions(widthMm: Double, heightMm: Double) {
        _project.update { it.copy(singleWallWidthMm = widthMm, singleWallHeightMm = heightMm) }
        recalculate()
    }

    fun updateWallpaperRoll(widthMm: Double, lengthMm: Double) {
        _project.update { current ->
            val updatedProduct = current.wallpaperProduct.copy(rollWidthMm = widthMm, rollLengthMm = lengthMm)
            current.copy(wallpaperProduct = updatedProduct)
        }
        recalculate()
    }

    fun updateTrimming(topMm: Double, bottomMm: Double) {
        _project.update { it.copy(topTrimMm = topMm, bottomTrimMm = bottomMm) }
        recalculate()
    }

    fun updateFinishes(topFinishMm: Double, bottomFinishMm: Double) {
        _project.update { it.copy(topFinishMm = topFinishMm, bottomFinishMm = bottomFinishMm) }
        recalculate()
    }

    fun updatePatternRepeat(repeatMm: Double, match: PatternMatch) {
        _project.update { current ->
            val updatedProduct = current.wallpaperProduct.copy(
                patternRepeatMm = repeatMm,
                patternMatch = match
            )
            current.copy(wallpaperProduct = updatedProduct)
        }
        recalculate()
    }

    fun setExtraRollAllowance(extraRolls: Int) {
        _project.update { it.copy(extraRollAllowance = extraRolls) }
        recalculate()
    }

    fun setExtraMaterialPercent(percent: Double) {
        _project.update { it.copy(extraMaterialPercent = percent) }
        recalculate()
    }

    fun setWallpaperDirection(direction: WallpaperDirection) {
        _project.update { it.copy(direction = direction) }
        recalculate()
    }

    // --- OPENINGS ---

    fun addOpening(opening: Opening) {
        _project.update { current ->
            current.copy(openings = current.openings + opening)
        }
        recalculate()
    }

    fun updateOpening(opening: Opening) {
        _project.update { current ->
            val updated = current.openings.map { if (it.id == opening.id) opening else it }
            current.copy(openings = updated)
        }
        recalculate()
    }

    fun removeOpening(openingId: String) {
        _project.update { current ->
            current.copy(openings = current.openings.filterNot { it.id == openingId })
        }
        recalculate()
    }

    // --- CUSTOM WALLS ---

    fun addWall(wall: WallpaperWall) {
        _project.update { current ->
            current.copy(walls = current.walls + wall)
        }
        recalculate()
    }

    fun updateWall(wall: WallpaperWall) {
        _project.update { current ->
            val updated = current.walls.map { if (it.id == wall.id) wall else it }
            current.copy(walls = updated)
        }
        recalculate()
    }

    fun toggleWallEnabled(wallId: String, enabled: Boolean) {
        _project.update { current ->
            val updated = current.walls.map { if (it.id == wallId) it.copy(wallpaperEnabled = enabled) else it }
            current.copy(walls = updated)
        }
        recalculate()
    }

    fun removeWall(wallId: String) {
        _project.update { current ->
            current.copy(walls = current.walls.filterNot { it.id == wallId })
        }
        recalculate()
    }

    // --- MULTIPLE ROOMS ---

    fun addRoom(room: WallpaperRoom) {
        _project.update { current ->
            current.copy(rooms = current.rooms + room)
        }
        recalculate()
    }

    fun duplicateRoom(roomId: String) {
        _project.update { current ->
            val roomToDuplicate = current.rooms.find { it.id == roomId } ?: return@update current
            val duplicated = roomToDuplicate.copy(
                id = UUID.randomUUID().toString(),
                name = "${roomToDuplicate.name} (Copy)"
            )
            current.copy(rooms = current.rooms + duplicated)
        }
        recalculate()
    }

    fun updateRoom(room: WallpaperRoom) {
        _project.update { current ->
            val updated = current.rooms.map { if (it.id == room.id) room else it }
            current.copy(rooms = updated)
        }
        recalculate()
    }

    fun removeRoom(roomId: String) {
        _project.update { current ->
            current.copy(rooms = current.rooms.filterNot { it.id == roomId })
        }
        recalculate()
    }

    // --- PRODUCT PROFILES ---

    fun applyProductProfile(product: WallpaperProduct) {
        _project.update { current ->
            current.copy(wallpaperProduct = product)
        }
        recalculate()
    }

    fun saveCurrentAsProductProfile(name: String) {
        val currentProd = _project.value.wallpaperProduct.copy(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Custom Wallpaper" }
        )
        val updatedList = repository.addProductProfile(currentProd)
        _products.value = updatedList
    }

    fun deleteProductProfile(id: String) {
        val updated = repository.deleteProductProfile(id)
        _products.value = updated
    }

    // --- HISTORY ---

    fun saveToHistory() {
        val updatedHistory = repository.saveHistoryItem(_project.value)
        _history.value = updatedHistory
    }

    fun openHistoryItem(item: WallpaperHistory) {
        _project.value = item.project
        recalculate()
    }

    fun deleteHistoryItem(id: String) {
        val updated = repository.deleteHistoryItem(id)
        _history.value = updated
    }

    fun duplicateHistoryItem(item: WallpaperHistory) {
        val duplicated = item.project.copy(
            id = UUID.randomUUID().toString(),
            name = "${item.project.name} (Copy)"
        )
        val updated = repository.saveHistoryItem(duplicated)
        _history.value = updated
    }

    fun clearAllHistory() {
        val updated = repository.clearAllHistory()
        _history.value = updated
    }

    // --- SETTINGS ---

    fun updateThemeMode(themeMode: AppThemeMode) {
        updateSettings { it.copy(themeMode = themeMode) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        updateSettings { it.copy(dynamicColor = enabled) }
    }

    fun updateDecimalPlaces(places: Int) {
        updateSettings { it.copy(decimalPlaces = places.coerceIn(0, 4)) }
    }

    fun updateSettingToggle(
        showRoomDiagram: Boolean? = null,
        showRollDiagram: Boolean? = null,
        showCuttingPlan: Boolean? = null,
        showBreakdown: Boolean? = null,
        showOpeningArea: Boolean? = null,
        showPurchaseExcess: Boolean? = null,
        liveCalculation: Boolean? = null,
        haptics: Boolean? = null,
        animations: Boolean? = null,
        confirmDelete: Boolean? = null,
        restoreLastCalculation: Boolean? = null
    ) {
        updateSettings { curr ->
            curr.copy(
                showRoomDiagram = showRoomDiagram ?: curr.showRoomDiagram,
                showRollDiagram = showRollDiagram ?: curr.showRollDiagram,
                showCuttingPlan = showCuttingPlan ?: curr.showCuttingPlan,
                showCalculationBreakdown = showBreakdown ?: curr.showCalculationBreakdown,
                showOpeningArea = showOpeningArea ?: curr.showOpeningArea,
                showPurchaseExcess = showPurchaseExcess ?: curr.showPurchaseExcess,
                liveCalculation = liveCalculation ?: curr.liveCalculation,
                haptics = haptics ?: curr.haptics,
                animations = animations ?: curr.animations,
                confirmDelete = confirmDelete ?: curr.confirmDelete,
                restoreLastCalculation = restoreLastCalculation ?: curr.restoreLastCalculation
            )
        }
    }

    fun resetSettingsToDefault() {
        val defaultSettings = repository.resetSettings()
        _settings.value = defaultSettings
    }

    private fun updateSettings(transform: (SettingsState) -> SettingsState) {
        val newSettings = transform(_settings.value)
        _settings.value = newSettings
        repository.saveSettings(newSettings)
    }

    // --- EXPORTS & SHARE ---

    fun getExportTxt(): String = repository.exportToTxt(_project.value)

    fun getExportCsv(): String = repository.exportToCsv(_project.value)

    fun getExportJson(): String = repository.exportToJson(_project.value)

    fun getShareText(): String = repository.createShareText(_project.value)
}
