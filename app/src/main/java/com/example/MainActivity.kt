package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ui.StudyViewModel
import com.example.ui.components.ApiKeyAndPermissionsDialog
import com.example.ui.components.DistractingAppsBlockerDialog
import com.example.ui.components.GlobalHardQuestionDialog
import com.example.ui.components.StrictFocusLockOverlay
import com.example.ui.components.StudyPhotoUploadDialog
import com.example.ui.components.UploadLargeNotesDialog
import com.example.ui.screens.ChapterDetailScreen
import com.example.ui.screens.StudyHomeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val effectiveDark = isDarkTheme ?: systemDark

            MyApplicationTheme(darkTheme = effectiveDark) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: StudyViewModel) {
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    val isGlobalSolverOpen by viewModel.isGlobalSolverOpen.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Onboarding & Permissions state
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()

    // Strict Study Lockdown state
    val isStrictLockActive by viewModel.isStrictLockActive.collectAsState()
    val strictLockRemainingSec by viewModel.strictLockRemainingSec.collectAsState()
    val strictLockTotalSec by viewModel.strictLockTotalSec.collectAsState()
    val blockedApps by viewModel.blockedApps.collectAsState()
    val isAppBlockerDialogOpen by viewModel.isAppBlockerDialogOpen.collectAsState()
    val isPhotoUploadDialogOpen by viewModel.isPhotoUploadDialogOpen.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    val isApiKeySettingsOpen by viewModel.isApiKeySettingsOpen.collectAsState()

    // 1. Startup API Key & Permissions Onboarding Dialog / Settings
    if (!hasCompletedOnboarding || isApiKeySettingsOpen) {
        ApiKeyAndPermissionsDialog(
            initialApiKey = customApiKey,
            onSaveApiKeyAndFinish = { key -> viewModel.finishOnboarding(key) },
            onDismiss = if (hasCompletedOnboarding) {
                { viewModel.isApiKeySettingsOpen.value = false }
            } else null
        )
    }

    // 2. Distracting Apps Blocker Settings Dialog
    if (isAppBlockerDialogOpen) {
        DistractingAppsBlockerDialog(
            blockedApps = blockedApps,
            onToggleApp = { app -> viewModel.toggleBlockedApp(app) },
            onAddCustomApp = { app -> viewModel.addCustomBlockedApp(app) },
            onStartStrictStudyLock = { mins -> viewModel.startStrictStudyLock(mins) },
            onDismiss = { viewModel.isAppBlockerDialogOpen.value = false }
        )
    }

    // 3. Study Photo Upload Dialog
    if (isPhotoUploadDialogOpen) {
        val chapterTitle = selectedChapter?.title ?: "सामान्य अध्ययन (General Study)"
        val chapterId = selectedChapter?.id ?: 0L
        StudyPhotoUploadDialog(
            chapterTitle = chapterTitle,
            onSaveStudyPhoto = { photoUri, mode, note ->
                viewModel.saveStudyPhotoLog(chapterId, chapterTitle, photoUri, mode, note)
            },
            onDismiss = { viewModel.isPhotoUploadDialogOpen.value = false }
        )
    }

    // 3.5 2GB Large Notes & Document Upload Dialog
    val isLargeNotesUploadOpen by viewModel.isLargeNotesUploadOpen.collectAsState()
    if (isLargeNotesUploadOpen) {
        UploadLargeNotesDialog(
            onSaveNote = { title, sub, content, fName, fUri, fSize, fType, locked, pass, hint ->
                viewModel.saveLargeNote(title, sub, content, fName, fUri, fSize, fType, locked, pass, hint)
            },
            onDismiss = { viewModel.closeLargeNotesUpload() }
        )
    }

    // 4. Global Hard Question Solver Dialog
    if (isGlobalSolverOpen) {
        GlobalHardQuestionDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.isGlobalSolverOpen.value = false }
        )
    }

    // 5. Main Screen Scaffold
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (selectedChapter != null) {
            ChapterDetailScreen(
                chapter = selectedChapter!!,
                viewModel = viewModel,
                onBack = { viewModel.closeChapterDetail() }
            )
        } else {
            StudyHomeScreen(
                viewModel = viewModel,
                onOpenGlobalSolver = { viewModel.isGlobalSolverOpen.value = true }
            )
        }
    }

    // 6. Strict Study Lockdown Fullscreen Overlay (Stays on top until timer completes)
    if (isStrictLockActive) {
        StrictFocusLockOverlay(
            remainingSeconds = strictLockRemainingSec,
            totalSeconds = strictLockTotalSec,
            blockedApps = blockedApps,
            onEmergencyUnlock = { viewModel.emergencyUnlockStrictLock() }
        )
    }
}
