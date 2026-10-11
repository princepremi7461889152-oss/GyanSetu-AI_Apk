package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_logs")
data class StudyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapterId: Long = 0,
    val chapterTitle: String,
    val mode: String, // "MEMORIZING" (याद करना) or "WRITING" (लिखना)
    val minutesSpent: Int = 0,
    val photoUri: String? = null, // URI of uploaded notes photo
    val confidenceLevel: String = "HIGH", // "HIGH", "MEDIUM", "LOW"
    val noteText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
