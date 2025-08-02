package com.example.mqtt_app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mqtt_app.data.database.AppDatabase
import com.example.mqtt_app.data.models.CrashIncident
import com.example.mqtt_app.data.models.IncidentStatus
import com.example.mqtt_app.utils.DataSimulator
import com.example.mqtt_app.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val incidentDao = database.incidentDao()
    private val preferencesManager = PreferencesManager(application)
    private val dataSimulator = DataSimulator()
    
    private val _activeIncidents = MutableStateFlow<List<CrashIncident>>(emptyList())
    val activeIncidents: StateFlow<List<CrashIncident>> = _activeIncidents
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        loadActiveIncidents()
        performDataCleanup()
    }
    
    fun generateEmergencyIncident(): CrashIncident {
        return dataSimulator.generateRandomIncident()
    }
    
    fun generateIncidentAtLocation(latitude: Double, longitude: Double): CrashIncident {
        return dataSimulator.generateIncidentAtLocation(latitude, longitude)
    }
    
    fun saveIncident(incident: CrashIncident) {
        viewModelScope.launch {
            try {
                incidentDao.insertIncident(incident)
                loadActiveIncidents()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun acknowledgeIncident(incidentId: String, responderId: String) {
        viewModelScope.launch {
            try {
                val acknowledgedAt = Instant.now().toString()
                incidentDao.acknowledgeIncident(
                    incidentId,
                    IncidentStatus.ACKNOWLEDGED,
                    acknowledgedAt,
                    responderId
                )
                loadActiveIncidents()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun resolveIncident(incidentId: String) {
        viewModelScope.launch {
            try {
                val resolvedAt = Instant.now().toString()
                incidentDao.resolveIncident(
                    incidentId,
                    IncidentStatus.RESOLVED,
                    resolvedAt
                )
                loadActiveIncidents()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun loadActiveIncidents() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                incidentDao.getIncidentsByStatus(IncidentStatus.ACTIVE).collect { incidents ->
                    _activeIncidents.value = incidents
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun performDataCleanup() {
        viewModelScope.launch {
            try {
                if (preferencesManager.shouldPerformCleanup()) {
                    val retentionDays = preferencesManager.getDataRetentionDays()
                    val cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
                    
                    incidentDao.deleteOldIncidents(cutoffTime)
                    preferencesManager.setLastCleanupTime(System.currentTimeMillis())
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun getIncidentCount(status: IncidentStatus): Int {
        var count = 0
        viewModelScope.launch {
            try {
                count = incidentDao.getIncidentCount(status)
            } catch (e: Exception) {
                // Handle error
            }
        }
        return count
    }
    
    fun getLatestActiveIncident(): CrashIncident? {
        var incident: CrashIncident? = null
        viewModelScope.launch {
            try {
                incident = incidentDao.getLatestActiveIncident()
            } catch (e: Exception) {
                // Handle error
            }
        }
        return incident
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            try {
                // Clear database
                incidentDao.deleteOldIncidents(0L) // Delete all incidents
                
                // Clear preferences
                preferencesManager.clearAllData()
                
                // Reset state
                _activeIncidents.value = emptyList()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun isPublisherMode(): Boolean {
        return preferencesManager.getIsPublisherMode()
    }
    
    fun setPublisherMode(isPublisher: Boolean) {
        preferencesManager.setIsPublisherMode(isPublisher)
    }
    
    fun getMqttBrokerUrl(): String {
        return preferencesManager.getMqttBrokerUrl()
    }
    
    fun setMqttBrokerUrl(url: String) {
        preferencesManager.setMqttBrokerUrl(url)
    }
    
    fun getMqttBrokerPort(): Int {
        return preferencesManager.getMqttBrokerPort()
    }
    
    fun setMqttBrokerPort(port: Int) {
        preferencesManager.setMqttBrokerPort(port)
    }
    
    fun getNotificationsEnabled(): Boolean {
        return preferencesManager.getNotificationsEnabled()
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        preferencesManager.setNotificationsEnabled(enabled)
    }
} 