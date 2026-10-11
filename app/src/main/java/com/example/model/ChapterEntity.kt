package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapterNumber: Int, // 1 through 6 or higher
    val title: String,
    val subject: String,
    val content: String,
    val isLocked: Boolean = false,
    val passwordHash: String = "", // Stored PIN or password
    val passwordHint: String = "",
    val summaryJson: String = "", // Cached AI summary
    val flashcardsJson: String = "", // Cached flashcards
    val quizJson: String = "", // Cached quiz questions
    val attachedFileName: String = "", // e.g. "Physics_Complete_Notes.pdf"
    val attachedFileUri: String = "", // Content or file URI
    val attachedFileSize: Long = 0L, // File size in bytes (up to 2GB supported)
    val attachedFileType: String = "", // MIME type or extension
    val createdAt: Long = System.currentTimeMillis(),
    val lastStudiedAt: Long = System.currentTimeMillis()
)
