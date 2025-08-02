package com.example.mqtt_app.utils

import com.example.mqtt_app.data.models.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class DataSimulator {
    
    companion object {
        // Bangalore city bounds
        private const val MIN_LAT = 12.8
        private const val MAX_LAT = 13.1
        private const val MIN_LON = 77.4
        private const val MAX_LON = 77.8
        
        // Common allergies
        private val COMMON_ALLERGIES = listOf(
            "Peanuts", "Shellfish", "Dairy", "Eggs", "Wheat", "Soy",
            "Tree nuts", "Fish", "Latex", "Dust", "Pollen", "Mold"
        )
        
        // Common medical conditions
        private val MEDICAL_CONDITIONS = mapOf(
            "18-30" to listOf("Asthma", "Allergies", "Migraine"),
            "31-50" to listOf("Hypertension", "Diabetes", "Asthma", "Allergies", "Migraine"),
            "51-70" to listOf("Hypertension", "Diabetes", "Heart disease", "Arthritis", "Asthma"),
            "71+" to listOf("Hypertension", "Diabetes", "Heart disease", "Arthritis", "Dementia")
        )
        
        // Common medications
        private val MEDICATIONS = listOf(
            "Aspirin", "Ibuprofen", "Paracetamol", "Insulin", "Metformin",
            "Amlodipine", "Lisinopril", "Atorvastatin", "Omeprazole", "Albuterol"
        )
        
        // Vehicle types with weights
        private val VEHICLE_TYPES = mapOf(
            "car" to 70,
            "motorcycle" to 15,
            "truck" to 10,
            "bus" to 5
        )
        
        // Blood group distribution (realistic population distribution)
        private val BLOOD_GROUP_DISTRIBUTION = mapOf(
            BloodGroup.O_POSITIVE to 35,
            BloodGroup.A_POSITIVE to 30,
            BloodGroup.B_POSITIVE to 8,
            BloodGroup.AB_POSITIVE to 2,
            BloodGroup.O_NEGATIVE to 7,
            BloodGroup.A_NEGATIVE to 6,
            BloodGroup.B_NEGATIVE to 1,
            BloodGroup.AB_NEGATIVE to 1
        )
    }
    
    fun generateRandomLocation(): Location {
        val latitude = Random.nextDouble(MIN_LAT, MAX_LAT)
        val longitude = Random.nextDouble(MIN_LON, MAX_LON)
        return Location(latitude, longitude)
    }
    
    fun generateRandomAge(): Int {
        // Normal distribution around mean 35 with std dev 15
        val mean = 35.0
        val stdDev = 15.0
        val age = (Random.nextGaussian() * stdDev + mean).toInt()
        return age.coerceIn(18, 99)
    }
    
    fun generateRandomBloodGroup(): BloodGroup {
        val random = Random.nextInt(100)
        var cumulative = 0
        for ((bloodGroup, percentage) in BLOOD_GROUP_DISTRIBUTION) {
            cumulative += percentage
            if (random < cumulative) {
                return bloodGroup
            }
        }
        return BloodGroup.O_POSITIVE // Default fallback
    }
    
    fun generateRandomAllergies(): List<String> {
        val numAllergies = Random.nextInt(0, 4) // 0-3 allergies
        return COMMON_ALLERGIES.shuffled().take(numAllergies)
    }
    
    fun generateRandomMedicalConditions(age: Int): List<String> {
        val ageGroup = when {
            age <= 30 -> "18-30"
            age <= 50 -> "31-50"
            age <= 70 -> "51-70"
            else -> "71+"
        }
        
        val conditions = MEDICAL_CONDITIONS[ageGroup] ?: emptyList()
        val numConditions = Random.nextInt(0, 3) // 0-2 conditions
        return conditions.shuffled().take(numConditions)
    }
    
    fun generateRandomMedications(): List<String> {
        val numMedications = Random.nextInt(0, 3) // 0-2 medications
        return MEDICATIONS.shuffled().take(numMedications)
    }
    
    fun generateRandomVehicleType(): String {
        val random = Random.nextInt(100)
        var cumulative = 0
        for ((vehicleType, percentage) in VEHICLE_TYPES) {
            cumulative += percentage
            if (random < cumulative) {
                return vehicleType
            }
        }
        return "car" // Default fallback
    }
    
    fun generateRandomEmergencyContact(): EmergencyContact {
        val names = listOf(
            "John Doe", "Jane Smith", "Mike Johnson", "Sarah Wilson",
            "David Brown", "Lisa Davis", "Robert Miller", "Emily Garcia"
        )
        val relationships = listOf(
            "Spouse", "Parent", "Sibling", "Friend", "Colleague"
        )
        
        return EmergencyContact(
            name = names.random(),
            phone = generateRandomPhone(),
            relationship = relationships.random()
        )
    }
    
    private fun generateRandomPhone(): String {
        val prefixes = listOf("+91-987", "+91-988", "+91-989", "+91-986", "+91-985")
        val prefix = prefixes.random()
        val suffix = String.format("%06d", Random.nextInt(100000, 999999))
        return "$prefix$suffix"
    }
    
    fun generateRandomIncident(): CrashIncident {
        val age = generateRandomAge()
        val location = generateRandomLocation()
        val timestamp = Instant.now().toString()
        
        return CrashIncident(
            timestamp = timestamp,
            location = location,
            victim = VictimInfo(
                age = age,
                bloodGroup = generateRandomBloodGroup(),
                allergies = generateRandomAllergies(),
                medicalConditions = generateRandomMedicalConditions(age),
                emergencyContact = generateRandomEmergencyContact(),
                medications = generateRandomMedications()
            ),
            vehicleType = generateRandomVehicleType()
        )
    }
    
    fun generateIncidentAtLocation(latitude: Double, longitude: Double): CrashIncident {
        val age = generateRandomAge()
        val timestamp = Instant.now().toString()
        
        return CrashIncident(
            timestamp = timestamp,
            location = Location(latitude, longitude),
            victim = VictimInfo(
                age = age,
                bloodGroup = generateRandomBloodGroup(),
                allergies = generateRandomAllergies(),
                medicalConditions = generateRandomMedicalConditions(age),
                emergencyContact = generateRandomEmergencyContact(),
                medications = generateRandomMedications()
            ),
            vehicleType = generateRandomVehicleType()
        )
    }
    
    fun generateMultipleIncidents(count: Int): List<CrashIncident> {
        return List(count) { generateRandomIncident() }
    }
} 