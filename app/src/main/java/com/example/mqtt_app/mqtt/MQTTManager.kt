package com.example.mqtt_app.mqtt

import android.content.Context
import android.util.Log
import com.example.mqtt_app.data.models.CrashIncident
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*

class MQTTManager(private val context: Context) {
    
    private var mqttClient: MqttAndroidClient? = null
    private val gson = Gson()
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus
    
    private val _receivedIncidents = MutableStateFlow<List<CrashIncident>>(emptyList())
    val receivedIncidents: StateFlow<List<CrashIncident>> = _receivedIncidents
    
    private val incidents = mutableListOf<CrashIncident>()
    
    fun connect(brokerUrl: String, port: Int, clientId: String = "AndroidClient_${System.currentTimeMillis()}") {
        try {
            val serverUri = "tcp://$brokerUrl:$port"
            mqttClient = MqttAndroidClient(context, serverUri, clientId)
            
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                isAutomaticReconnect = true
                connectionTimeout = 30
                keepAliveInterval = 60
            }
            
            mqttClient?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "MQTT Connected successfully")
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    subscribeToTopics()
                }
                
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "MQTT Connection failed", exception)
                    _connectionStatus.value = ConnectionStatus.FAILED
                }
            })
            
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to MQTT broker", e)
            _connectionStatus.value = ConnectionStatus.FAILED
        }
    }
    
    fun disconnect() {
        try {
            mqttClient?.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "MQTT Disconnected successfully")
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
                
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "MQTT Disconnect failed", exception)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from MQTT broker", e)
        }
    }
    
    private fun subscribeToTopics() {
        val topics = arrayOf(
            "crash/alerts/+",
            "crash/status/+",
            "crash/responses/+"
        )
        
        val qos = IntArray(topics.size) { 1 }
        
        mqttClient?.subscribe(topics, qos, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d(TAG, "Subscribed to topics successfully")
                setupMessageCallback()
            }
            
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e(TAG, "Failed to subscribe to topics", exception)
            }
        })
    }
    
    private fun setupMessageCallback() {
        mqttClient?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.d(TAG, "Connection complete, reconnect: $reconnect")
            }
            
            override fun connectionLost(cause: Throwable?) {
                Log.w(TAG, "Connection lost", cause)
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
            
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "Message arrived on topic: $topic")
                message?.let { handleIncomingMessage(topic, it) }
            }
            
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d(TAG, "Message delivery complete")
            }
        })
    }
    
    private fun handleIncomingMessage(topic: String?, message: MqttMessage) {
        try {
            val payload = String(message.payload)
            Log.d(TAG, "Received message: $payload on topic: $topic")
            
            when {
                topic?.startsWith("crash/alerts/") == true -> {
                    val incident = gson.fromJson(payload, CrashIncident::class.java)
                    addIncident(incident)
                }
                topic?.startsWith("crash/status/") == true -> {
                    // Handle status updates
                    Log.d(TAG, "Status update received: $payload")
                }
                topic?.startsWith("crash/responses/") == true -> {
                    // Handle response confirmations
                    Log.d(TAG, "Response received: $payload")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing incoming message", e)
        }
    }
    
    private fun addIncident(incident: CrashIncident) {
        incidents.add(incident)
        _receivedIncidents.value = incidents.toList()
    }
    
    fun publishEmergencyAlert(incident: CrashIncident) {
        val topic = "crash/alerts/region"
        val message = gson.toJson(incident)
        
        publishMessage(topic, message, 1)
    }
    
    fun publishStatusUpdate(incidentId: String, status: String, responderId: String? = null) {
        val topic = "crash/status/$incidentId"
        val statusMessage = mapOf(
            "incident_id" to incidentId,
            "status" to status,
            "responder_id" to responderId,
            "timestamp" to System.currentTimeMillis()
        )
        val message = gson.toJson(statusMessage)
        
        publishMessage(topic, message, 1)
    }
    
    fun publishResponse(incidentId: String, response: String) {
        val topic = "crash/responses/$incidentId"
        val responseMessage = mapOf(
            "incident_id" to incidentId,
            "response" to response,
            "timestamp" to System.currentTimeMillis()
        )
        val message = gson.toJson(responseMessage)
        
        publishMessage(topic, message, 1)
    }
    
    private fun publishMessage(topic: String, message: String, qos: Int) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray()).apply {
                this.qos = qos
                isRetained = false
            }
            
            mqttClient?.publish(topic, mqttMessage, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Message published successfully to topic: $topic")
                }
                
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Failed to publish message to topic: $topic", exception)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing message", e)
        }
    }
    
    fun isConnected(): Boolean {
        return mqttClient?.isConnected == true
    }
    
    enum class ConnectionStatus {
        CONNECTED,
        DISCONNECTED,
        FAILED,
        CONNECTING
    }
    
    companion object {
        private const val TAG = "MQTTManager"
    }
} 