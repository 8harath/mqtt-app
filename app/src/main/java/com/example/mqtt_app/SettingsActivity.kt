package com.example.mqtt_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mqtt_app.databinding.ActivitySettingsBinding
import com.example.mqtt_app.mqtt.MQTTManager
import com.example.mqtt_app.utils.PreferencesManager
import com.example.mqtt_app.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var mqttManager: MQTTManager
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeManagers()
        setupUI()
        setupObservers()
        loadCurrentSettings()
    }
    
    private fun initializeManagers() {
        preferencesManager = PreferencesManager(this)
        mqttManager = MQTTManager(this)
    }
    
    private fun setupUI() {
        setupToolbar()
        setupModeToggle()
        setupMqttConfiguration()
        setupAppPreferences()
        setupAboutSection()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }
    
    private fun setupModeToggle() {
        binding.modeToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != preferencesManager.getIsPublisherMode()) {
                showModeSwitchDialog(isChecked)
            }
        }
    }
    
    private fun setupMqttConfiguration() {
        binding.testConnectionButton.setOnClickListener {
            testMqttConnection()
        }
        
        binding.saveMqttButton.setOnClickListener {
            saveMqttConfiguration()
        }
    }
    
    private fun setupAppPreferences() {
        binding.notificationsToggle.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setNotificationsEnabled(isChecked)
        }
        
        binding.clearDataButton.setOnClickListener {
            showClearDataDialog()
        }
    }
    
    private fun setupAboutSection() {
        binding.versionText.text = getString(R.string.version)
        binding.aboutText.text = getString(R.string.academic_project)
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            preferencesManager.isPublisherMode.collect { isPublisher ->
                updateModeToggle(isPublisher)
            }
        }
        
        lifecycleScope.launch {
            preferencesManager.mqttBrokerUrl.collect { url ->
                binding.brokerUrlInput.setText(url)
            }
        }
        
        lifecycleScope.launch {
            preferencesManager.mqttBrokerPort.collect { port ->
                binding.brokerPortInput.setText(port.toString())
            }
        }
        
        lifecycleScope.launch {
            preferencesManager.notificationsEnabled.collect { enabled ->
                binding.notificationsToggle.isChecked = enabled
            }
        }
    }
    
    private fun loadCurrentSettings() {
        binding.modeToggle.isChecked = preferencesManager.getIsPublisherMode()
        binding.brokerUrlInput.setText(preferencesManager.getMqttBrokerUrl())
        binding.brokerPortInput.setText(preferencesManager.getMqttBrokerPort().toString())
        binding.notificationsToggle.isChecked = preferencesManager.getNotificationsEnabled()
    }
    
    private fun updateModeToggle(isPublisher: Boolean) {
        binding.modeToggle.isChecked = isPublisher
        binding.modeDescription.text = if (isPublisher) {
            getString(R.string.publisher_mode_description)
        } else {
            getString(R.string.subscriber_mode_description)
        }
    }
    
    private fun showModeSwitchDialog(isPublisher: Boolean) {
        AlertDialog.Builder(this)
            .setTitle("Switch Mode")
            .setMessage(getString(R.string.confirm_mode_switch))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                preferencesManager.setIsPublisherMode(isPublisher)
                restartApp()
            }
            .setNegativeButton(getString(R.string.no)) { _, _ ->
                // Revert the toggle
                binding.modeToggle.isChecked = !isPublisher
            }
            .setCancelable(false)
            .show()
    }
    
    private fun testMqttConnection() {
        val url = binding.brokerUrlInput.text.toString()
        val port = binding.brokerPortInput.text.toString().toIntOrNull() ?: 1883
        
        if (url.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_invalid_broker_url), Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.testConnectionButton.isEnabled = false
        binding.testConnectionButton.text = "Testing..."
        
        // Test connection
        mqttManager.connect(url, port)
        
        // Simulate connection test (in real app, you'd wait for actual connection result)
        lifecycleScope.launch {
            kotlinx.coroutines.delay(2000)
            val isConnected = mqttManager.isConnected()
            
            binding.testConnectionButton.isEnabled = true
            binding.testConnectionButton.text = getString(R.string.test_connection)
            
            if (isConnected) {
                Toast.makeText(this@SettingsActivity, getString(R.string.connection_successful), Toast.LENGTH_SHORT).show()
                binding.connectionStatus.text = "Connected"
                binding.connectionStatus.setTextColor(getColor(R.color.connected_green))
            } else {
                Toast.makeText(this@SettingsActivity, getString(R.string.connection_failed), Toast.LENGTH_SHORT).show()
                binding.connectionStatus.text = "Disconnected"
                binding.connectionStatus.setTextColor(getColor(R.color.disconnected_red))
            }
        }
    }
    
    private fun saveMqttConfiguration() {
        val url = binding.brokerUrlInput.text.toString()
        val port = binding.brokerPortInput.text.toString().toIntOrNull()
        
        if (url.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_invalid_broker_url), Toast.LENGTH_SHORT).show()
            return
        }
        
        if (port == null || port <= 0 || port > 65535) {
            Toast.makeText(this, getString(R.string.error_invalid_port), Toast.LENGTH_SHORT).show()
            return
        }
        
        preferencesManager.setMqttBrokerUrl(url)
        preferencesManager.setMqttBrokerPort(port)
        
        Toast.makeText(this, "MQTT configuration saved", Toast.LENGTH_SHORT).show()
    }
    
    private fun showClearDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Data")
            .setMessage(getString(R.string.confirm_clear_data))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.clearAllData()
                Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    
    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
} 