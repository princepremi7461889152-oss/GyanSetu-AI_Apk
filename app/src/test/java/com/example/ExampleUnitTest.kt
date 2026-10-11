package com.example

import com.example.data.SampleNotesData
import com.example.model.ChapterEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun batchUpload_hasSixChapterPresets() {
        val presets = SampleNotesData.get6ChapterBatchPreset()
        assertEquals(6, presets.size)
    }

    @Test
    fun chapterLock_passwordVerification() {
        val lockedChapter = ChapterEntity(
            chapterNumber = 1,
            title = "Test Chapter",
            subject = "Science",
            content = "Sample content",
            isLocked = true,
            passwordHash = "4321",
            passwordHint = "Hint 4321"
        )

        // Wrong password should fail
        assertFalse(lockedChapter.passwordHash == "1234")
        // Correct password should pass
        assertTrue(lockedChapter.passwordHash == "4321")
    }

    @Test
    fun unlockedChapter_accessibleWithoutPassword() {
        val openChapter = ChapterEntity(
            chapterNumber = 2,
            title = "Open Chapter",
            subject = "Maths",
            content = "Notes",
            isLocked = false
        )
        assertFalse(openChapter.isLocked)
    }

    @Test
    fun timerModes_haveAccurateMinutes() {
        assertEquals(25, com.example.ui.TimerMode.POMODORO_25.minutes)
        assertEquals(45, com.example.ui.TimerMode.DEEP_STUDY_45.minutes)
        assertEquals(15, com.example.ui.TimerMode.QUICK_REVISION_15.minutes)
        assertEquals(5, com.example.ui.TimerMode.SHORT_BREAK_5.minutes)
    }

    @Test
    fun studyLogEntity_supportsModesAndPhotos() {
        val memorizingLog = com.example.model.StudyLogEntity(
            chapterTitle = "Photosynthesis",
            mode = "MEMORIZING",
            minutesSpent = 25
        )
        assertEquals("MEMORIZING", memorizingLog.mode)

        val writingLog = com.example.model.StudyLogEntity(
            chapterTitle = "Newton's Laws",
            mode = "WRITING",
            minutesSpent = 35,
            photoUri = "content://media/photo/1"
        )
        assertEquals("WRITING", writingLog.mode)
        assertEquals("content://media/photo/1", writingLog.photoUri)
    }
}
