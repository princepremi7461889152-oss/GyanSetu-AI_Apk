package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChapterRepository
import com.example.data.SampleNotesData
import com.example.data.ai.AiQuotaManager
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChapterDraft(
    val chapterNumber: Int,
    val title: String,
    val subject: String,
    val content: String,
    val isLocked: Boolean = false,
    val password: String = "",
    val passwordHint: String = ""
)

enum class TimerMode(val title: String, val minutes: Int) {
    POMODORO_25("🍅 पोमोडोरो (25m)", 25),
    DEEP_STUDY_45("⏱️ डीप स्टडी (45m)", 45),
    QUICK_REVISION_15("⚡ क्विक रिवीजन (15m)", 15),
    SHORT_BREAK_5("☕ शॉर्ट ब्रेक (5m)", 5)
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("gyansetu_prefs", Context.MODE_PRIVATE)
    val aiQuotaManager = AiQuotaManager(application)
    private val repository: ChapterRepository = run {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        val aiService = GeminiStudyService(aiQuotaManager)
        val savedKey = prefs.getString("custom_api_key", "") ?: ""
        if (savedKey.isNotBlank()) {
            aiService.customApiKey = savedKey
        }
        ChapterRepository(db.chapterDao(), db.studyLogDao(), aiService)
    }

    // AI Quota state flows for resource tracking & transparency
    val dailyAiUsage: StateFlow<Int> = aiQuotaManager.dailyUsageCount
    val maxAiQuota: StateFlow<Int> = aiQuotaManager.maxQuota

