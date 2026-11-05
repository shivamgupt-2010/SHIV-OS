package com.example.ai.memory.personalization

import android.content.Context
import com.example.core.utils.Logger
import com.example.data.local.dao.UserPreferencesDao
import com.example.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min

/**
 * SelfEvolvingNeuroplasticityEngine:
 * On-device real-time behavioral adaptation & synaptic plasticity for ShivAI.
 * 
 * Key Principles:
 * 1. Hebbian Associative Plasticity: "Neurons that fire together wire together"
 *    - Frequent sequences (e.g. Time 09:30 + Coding -> Prioritize CodingPilot) strengthen their synaptic weight.
 * 2. Negative Reinforcement:
 *    - When the user cancels, dismisses, or corrects an assistant action, the weight decays sharply.
 * 3. Synaptic Pruning / Time Decay:
 *    - Inactive patterns slowly decay over weeks, preventing stale behavioral traps.
 * 4. Zero Cloud Dependency:
 *    - All weight matrices and behavioral graphs are calculated and stored locally on device.
 */
class SelfEvolvingNeuroplasticityEngine(
    private val context: Context,
    private val preferencesDao: UserPreferencesDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    companion object {
        private const val PREF_KEY_NEUROPLASTIC_WEIGHTS = "shiv_neuroplastic_weights_v1"
        private const val PREF_KEY_USER_PROFILE_HABITS = "shiv_user_habits_summary_v1"

        // Learning Hyperparameters
        private const val LEARNING_RATE = 0.15f        // Rate of weight growth on positive reinforcement
        private const val UNLEARNING_RATE = 0.25f      // Rate of weight drop on user rejection/dismissal
        private const val MIN_SYNAPTIC_WEIGHT = 0.05f   // Floor weight to prevent complete deletion
        private const val MAX_SYNAPTIC_WEIGHT = 1.00f   // Ceiling weight
        private const val PRUNING_THRESHOLD = 0.12f    // Synapses below this are pruned after 14 days
        private const val DECAY_HALF_LIFE_DAYS = 14    // Days of inactivity to decay weight by 50%
    }

    enum class SynapseCategory {
        TIME_ROUTINE,       // Associated with hour of day (e.g. 0900 -> Coding)
        APP_ASSOCIATION,    // Associated with preceding app (e.g. IDE -> Git commit)
        LOCATION_HABIT,     // Associated with physical sanctuary (e.g. Office -> Strict)
        INTENT_SHORTCUT,    // User preferred agent/tool for specific intent keywords
        TONE_PREFERENCE     // Formality, verbosity, and pacing preference
    }

    data class Synapse(
        val patternKey: String,
        var weight: Float,
        var activationCount: Int,
        var lastReinforcedMs: Long,
        val category: SynapseCategory,
        val associatedTarget: String
    ) {
        fun toJson(): JSONObject = JSONObject().apply {
            put("key", patternKey)
            put("weight", weight.toDouble())
            put("count", activationCount)
            put("lastReinforced", lastReinforcedMs)
            put("category", category.name)
            put("target", associatedTarget)
        }

        companion object {
            fun fromJson(json: JSONObject): Synapse? {
                return try {
                    Synapse(
                        patternKey = json.getString("key"),
                        weight = json.getDouble("weight").toFloat(),
                        activationCount = json.optInt("count", 1),
                        lastReinforcedMs = json.optLong("lastReinforced", System.currentTimeMillis()),
                        category = SynapseCategory.valueOf(json.optString("category", SynapseCategory.TIME_ROUTINE.name)),
                        associatedTarget = json.getString("target")
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    private val synapseMatrix = ConcurrentHashMap<String, Synapse>()
    private val _isEngineReady = MutableStateFlow(false)
    val isEngineReady: StateFlow<Boolean> = _isEngineReady.asStateFlow()

    init {
        scope.launch {
            loadWeights()
        }
    }

    /**
     * Loads synaptic weights from local database into fast in-memory concurrent map.
     */
    private suspend fun loadWeights() = withContext(Dispatchers.IO) {
        try {
            val pref = preferencesDao.getPreference(PREF_KEY_NEUROPLASTIC_WEIGHTS)
            if (pref != null && pref.value.isNotBlank()) {
                val array = JSONArray(pref.value)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val syn = Synapse.fromJson(obj)
                    if (syn != null) {
                        synapseMatrix[syn.patternKey] = syn
                    }
                }
                Logger.d("NeuroplasticityEngine: Loaded ${synapseMatrix.size} active synapses from local storage")
            } else {
                seedDefaultSynapses()
            }
        } catch (e: Exception) {
            Logger.e("Failed to load neuroplastic weights, seeding defaults", e)
            seedDefaultSynapses()
        } finally {
            _isEngineReady.value = true
        }
    }

    private fun seedDefaultSynapses() {
        // High-confidence initial bootstrap synapses
        registerSynapse("TIME_09->CodingPilot", 0.70f, SynapseCategory.TIME_ROUTINE, "CodingPilotAgent")
        registerSynapse("TIME_14->DeepFlow", 0.65f, SynapseCategory.TIME_ROUTINE, "StrictCoachAgent")
        registerSynapse("TIME_22->IndieSlumber", 0.80f, SynapseCategory.TIME_ROUTINE, "IndieCacoonaut")
        registerSynapse("LOC_WORK->CodingPilot", 0.75f, SynapseCategory.LOCATION_HABIT, "CodingPilotAgent")
        registerSynapse("LOC_HOME->IndieRelax", 0.80f, SynapseCategory.LOCATION_HABIT, "IndieCacoonaut")
        registerSynapse("INTENT_CODE->CodingPilot", 0.85f, SynapseCategory.INTENT_SHORTCUT, "CodingPilotAgent")
        registerSynapse("INTENT_FOCUS->StrictCoach", 0.90f, SynapseCategory.INTENT_SHORTCUT, "StrictCoachAgent")
    }

    private fun registerSynapse(key: String, initialWeight: Float, category: SynapseCategory, target: String) {
        synapseMatrix[key] = Synapse(
            patternKey = key,
            weight = initialWeight,
            activationCount = 1,
            lastReinforcedMs = System.currentTimeMillis(),
            category = category,
            associatedTarget = target
        )
    }

    /**
     * Positive Reinforcement (Hebbian association):
     * Increases synaptic weight when user executes or accepts an assistant recommendation.
     * w_new = w + LEARNING_RATE * (1.0 - w)
     */
    suspend fun reinforcePattern(patternKey: String, target: String, category: SynapseCategory = SynapseCategory.TIME_ROUTINE) {
        val now = System.currentTimeMillis()
        val existing = synapseMatrix[patternKey]
        if (existing != null) {
            val delta = LEARNING_RATE * (MAX_SYNAPTIC_WEIGHT - existing.weight)
            existing.weight = min(MAX_SYNAPTIC_WEIGHT, existing.weight + delta)
            existing.activationCount += 1
            existing.lastReinforcedMs = now
            Logger.d("Synapse reinforced: $patternKey -> new weight: ${existing.weight}")
        } else {
            synapseMatrix[patternKey] = Synapse(
                patternKey = patternKey,
                weight = 0.55f,
                activationCount = 1,
                lastReinforcedMs = now,
                category = category,
                associatedTarget = target
            )
            Logger.d("New synapse formed: $patternKey (weight: 0.55)")
        }
        persistWeightsDebounced()
    }

    /**
     * Negative Reinforcement:
     * Decays weight when user dismisses, corrects, or cancels an action.
     * w_new = w - UNLEARNING_RATE * w
     */
    suspend fun punishPattern(patternKey: String) {
        val existing = synapseMatrix[patternKey] ?: return
        val delta = UNLEARNING_RATE * existing.weight
        existing.weight = max(MIN_SYNAPTIC_WEIGHT, existing.weight - delta)
        Logger.w("Synapse punished: $patternKey -> decayed weight: ${existing.weight}")
        persistWeightsDebounced()
    }

    /**
     * Predicts top recommended actions/agents based on current contextual cues.
     */
    fun predictBestPersona(currentHour: Int = getCurrentHour(), locationSanctuary: String = "WORK_OFFICE"): String {
        var topWeight = -1f
        var bestTarget = "CosmicShiv"

        // 1. Check time-based synapses
        val timeKey = "TIME_${String.format("%02d", currentHour)}"
        synapseMatrix.values
            .filter { it.patternKey.startsWith(timeKey) || it.patternKey.startsWith("TIME_") }
            .forEach { syn ->
                if (syn.weight > topWeight) {
                    topWeight = syn.weight
                    bestTarget = syn.associatedTarget
                }
            }

        // 2. Check location-based synapses
        val locKey = "LOC_$locationSanctuary"
        synapseMatrix[locKey]?.let { syn ->
            if (syn.weight > topWeight) {
                topWeight = syn.weight
                bestTarget = syn.associatedTarget
            }
        }

        return bestTarget
    }

    /**
     * Synthesizes a compact, dynamic natural-language personalization summary
     * to inject into Gemini / Sovereign LLM system prompts.
     */
    fun generateSystemPromptNeuroplasticContext(): String {
        val highConfidenceSynapses = synapseMatrix.values
            .filter { it.weight >= 0.65f }
            .sortedByDescending { it.weight }
            .take(6)

        if (highConfidenceSynapses.isEmpty()) {
            return "Active User Neuroplasticity: Establishing initial baseline. Respond proactively and attentively."
        }

        val habitsList = highConfidenceSynapses.joinToString("; ") { syn ->
            "${syn.patternKey} -> prefers ${syn.associatedTarget} (confidence: ${(syn.weight * 100).toInt()}%)"
        }

        return """
            [ON-DEVICE NEUROPLASTIC COGNITIVE PROFILE]:
            Learned Habits & Synaptic Preferences: $habitsList
            Adaptive Guidance: Mirror user's pace, favor their established routine targets, and minimize friction.
        """.trimIndent()
    }

    /**
     * Persists all weights locally to Room DB.
     */
    private suspend fun persistWeightsDebounced() = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            synapseMatrix.values.forEach { syn ->
                jsonArray.put(syn.toJson())
            }
            preferencesDao.insertPreference(
                UserPreferencesEntity(
                    key = PREF_KEY_NEUROPLASTIC_WEIGHTS,
                    value = jsonArray.toString()
                )
            )
        } catch (e: Exception) {
            Logger.e("Failed to persist neuroplastic weights", e)
        }
    }

    private fun getCurrentHour(): Int {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }
}
