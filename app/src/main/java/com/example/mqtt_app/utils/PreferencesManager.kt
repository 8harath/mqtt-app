package com.example.mqtt_app.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "crash_detection_prefs",
        Context.MODE_PRIVATE
    )
    
    private val _isPublisherMode = MutableStateFlow(getIsPublisherMode())
    val isPublisherMode: StateFlow<Boolean> = _isPublisherMode
    
    private val _mqttBrokerUrl = MutableStateFlow(getMqttBrokerUrl())
    val mqttBrokerUrl: StateFlow<String> = _mqttBrokerUrl
    
    private val _mqttBrokerPort = MutableStateFlow(getMqttBrokerPort())
    val mqttBrokerPort: StateFlow<Int> = _mqttBrokerPort
    
    private val _notificationsEnabled = MutableStateFlow(getNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled
    
    companion object {
        private const val KEY_PUBLISHER_MODE = "publisher_mode"
        private const val KEY_MQTT_BROKER_URL = "mqtt_broker_url"
        private const val KEY_MQTT_BROKER_PORT = "mqtt_broker_port"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DATA_RETENTION_DAYS = "data_retention_days"
        private const val KEY_LAST_CLEANUP = "last_cleanup"
        
        private const val DEFAULT_BROKER_URL = "192.168.1.100"
        private const val DEFAULT_BROKER_PORT = 1883
        private const val DEFAULT_RETENTION_DAYS = 7
    }
    
    fun setIsPublisherMode(isPublisher: Boolean) {
        prefs.edit().putBoolean(KEY_PUBLISHER_MODE, isPublisher).apply()
        _isPublisherMode.value = isPublisher
    }
    
    fun getIsPublisherMode(): Boolean {
        return prefs.getBoolean(KEY_PUBLISHER_MODE, true) // Default to publisher mode
    }
    
    fun setMqttBrokerUrl(url: String) {
        prefs.edit().putString(KEY_MQTT_BROKER_URL, url).apply()
        _mqttBrokerUrl.value = url
    }
    
    fun getMqttBrokerUrl(): String {
        return prefs.getString(KEY_MQTT_BROKER_URL, DEFAULT_BROKER_URL) ?: DEFAULT_BROKER_URL
    }
    
    fun setMqttBrokerPort(port: Int) {
        prefs.edit().putInt(KEY_MQTT_BROKER_PORT, port).apply()
        _mqttBrokerPort.value = port
    }
    
    fun getMqttBrokerPort(): Int {
        return prefs.getInt(KEY_MQTT_BROKER_PORT, DEFAULT_BROKER_PORT)
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }
    
    fun getNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
    
    fun getDataRetentionDays(): Int {
        return prefs.getInt(KEY_DATA_RETENTION_DAYS, DEFAULT_RETENTION_DAYS)
    }
    
    fun setDataRetentionDays(days: Int) {
        prefs.edit().putInt(KEY_DATA_RETENTION_DAYS, days).apply()
    }
    
    fun getLastCleanupTime(): Long {
        return prefs.getLong(KEY_LAST_CLEANUP, 0L)
    }
    
    fun setLastCleanupTime(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_CLEANUP, timestamp).apply()
    }
    
    fun clearAllData() {
        prefs.edit().clear().apply()
        // Reset state flows to defaults
        _isPublisherMode.value = true
        _mqttBrokerUrl.value = DEFAULT_BROKER_URL
        _mqttBrokerPort.value = DEFAULT_BROKER_PORT
        _notificationsEnabled.value = true
    }
    
    fun shouldPerformCleanup(): Boolean {
        val lastCleanup = getLastCleanupTime()
        val currentTime = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000L
        
        return (currentTime - lastCleanup) >= oneDayInMillis
    }
} 