package com.example.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.NoteDao
import com.example.data.local.entity.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isChecked: Boolean = false
)

data class DoodlePath(
    val colorHex: String,
    val strokeWidth: Float,
    val points: List<Pair<Float, Float>>
)

class NotesViewModel(
    private val noteDao: NoteDao
) : ViewModel() {

    val activeNotes: StateFlow<List<NoteEntity>> = noteDao.getActiveNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashedNotes: StateFlow<List<NoteEntity>> = noteDao.getTrashedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed initial welcome note if active notes are empty
        viewModelScope.launch(Dispatchers.IO) {
            val initial = noteDao.getActiveNotes().first()
            if (initial.isEmpty()) {
                val welcomeChecklist = JSONArray().apply {
                    put(JSONObject().put("id", "c1").put("text", "Explore SHIV Launcher custom gestures").put("isChecked", true))
                    put(JSONObject().put("id", "c2").put("text", "Try drawing a sketch on the Doodle canvas").put("isChecked", false))
                    put(JSONObject().put("id", "c3").put("text", "Attach photos from gallery").put("isChecked", false))
                    put(JSONObject().put("id", "c4").put("text", "Test SHIV Sentinel App Lock").put("isChecked", true))
                }.toString()

                noteDao.saveNote(
                    NoteEntity(
                        id = "welcome_note_1",
                        title = "Welcome to SHIV Notes & Doodles",
                        content = "Your private, on-device workspace. Supports checklists, rich media attachments, category tags, interactive sketch doodle canvas, and autosaving.",
                        category = "Personal",
                        colorHex = "#0EA5E9",
                        isPinned = true,
                        checklistJson = welcomeChecklist,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.saveNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun togglePin(id: String, isPinned: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.togglePin(id, !isPinned)
        }
    }

    fun moveToTrash(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.moveToTrash(id)
        }
    }

    fun restoreFromTrash(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.restoreFromTrash(id)
        }
    }

    fun deletePermanently(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.deletePermanently(id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.emptyTrash()
        }
    }

    // --- JSON Utility Helpers ---
    fun parseChecklist(jsonStr: String): List<ChecklistItem> {
        return try {
            val arr = JSONArray(jsonStr)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                ChecklistItem(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    text = obj.optString("text", ""),
                    isChecked = obj.optBoolean("isChecked", false)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeChecklist(items: List<ChecklistItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("text", item.text)
                    put("isChecked", item.isChecked)
                }
            )
        }
        return arr.toString()
    }

    fun parseImageUris(jsonStr: String): List<String> {
        return try {
            val arr = JSONArray(jsonStr)
            List(arr.length()) { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeImageUris(uris: List<String>): String {
        return JSONArray(uris).toString()
    }

    fun parseDoodlePaths(jsonStr: String): List<DoodlePath> {
        return try {
            val arr = JSONArray(jsonStr)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val colorHex = obj.optString("colorHex", "#00F5FF")
                val strokeWidth = obj.optDouble("strokeWidth", 6.0).toFloat()
                val ptsArr = obj.optJSONArray("points") ?: JSONArray()
                val pts = List(ptsArr.length()) { p ->
                    val ptObj = ptsArr.getJSONObject(p)
                    Pair(ptObj.getDouble("x").toFloat(), ptObj.getDouble("y").toFloat())
                }
                DoodlePath(colorHex, strokeWidth, pts)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeDoodlePaths(paths: List<DoodlePath>): String {
        val arr = JSONArray()
        paths.forEach { path ->
            val obj = JSONObject()
            obj.put("colorHex", path.colorHex)
            obj.put("strokeWidth", path.strokeWidth.toDouble())
            val ptsArr = JSONArray()
            path.points.forEach { (x, y) ->
                ptsArr.put(JSONObject().put("x", x.toDouble()).put("y", y.toDouble()))
            }
            obj.put("points", ptsArr)
            arr.put(obj)
        }
        return arr.toString()
    }
}
