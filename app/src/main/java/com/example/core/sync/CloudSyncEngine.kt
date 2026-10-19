package com.example.core.sync

import android.content.Context
import android.util.Log
import com.example.auth.ShivIdentityAuth
import com.example.data.local.dao.*
import com.example.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val message: String, val timestamp: Long = System.currentTimeMillis()) : SyncState()
    data class Error(val error: String) : SyncState()
}

class CloudSyncEngine(
    private val context: Context,
    private val syncQueueDao: SyncQueueDao,
    private val layoutDao: LauncherLayoutDao,
    private val themeDao: LauncherThemeDao,
    private val familyDao: FamilyPolicyDao,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "CloudSyncEngine"
        private const val BASE_URL = "https://shiv-database.onrender.com"
        private const val PROJECT_ID = "7d810bd0-f625-4856-a154-4e04aca955fc"
        private const val API_KEY = "shiv_sk_7VpCyHo_X0_Q74V2oX3UJgCQ28DASaccElh5NbJ8amU"
        private const val MAX_RETRIES = 5
    }

    private val httpClient = OkHttpClient()
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    fun enqueueChange(collection: String, recordId: String, action: String, data: JSONObject) {
        scope.launch(Dispatchers.IO) {
            val item = SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                collection = collection,
                recordId = recordId,
                action = action,
                payloadJson = data.toString(),
                retryCount = 0,
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
            syncQueueDao.enqueueItem(item)
            triggerSync()
        }
    }

    fun triggerSync() {
        scope.launch(Dispatchers.IO) {
            val token = ShivIdentityAuth.getAccessToken(context)
            if (token == null && API_KEY.isEmpty()) {
                _syncState.value = SyncState.Idle
                return@launch
            }

            _syncState.value = SyncState.Syncing

            val pending = syncQueueDao.getPendingItems()
            if (pending.isEmpty()) {
                // If queue is empty, backup current snapshots to cloud
                backupCurrentSnapshotsToCloud()
                _syncState.value = SyncState.Success("All configurations synced with SHIV Cloud")
                return@launch
            }

            var failCount = 0
            for (item in pending) {
                if (item.retryCount >= MAX_RETRIES) {
                    continue
                }

                val success = sendItemToBackend(item, token)
                if (success) {
                    syncQueueDao.removeItem(item.id)
                } else {
                    failCount++
                    syncQueueDao.updateItem(
                        item.copy(
                            retryCount = item.retryCount + 1,
                            lastAttemptAt = System.currentTimeMillis(),
                            status = if (item.retryCount + 1 >= MAX_RETRIES) "FAILED" else "PENDING"
                        )
                    )
                }
            }

            if (failCount == 0) {
                _syncState.value = SyncState.Success("Cloud sync completed successfully")
            } else {
                _syncState.value = SyncState.Error("Sync completed with $failCount errors (will retry)")
            }
        }
    }

    private suspend fun backupCurrentSnapshotsToCloud() = withContext(Dispatchers.IO) {
        try {
            val token = ShivIdentityAuth.getAccessToken(context)
            val theme = themeDao.getTheme()
            if (theme != null) {
                val themePayload = JSONObject().apply {
                    put("id", "theme_snapshot")
                    put("themePreset", theme.themePreset)
                    put("iconShape", theme.iconShape)
                    put("iconScalePercent", theme.iconScalePercent)
                    put("gridRows", theme.gridRows)
                    put("gridColumns", theme.gridColumns)
                    put("dockCount", theme.dockCount)
                    put("updatedAt", theme.updatedAt)
                }
                uploadRecord("launcher_themes", "theme_snapshot", themePayload, token)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Snapshot backup error: ${e.message}")
        }
    }

    private fun sendItemToBackend(item: SyncQueueEntity, token: String?): Boolean {
        return try {
            val payload = JSONObject(item.payloadJson)
            uploadRecord(item.collection, item.recordId, payload, token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload queue item: ${e.message}")
            false
        }
    }

    private fun uploadRecord(collection: String, recordId: String, data: JSONObject, token: String?): Boolean {
        val url = "$BASE_URL/data/$collection"
        val body = JSONObject().apply {
            put("id", recordId)
            put("data", data)
        }.toString().toRequestBody("application/json".toMediaType())

        val reqBuilder = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("x-api-key", API_KEY)
            .addHeader("x-project-id", PROJECT_ID)

        if (token != null) {
            reqBuilder.addHeader("Authorization", "Bearer $token")
        }

        return try {
            val resp = httpClient.newCall(reqBuilder.build()).execute()
            val code = resp.code
            resp.close()
            code in 200..299 || code == 307
        } catch (e: Exception) {
            false
        }
    }

    // Export complete launcher backup JSON for migration
    suspend fun createFullBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 2)
        root.put("createdAt", System.currentTimeMillis())

        // Themes
        val theme = themeDao.getTheme()
        if (theme != null) {
            val themeObj = JSONObject().apply {
                put("themePreset", theme.themePreset)
                put("iconShape", theme.iconShape)
                put("iconScalePercent", theme.iconScalePercent)
                put("gridRows", theme.gridRows)
                put("gridColumns", theme.gridColumns)
                put("dockCount", theme.dockCount)
            }
            root.put("theme", themeObj)
        }

        // Layouts
        val layouts = layoutDao.getAllLayoutItemsFlow().first()
        val layoutsArr = JSONArray()
        for (l in layouts) {
            val itemObj = JSONObject().apply {
                put("id", l.id)
                put("pageIndex", l.pageIndex)
                put("cellX", l.cellX)
                put("cellY", l.cellY)
                put("itemType", l.itemType)
                put("packageName", l.packageName)
            }
            layoutsArr.put(itemObj)
        }
        root.put("layouts", layoutsArr)

        root.toString(2)
    }

    // Restore complete launcher backup with validation
    suspend fun restoreFromBackupJson(jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)
            val version = root.optInt("version", 1)
            if (version < 1) return@withContext false

            if (root.has("theme")) {
                val themeObj = root.getJSONObject("theme")
                val current = themeDao.getTheme() ?: com.example.data.local.entity.LauncherThemeEntity()
                themeDao.saveTheme(
                    current.copy(
                        themePreset = themeObj.optString("themePreset", "COSMIC_SHIVA"),
                        iconShape = themeObj.optString("iconShape", "SQUIRCLE"),
                        iconScalePercent = themeObj.optInt("iconScalePercent", 100).coerceIn(80, 120),
                        gridRows = themeObj.optInt("gridRows", 5).coerceIn(4, 7),
                        gridColumns = themeObj.optInt("gridColumns", 4).coerceIn(4, 6),
                        dockCount = themeObj.optInt("dockCount", 5).coerceIn(3, 6),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            if (root.has("layouts")) {
                val layoutsArr = root.getJSONArray("layouts")
                val entities = mutableListOf<com.example.data.local.entity.LauncherLayoutEntity>()
                for (i in 0 until layoutsArr.length()) {
                    val obj = layoutsArr.getJSONObject(i)
                    entities.add(
                        com.example.data.local.entity.LauncherLayoutEntity(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            pageIndex = obj.optInt("pageIndex", 0),
                            cellX = obj.optInt("cellX", 0),
                            cellY = obj.optInt("cellY", 0),
                            itemType = obj.optString("itemType", "APP"),
                            packageName = obj.optString("packageName", "")
                        )
                    )
                }
                if (entities.isNotEmpty()) {
                    layoutDao.clearAllLayouts()
                    layoutDao.insertItems(entities)
                }
            }

            com.example.core.launcher.AppIconManager.clearCache()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Restore error: ${e.message}")
            false
        }
    }
}