    // Onboarding (API Key & Permissions)
    val hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean("has_completed_onboarding", false))
    val customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val isApiKeySettingsOpen = MutableStateFlow(false)

    // Theme Mode Toggle (null = system default, true = Dark, false = Light)
    val isDarkTheme = MutableStateFlow<Boolean?>(null)

    // Search and filter state
    val searchQuery = MutableStateFlow("")
    val selectedSubjectFilter = MutableStateFlow<String?>(null)

    // Unlocked chapters in current session
    private val _unlockedChapterIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedChapterIds: StateFlow<Set<Long>> = _unlockedChapterIds.asStateFlow()

    // All chapters with privacy-safe filter (prevents search leakage of locked chapters)
    val chapters: StateFlow<List<ChapterEntity>> = combine(
        repository.allChapters,
        searchQuery,
        selectedSubjectFilter,
        _unlockedChapterIds
    ) { chapterList, query, filter, unlockedIds ->
        chapterList.filter { chapter ->
            val isUnlocked = !chapter.isLocked || unlockedIds.contains(chapter.id)
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val titleMatch = chapter.title.contains(query, ignoreCase = true)
                val subjectMatch = chapter.subject.contains(query, ignoreCase = true)
                // PRIVACY LOOPHOLE FIX: Only search content if note is already unlocked!
                val contentMatch = if (isUnlocked) chapter.content.contains(query, ignoreCase = true) else false
                titleMatch || subjectMatch || contentMatch
            }
            val matchesSubject = filter == null || chapter.subject.equals(filter, ignoreCase = true)
            matchesQuery && matchesSubject
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Study Logs and Photos Flow
    val allStudyLogs: StateFlow<List<StudyLogEntity>> = repository.allStudyLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMemorizingMinutes: StateFlow<Int?> = repository.totalMemorizingMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalWritingMinutes: StateFlow<Int?> = repository.totalWritingMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Currently selected chapter for viewing / study
    private val _selectedChapter = MutableStateFlow<ChapterEntity?>(null)
    val selectedChapter: StateFlow<ChapterEntity?> = _selectedChapter.asStateFlow()

    // Unlock Dialog State
    private val _pendingUnlockChapter = MutableStateFlow<ChapterEntity?>(null)
    val pendingUnlockChapter: StateFlow<ChapterEntity?> = _pendingUnlockChapter.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    // Batch Upload Modal State (Supports up to 6 chapters)
    private val _isBatchUploadOpen = MutableStateFlow(false)
    val isBatchUploadOpen: StateFlow<Boolean> = _isBatchUploadOpen.asStateFlow()

    private val _batchDrafts = MutableStateFlow<List<ChapterDraft>>(createInitialBatchDrafts())
    val batchDrafts: StateFlow<List<ChapterDraft>> = _batchDrafts.asStateFlow()

    private val _activeBatchTabIndex = MutableStateFlow(0)
    val activeBatchTabIndex: StateFlow<Int> = _activeBatchTabIndex.asStateFlow()

    // Edit Chapter Modal State
    private val _chapterToEdit = MutableStateFlow<ChapterEntity?>(null)
    val chapterToEdit: StateFlow<ChapterEntity?> = _chapterToEdit.asStateFlow()

    // Study Detail Screen State
    val selectedLanguage = MutableStateFlow(LanguageStyle.HINGLISH)
    val selectedExplanationLevel = MutableStateFlow(ExplanationLevel.REAL_WORLD)
    val studyTab = MutableStateFlow(0) // 0: Notes & Summary, 1: Tough Question Solver, 2: Flashcards, 3: Quiz, 4: Ask AI

    // AI Generated Data States
    private val _currentSummary = MutableStateFlow<ChapterSummary?>(null)
    val currentSummary: StateFlow<ChapterSummary?> = _currentSummary.asStateFlow()
    val isSummaryLoading = MutableStateFlow(false)

    private val _currentExplanation = MutableStateFlow<HardQuestionExplanation?>(null)
    val currentExplanation: StateFlow<HardQuestionExplanation?> = _currentExplanation.asStateFlow()
    val isExplanationLoading = MutableStateFlow(false)

    private val _flashcards = MutableStateFlow<List<StudyFlashcard>>(emptyList())
    val flashcards: StateFlow<List<StudyFlashcard>> = _flashcards.asStateFlow()
    val isFlashcardsLoading = MutableStateFlow(false)

    private val _quiz = MutableStateFlow<List<StudyQuizQuestion>>(emptyList())
    val quiz: StateFlow<List<StudyQuizQuestion>> = _quiz.asStateFlow()
    val isQuizLoading = MutableStateFlow(false)

    // Hard Question prompt input
    val hardQuestionInput = MutableStateFlow("")

    // Ask Doubt input & conversation
    val doubtInput = MutableStateFlow("")
    private val _doubtAnswer = MutableStateFlow<String?>(null)
    val doubtAnswer: StateFlow<String?> = _doubtAnswer.asStateFlow()
    val isDoubtLoading = MutableStateFlow(false)

    // Dedicated Mistake Analyzer State
    val mistakeQuestionInput = MutableStateFlow("")
    val mistakeAttemptInput = MutableStateFlow("")
    private val _mistakeAnalysisResult = MutableStateFlow<QuestionMistakeAnalysis?>(null)
    val mistakeAnalysisResult: StateFlow<QuestionMistakeAnalysis?> = _mistakeAnalysisResult.asStateFlow()
    val isMistakeAnalysisLoading = MutableStateFlow(false)

    // Modals
    val isGlobalSolverOpen = MutableStateFlow(false)
    val isTimerDialogOpen = MutableStateFlow(false)
    val isShareDialogOpen = MutableStateFlow(false)
    val isAppBlockerDialogOpen = MutableStateFlow(false)
    val isPhotoUploadDialogOpen = MutableStateFlow(false)
    val isLargeNotesUploadOpen = MutableStateFlow(false)
    private var pendingActionAfterUnlock: (() -> Unit)? = null

    // --- Study Focus Timer State ---
    val timerMode = MutableStateFlow(TimerMode.POMODORO_25)
    val timerTotalSeconds = MutableStateFlow(25 * 60)
    val timerRemainingSeconds = MutableStateFlow(25 * 60)
    val isTimerRunning = MutableStateFlow(false)
    val totalStudyMinutesToday = MutableStateFlow(45)
    val streakDays = MutableStateFlow(5)
    private var timerJob: Job? = null

    // --- Strict Study App Blocker Lockout State ---
    val isStrictLockActive = MutableStateFlow(false)
    val strictLockRemainingSec = MutableStateFlow(30 * 60)
    val strictLockTotalSec = MutableStateFlow(30 * 60)
    private var strictLockJob: Job? = null

    init {
        // Persistent strict lockdown check across app restarts
        val strictEndTime = prefs.getLong("strict_lock_end_timestamp", 0L)
        val now = System.currentTimeMillis()
        if (strictEndTime > now) {
            val remainingSec = ((strictEndTime - now) / 1000).toInt()
            if (remainingSec > 0) {
                val totalSec = prefs.getInt("strict_lock_total_sec", remainingSec)
                strictLockTotalSec.value = totalSec
                strictLockRemainingSec.value = remainingSec
                isStrictLockActive.value = true
                resumeStrictLockCountdown()
            }
        }
    }

    // Distracting Apps list to block during study
    val blockedApps = MutableStateFlow(
        prefs.getStringSet(
            "blocked_apps",
            setOf("Instagram", "YouTube & Shorts", "Snapchat", "WhatsApp", "Free Fire / BGMI")
        ) ?: setOf("Instagram", "YouTube & Shorts", "Snapchat", "WhatsApp")
    )

    // Snack/Toast message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Onboarding finish or API Key Update
    fun finishOnboarding(apiKey: String) {
        val trimmed = apiKey.trim()
        customApiKey.value = trimmed
        hasCompletedOnboarding.value = true
        isApiKeySettingsOpen.value = false
        prefs.edit()
            .putString("custom_api_key", trimmed)
            .putBoolean("has_completed_onboarding", true)
            .apply()

        repository.setCustomApiKey(trimmed)
        if (trimmed.isNotBlank()) {
            _userMessage.value = "कस्टम Gemini API Key सेव हो गई (असीमित AI कोटा सक्रिय)! ✨"
        } else {
            _userMessage.value = "शेयर किए गए फेयर-यूज़ कोटे (35/दिन) के साथ ज्ञानसेतु तैयार है! 📚"
        }
    }

    // Distracting Apps management
    fun toggleBlockedApp(appName: String) {
        val current = blockedApps.value.toMutableSet()
        if (current.contains(appName)) {
            current.remove(appName)
        } else {
            current.add(appName)
        }
        blockedApps.value = current
        prefs.edit().putStringSet("blocked_apps", current).apply()
    }

    fun addCustomBlockedApp(appName: String) {
        val trimmed = appName.trim().take(40)
        if (trimmed.isBlank()) return
        val current = blockedApps.value.toMutableSet()
        if (current.size >= 30) {
            _userMessage.value = "अधिकतम 30 ऐप्स ब्लॉक लिस्ट में जोड़े जा सकते हैं."
            return
        }
        current.add(trimmed)
        blockedApps.value = current
        prefs.edit().putStringSet("blocked_apps", current).apply()
        _userMessage.value = "\"$trimmed\" को ब्लॉक लिस्ट में जोड़ा गया."
    }

    // Start Strict Study Lockdown with Persistent End Time
    fun startStrictStudyLock(minutes: Int) {
        val safeMins = minutes.coerceIn(1, 180)
        val sec = safeMins * 60
        strictLockTotalSec.value = sec
        strictLockRemainingSec.value = sec
        isStrictLockActive.value = true
        isAppBlockerDialogOpen.value = false

        prefs.edit()
            .putLong("strict_lock_end_timestamp", System.currentTimeMillis() + (sec * 1000L))
            .putInt("strict_lock_total_sec", sec)
            .apply()

        resumeStrictLockCountdown()
    }

    private fun resumeStrictLockCountdown() {
        strictLockJob?.cancel()
        strictLockJob = viewModelScope.launch {
            while (isStrictLockActive.value && strictLockRemainingSec.value > 0) {
                delay(1000L)
                strictLockRemainingSec.value -= 1
            }
            if (strictLockRemainingSec.value <= 0) {
                isStrictLockActive.value = false
                prefs.edit().remove("strict_lock_end_timestamp").apply()
                totalStudyMinutesToday.value += (strictLockTotalSec.value / 60)
                _userMessage.value = "🎉 शानदार! स्ट्रिक्ट स्टडी सेशन पूरा हुआ!"
                triggerVibration()
            }
        }
    }

    fun emergencyUnlockStrictLock() {
        isStrictLockActive.value = false
        prefs.edit().remove("strict_lock_end_timestamp").apply()
        strictLockJob?.cancel()
        _userMessage.value = "स्ट्रिक्ट स्टडी मोड अनलॉक हुआ."
    }

    // Save Study Notes Photo & Homework Log
    fun saveStudyPhotoLog(chapterId: Long, chapterTitle: String, photoUri: String, mode: String, note: String) {
        viewModelScope.launch {
            val log = StudyLogEntity(
                chapterId = chapterId,
                chapterTitle = chapterTitle.take(100),
                mode = mode,
                minutesSpent = if (mode == "WRITING") 25 else 15,
                photoUri = photoUri.ifBlank { null },
                noteText = note.trim().take(500)
            )
            repository.insertStudyLog(log)
            isPhotoUploadDialogOpen.value = false
            _userMessage.value = "पढ़ाई का फोटो रिकॉर्ड सेव हो गया! 📷"
        }
    }

    private fun createInitialBatchDrafts(): List<ChapterDraft> {
        return (1..6).map { num ->
            ChapterDraft(
                chapterNumber = num,
                title = "अध्याय $num",
                subject = "General Study",
                content = "",
                isLocked = false,
                password = "",
                passwordHint = ""
            )
        }
    }

    fun openBatchUpload() {
        _batchDrafts.value = createInitialBatchDrafts()
        _activeBatchTabIndex.value = 0
        _isBatchUploadOpen.value = true
    }

    fun closeBatchUpload() {
        _isBatchUploadOpen.value = false
    }

    fun setActiveBatchTab(index: Int) {
        if (index in 0 until _batchDrafts.value.size) {
            _activeBatchTabIndex.value = index
        }
    }

    fun updateBatchDraft(index: Int, updated: ChapterDraft) {
        val current = _batchDrafts.value.toMutableList()
        if (index in current.indices) {
            current[index] = updated
            _batchDrafts.value = current
        }
    }

    fun autoFillSampleBatch() {
        val presets = SampleNotesData.get6ChapterBatchPreset()
        val current = _batchDrafts.value.toMutableList()
        presets.forEachIndexed { i, preset ->
            if (i < current.size) {
                current[i] = current[i].copy(
                    title = preset.first,
                    subject = preset.second,
                    content = preset.third,
                    isLocked = (i == 2),
                    password = if (i == 2) "9999" else "",
                    passwordHint = if (i == 2) "Sample PIN: 9999" else ""
                )
            }
        }
        _batchDrafts.value = current
        _userMessage.value = "6 Chapters का सैंपल डेटा भर दिया गया! अब Save दबाएं."
    }

    fun saveBatchChapters() {
        viewModelScope.launch {
            val validDrafts = _batchDrafts.value.filter { it.title.isNotBlank() && it.content.isNotBlank() }
            if (validDrafts.isEmpty()) {
                _userMessage.value = "कम से कम 1 चैप्टर का शीर्षक और नोट्स लिखें!"
                return@launch
            }

            val invalidLock = validDrafts.find { it.isLocked && it.password.isBlank() }
            if (invalidLock != null) {
                _userMessage.value = "चैप्टर ${invalidLock.chapterNumber} में लॉक ऑन है, कृपया पिन या पासवर्ड डालें!"
                return@launch
            }

            // RESOURCE PROTECTION: Validate bounds to prevent SQLite bloat/crash
            val entities = validDrafts.map { draft ->
                ChapterEntity(
                    chapterNumber = draft.chapterNumber,
                    title = draft.title.trim().take(100),
                    subject = draft.subject.trim().take(50).ifBlank { "General" },
                    content = draft.content.trim().take(15000),
                    isLocked = draft.isLocked,
                    passwordHash = if (draft.isLocked) repository.hashPassword(draft.password.trim()) else "",
                    passwordHint = if (draft.isLocked) draft.passwordHint.trim().take(80) else ""
                )
            }

            repository.insertBatchChapters(entities)
            _isBatchUploadOpen.value = false
            _userMessage.value = "${entities.size} Chapters सुरक्षित रूप से सेव हो गए! 📚"
        }
    }

    fun isChapterUnlocked(chapterId: Long): Boolean {
        return _unlockedChapterIds.value.contains(chapterId)
    }

    fun executeGuardedAction(chapter: ChapterEntity, action: () -> Unit) {
        if (chapter.isLocked && !_unlockedChapterIds.value.contains(chapter.id)) {
            _pendingUnlockChapter.value = chapter
            _passwordError.value = null
            pendingActionAfterUnlock = action
        } else {
            action.invoke()
        }
    }

    fun onChapterClicked(chapter: ChapterEntity) {
        executeGuardedAction(chapter) {
            selectChapter(chapter)
        }
    }

    fun openMistakeSolverForChapter(chapter: ChapterEntity) {
        executeGuardedAction(chapter) {
            selectChapter(chapter)
            studyTab.value = 1 // Tab 1: Mistake Solver
        }
    }

    private var failedUnlockAttempts = 0
    private var lockoutUntilTime = 0L

    fun attemptUnlock(enteredPassword: String) {
        val now = System.currentTimeMillis()
        if (now < lockoutUntilTime) {
            val waitSec = ((lockoutUntilTime - now) / 1000) + 1
            _passwordError.value = "बहुत सारे गलत प्रयास! कृपया $waitSec सेकंड बाद प्रयास करें."
            return
        }

        val chapter = _pendingUnlockChapter.value ?: _selectedChapter.value ?: return
        if (repository.verifyPassword(chapter, enteredPassword)) {
            failedUnlockAttempts = 0
            _unlockedChapterIds.value = _unlockedChapterIds.value + chapter.id
            _pendingUnlockChapter.value = null
            _passwordError.value = null
            val pendingAction = pendingActionAfterUnlock
            pendingActionAfterUnlock = null
            if (pendingAction != null) {
                pendingAction.invoke()
            } else {
                selectChapter(chapter)
            }
            _userMessage.value = "चैप्टर अनलॉक हो गया! 🔓"
        } else {
            failedUnlockAttempts++
            if (failedUnlockAttempts >= 5) {
                lockoutUntilTime = now + 30000L
                _passwordError.value = "5 गलत प्रयास! सुरक्षा के लिए 30 सेकंड का लॉक लगाया गया है."
            } else {
                _passwordError.value = "गलत पासवर्ड! (प्रयास: $failedUnlockAttempts/5)"
            }
        }
    }

    fun cancelUnlock() {
        _pendingUnlockChapter.value = null
        _passwordError.value = null
        pendingActionAfterUnlock = null
    }

    fun relockChapterImmediately(chapter: ChapterEntity) {
        _unlockedChapterIds.value = _unlockedChapterIds.value - chapter.id
        if (_selectedChapter.value?.id == chapter.id) {
            _selectedChapter.value = null
        }
        _userMessage.value = "\"${chapter.title}\" को फिर से सुरक्षित लॉक कर दिया गया! 🔒"
    }

    // --- 2GB Notes & Document Upload ---
    fun openLargeNotesUpload() {
        isLargeNotesUploadOpen.value = true
    }

    fun closeLargeNotesUpload() {
        isLargeNotesUploadOpen.value = false
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> "%.2f GB".format(gb)
            mb >= 1.0 -> "%.1f MB".format(mb)
            kb >= 1.0 -> "%.1f KB".format(kb)
            else -> "$bytes B"
        }
    }

    fun saveLargeNote(
        title: String,
        subject: String,
        content: String,
        fileName: String,
        fileUri: String,
        fileSize: Long,
        fileType: String,
        isLocked: Boolean,
        password: String,
        hint: String
    ) {
        val maxSizeBytes = 2L * 1024 * 1024 * 1024 // 2 GB = 2,147,483,648 bytes
        if (fileSize > maxSizeBytes) {
            _userMessage.value = "त्रुटि: फाइल का आकार 2GB सीमा से अधिक है! (${formatFileSize(fileSize)})"
            return
        }

        if (title.isBlank()) {
            _userMessage.value = "कृपया नोट्स का शीर्षक लिखें!"
            return
        }

        if (isLocked && password.isBlank()) {
            _userMessage.value = "लॉक चालू है, कृपया पासवर्ड या पिन दर्ज करें!"
            return
        }

        viewModelScope.launch {
            val totalExisting = chapters.value.size
            val cleanContent = content.trim().take(15000).ifBlank {
                "2GB डॉक्यूमेंट संलग्न: $fileName (${formatFileSize(fileSize)})"
            }
            val newChapter = ChapterEntity(
                chapterNumber = totalExisting + 1,
                title = title.trim().take(120),
                subject = subject.trim().take(60).ifBlank { "General" },
                content = cleanContent,
                isLocked = isLocked,
                passwordHash = if (isLocked) repository.hashPassword(password.trim()) else "",
                passwordHint = if (isLocked) hint.trim().take(80) else "",
                attachedFileName = fileName.trim().take(150),
                attachedFileUri = fileUri.trim(),
                attachedFileSize = fileSize,
                attachedFileType = fileType.trim().take(50)
            )

            repository.insertChapter(newChapter)
            isLargeNotesUploadOpen.value = false
            val sizeFormatted = formatFileSize(fileSize)
            val lockStatus = if (isLocked) " (पासवर्ड सुरक्षित 🔒)" else ""
            _userMessage.value = "2GB नोट्स '$title' ($sizeFormatted)$lockStatus सेव हो गया! 📄"
        }
    }

    // --- Dedicated Question Mistake Analyzer ---
    fun analyzeQuestionMistake(chapter: ChapterEntity) {
        val q = mistakeQuestionInput.value.trim()
        val attempt = mistakeAttemptInput.value.trim()
        if (q.isBlank()) {
            _userMessage.value = "कृपया वह सवाल लिखें जिसमें गलती या संदेह है!"
            return
        }

        viewModelScope.launch {
            isMistakeAnalysisLoading.value = true
            try {
                val result = repository.analyzeQuestionMistake(
                    question = q,
                    studentAttempt = attempt,
                    chapterTitle = chapter.title,
                    notesContent = chapter.content,
                    languageStyle = selectedLanguage.value
                )
                _mistakeAnalysisResult.value = result
                _userMessage.value = "AI ने सवाल में हुई गलती का विश्लेषण कर दिया! 💡"
            } catch (e: Exception) {
                _userMessage.value = "त्रुटि: ${e.message}"
            } finally {
                isMistakeAnalysisLoading.value = false
            }
        }
    }

    fun clearMistakeAnalysis() {
        mistakeQuestionInput.value = ""
        mistakeAttemptInput.value = ""
        _mistakeAnalysisResult.value = null
    }

    private fun selectChapter(chapter: ChapterEntity) {
        _selectedChapter.value = chapter
        studyTab.value = 0
        _currentSummary.value = null
        _currentExplanation.value = null
        _flashcards.value = emptyList()
        _quiz.value = emptyList()
        _doubtAnswer.value = null
        doubtInput.value = ""
        hardQuestionInput.value = ""

        loadSummary(chapter, forceRefresh = false)
        loadFlashcards(chapter, forceRefresh = false)
    }

    fun closeChapterDetail() {
        _selectedChapter.value = null
    }

    fun openEditChapter(chapter: ChapterEntity) {
        if (chapter.isLocked && !_unlockedChapterIds.value.contains(chapter.id)) {
            _pendingUnlockChapter.value = chapter
            _passwordError.value = "नोट्स संपादित करने से पहले कृपया पासवर्ड अनलॉक करें!"
            return
        }
        _chapterToEdit.value = chapter
    }

    fun closeEditChapter() {
        _chapterToEdit.value = null
    }

    fun saveEditedChapter(id: Long, newTitle: String, newSubject: String, newContent: String) {
        viewModelScope.launch {
            val existing = repository.getChapterById(id) ?: return@launch
            val updated = existing.copy(
                title = newTitle.trim().take(100),
                subject = newSubject.trim().take(50).ifBlank { "General" },
                content = newContent.trim().take(15000),
                summaryJson = "", // Invalidate summary cache so it reflects edited notes
                flashcardsJson = "",
                quizJson = ""
            )
            repository.updateChapter(updated)
            if (_selectedChapter.value?.id == id) {
                _selectedChapter.value = updated
            }
            _chapterToEdit.value = null
            _userMessage.value = "चैप्टर अपडेट हो गया! 📝"
        }
    }

    fun toggleLockForChapter(chapter: ChapterEntity, newLock: Boolean, pin: String, hint: String) {
        viewModelScope.launch {
            repository.setChapterLock(chapter.id, newLock, pin, hint)
            val updated = chapter.copy(
                isLocked = newLock,
                passwordHash = if (newLock) repository.hashPassword(pin) else "",
                passwordHint = if (newLock) hint.trim().take(80) else ""
            )
            _selectedChapter.value = updated
            if (newLock) {
                _unlockedChapterIds.value = _unlockedChapterIds.value + chapter.id
                _userMessage.value = "पासवर्ड लॉक सेट हो गया! 🔒"
            } else {
                _userMessage.value = "पासवर्ड लॉक हटा दिया गया! 🔓"
            }
        }
    }

    fun deleteChapter(chapterId: Long) {
        viewModelScope.launch {
            val chapter = repository.getChapterById(chapterId) ?: return@launch
            if (chapter.isLocked && !_unlockedChapterIds.value.contains(chapter.id)) {
                _pendingUnlockChapter.value = chapter
                _passwordError.value = "चैप्टर हटाने से पहले पासवर्ड अनलॉक करें!"
                return@launch
            }
            repository.deleteChapter(chapterId)
            if (_selectedChapter.value?.id == chapterId) {
                _selectedChapter.value = null
            }
            _userMessage.value = "चैप्टर हटा दिया गया."
        }
    }

    fun loadSummary(chapter: ChapterEntity, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            isSummaryLoading.value = true
            try {
                val sum = repository.getOrGenerateSummary(chapter, selectedLanguage.value, forceRefresh)
                _currentSummary.value = sum
            } catch (e: Exception) {
                _userMessage.value = "सारांश लोड करने में त्रुटि: ${e.localizedMessage}"
            } finally {
                isSummaryLoading.value = false
            }
        }
    }

    fun explainHardQuestion(questionText: String) {
        val chapter = _selectedChapter.value ?: return
        val trimmedQ = questionText.trim().take(500)
        if (trimmedQ.isBlank()) {
            _userMessage.value = "कृपया कठिन सवाल लिखें!"
            return
        }

        viewModelScope.launch {
            isExplanationLoading.value = true
            try {
                val exp = repository.explainHardQuestion(
                    question = trimmedQ,
                    chapterContext = chapter.content,
                    level = selectedExplanationLevel.value,
                    languageStyle = selectedLanguage.value
                )
                _currentExplanation.value = exp
            } catch (e: Exception) {
                _userMessage.value = "Explanation error: ${e.localizedMessage}"
            } finally {
                isExplanationLoading.value = false
            }
        }
    }

    fun explainStandaloneQuestion(questionText: String, topic: String) {
        val trimmedQ = questionText.trim().take(500)
        if (trimmedQ.isBlank()) {
            _userMessage.value = "कृपया सवाल लिखें!"
            return
        }
        viewModelScope.launch {
            isExplanationLoading.value = true
            try {
                val exp = repository.explainHardQuestion(
                    question = trimmedQ,
                    chapterContext = "Topic: ${topic.take(100)}",
                    level = selectedExplanationLevel.value,
                    languageStyle = selectedLanguage.value
                )
                _currentExplanation.value = exp
            } catch (e: Exception) {
                _userMessage.value = "Explanation error: ${e.localizedMessage}"
            } finally {
                isExplanationLoading.value = false
            }
        }
    }

    fun loadFlashcards(chapter: ChapterEntity, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            isFlashcardsLoading.value = true
            try {
                val fc = repository.getOrGenerateFlashcards(chapter, forceRefresh)
                _flashcards.value = fc
            } catch (e: Exception) {
                _userMessage.value = "Flashcards error: ${e.localizedMessage}"
            } finally {
                isFlashcardsLoading.value = false
            }
        }
    }

    fun loadQuiz(chapter: ChapterEntity, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            isQuizLoading.value = true
            try {
                val q = repository.getOrGenerateQuiz(chapter, forceRefresh)
                _quiz.value = q
            } catch (e: Exception) {
                _userMessage.value = "Quiz error: ${e.localizedMessage}"
            } finally {
                isQuizLoading.value = false
            }
        }
    }

    fun askDoubt() {
        val chapter = _selectedChapter.value ?: return
        val doubt = doubtInput.value.trim().take(500)
        if (doubt.isBlank()) return

        viewModelScope.launch {
            isDoubtLoading.value = true
            try {
                val ans = repository.askDoubt(
                    userDoubt = doubt,
                    chapterTitle = chapter.title,
                    notesContent = chapter.content,
                    languageStyle = selectedLanguage.value
                )
                _doubtAnswer.value = ans
            } catch (e: Exception) {
                _userMessage.value = "Doubt error: ${e.localizedMessage}"
            } finally {
                isDoubtLoading.value = false
            }
        }
    }

    // --- Study Timer Actions ---
    fun setTimerMode(mode: TimerMode) {
        timerMode.value = mode
        val sec = mode.minutes * 60
        timerTotalSeconds.value = sec
        timerRemainingSeconds.value = sec
        pauseTimer()
    }

    fun setCustomTimerMinutes(mins: Int) {
        val safeMins = mins.coerceIn(1, 180)
        timerTotalSeconds.value = safeMins * 60
        timerRemainingSeconds.value = safeMins * 60
        pauseTimer()
    }

    fun toggleTimer() {
        if (isTimerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startTimer() {
        if (isTimerRunning.value) return
        isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isTimerRunning.value && timerRemainingSeconds.value > 0) {
                delay(1000L)
                timerRemainingSeconds.value -= 1
            }
            if (timerRemainingSeconds.value <= 0) {
                isTimerRunning.value = false
                onTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        timerRemainingSeconds.value = timerTotalSeconds.value
    }

    private fun onTimerFinished() {
        val studiedMins = timerTotalSeconds.value / 60
        totalStudyMinutesToday.value += studiedMins
        _userMessage.value = "🎉 बधाई हो! $studiedMins मिनट का स्टडी सेशन पूरा हुआ!"
        triggerVibration()
    }

    private fun triggerVibration() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (_: Exception) {}
    }

    // --- Sharing Utilities ---
    fun shareApp(context: Context) {
        val shareText = """
📚 GyanSetu AI (ज्ञानसेतु) - Smart Study Companion!

✨ मुख्य फीचर्स:
• 📖 6 Chapters तक के नोट्स एक साथ अपलोड करें
• 🔒 पासवर्ड लॉक सुरक्षित नोट्स (SHA-256 Protected)
• 💡 कठिन सवाल आसान भाषा में (Real-life Analogies & Steps)
• ⏱️ पोमोडोरो स्टडी टाइमर और स्ट्रीक ट्रैकर
• 🚫 ध्यान भटकाने वाले ऐप्स ब्लॉकर व स्ट्रिक्ट लॉक
• 📷 पढ़ाई व होमवर्क नोट्स की फोटो अपलोड
• 🧠 याद करना vs ✍️ लिखना मोड्स

ऐप लिंक: https://ais-pre-otqb6pqfj542gf2i5rwz3u-361173408318.asia-southeast1.run.app
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "GyanSetu AI - Study App")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "GyanSetu AI शेयर करें"))
    }

    fun shareChapterNotes(context: Context, chapter: ChapterEntity) {
        // SECURITY LOOPHOLE FIX: Do not allow sharing locked notes without unlocking!
        if (chapter.isLocked && !_unlockedChapterIds.value.contains(chapter.id)) {
            _pendingUnlockChapter.value = chapter
            _passwordError.value = "सुरक्षा के लिए कृपया पहले चैप्टर अनलॉक करें!"
            return
        }

        val summaryText = if (chapter.summaryJson.isNotBlank()) "\n\n💡 सारांश उपलब्ध" else ""
        val shareText = """
📖 ${chapter.title} (${chapter.subject})
${chapter.content.take(600)}${if (chapter.content.length > 600) "..." else ""}$summaryText

— Shared via GyanSetu AI 📚
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, chapter.title)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "चैप्टर नोट्स शेयर करें"))
    }
}
