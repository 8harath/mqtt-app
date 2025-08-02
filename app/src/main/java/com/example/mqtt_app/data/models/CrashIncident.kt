package com.example.mqtt_app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID

@Entity(tableName = "crash_incidents")
data class CrashIncident(
    @PrimaryKey
    val incidentId: String = UUID.randomUUID().toString(),
    
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("location")
    val location: Location,
    
    @SerializedName("victim")
    val victim: VictimInfo,
    
    @SerializedName("vehicle_type")
    val vehicleType: String,
    
    @SerializedName("status")
    val status: IncidentStatus = IncidentStatus.ACTIVE,
    
    val responderId: String? = null,
    val acknowledgedAt: String? = null,
    val resolvedAt: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Location(
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)

data class VictimInfo(
    @SerializedName("age")
    val age: Int,
    
    @SerializedName("blood_group")
    val bloodGroup: BloodGroup,
    
    @SerializedName("allergies")
    val allergies: List<String> = emptyList(),
    
    @SerializedName("medical_conditions")
    val medicalConditions: List<String> = emptyList(),
    
    @SerializedName("emergency_contact")
    val emergencyContact: EmergencyContact,
    
    @SerializedName("medications")
    val medications: List<String> = emptyList()
)

data class EmergencyContact(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("relationship")
    val relationship: String
)

enum class BloodGroup {
    @SerializedName("A+")
    A_POSITIVE,
    
    @SerializedName("A-")
    A_NEGATIVE,
    
    @SerializedName("B+")
    B_POSITIVE,
    
    @SerializedName("B-")
    B_NEGATIVE,
    
    @SerializedName("AB+")
    AB_POSITIVE,
    
    @SerializedName("AB-")
    AB_NEGATIVE,
    
    @SerializedName("O+")
    O_POSITIVE,
    
    @SerializedName("O-")
    O_NEGATIVE
}

enum class IncidentStatus {
    @SerializedName("active")
    ACTIVE,
    
    @SerializedName("acknowledged")
    ACKNOWLEDGED,
    
    @SerializedName("resolved")
    RESOLVED
}

enum class VehicleType {
    CAR,
    MOTORCYCLE,
    TRUCK,
    BUS
} 