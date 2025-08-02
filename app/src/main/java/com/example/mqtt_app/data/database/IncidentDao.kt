package com.example.mqtt_app.data.database

import androidx.room.*
import com.example.mqtt_app.data.models.CrashIncident
import com.example.mqtt_app.data.models.IncidentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    
    @Query("SELECT * FROM crash_incidents ORDER BY createdAt DESC")
    fun getAllIncidents(): Flow<List<CrashIncident>>
    
    @Query("SELECT * FROM crash_incidents WHERE status = :status ORDER BY createdAt DESC")
    fun getIncidentsByStatus(status: IncidentStatus): Flow<List<CrashIncident>>
    
    @Query("SELECT * FROM crash_incidents WHERE incidentId = :incidentId")
    suspend fun getIncidentById(incidentId: String): CrashIncident?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: CrashIncident)
    
    @Update
    suspend fun updateIncident(incident: CrashIncident)
    
    @Query("UPDATE crash_incidents SET status = :status, acknowledgedAt = :acknowledgedAt, responderId = :responderId WHERE incidentId = :incidentId")
    suspend fun acknowledgeIncident(incidentId: String, status: IncidentStatus, acknowledgedAt: String, responderId: String)
    
    @Query("UPDATE crash_incidents SET status = :status, resolvedAt = :resolvedAt WHERE incidentId = :incidentId")
    suspend fun resolveIncident(incidentId: String, status: IncidentStatus, resolvedAt: String)
    
    @Query("DELETE FROM crash_incidents WHERE createdAt < :timestamp")
    suspend fun deleteOldIncidents(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM crash_incidents WHERE status = :status")
    suspend fun getIncidentCount(status: IncidentStatus): Int
    
    @Query("SELECT * FROM crash_incidents WHERE status = 'ACTIVE' ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestActiveIncident(): CrashIncident?
} 