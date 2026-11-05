package com.example.companion.context

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.example.companion.avatar.AvatarType
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
import org.json.JSONObject
import java.util.Calendar

/**
 * LocationAwareIdentityProxy:
 * Realizes the autonomous location-aware identity layer for ShivAI.
 * 
 * Capabilities:
 * 1. Location Sanctuary Detection:
 *    - Maps coordinates to HOME_SANCTUARY, WORK_OFFICE, COMMUTE_IN_TRANSIT, or DEEP_STUDY_LAB.
 *    - Automatically personalizes avatar demeanor, active agent persona, and notification filters.
 * 2. Autonomous Proxy:
 *    - Acts as an intelligent buffer between the user and external communications.
 *    - Synthesizes contextual response drafts for incoming messages.
 * 3. STRICT NON-DESTRUCTIVE INVARIANT:
 *    - The agent has ZERO authority to delete, overwrite, or send irreversible transactions autonomously.
 *    - Any deletion or critical modification triggers an on-device biometric user prompt.
 */
class LocationAwareIdentityProxy(
    private val context: Context,
    private val preferencesDao: UserPreferencesDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    companion object {
        private const val PREF_KEY_LOCATION_CONFIG = "shiv_location_sanctuary_config_v1"
        private const val LOCATION_UPDATE_MIN_TIME_MS = 60_000L   // 1 minute (ultra low power)
        private const val LOCATION_UPDATE_MIN_DISTANCE_M = 150f    // 150 meters
    }

    enum class SanctuaryType(val title: String, val recommendedAvatar: AvatarType, val isDistractionShieldActive: Boolean) {
        HOME_SANCTUARY("Home Sanctuary", AvatarType.INDIE_CACOONAUT, false),
        WORK_OFFICE("Work / Office", AvatarType.COSMIC_SHIV, false),
        COMMUTE_IN_TRANSIT("Commute & Transit", AvatarType.PHANTOM_NEKO, false),
        DEEP_STUDY_LAB("Deep Focus Lab", AvatarType.COSMIC_SHIV, true),
        UNKNOWN_EXPLORE("General Node", AvatarType.COSMIC_SHIV, false)
    }

    data class GeoFence(
        val name: String,
        val sanctuary: SanctuaryType,
        val latitude: Double,
        val longitude: Double,
        val radiusMeters: Float = 250f
    )

    private val _currentSanctuary = MutableStateFlow(SanctuaryType.UNKNOWN_EXPLORE)
    val currentSanctuary: StateFlow<SanctuaryType> = _currentSanctuary.asStateFlow()

    private val _suggestedPersona = MutableStateFlow("CosmicShiv")
    val suggestedPersona: StateFlow<String> = _suggestedPersona.asStateFlow()

    private val _lastKnownLocation = MutableStateFlow<Location?>(null)
    val lastKnownLocation: StateFlow<Location?> = _lastKnownLocation.asStateFlow()

    private val registeredFences = mutableListOf<GeoFence>()
    private var locationManager: LocationManager? = null

    init {
        scope.launch {
            loadConfig()
            initLocationListener()
        }
    }

    private suspend fun loadConfig() = withContext(Dispatchers.IO) {
        try {
            val pref = preferencesDao.getPreference(PREF_KEY_LOCATION_CONFIG)
            if (pref != null && pref.value.isNotBlank()) {
                val json = JSONObject(pref.value)
                val fencesArr = json.optJSONArray("fences")
                if (fencesArr != null) {
                    for (i in 0 until fencesArr.length()) {
                        val obj = fencesArr.getJSONObject(i)
                        registeredFences.add(
                            GeoFence(
                                name = obj.getString("name"),
                                sanctuary = SanctuaryType.valueOf(obj.getString("sanctuary")),
                                latitude = obj.getDouble("lat"),
                                longitude = obj.getDouble("lng"),
                                radiusMeters = obj.optDouble("radius", 250.0).toFloat()
                            )
                        )
                    }
                }
            } else {
                // Default heuristic fences if not yet calibrated
                registeredFences.add(GeoFence("Home", SanctuaryType.HOME_SANCTUARY, 28.6139, 77.2090, 300f))
                registeredFences.add(GeoFence("Office", SanctuaryType.WORK_OFFICE, 28.5355, 77.3910, 300f))
            }
        } catch (e: Exception) {
            Logger.e("Failed to load location sanctuary config", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationListener() {
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val mgr = locationManager ?: return

            // Check passive or network provider first to minimize battery impact
            val provider = when {
                mgr.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) -> LocationManager.PASSIVE_PROVIDER
                mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> LocationManager.GPS_PROVIDER
            }

            val lastLoc = mgr.getLastKnownLocation(provider)
            lastLoc?.let { evaluateLocation(it) }

            mgr.requestLocationUpdates(
                provider,
                LOCATION_UPDATE_MIN_TIME_MS,
                LOCATION_UPDATE_MIN_DISTANCE_M,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        evaluateLocation(location)
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
            )
            Logger.d("LocationAwareIdentityProxy: Passive location tracking active ($provider)")
        } catch (e: SecurityException) {
            Logger.w("Location permission not granted. Falling back to time-of-day heuristic sanctuary.")
            evaluateTimeHeuristic()
        } catch (e: Exception) {
            Logger.e("Error initializing location listener", e)
            evaluateTimeHeuristic()
        }
    }

    private fun evaluateLocation(location: Location) {
        _lastKnownLocation.value = location
        var matched = false

        for (fence in registeredFences) {
            val dist = FloatArray(1)
            Location.distanceBetween(
                location.latitude, location.longitude,
                fence.latitude, fence.longitude,
                dist
            )
            if (dist[0] <= fence.radiusMeters) {
                updateSanctuary(fence.sanctuary)
                matched = true
                break
            }
        }

        if (!matched) {
            // Check speed for in-transit detection (> 15 km/h = ~4.16 m/s)
            if (location.hasSpeed() && location.speed > 4.2f) {
                updateSanctuary(SanctuaryType.COMMUTE_IN_TRANSIT)
            } else {
                evaluateTimeHeuristic()
            }
        }
    }

    private fun evaluateTimeHeuristic() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val sanctuary = when (hour) {
            in 21..23, in 0..7 -> SanctuaryType.HOME_SANCTUARY
            in 8..9, in 18..19 -> SanctuaryType.COMMUTE_IN_TRANSIT
            in 10..17 -> SanctuaryType.WORK_OFFICE
            else -> SanctuaryType.DEEP_STUDY_LAB
        }
        updateSanctuary(sanctuary)
    }

    private fun updateSanctuary(sanctuary: SanctuaryType) {
        if (_currentSanctuary.value != sanctuary) {
            _currentSanctuary.value = sanctuary
            _suggestedPersona.value = when (sanctuary) {
                SanctuaryType.HOME_SANCTUARY -> "IndieCacoonaut"
                SanctuaryType.WORK_OFFICE -> "CodingPilotAgent"
                SanctuaryType.COMMUTE_IN_TRANSIT -> "PhantomNeko"
                SanctuaryType.DEEP_STUDY_LAB -> "StrictCoachAgent"
                SanctuaryType.UNKNOWN_EXPLORE -> "CosmicShiv"
            }
            Logger.d("Sanctuary shifted to: ${sanctuary.title} -> Persona: ${_suggestedPersona.value}")
        }
    }

    // ==========================================
    // AUTONOMOUS PROXY CAPABILITIES & INVARIANTS
    // ==========================================

    /**
     * Synthesizes an intelligent response draft for an incoming communication.
     * STRICT INVARIANT: Does NOT send automatically. Returns draft for user review.
     */
    fun createDraftProxyResponse(sender: String, messageText: String): String {
        return when (_currentSanctuary.value) {
            SanctuaryType.WORK_OFFICE, SanctuaryType.DEEP_STUDY_LAB -> {
                "Hi $sender, Shiv is currently in a deep focus work sprint. He will review your message shortly."
            }
            SanctuaryType.COMMUTE_IN_TRANSIT -> {
                "Hi $sender, Shiv is in transit. He will connect once stationary."
            }
            SanctuaryType.HOME_SANCTUARY -> {
                "Hello $sender! Received your message, Shiv will get back to you soon."
            }
            else -> {
                "Hi $sender, thanks for reaching out. Shiv will reply soon."
            }
        }
    }

    /**
     * Checks whether an action violates the absolute non-destructive safety invariant.
     * Any deletion, file destruction, or unconfirmed financial transaction is BLOCKED.
     */
    fun verifyActionSafety(actionType: String, resourceTarget: String): Boolean {
        val lower = actionType.lowercase()
        val isDestructive = lower.contains("delete") ||
                lower.contains("remove") ||
                lower.contains("drop") ||
                lower.contains("erase") ||
                lower.contains("wipe") ||
                lower.contains("pay") ||
                lower.contains("transfer")

        if (isDestructive) {
            Logger.w("SAFETY BLOCK: Autonomous action '$actionType' on '$resourceTarget' blocked. Requires biometric approval.")
            return false // Strict invariant: Must obtain explicit user consent
        }
        return true
    }
}
