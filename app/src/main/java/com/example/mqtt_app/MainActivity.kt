package com.example.mqtt_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mqtt_app.databinding.ActivityMainBinding
import com.example.mqtt_app.mqtt.MQTTManager
import com.example.mqtt_app.utils.PreferencesManager
import com.example.mqtt_app.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var mqttManager: MQTTManager
    private val viewModel: MainViewModel by viewModels()
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
        private const val NOTIFICATION_PERMISSION_REQUEST = 1002
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeManagers()
        setupUI()
        setupObservers()
        checkPermissions()
        connectToMQTT()
    }
    
    private fun initializeManagers() {
        preferencesManager = PreferencesManager(this)
        mqttManager = MQTTManager(this)
    }
    
    private fun setupUI() {
        // Apply Apple-quality design
        setupAnimations()
        setupEmergencyButton()
        setupSettingsButton()
        setupModeIndicator()
    }
    
    private fun setupAnimations() {
        // Logo fade-in animation
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.logoImage.startAnimation(fadeIn)
        
        // Mode indicator slide animation
        val slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        binding.modeIndicator.startAnimation(slideIn)
    }
    
    private fun setupEmergencyButton() {
        binding.emergencyButton.setOnClickListener {
            if (preferencesManager.getIsPublisherMode()) {
                triggerEmergency()
            } else {
                // In subscriber mode, show map
                startIncidentMap()
            }
        }
        
        // Pulsing animation for emergency button
        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        binding.emergencyButton.startAnimation(pulseAnimation)
    }
    
    private fun setupSettingsButton() {
        binding.settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        
        // Rotation animation on tap
        binding.settingsButton.setOnClickListener {
            val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation)
            binding.settingsButton.startAnimation(rotateAnimation)
            
            // Start settings activity after animation
            rotateAnimation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(intent)
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
        }
    }
    
    private fun setupModeIndicator() {
        updateModeIndicator()
    }
    
    private fun updateModeIndicator() {
        val isPublisher = preferencesManager.getIsPublisherMode()
        binding.modeIndicator.text = if (isPublisher) "Publisher Mode" else "Subscriber Mode"
        binding.modeIndicator.setTextColor(
            ContextCompat.getColor(
                this,
                if (isPublisher) R.color.publisher_green else R.color.subscriber_blue
            )
        )
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            preferencesManager.isPublisherMode.collect { isPublisher ->
                updateModeIndicator()
                updateEmergencyButton()
            }
        }
        
        lifecycleScope.launch {
            mqttManager.connectionStatus.collect { status ->
                updateConnectionStatus(status)
            }
        }
    }
    
    private fun updateEmergencyButton() {
        val isPublisher = preferencesManager.getIsPublisherMode()
        if (isPublisher) {
            binding.emergencyButton.text = "EMERGENCY ALERT"
            binding.emergencyButton.setBackgroundResource(R.drawable.emergency_button_background)
        } else {
            binding.emergencyButton.text = "VIEW INCIDENTS"
            binding.emergencyButton.setBackgroundResource(R.drawable.subscriber_button_background)
        }
    }
    
    private fun updateConnectionStatus(status: MQTTManager.ConnectionStatus) {
        val statusColor = when (status) {
            MQTTManager.ConnectionStatus.CONNECTED -> R.color.connected_green
            MQTTManager.ConnectionStatus.DISCONNECTED -> R.color.disconnected_red
            MQTTManager.ConnectionStatus.FAILED -> R.color.failed_red
            MQTTManager.ConnectionStatus.CONNECTING -> R.color.connecting_yellow
        }
        
        binding.connectionStatus.setColorFilter(
            ContextCompat.getColor(this, statusColor)
        )
    }
    
    private fun checkPermissions() {
        // Check location permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
        
        // Check notification permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST
                )
            }
        }
    }
    
    private fun connectToMQTT() {
        val brokerUrl = preferencesManager.getMqttBrokerUrl()
        val brokerPort = preferencesManager.getMqttBrokerPort()
        mqttManager.connect(brokerUrl, brokerPort)
    }
    
    private fun triggerEmergency() {
        // Check if MQTT is connected
        if (!mqttManager.isConnected()) {
            Toast.makeText(this, "Not connected to MQTT broker", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Generate emergency incident
        val incident = viewModel.generateEmergencyIncident()
        
        // Publish to MQTT
        mqttManager.publishEmergencyAlert(incident)
        
        // Start emergency active activity
        val intent = Intent(this, EmergencyActiveActivity::class.java).apply {
            putExtra("incident_id", incident.incidentId)
        }
        startActivity(intent)
    }
    
    private fun startIncidentMap() {
        val intent = Intent(this, IncidentMapActivity::class.java)
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        updateModeIndicator()
        updateEmergencyButton()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted
                } else {
                    Toast.makeText(this, "Location permission required for emergency alerts", Toast.LENGTH_LONG).show()
                }
            }
            NOTIFICATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Notification permission granted
                } else {
                    Toast.makeText(this, "Notification permission recommended for alerts", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
} 