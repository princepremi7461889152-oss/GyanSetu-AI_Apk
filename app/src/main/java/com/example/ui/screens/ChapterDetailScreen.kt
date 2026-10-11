package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockReset
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.QuestionMistakeSolverCard
import com.example.ui.theme.EmeraldSuccess
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ChapterEntity
import com.example.model.LanguageStyle
import com.example.ui.StudyViewModel
import com.example.ui.components.FlashcardsView
import com.example.ui.components.HardQuestionSolverCard
import com.example.ui.components.PracticeQuizView
import com.example.ui.theme.RoseLock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    chapter: ChapterEntity,
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val activeTab by viewModel.studyTab.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedExplanationLevel by viewModel.selectedExplanationLevel.collectAsState()
    val currentSummary by viewModel.currentSummary.collectAsState()
    val isSummaryLoading by viewModel.isSummaryLoading.collectAsState()
    val currentExplanation by viewModel.currentExplanation.collectAsState()
    val isExplanationLoading by viewModel.isExplanationLoading.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()
    val isFlashcardsLoading by viewModel.isFlashcardsLoading.collectAsState()
    val quiz by viewModel.quiz.collectAsState()
    val isQuizLoading by viewModel.isQuizLoading.collectAsState()
    val hardQuestionInput by viewModel.hardQuestionInput.collectAsState()
    val doubtInput by viewModel.doubtInput.collectAsState()
    val doubtAnswer by viewModel.doubtAnswer.collectAsState()
    val isDoubtLoading by viewModel.isDoubtLoading.collectAsState()

    // Mistake Analyzer States
    val mistakeQuestionInput by viewModel.mistakeQuestionInput.collectAsState()
    val mistakeAttemptInput by viewModel.mistakeAttemptInput.collectAsState()
    val mistakeAnalysisResult by viewModel.mistakeAnalysisResult.collectAsState()
    val isMistakeAnalysisLoading by viewModel.isMistakeAnalysisLoading.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()

    val isUnlockedInSession = !chapter.isLocked || viewModel.isChapterUnlocked(chapter.id)

    // Security Gate: If notes have a lock, show password operation immediately!
    if (!isUnlockedInSession) {
        var pinInput by remember { mutableStateOf("") }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("सुरक्षित नोट्स (Locked)") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(RoseLock.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = RoseLock,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "इस नोट्स में पहले से पासवर्ड लॉक लगा है। नोट्स, 2GB फाइल और AI मिस्टेक सॉल्वर देखने के लिए पासवर्ड दर्ज करें.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        if (chapter.passwordHint.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "💡 पासवर्ड हिंट: ${chapter.passwordHint}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { pinInput = it },
                            label = { Text("पासवर्ड या पिन डालें") },
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("detail_screen_password_input")
                        )

                        if (passwordError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = passwordError!!,
                                color = RoseLock,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.attemptUnlock(pinInput) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("detail_screen_unlock_submit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("अनलॉक करें (Unlock)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("वापस जाएं (Go Back)")
                        }
                    }
                }
            }
        }
        return
    }

    var showLockConfigDialog by remember { mutableStateOf(false) }

    // Lock configuration dialog
    if (showLockConfigDialog) {
        var isLockedState by remember { mutableStateOf(chapter.isLocked) }
        var pinInput by remember { mutableStateOf("") }
        var hintInput by remember { mutableStateOf(chapter.passwordHint) }

        AlertDialog(
            onDismissRequest = { showLockConfigDialog = false },
            title = {
                Text(
                    text = "नोट्स लॉक सेटिंग्स (Lock Security)",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("इस चैप्टर में पासवर्ड लॉक लगाएं:")
                        Switch(
                            checked = isLockedState,
                            onCheckedChange = { isLockedState = it }
                        )
                    }

                    if (isLockedState) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { pinInput = it },
                            label = { Text("पासवर्ड / 4-अंकों का पिन") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = hintInput,
                            onValueChange = { hintInput = it },
                            label = { Text("पासवर्ड हिंट (वैकल्पिक)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleLockForChapter(chapter, isLockedState, pinInput, hintInput)
                        showLockConfigDialog = false
                    }
                ) {
                    Text("सेव करें (Save)")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLockConfigDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Chapter ${chapter.chapterNumber} • ${chapter.subject}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current

                    // Study Timer Shortcut
                    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
                    IconButton(
                        onClick = { viewModel.isTimerDialogOpen.value = true },
                        modifier = Modifier.testTag("detail_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Study Timer",
                            tint = if (isTimerRunning) EmeraldSuccess else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Share Notes
                    IconButton(
                        onClick = { viewModel.shareChapterNotes(context, chapter) },
                        modifier = Modifier.testTag("detail_share_notes_button")
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Share,
                            contentDescription = "Share Notes",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Immediate Relock if locked
                    if (chapter.isLocked) {
                        IconButton(
                            onClick = {
                                viewModel.relockChapterImmediately(chapter)
                                onBack()
                            },
                            modifier = Modifier.testTag("detail_relock_button")
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.LockReset,
                                contentDescription = "Relock Chapter",
                                tint = RoseLock
                            )
                        }
                    }

                    // Lock Settings
                    IconButton(
                        onClick = { showLockConfigDialog = true },
                        modifier = Modifier.testTag("toggle_lock_button")
                    ) {
                        Icon(
                            imageVector = if (chapter.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock settings",
                            tint = if (chapter.isLocked) RoseLock else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        // Show timer if opened from here
        val isTimerDialogOpen by viewModel.isTimerDialogOpen.collectAsState()
        if (isTimerDialogOpen) {
            com.example.ui.components.StudyTimerDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.isTimerDialogOpen.value = false }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            val tabs = listOf(
                "📖 सारांश व 2GB नोट्स",
                "🔍 सवाल में मिस्टेक",
                "💡 कठिन सवाल",
                "🗂️ फ्लैशकार्ड्स",
                "📝 क्विज़",
                "💬 AI से पूछें"
            )

            PrimaryScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_tab_row")
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = {
                            viewModel.studyTab.value = index
                            if (index == 3 && flashcards.isEmpty()) {
                                viewModel.loadFlashcards(chapter)
                            } else if (index == 4 && quiz.isEmpty()) {
                                viewModel.loadQuiz(chapter)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("study_tab_$index")
                    )
                }
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("study_tab_content"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeTab) {
                    0 -> {
                        // TAB 1: SUMMARY & NOTES
                        // 2GB Attached Document Card (if document attached)
                        if (chapter.attachedFileName.isNotBlank()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("attached_document_card"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (chapter.attachedFileName.endsWith(".pdf", ignoreCase = true))
                                                        Icons.Default.PictureAsPdf
                                                    else
                                                        Icons.Default.Description,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "📄 संलग्न 2GB नोट्स फाइल",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = chapter.attachedFileName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "साइज: ${viewModel.formatFileSize(chapter.attachedFileSize)} (अधिकतम 2GB समर्थित)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(
                                                            Uri.parse(chapter.attachedFileUri),
                                                            chapter.attachedFileType.ifBlank { "*/*" }
                                                        )
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {
                                                    viewModel.openMistakeSolverForChapter(chapter)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("open_attached_doc_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("डॉक्यूमेंट खोलें / पढ़ें")
                                        }
                                    }
                                }
                            }
                        }

                        // Memorization vs Writing Mode Section
                        item {
                            com.example.ui.components.MemorizationVsWritingModeSection(
                                chapter = chapter,
                                onUploadPhotoClick = { viewModel.isPhotoUploadDialogOpen.value = true }
                            )
                        }

                        item {
                            // Language switcher for AI Summary
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI सारांश की भाषा:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    LanguageStyle.values().forEach { lang ->
                                        FilterChip(
                                            selected = selectedLanguage == lang,
                                            onClick = {
                                                viewModel.selectedLanguage.value = lang
                                                viewModel.loadSummary(chapter, forceRefresh = true)
                                            },
                                            label = { Text(lang.label, style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // AI Summary Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "AI सारांश (Sunrise & Summarize)",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "सरल और आसान भाषा में मुख्य बिंदु",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { viewModel.loadSummary(chapter, forceRefresh = true) },
                                            enabled = !isSummaryLoading,
                                            modifier = Modifier.testTag("refresh_summary_button")
                                        ) {
                                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh summary")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (isSummaryLoading) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("AI नोट्स को आसान भाषा में तैयार कर रहा है...")
                                        }
                                    } else if (currentSummary != null) {
                                        val sum = currentSummary!!
                                        // 1. Overview
                                        Text(
                                            text = sum.overview,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )

                                        // 2. Key Points
                                        if (sum.keyPoints.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = "📌 मुख्य परीक्षा बिंदु (Key Exam Points):",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            sum.keyPoints.forEach { pt ->
                                                Text(
                                                    text = pt,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }
                                        }

                                        // 3. Formulas or Definitions
                                        if (sum.importantFormulasOrDefinitions.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = "⚡ महत्वपूर्ण सूत्र और परिभाषाएं:",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            sum.importantFormulasOrDefinitions.forEach { f ->
                                                Text(
                                                    text = "• $f",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }
                                        }

                                        // 4. Exam Warning Trap
                                        if (sum.examWarningOrTrap.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Card(
                                                shape = RoundedCornerShape(10.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.tertiary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = sum.examWarningOrTrap,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.loadSummary(chapter, forceRefresh = true) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("AI सारांश तैयार करें")
                                        }
                                    }
                                }
                            }
                        }

                        // Original Full Notes Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📖 पूरे नोट्स (Original Notes)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        OutlinedButton(
                                            onClick = { viewModel.openEditChapter(chapter) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("एडिट करें", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = chapter.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 2: QUESTION MISTAKE SOLVER
                        item {
                            QuestionMistakeSolverCard(
                                questionInput = mistakeQuestionInput,
                                onQuestionChange = { viewModel.mistakeQuestionInput.value = it },
                                attemptInput = mistakeAttemptInput,
                                onAttemptChange = { viewModel.mistakeAttemptInput.value = it },
                                analysis = mistakeAnalysisResult,
                                isLoading = isMistakeAnalysisLoading,
                                onAnalyzeClick = { viewModel.analyzeQuestionMistake(chapter) },
                                onClearClick = { viewModel.clearMistakeAnalysis() },
                                chapterTitle = chapter.title
                            )
                        }
                    }

                    2 -> {
                        // TAB 3: HARD QUESTIONS SIMPLIFIER
                        item {
                            HardQuestionSolverCard(
                                questionInput = hardQuestionInput,
                                onQuestionChange = { viewModel.hardQuestionInput.value = it },
                                explanation = currentExplanation,
                                isLoading = isExplanationLoading,
                                selectedLevel = selectedExplanationLevel,
                                onLevelSelect = { viewModel.selectedExplanationLevel.value = it },
                                selectedLanguage = selectedLanguage,
                                onLanguageSelect = { viewModel.selectedLanguage.value = it },
                                onExplainClick = { viewModel.explainHardQuestion(hardQuestionInput) }
                            )
                        }
                    }

                    3 -> {
                        // TAB 4: FLASHCARDS
                        item {
                            FlashcardsView(
                                flashcards = flashcards,
                                isLoading = isFlashcardsLoading,
                                onRefresh = { viewModel.loadFlashcards(chapter, forceRefresh = true) }
                            )
                        }
                    }

                    4 -> {
                        // TAB 5: PRACTICE QUIZ
                        item {
                            PracticeQuizView(
                                questions = quiz,
                                isLoading = isQuizLoading,
                                onGenerateQuiz = { viewModel.loadQuiz(chapter, forceRefresh = true) }
                            )
                        }
                    }

                    5 -> {
                        // TAB 6: ASK AI DOUBT CHAT
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "AI ट्यूटर से डाउट पूछें",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Ask any doubt based on this chapter",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = doubtInput,
                                        onValueChange = { viewModel.doubtInput.value = it },
                                        label = { Text("अपना सवाल या डाउट यहाँ लिखें...") },
                                        placeholder = { Text("उदा. क्या स्टोमेटा रात में बंद रहते हैं?") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("doubt_input_field")
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { viewModel.askDoubt() },
                                        enabled = !isDoubtLoading && doubtInput.isNotBlank(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("ask_doubt_button")
                                    ) {
                                        if (isDoubtLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("AI उत्तर सोच रहा है...")
                                        } else {
                                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("पूछें (Ask Doubt)")
                                        }
                                    }

                                    if (doubtAnswer != null) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Card(
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    text = "💡 AI ट्यूटर का जवाब:",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = doubtAnswer!!,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
