package com.example.data.local

import androidx.room.*
import com.example.data.model.Worker
import com.example.data.model.LabourLog
import kotlinx.coroutines.flow.Flow

@Dao
interface LabourDao {
    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveWorkers(): Flow<List<Worker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker)

    @Update
    suspend fun updateWorker(worker: Worker)

    @Query("SELECT * FROM labour_logs ORDER BY date DESC, id DESC")
    fun getAllLabourLogs(): Flow<List<LabourLog>>

    @Query("SELECT * FROM labour_logs WHERE date = :date")
    fun getLabourLogsForDate(date: String): Flow<List<LabourLog>>

    @Query("SELECT * FROM labour_logs WHERE synced = 0")
    suspend fun getUnsyncedLabourLogs(): List<LabourLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabourLog(log: LabourLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabourLogs(logs: List<LabourLog>)

    @Query("UPDATE labour_logs SET synced = 1 WHERE id IN (:ids)")
    suspend fun markLabourLogsAsSynced(ids: List<Int>)

    @Query("DELETE FROM labour_logs WHERE id = :id")
    suspend fun deleteLabourLog(id: Int)
}
