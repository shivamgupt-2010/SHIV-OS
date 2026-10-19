package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.LauncherThemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherThemeDao {
    @Query("SELECT * FROM launcher_themes WHERE id = 'current_theme' LIMIT 1")
    fun getThemeFlow(): Flow<LauncherThemeEntity?>

    @Query("SELECT * FROM launcher_themes WHERE id = 'current_theme' LIMIT 1")
    suspend fun getTheme(): LauncherThemeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTheme(theme: LauncherThemeEntity)

    @Query("UPDATE launcher_themes SET isLayoutLocked = :locked WHERE id = 'current_theme'")
    suspend fun setLayoutLocked(locked: Boolean)
}
