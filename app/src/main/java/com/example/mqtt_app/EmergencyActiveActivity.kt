package com.example.mqtt_app

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mqtt_app.databinding.ActivityEmergencyActiveBinding
import com.example.mqtt_app.mqtt.MQTTManager
import com.example.mqtt_app.utils.PreferencesManager
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class EmergencyActiveActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityEmergencyActiveBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var mqttManager: MQTTManager
    private var countDownTimer: CountDownTimer? = null
    private var incidentId: String? = null
    
    companion object {
        private const val TIMER_DURATION = 5 * 60 * 1000L // 5 minutes in milliseconds
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyActiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get incident ID from intent
        incidentId = intent.getStringExtra("incident_id")
        
        initializeManagers()
        setupUI()
        startEmergencyTimer()
        setupAnimations()
    }
    
    private fun initializeManagers() {
        preferencesManager = PreferencesManager(this)
        mqttManager = MQTTManager(this)
    }
    
    private fun setupUI() {
        setupImOkButton()
        setupCancelButton()
        setupStatusIndicators()
    }
    
    private fun setupImOkButton() {
        binding.imOkButton.setOnClickListener {
            showImOkConfirmation()
        }
    }
    
    private fun setupCancelButton() {
        binding.cancelButton.setOnClickListener {
            showCancelConfirmation()
        }
    }
    
    private fun setupStatusIndicators() {
        // Animate status indicators
        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        binding.locationIndicator.startAnimation(pulseAnimation)
        binding.messageIndicator.startAnimation(pulseAnimation)
    }
    
    private fun setupAnimations() {
        // Fade in animation for the entire screen
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.root.startAnimation(fadeIn)
        
        // Scale animation for the checkmark
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_animation)
        binding.checkmarkImage.startAnimation(scaleAnimation)
    }
    
    private fun startEmergencyTimer() {
        countDownTimer = object : CountDownTimer(TIMER_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                binding.timerText.text = String.format("%02d:%02d", minutes, seconds)
            }
            
            override fun onFinish() {
                // Auto-confirm emergency if timer reaches zero
                binding.timerText.text = "00:00"
                binding.timerText.setTextColor(getColor(R.color.emergency_red))
                showAutoConfirmDialog()
            }
        }.start()
    }
    
    private fun showImOkConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Confirm I'm OK")
            .setMessage("Are you sure you're safe? This will cancel the emergency alert.")
            .setPositiveButton("Yes, I'm OK") { _, _ ->
                cancelEmergency()
            }
            .setNegativeButton("No, I need help") { _, _ ->
                // Continue with emergency
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showCancelConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Emergency Alert")
            .setMessage(getString(R.string.confirm_cancel_alert))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                cancelEmergency()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    
    private fun showAutoConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Emergency Confirmed")
            .setMessage("No response received. Emergency services have been automatically notified.")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun cancelEmergency() {
        // Publish cancellation message
        incidentId?.let { id ->
            mqttManager.publishStatusUpdate(id, "cancelled")
        }
        
        // Show success message
        Toast.makeText(this, "Emergency alert cancelled", Toast.LENGTH_SHORT).show()
        
        // Stop timer
        countDownTimer?.cancel()
        
        // Return to main activity
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
    
    override fun onBackPressed() {
        // Prevent back button from closing emergency screen
        showCancelConfirmation()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
} 