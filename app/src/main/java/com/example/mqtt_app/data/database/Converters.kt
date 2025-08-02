package com.example.mqtt_app.data.database

import androidx.room.TypeConverter
import com.example.mqtt_app.data.models.BloodGroup
import com.example.mqtt_app.data.models.IncidentStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromBloodGroup(bloodGroup: BloodGroup): String {
        return bloodGroup.name
    }
    
    @TypeConverter
    fun toBloodGroup(value: String): BloodGroup {
        return BloodGroup.valueOf(value)
    }
    
    @TypeConverter
    fun fromIncidentStatus(status: IncidentStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toIncidentStatus(value: String): IncidentStatus {
        return IncidentStatus.valueOf(value)
    }
    
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
} 