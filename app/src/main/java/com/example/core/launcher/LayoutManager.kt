package com.example.core.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import com.example.data.local.dao.LauncherLayoutDao
import com.example.data.local.dao.LauncherThemeDao
import com.example.data.local.entity.LauncherLayoutEntity
import com.example.data.local.entity.LauncherThemeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class LayoutManager(
    private val context: Context,
    private val layoutDao: LauncherLayoutDao,
    private val themeDao: LauncherThemeDao,
    private val scope: CoroutineScope
) {
    val allLayoutItems: StateFlow<List<LauncherLayoutEntity>> = layoutDao.getAllLayoutItemsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val dockItems: StateFlow<List<LauncherLayoutEntity>> = layoutDao.getDockItemsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val currentTheme: StateFlow<LauncherThemeEntity> = themeDao.getThemeFlow()
        .map { it ?: LauncherThemeEntity() }
        .stateIn(scope, SharingStarted.Eagerly, LauncherThemeEntity())

    val activeComplexityMode = MutableStateFlow(ComplexityMode.BALANCED)
    val activeProfile = MutableStateFlow(ProfileType.EVERYDAY)

    private val layoutHistory = mutableListOf<List<LauncherLayoutEntity>>()

    init {
        scope.launch(Dispatchers.IO) {
            // Seed initial theme if missing
            if (themeDao.getTheme() == null) {
                themeDao.saveTheme(LauncherThemeEntity())
            }

            // Seed initial layouts if empty
            val initial = layoutDao.getAllLayoutItemsFlow().first()
            if (initial.isEmpty()) {
                seedDefaultLayout()
            }
        }
    }

    private suspend fun seedDefaultLayout() {
        val pm = context.packageManager
        val seedItems = mutableListOf<LauncherLayoutEntity>()

        // 1. Identify Essential Apps for Dock
        val phonePkg = resolveDefaultPackage(Intent(Intent.ACTION_DIAL))
        val smsPkg = resolveDefaultPackage(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")))
        val browserPkg = resolveDefaultPackage(Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")))
        val cameraPkg = resolveDefaultPackage(Intent(MediaStore.ACTION_IMAGE_CAPTURE))

        var dockIndex = 0
        val dockCandidates = listOfNotNull(phonePkg, smsPkg, browserPkg, cameraPkg).distinct()
        for (pkg in dockCandidates) {
            seedItems.add(
                LauncherLayoutEntity(
                    id = "dock_${UUID.randomUUID()}",
                    pageIndex = -1, // Dock is page -1
                    cellX = dockIndex,
                    cellY = 0,
                    itemType = "DOCK",
                    packageName = pkg,
                    activityName = getLaunchActivity(pm, pkg) ?: ""
                )
            )
            dockIndex++
            if (dockIndex >= 5) break
        }

        // 2. Identify top 12 installed launcher apps for Page 0
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
        val homeApps = resolveInfos.mapNotNull {
            val pkg = it.activityInfo.packageName
            if (pkg == context.packageName || dockCandidates.contains(pkg)) null
            else pkg
        }.distinct().take(12)

        var cellX = 0
        var cellY = 0
        for (pkg in homeApps) {
            seedItems.add(
                LauncherLayoutEntity(
                    id = "page0_${UUID.randomUUID()}",
                    pageIndex = 0,
                    cellX = cellX,
                    cellY = cellY,
                    itemType = "APP",
                    packageName = pkg,
                    activityName = getLaunchActivity(pm, pkg) ?: ""
                )
            )
            cellX++
            if (cellX >= 4) {
                cellX = 0
                cellY++
            }
        }

        layoutDao.insertItems(seedItems)
    }

    private fun resolveDefaultPackage(intent: Intent): String? {
        return try {
            val res = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            val pkg = res?.activityInfo?.packageName
            if (pkg != null && pkg != "android") pkg else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getLaunchActivity(pm: PackageManager, packageName: String): String? {
        return try {
            pm.getLaunchIntentForPackage(packageName)?.component?.className
        } catch (e: Exception) {
            null
        }
    }

    fun addItemToPage(pageIndex: Int, cellX: Int, cellY: Int, packageName: String, itemType: String = "APP") {
        scope.launch(Dispatchers.IO) {
            val theme = currentTheme.value
            if (theme.isLayoutLocked) return@launch

            val entity = LauncherLayoutEntity(
                id = UUID.randomUUID().toString(),
                pageIndex = pageIndex,
                cellX = cellX,
                cellY = cellY,
                itemType = itemType,
                packageName = packageName,
                activityName = getLaunchActivity(context.packageManager, packageName) ?: ""
            )
            layoutDao.insertItem(entity)
        }
    }

    fun removeItem(id: String) {
        scope.launch(Dispatchers.IO) {
            if (currentTheme.value.isLayoutLocked) return@launch
            backupCurrentLayout()
            layoutDao.deleteItemById(id)
        }
    }

    fun updateItemPlacement(id: String, cellX: Int, cellY: Int, spanX: Int = 1, spanY: Int = 1) {
        scope.launch(Dispatchers.IO) {
            if (currentTheme.value.isLayoutLocked) return@launch
            val item = layoutDao.getItemById(id) ?: return@launch
            val updated = item.copy(
                cellX = cellX,
                cellY = cellY,
                spanX = spanX,
                spanY = spanY,
                updatedAt = System.currentTimeMillis()
            )
            layoutDao.updateItem(updated)
        }
    }

    fun addCustomWidget(
        archetype: String,
        cellX: Int,
        cellY: Int,
        spanX: Int = 2,
        spanY: Int = 2,
        configJson: String = "{}"
    ) {
        scope.launch(Dispatchers.IO) {
            if (currentTheme.value.isLayoutLocked) return@launch
            val entity = LauncherLayoutEntity(
                id = "custom_widget_${UUID.randomUUID()}",
                pageIndex = 0,
                cellX = cellX,
                cellY = cellY,
                spanX = spanX,
                spanY = spanY,
                itemType = "CUSTOM_WIDGET",
                widgetArchetype = archetype,
                widgetConfigJson = configJson
            )
            layoutDao.insertItem(entity)
        }
    }

    fun addCustomWidget(
        archetype: String,
        configJson: String = "{}",
        widthDp: Int = 4,
        heightDp: Int = 2
    ) {
        addCustomWidget(
            archetype = archetype,
            cellX = 0,
            cellY = 0,
            spanX = widthDp,
            spanY = heightDp,
            configJson = configJson
        )
    }

    fun addAppWidgetWithId(
        appWidgetId: Int,
        packageName: String,
        providerClass: String,
        widthDp: Int = 4,
        heightDp: Int = 2
    ) {
        scope.launch(Dispatchers.IO) {
            if (currentTheme.value.isLayoutLocked) return@launch
            val entity = LauncherLayoutEntity(
                id = "appwidget_${UUID.randomUUID()}",
                pageIndex = 0,
                cellX = 0,
                cellY = 0,
                spanX = widthDp,
                spanY = heightDp,
                itemType = "APP_WIDGET",
                packageName = packageName,
                activityName = providerClass,
                appWidgetId = appWidgetId
            )
            layoutDao.insertItem(entity)
        }
    }

    fun saveHomeAppOrder(packageNames: List<String>) {
        scope.launch(Dispatchers.IO) {
            packageNames.forEachIndexed { index, pkg ->
                val entity = LauncherLayoutEntity(
                    id = "home_app_$pkg",
                    pageIndex = 0,
                    cellX = index % 4,
                    cellY = index / 4,
                    itemType = "APP",
                    packageName = pkg
                )
                layoutDao.insertItem(entity)
            }
        }
    }

    fun removeHomeItem(id: String) {
        scope.launch(Dispatchers.IO) {
            layoutDao.deleteItemById(id)
        }
    }

    fun addAppWidget(
        packageName: String,
        providerClass: String,
        widthDp: Int = 4,
        heightDp: Int = 2
    ) {
        scope.launch(Dispatchers.IO) {
            if (currentTheme.value.isLayoutLocked) return@launch
            val entity = LauncherLayoutEntity(
                id = "appwidget_${UUID.randomUUID()}",
                pageIndex = 0,
                cellX = 0,
                cellY = 0,
                spanX = widthDp,
                spanY = heightDp,
                itemType = "APP_WIDGET",
                packageName = packageName,
                activityName = providerClass
            )
            layoutDao.insertItem(entity)
        }
    }

    fun addAppWidget(
        appWidgetId: Int,
        cellX: Int,
        cellY: Int,
        spanX: Int = 2,
        spanY: Int = 2
    ) {
        scope.launch(Dispatchers.IO) {
            if (currentTheme.value.isLayoutLocked) return@launch
            val entity = LauncherLayoutEntity(
                id = "appwidget_${UUID.randomUUID()}",
                pageIndex = 0,
                cellX = cellX,
                cellY = cellY,
                spanX = spanX,
                spanY = spanY,
                itemType = "WIDGET",
                appWidgetId = appWidgetId
            )
            layoutDao.insertItem(entity)
        }
    }

    fun updateFloatingWindowState(id: String, x: Float, y: Float, widthDp: Int, heightDp: Int, isMinimized: Boolean) {
        scope.launch(Dispatchers.IO) {
            val item = layoutDao.getItemById(id) ?: return@launch
            val updated = item.copy(
                isFloatingWindow = true,
                windowX = x,
                windowY = y,
                windowWidthDp = widthDp,
                windowHeightDp = heightDp,
                isMinimized = isMinimized,
                updatedAt = System.currentTimeMillis()
            )
            layoutDao.updateItem(updated)
        }
    }

    fun toggleLayoutLock(locked: Boolean) {
        scope.launch(Dispatchers.IO) {
            themeDao.setLayoutLocked(locked)
        }
    }

    fun setComplexityMode(mode: ComplexityMode) {
        activeComplexityMode.value = mode
        scope.launch(Dispatchers.IO) {
            val current = currentTheme.value
            val newCols = when (mode) {
                ComplexityMode.SIMPLE -> 4
                ComplexityMode.BALANCED -> 4
                ComplexityMode.ADVANCED -> 5
            }
            val newRows = when (mode) {
                ComplexityMode.SIMPLE -> 4
                ComplexityMode.BALANCED -> 5
                ComplexityMode.ADVANCED -> 6
            }
            val newDock = when (mode) {
                ComplexityMode.SIMPLE -> 4
                ComplexityMode.BALANCED -> 5
                ComplexityMode.ADVANCED -> 6
            }
            themeDao.saveTheme(
                current.copy(
                    gridColumns = newCols,
                    gridRows = newRows,
                    dockCount = newDock,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun applyProfile(profile: ProfileType) {
        scope.launch(Dispatchers.IO) {
            backupCurrentLayout()
            activeProfile.value = profile
            activeComplexityMode.value = profile.recommendedComplexity

            val pm = context.packageManager
            val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val installedApps = pm.queryIntentActivities(launcherIntent, 0)
                .map { it.activityInfo.packageName }
                .distinct()
                .filter { it != context.packageName }

            val scoredApps = installedApps.map { pkg ->
                val lowerPkg = pkg.lowercase()
                val appLabel = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString().lowercase()
                } catch (e: Exception) {
                    ""
                }
                var score = 0
                for (kw in profile.searchKeywords) {
                    if (lowerPkg.contains(kw) || appLabel.contains(kw)) {
                        score += 10
                    }
                }
                pkg to score
            }.sortedByDescending { it.second }

            val topApps = scoredApps.map { it.first }
            val dockCount = when (profile.recommendedComplexity) {
                ComplexityMode.SIMPLE -> 4
                ComplexityMode.BALANCED -> 5
                ComplexityMode.ADVANCED -> 6
            }
            val dockPkgs = topApps.take(dockCount)
            val maxHomeApps = when (profile.recommendedComplexity) {
                ComplexityMode.SIMPLE -> 8
                ComplexityMode.BALANCED -> 12
                ComplexityMode.ADVANCED -> 16
            }
            val pageApps = topApps.drop(dockCount).take(maxHomeApps)

            layoutDao.clearAllLayouts()
            val newItems = mutableListOf<LauncherLayoutEntity>()

            // Dock apps
            dockPkgs.forEachIndexed { index, pkg ->
                newItems.add(
                    LauncherLayoutEntity(
                        id = "dock_${UUID.randomUUID()}",
                        pageIndex = -1,
                        cellX = index,
                        cellY = 0,
                        itemType = "DOCK",
                        packageName = pkg,
                        activityName = getLaunchActivity(pm, pkg) ?: ""
                    )
                )
            }

            // Page 0 apps
            val gridCols = when (profile.recommendedComplexity) {
                ComplexityMode.SIMPLE -> 4
                ComplexityMode.BALANCED -> 4
                ComplexityMode.ADVANCED -> 5
            }
            pageApps.forEachIndexed { index, pkg ->
                newItems.add(
                    LauncherLayoutEntity(
                        id = "page0_${UUID.randomUUID()}",
                        pageIndex = 0,
                        cellX = index % gridCols,
                        cellY = index / gridCols,
                        itemType = "APP",
                        packageName = pkg,
                        activityName = getLaunchActivity(pm, pkg) ?: ""
                    )
                )
            }

            layoutDao.insertItems(newItems)

            val current = currentTheme.value
            themeDao.saveTheme(
                current.copy(
                    dockCount = dockCount,
                    gridColumns = gridCols,
                    gridRows = if (profile.recommendedComplexity == ComplexityMode.ADVANCED) 6 else 5,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private suspend fun backupCurrentLayout() {
        val current = layoutDao.getAllLayoutItemsFlow().first()
        if (current.isNotEmpty()) {
            if (layoutHistory.size >= 10) {
                layoutHistory.removeAt(0)
            }
            layoutHistory.add(current)
        }
    }

    fun undoLastLayoutChange(): Boolean {
        if (layoutHistory.isEmpty()) return false
        val previous = layoutHistory.removeAt(layoutHistory.size - 1)
        scope.launch(Dispatchers.IO) {
            layoutDao.clearAllLayouts()
            layoutDao.insertItems(previous)
        }
        return true
    }

    fun resetLayoutToDefaults() {
        scope.launch(Dispatchers.IO) {
            backupCurrentLayout()
            layoutDao.clearAllLayouts()
            seedDefaultLayout()
        }
    }
}
