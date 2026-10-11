package com.example.data

import com.example.data.ai.GeminiStudyService
import com.example.model.ChapterEntity
import com.example.model.ChapterSummary
import com.example.model.ExplanationLevel
import com.example.model.HardQuestionExplanation
import com.example.model.LanguageStyle
import com.example.model.QuestionMistakeAnalysis
import com.example.model.StudyFlashcard
import com.example.model.StudyLogEntity
import com.example.model.StudyQuizQuestion
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

class ChapterRepository(
    private val chapterDao: ChapterDao,
    private val studyLogDao: StudyLogDao,
    val aiService: GeminiStudyService = GeminiStudyService()
) {
    val allChapters: Flow<List<ChapterEntity>> = chapterDao.getAllChapters()
    val allStudyLogs: Flow<List<StudyLogEntity>> = studyLogDao.getAllLogs()
    val totalMemorizingMinutes: Flow<Int?> = studyLogDao.getTotalMemorizingMinutes()
    val totalWritingMinutes: Flow<Int?> = studyLogDao.getTotalWritingMinutes()

    fun setCustomApiKey(key: String) {
        aiService.customApiKey = key.trim()
    }

    suspend fun getChapterById(id: Long): ChapterEntity? = chapterDao.getChapterById(id)

    suspend fun insertChapter(chapter: ChapterEntity): Long = chapterDao.insertChapter(chapter)

    suspend fun insertBatchChapters(chapters: List<ChapterEntity>): List<Long> {
        return chapterDao.insertChapters(chapters)
    }

    suspend fun updateChapter(chapter: ChapterEntity) = chapterDao.updateChapter(chapter)

    suspend fun deleteChapter(id: Long) = chapterDao.deleteChapterById(id)

    // Study Logs & Photos
    suspend fun insertStudyLog(log: StudyLogEntity): Long = studyLogDao.insertLog(log)
    suspend fun deleteStudyLog(log: StudyLogEntity) = studyLogDao.deleteLog(log)

    // Secure Password & Lock Validation (SHA-256)
    fun hashPassword(pin: String): String {
        val trimmed = pin.trim()
        if (trimmed.isEmpty()) return ""
        val bytes = MessageDigest.getInstance("SHA-256").digest(trimmed.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(chapter: ChapterEntity, enteredPin: String): Boolean {
        if (!chapter.isLocked) return true
        val trimmed = enteredPin.trim()
        val hashed = hashPassword(trimmed)
        // Verify against hash or legacy plain string for seamless migration
        return chapter.passwordHash == hashed || chapter.passwordHash == trimmed
    }

    suspend fun setChapterLock(chapterId: Long, isLocked: Boolean, pin: String, hint: String) {
        val chapter = chapterDao.getChapterById(chapterId) ?: return
        val updated = chapter.copy(
            isLocked = isLocked,
            passwordHash = if (isLocked) hashPassword(pin) else "",
            passwordHint = if (isLocked) hint.trim() else ""
        )
        chapterDao.updateChapter(updated)
    }

    // AI Operations with Persistent Caching to prevent resource misuse

    suspend fun getOrGenerateSummary(
        chapter: ChapterEntity,
        languageStyle: LanguageStyle,
        forceRefresh: Boolean = false
    ): ChapterSummary {
        // RESOURCE PROTECTION: Check if cached summary already exists!
        if (!forceRefresh && chapter.summaryJson.isNotBlank()) {
            try {
                val cached = parseCachedSummary(chapter.summaryJson)
                if (cached != null) return cached
            } catch (_: Exception) {}
        }

        // Generate via AI
        val summary = aiService.summarizeNotes(chapter.title, chapter.content, languageStyle)
        
        // Serialize and persist to SQLite so we never call AI again for this chapter unnecessarily
        val json = JSONObject().apply {
            put("overview", summary.overview)
            put("keyPoints", JSONArray(summary.keyPoints))
            put("importantFormulasOrDefinitions", JSONArray(summary.importantFormulasOrDefinitions))
            put("examWarningOrTrap", summary.examWarningOrTrap)
        }.toString()

        val updated = chapter.copy(summaryJson = json)
        chapterDao.updateChapter(updated)
        return summary
    }

    private fun parseCachedSummary(jsonStr: String): ChapterSummary? {
        return try {
            val obj = JSONObject(jsonStr)
            val overview = obj.optString("overview", "")
            val keyPointsArr = obj.optJSONArray("keyPoints")
            val keyPoints = mutableListOf<String>()
            if (keyPointsArr != null) {
                for (i in 0 until keyPointsArr.length()) {
                    keyPoints.add(keyPointsArr.getString(i))
                }
            }
            val formulasArr = obj.optJSONArray("importantFormulasOrDefinitions")
            val formulas = mutableListOf<String>()
            if (formulasArr != null) {
                for (i in 0 until formulasArr.length()) {
                    formulas.add(formulasArr.getString(i))
                }
            }
            val trap = obj.optString("examWarningOrTrap", "")
            if (overview.isNotBlank()) {
                ChapterSummary(
                    overview = overview,
                    keyPoints = if (keyPoints.isNotEmpty()) keyPoints else listOf("अध्याय के मुख्य बिंदुओं को ध्यान से पढ़ें."),
                    importantFormulasOrDefinitions = formulas,
                    examWarningOrTrap = trap
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun explainHardQuestion(
        question: String,
        chapterContext: String,
        level: ExplanationLevel,
        languageStyle: LanguageStyle
    ): HardQuestionExplanation {
        return aiService.explainHardQuestion(question, chapterContext, level, languageStyle)
    }

    suspend fun getOrGenerateFlashcards(
        chapter: ChapterEntity,
        forceRefresh: Boolean = false
    ): List<StudyFlashcard> {
        if (!forceRefresh && chapter.flashcardsJson.isNotBlank()) {
            try {
                val list = mutableListOf<StudyFlashcard>()
                val arr = JSONArray(chapter.flashcardsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        StudyFlashcard(
                            id = obj.optInt("id", i + 1),
                            question = obj.optString("question", ""),
                            answer = obj.optString("answer", ""),
                            memoryTrick = obj.optString("memoryTrick", "")
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }

        val flashcards = aiService.generateFlashcards(chapter.title, chapter.content)
        val jsonArray = JSONArray()
        for (fc in flashcards) {
            val obj = JSONObject().apply {
                put("id", fc.id)
                put("question", fc.question)
                put("answer", fc.answer)
                put("memoryTrick", fc.memoryTrick)
            }
            jsonArray.put(obj)
        }
        val updated = chapter.copy(flashcardsJson = jsonArray.toString())
        chapterDao.updateChapter(updated)
        return flashcards
    }

    suspend fun getOrGenerateQuiz(
        chapter: ChapterEntity,
        forceRefresh: Boolean = false
    ): List<StudyQuizQuestion> {
        if (!forceRefresh && chapter.quizJson.isNotBlank()) {
            try {
                val list = mutableListOf<StudyQuizQuestion>()
                val arr = JSONArray(chapter.quizJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val options = mutableListOf<String>()
                    val optArr = obj.optJSONArray("options")
                    if (optArr != null) {
                        for (j in 0 until optArr.length()) {
                            options.add(optArr.getString(j))
                        }
                    }
                    list.add(
                        StudyQuizQuestion(
                            id = obj.optInt("id", i + 1),
                            question = obj.optString("question", ""),
                            options = options,
                            correctIndex = obj.optInt("correctIndex", 0),
                            explanation = obj.optString("explanation", "")
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }

        val quiz = aiService.generateQuiz(chapter.title, chapter.content)
        val jsonArray = JSONArray()
        for (q in quiz) {
            val obj = JSONObject().apply {
                put("id", q.id)
                put("question", q.question)
                put("options", JSONArray(q.options))
                put("correctIndex", q.correctIndex)
                put("explanation", q.explanation)
            }
            jsonArray.put(obj)
        }
        val updated = chapter.copy(quizJson = jsonArray.toString())
        chapterDao.updateChapter(updated)
        return quiz
    }

    suspend fun askDoubt(
        userDoubt: String,
        chapterTitle: String,
        notesContent: String,
        languageStyle: LanguageStyle
    ): String {
        return aiService.askDoubt(userDoubt, chapterTitle, notesContent, languageStyle)
    }

    suspend fun analyzeQuestionMistake(
        question: String,
        studentAttempt: String,
        chapterTitle: String,
        notesContent: String,
        languageStyle: LanguageStyle
    ): QuestionMistakeAnalysis {
        return aiService.analyzeQuestionMistake(question, studentAttempt, chapterTitle, notesContent, languageStyle)
    }
}
