package com.example.wallpaperneed.data

import android.content.Context
import android.content.SharedPreferences
import com.example.wallpaperneed.calculator.WallpaperCalculator
import com.example.wallpaperneed.model.AppThemeMode
import com.example.wallpaperneed.model.CalculationMode
import com.example.wallpaperneed.model.DropOptimizationMode
import com.example.wallpaperneed.model.LengthUnit
import com.example.wallpaperneed.model.Opening
import com.example.wallpaperneed.model.OpeningType
import com.example.wallpaperneed.model.PatternMatch
import com.example.wallpaperneed.model.RollCalculationMode
import com.example.wallpaperneed.model.SettingsState
import com.example.wallpaperneed.model.WallpaperDirection
import com.example.wallpaperneed.model.WallpaperHistory
import com.example.wallpaperneed.model.WallpaperProduct
import com.example.wallpaperneed.model.WallpaperProject
import com.example.wallpaperneed.model.WallpaperRoom
import com.example.wallpaperneed.model.WallpaperWall
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class WallpaperPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("wallpaper_need_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SETTINGS = "key_settings_json"
        private const val KEY_HISTORY = "key_history_json"
        private const val KEY_PRODUCTS = "key_products_json"
        private const val KEY_LAST_PROJECT = "key_last_project_json"
        private const val MAX_HISTORY_ITEMS = 10
        private const val MAX_PRODUCT_PROFILES = 20
    }

    // --- SETTINGS ---

    fun getSettings(): SettingsState {
        val jsonStr = prefs.getString(KEY_SETTINGS, null) ?: return SettingsState()
        return try {
            val json = JSONObject(jsonStr)
            SettingsState(
                themeMode = AppThemeMode.valueOf(json.optString("themeMode", AppThemeMode.SYSTEM.name)),
                dynamicColor = json.optBoolean("dynamicColor", true),
                defaultUnit = LengthUnit.valueOf(json.optString("defaultUnit", LengthUnit.CENTIMETERS.name)),
                defaultRollWidthMm = json.optDouble("defaultRollWidthMm", 530.0),
                defaultRollLengthMm = json.optDouble("defaultRollLengthMm", 10050.0),
                defaultTopTrimMm = json.optDouble("defaultTopTrimMm", 50.0),
                defaultBottomTrimMm = json.optDouble("defaultBottomTrimMm", 50.0),
                defaultPatternRepeatMm = json.optDouble("defaultPatternRepeatMm", 0.0),
                defaultRollCalculationMode = RollCalculationMode.valueOf(
                    json.optString("defaultRollCalculationMode", RollCalculationMode.COMBINED.name)
                ),
                showRoomDiagram = json.optBoolean("showRoomDiagram", true),
                showRollDiagram = json.optBoolean("showRollDiagram", true),
                showCuttingPlan = json.optBoolean("showCuttingPlan", true),
                showCalculationBreakdown = json.optBoolean("showCalculationBreakdown", true),
                showOpeningArea = json.optBoolean("showOpeningArea", true),
                showPurchaseExcess = json.optBoolean("showPurchaseExcess", true),
                decimalPlaces = json.optInt("decimalPlaces", 2),
                liveCalculation = json.optBoolean("liveCalculation", true),
                haptics = json.optBoolean("haptics", true),
                animations = json.optBoolean("animations", true),
                confirmDelete = json.optBoolean("confirmDelete", true),
                restoreLastCalculation = json.optBoolean("restoreLastCalculation", true)
            )
        } catch (_: Exception) {
            SettingsState()
        }
    }

    fun saveSettings(settings: SettingsState) {
        val json = JSONObject().apply {
            put("themeMode", settings.themeMode.name)
            put("dynamicColor", settings.dynamicColor)
            put("defaultUnit", settings.defaultUnit.name)
            put("defaultRollWidthMm", settings.defaultRollWidthMm)
            put("defaultRollLengthMm", settings.defaultRollLengthMm)
            put("defaultTopTrimMm", settings.defaultTopTrimMm)
            put("defaultBottomTrimMm", settings.defaultBottomTrimMm)
            put("defaultPatternRepeatMm", settings.defaultPatternRepeatMm)
            put("defaultRollCalculationMode", settings.defaultRollCalculationMode.name)
            put("showRoomDiagram", settings.showRoomDiagram)
            put("showRollDiagram", settings.showRollDiagram)
            put("showCuttingPlan", settings.showCuttingPlan)
            put("showCalculationBreakdown", settings.showCalculationBreakdown)
            put("showOpeningArea", settings.showOpeningArea)
            put("showPurchaseExcess", settings.showPurchaseExcess)
            put("decimalPlaces", settings.decimalPlaces)
            put("liveCalculation", settings.liveCalculation)
            put("haptics", settings.haptics)
            put("animations", settings.animations)
            put("confirmDelete", settings.confirmDelete)
            put("restoreLastCalculation", settings.restoreLastCalculation)
        }
        prefs.edit().putString(KEY_SETTINGS, json.toString()).apply()
    }

    fun resetSettings(): SettingsState {
        val defaultSettings = SettingsState()
        saveSettings(defaultSettings)
        return defaultSettings
    }

    // --- PRODUCT PROFILES ---

    fun getProductProfiles(): List<WallpaperProduct> {
        val jsonStr = prefs.getString(KEY_PRODUCTS, null)
        if (jsonStr == null) {
            // Seed defaults
            saveProductProfiles(WallpaperProduct.DEFAULT_PRESETS)
            return WallpaperProduct.DEFAULT_PRESETS
        }
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<WallpaperProduct>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    WallpaperProduct(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        rollWidthMm = obj.getDouble("rollWidthMm"),
                        rollLengthMm = obj.getDouble("rollLengthMm"),
                        patternRepeatMm = obj.optDouble("patternRepeatMm", 0.0),
                        patternMatch = PatternMatch.valueOf(obj.optString("patternMatch", PatternMatch.NONE.name))
                    )
                )
            }
            if (list.isEmpty()) WallpaperProduct.DEFAULT_PRESETS else list
        } catch (_: Exception) {
            WallpaperProduct.DEFAULT_PRESETS
        }
    }

    fun saveProductProfiles(products: List<WallpaperProduct>) {
        val array = JSONArray()
        // Enforce max 20 product profiles
        val limited = products.take(MAX_PRODUCT_PROFILES)
        for (prod in limited) {
            val obj = JSONObject().apply {
                put("id", prod.id)
                put("name", prod.name)
                put("rollWidthMm", prod.rollWidthMm)
                put("rollLengthMm", prod.rollLengthMm)
                put("patternRepeatMm", prod.patternRepeatMm)
                put("patternMatch", prod.patternMatch.name)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_PRODUCTS, array.toString()).apply()
    }

    fun addProductProfile(product: WallpaperProduct): List<WallpaperProduct> {
        val current = getProductProfiles().toMutableList()
        current.removeAll { it.id == product.id }
        current.add(0, product)
        val trimmed = current.take(MAX_PRODUCT_PROFILES)
        saveProductProfiles(trimmed)
        return trimmed
    }

    fun deleteProductProfile(productId: String): List<WallpaperProduct> {
        val current = getProductProfiles().filterNot { it.id == productId }
        saveProductProfiles(current)
        return current
    }

    // --- HISTORY ---

    fun getHistory(): List<WallpaperHistory> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<WallpaperHistory>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val createdAt = obj.getLong("createdAt")
                val projJson = obj.getJSONObject("project")
                val project = deserializeProject(projJson)
                list.add(WallpaperHistory(id = id, name = name, project = project, createdAt = createdAt))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveHistoryItem(project: WallpaperProject): List<WallpaperHistory> {
        val current = getHistory().toMutableList()
        val calculatedProject = project.copy(result = WallpaperCalculator.calculateProject(project))
        val newItem = WallpaperHistory(
            name = project.name.ifBlank { "Calculation" },
            project = calculatedProject,
            createdAt = System.currentTimeMillis()
        )
        // Add to top
        current.add(0, newItem)
        // If > 10, remove the oldest (from tail)
        val limited = current.take(MAX_HISTORY_ITEMS)
        saveHistoryList(limited)
        return limited
    }

    fun deleteHistoryItem(id: String): List<WallpaperHistory> {
        val current = getHistory().filterNot { it.id == id }
        saveHistoryList(current)
        return current
    }

    fun clearAllHistory(): List<WallpaperHistory> {
        saveHistoryList(emptyList())
        return emptyList()
    }

    private fun saveHistoryList(list: List<WallpaperHistory>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("createdAt", item.createdAt)
                put("project", serializeProject(item.project))
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    // --- LAST ACTIVE PROJECT ---

    fun saveLastProject(project: WallpaperProject) {
        val json = serializeProject(project)
        prefs.edit().putString(KEY_LAST_PROJECT, json.toString()).apply()
    }

    fun getLastProject(): WallpaperProject? {
        val jsonStr = prefs.getString(KEY_LAST_PROJECT, null) ?: return null
        return try {
            deserializeProject(JSONObject(jsonStr))
        } catch (_: Exception) {
            null
        }
    }

    // --- SERIALIZATION HELPERS ---

    private fun serializeProject(project: WallpaperProject): JSONObject {
        return JSONObject().apply {
            put("id", project.id)
            put("name", project.name)
            put("calculationMode", project.calculationMode.name)
            put("rollCalculationMode", project.rollCalculationMode.name)
            put("dropOptimizationMode", project.dropOptimizationMode.name)
            put("lengthUnit", project.lengthUnit.name)
            put("roomLengthMm", project.roomLengthMm)
            put("roomWidthMm", project.roomWidthMm)
            put("wallHeightMm", project.wallHeightMm)
            put("sameWallHeight", project.sameWallHeight)
            put("singleWallWidthMm", project.singleWallWidthMm)
            put("singleWallHeightMm", project.singleWallHeightMm)
            put("topTrimMm", project.topTrimMm)
            put("bottomTrimMm", project.bottomTrimMm)
            put("topFinishMm", project.topFinishMm)
            put("bottomFinishMm", project.bottomFinishMm)
            put("extraRollAllowance", project.extraRollAllowance)
            put("extraMaterialPercent", project.extraMaterialPercent)
            put("direction", project.direction.name)

            // Product
            val prodObj = JSONObject().apply {
                put("id", project.wallpaperProduct.id)
                put("name", project.wallpaperProduct.name)
                put("rollWidthMm", project.wallpaperProduct.rollWidthMm)
                put("rollLengthMm", project.wallpaperProduct.rollLengthMm)
                put("patternRepeatMm", project.wallpaperProduct.patternRepeatMm)
                put("patternMatch", project.wallpaperProduct.patternMatch.name)
            }
            put("wallpaperProduct", prodObj)

            // Openings
            val openingsArr = JSONArray()
            for (op in project.openings) {
                openingsArr.put(
                    JSONObject().apply {
                        put("id", op.id)
                        put("name", op.name)
                        put("type", op.type.name)
                        put("widthMm", op.widthMm)
                        put("heightMm", op.heightMm)
                        put("quantity", op.quantity)
                        put("wallId", op.wallId)
                    }
                )
            }
            put("openings", openingsArr)

            // Walls
            val wallsArr = JSONArray()
            for (w in project.walls) {
                wallsArr.put(
                    JSONObject().apply {
                        put("id", w.id)
                        put("name", w.name)
                        put("widthMm", w.widthMm)
                        put("heightMm", w.heightMm)
                        put("wallpaperEnabled", w.wallpaperEnabled)
                        put("isFeatureWall", w.isFeatureWall)
                    }
                )
            }
            put("walls", wallsArr)

            // Rooms
            val roomsArr = JSONArray()
            for (r in project.rooms) {
                roomsArr.put(
                    JSONObject().apply {
                        put("id", r.id)
                        put("name", r.name)
                        put("lengthMm", r.lengthMm)
                        put("widthMm", r.widthMm)
                        put("heightMm", r.heightMm)
                        put("sameWallHeight", r.sameWallHeight)
                        put("topTrimMm", r.topTrimMm)
                        put("bottomTrimMm", r.bottomTrimMm)
                    }
                )
            }
            put("rooms", roomsArr)
        }
    }

    private fun deserializeProject(json: JSONObject): WallpaperProject {
        val prodJson = json.optJSONObject("wallpaperProduct")
        val product = if (prodJson != null) {
            WallpaperProduct(
                id = prodJson.optString("id", "prod_1"),
                name = prodJson.optString("name", "Standard Wallpaper"),
                rollWidthMm = prodJson.optDouble("rollWidthMm", 530.0),
                rollLengthMm = prodJson.optDouble("rollLengthMm", 10050.0),
                patternRepeatMm = prodJson.optDouble("patternRepeatMm", 0.0),
                patternMatch = PatternMatch.valueOf(prodJson.optString("patternMatch", PatternMatch.NONE.name))
            )
        } else {
            WallpaperProduct.STANDARD_EURO
        }

        val openingsList = mutableListOf<Opening>()
        val openingsArr = json.optJSONArray("openings")
        if (openingsArr != null) {
            for (i in 0 until openingsArr.length()) {
                val opObj = openingsArr.getJSONObject(i)
                openingsList.add(
                    Opening(
                        id = opObj.optString("id"),
                        name = opObj.optString("name", "Opening"),
                        type = OpeningType.valueOf(opObj.optString("type", OpeningType.DOOR.name)),
                        widthMm = opObj.optDouble("widthMm", 900.0),
                        heightMm = opObj.optDouble("heightMm", 2100.0),
                        quantity = opObj.optInt("quantity", 1),
                        wallId = opObj.optString("wallId", "")
                    )
                )
            }
        }

        val wallsList = mutableListOf<WallpaperWall>()
        val wallsArr = json.optJSONArray("walls")
        if (wallsArr != null) {
            for (i in 0 until wallsArr.length()) {
                val wObj = wallsArr.getJSONObject(i)
                wallsList.add(
                    WallpaperWall(
                        id = wObj.optString("id"),
                        name = wObj.optString("name", "Wall"),
                        widthMm = wObj.optDouble("widthMm", 5000.0),
                        heightMm = wObj.optDouble("heightMm", 3000.0),
                        wallpaperEnabled = wObj.optBoolean("wallpaperEnabled", true),
                        isFeatureWall = wObj.optBoolean("isFeatureWall", false)
                    )
                )
            }
        }

        val roomsList = mutableListOf<WallpaperRoom>()
        val roomsArr = json.optJSONArray("rooms")
        if (roomsArr != null) {
            for (i in 0 until roomsArr.length()) {
                val rObj = roomsArr.getJSONObject(i)
                roomsList.add(
                    WallpaperRoom(
                        id = rObj.optString("id"),
                        name = rObj.optString("name", "Room"),
                        lengthMm = rObj.optDouble("lengthMm", 5000.0),
                        widthMm = rObj.optDouble("widthMm", 4000.0),
                        heightMm = rObj.optDouble("heightMm", 3000.0),
                        sameWallHeight = rObj.optBoolean("sameWallHeight", true),
                        topTrimMm = rObj.optDouble("topTrimMm", 50.0),
                        bottomTrimMm = rObj.optDouble("bottomTrimMm", 50.0)
                    )
                )
            }
        }

        val project = WallpaperProject(
            id = json.optString("id"),
            name = json.optString("name", "Room Project"),
            calculationMode = CalculationMode.valueOf(json.optString("calculationMode", CalculationMode.COMPLETE_ROOM.name)),
            rollCalculationMode = RollCalculationMode.valueOf(json.optString("rollCalculationMode", RollCalculationMode.COMBINED.name)),
            dropOptimizationMode = DropOptimizationMode.valueOf(json.optString("dropOptimizationMode", DropOptimizationMode.CONSERVATIVE.name)),
            lengthUnit = LengthUnit.valueOf(json.optString("lengthUnit", LengthUnit.CENTIMETERS.name)),
            roomLengthMm = json.optDouble("roomLengthMm", 5000.0),
            roomWidthMm = json.optDouble("roomWidthMm", 4000.0),
            wallHeightMm = json.optDouble("wallHeightMm", 3000.0),
            sameWallHeight = json.optBoolean("sameWallHeight", true),
            singleWallWidthMm = json.optDouble("singleWallWidthMm", 5000.0),
            singleWallHeightMm = json.optDouble("singleWallHeightMm", 3000.0),
            topTrimMm = json.optDouble("topTrimMm", 50.0),
            bottomTrimMm = json.optDouble("bottomTrimMm", 50.0),
            topFinishMm = json.optDouble("topFinishMm", 0.0),
            bottomFinishMm = json.optDouble("bottomFinishMm", 0.0),
            extraRollAllowance = json.optInt("extraRollAllowance", 0),
            extraMaterialPercent = json.optDouble("extraMaterialPercent", 0.0),
            direction = WallpaperDirection.valueOf(json.optString("direction", WallpaperDirection.VERTICAL.name)),
            wallpaperProduct = product,
            openings = openingsList,
            walls = if (wallsList.isEmpty()) {
                listOf(
                    WallpaperWall(id = "wall_north", name = "North Wall", widthMm = 5000.0, heightMm = 3000.0),
                    WallpaperWall(id = "wall_east", name = "East Wall", widthMm = 4000.0, heightMm = 3000.0),
                    WallpaperWall(id = "wall_south", name = "South Wall", widthMm = 5000.0, heightMm = 3000.0),
                    WallpaperWall(id = "wall_west", name = "West Wall", widthMm = 4000.0, heightMm = 3000.0)
                )
            } else wallsList,
            rooms = roomsList
        )

        return project.copy(result = WallpaperCalculator.calculateProject(project))
    }

    // --- EXPORTERS ---

    fun exportToTxt(project: WallpaperProject): String {
        val res = if (project.result.isValid) project.result else WallpaperCalculator.calculateProject(project)
        val l = (project.roomLengthMm / 10.0).toInt()
        val w = (project.roomWidthMm / 10.0).toInt()
        val h = (project.wallHeightMm / 10.0).toInt()
        val rw = (project.wallpaperProduct.rollWidthMm / 10.0).toInt()
        val rl = project.wallpaperProduct.rollLengthMm / 1000.0
        val rep = (project.wallpaperProduct.patternRepeatMm / 10.0).toInt()
        val top = (project.topTrimMm / 10.0).toInt()
        val bot = (project.bottomTrimMm / 10.0).toInt()
        val adjDrop = (res.adjustedDropLengthMm / 10.0).toInt()
        val totalLength = res.totalPurchaseLengthMm / 1000.0

        return """
WALLPAPERNEED

Project:
${project.name}

Room:
$l × $w cm

Wall Height:
$h cm

Gross Wall Area:
${String.format(Locale.US, "%.2f", res.grossWallAreaMm2 / 1_000_000.0)} m²

Opening Area:
${String.format(Locale.US, "%.2f", res.openingAreaMm2 / 1_000_000.0)} m²

Net Wall Area:
${String.format(Locale.US, "%.2f", res.netWallAreaMm2 / 1_000_000.0)} m²

Wallpaper:
$rw cm × ${String.format(Locale.US, "%.2f", rl)} m

Pattern Repeat:
${if (rep > 0) "$rep cm" else "None"}

Trim:
$top cm top
$bot cm bottom

Adjusted Drop:
$adjDrop cm

Required Drops:
${res.requiredDrops}

Drops Per Roll:
${res.dropsPerRoll}

Rolls Required:
${res.requiredRolls}

Total Purchase Length:
${String.format(Locale.US, "%.2f", totalLength)} m

Purchase Area:
${String.format(Locale.US, "%.2f", res.purchaseAreaMm2 / 1_000_000.0)} m²
""".trimIndent()
    }

    fun exportToCsv(project: WallpaperProject): String {
        val res = if (project.result.isValid) project.result else WallpaperCalculator.calculateProject(project)
        val grossM2 = String.format(Locale.US, "%.2f", res.grossWallAreaMm2 / 1_000_000.0)
        val openM2 = String.format(Locale.US, "%.2f", res.openingAreaMm2 / 1_000_000.0)
        val netM2 = String.format(Locale.US, "%.2f", res.netWallAreaMm2 / 1_000_000.0)
        val rWidthCm = (project.wallpaperProduct.rollWidthMm / 10.0).toInt()
        val rLenM = String.format(Locale.US, "%.2f", project.wallpaperProduct.rollLengthMm / 1000.0)
        val dropM = String.format(Locale.US, "%.2f", res.adjustedDropLengthMm / 1000.0)

        return """Room,Wall Area,Opening Area,Net Area,Roll Width,Roll Length,Drops,Drop Length,Drops Per Roll,Rolls
${project.name},$grossM2,$openM2,$netM2,$rWidthCm,$rLenM,${res.requiredDrops},$dropM,${res.dropsPerRoll},${res.requiredRolls}"""
    }

    fun exportToJson(project: WallpaperProject): String {
        val res = if (project.result.isValid) project.result else WallpaperCalculator.calculateProject(project)
        val json = JSONObject().apply {
            put("name", project.name)
            put("unit", project.lengthUnit.name)
            val roomObj = JSONObject().apply {
                put("length", (project.roomLengthMm / 10.0).toInt())
                put("width", (project.roomWidthMm / 10.0).toInt())
                put("height", (project.wallHeightMm / 10.0).toInt())
            }
            put("room", roomObj)
            put("wallAreaM2", (res.grossWallAreaMm2 / 1_000_000.0 * 100).toLong() / 100.0)
            put("openingAreaM2", (res.openingAreaMm2 / 1_000_000.0 * 100).toLong() / 100.0)
            put("netWallAreaM2", (res.netWallAreaMm2 / 1_000_000.0 * 100).toLong() / 100.0)

            val wpObj = JSONObject().apply {
                put("rollWidthCm", (project.wallpaperProduct.rollWidthMm / 10.0).toInt())
                put("rollLengthM", project.wallpaperProduct.rollLengthMm / 1000.0)
                put("patternRepeatCm", (project.wallpaperProduct.patternRepeatMm / 10.0).toInt())
                put("adjustedDropCm", (res.adjustedDropLengthMm / 10.0).toInt())
                put("requiredDrops", res.requiredDrops)
                put("dropsPerRoll", res.dropsPerRoll)
                put("rollsRequired", res.requiredRolls)
            }
            put("wallpaper", wpObj)
        }
        return json.toString(2)
    }

    fun createShareText(project: WallpaperProject): String {
        val res = if (project.result.isValid) project.result else WallpaperCalculator.calculateProject(project)
        val rWidthCm = (project.wallpaperProduct.rollWidthMm / 10.0).toInt()
        val rLenM = String.format(Locale.US, "%.2f", project.wallpaperProduct.rollLengthMm / 1000.0)
        val dropCm = (res.adjustedDropLengthMm / 10.0).toInt()
        val grossM2 = String.format(Locale.US, "%.2f", res.grossWallAreaMm2 / 1_000_000.0)

        return """WallpaperNeed

${project.name}

Wall Area:
$grossM2 m²

Wallpaper:
$rWidthCm cm × $rLenM m

Required Drops:
${res.requiredDrops}

Drops / Roll:
${res.dropsPerRoll}

Adjusted Drop:
$dropCm cm

Wallpaper Required:
${res.requiredRolls} rolls
""".trimIndent()
    }
}
