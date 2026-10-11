package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.StudyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyLogDao {
    @Query("SELECT * FROM study_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<StudyLogEntity>>

    @Query("SELECT * FROM study_logs WHERE mode = :mode ORDER BY timestamp DESC")
    fun getLogsByMode(mode: String): Flow<List<StudyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StudyLogEntity): Long

    @Delete
    suspend fun deleteLog(log: StudyLogEntity)

    @Query("SELECT SUM(minutesSpent) FROM study_logs WHERE mode = 'MEMORIZING'")
    fun getTotalMemorizingMinutes(): Flow<Int?>

    @Query("SELECT SUM(minutesSpent) FROM study_logs WHERE mode = 'WRITING'")
    fun getTotalWritingMinutes(): Flow<Int?>
}
