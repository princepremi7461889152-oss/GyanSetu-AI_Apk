package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.StudyViewModel
import com.example.ui.components.AppShareDialog
import com.example.ui.components.BatchUploadDialog
import com.example.ui.components.EditChapterDialog
import com.example.ui.components.PasswordUnlockDialog
import com.example.ui.components.StudyTimerDialog
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseLock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyHomeScreen(
    viewModel: StudyViewModel,
    onOpenGlobalSolver: () -> Unit
) {
    val context = LocalContext.current
    val chapters by viewModel.chapters.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSubjectFilter by viewModel.selectedSubjectFilter.collectAsState()
    val unlockedChapterIds by viewModel.unlockedChapterIds.collectAsState()
    val pendingUnlockChapter by viewModel.pendingUnlockChapter.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val isBatchUploadOpen by viewModel.isBatchUploadOpen.collectAsState()
    val batchDrafts by viewModel.batchDrafts.collectAsState()
    val activeBatchTabIndex by viewModel.activeBatchTabIndex.collectAsState()
    val chapterToEdit by viewModel.chapterToEdit.collectAsState()
    val isTimerDialogOpen by viewModel.isTimerDialogOpen.collectAsState()
    val isShareDialogOpen by viewModel.isShareDialogOpen.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val remainingSec by viewModel.timerRemainingSeconds.collectAsState()
    val todayMinutes by viewModel.totalStudyMinutesToday.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val dailyAiUsage by viewModel.dailyAiUsage.collectAsState()
    val maxAiQuota by viewModel.maxAiQuota.collectAsState()

    // Modals
    if (pendingUnlockChapter != null) {
        PasswordUnlockDialog(
            chapter = pendingUnlockChapter!!,
            errorMessage = passwordError,
            onUnlock = { pin -> viewModel.attemptUnlock(pin) },
            onDismiss = { viewModel.cancelUnlock() }
        )
    }

    if (isBatchUploadOpen) {
        BatchUploadDialog(
            drafts = batchDrafts,
            activeTabIndex = activeBatchTabIndex,
            onTabSelect = { viewModel.setActiveBatchTab(it) },
            onUpdateDraft = { idx, draft -> viewModel.updateBatchDraft(idx, draft) },
            onAutoFillSample = { viewModel.autoFillSampleBatch() },
            onSaveAll = { viewModel.saveBatchChapters() },
            onDismiss = { viewModel.closeBatchUpload() }
        )
    }

    if (chapterToEdit != null) {
        EditChapterDialog(
            chapter = chapterToEdit!!,
            onSave = { id, title, sub, content -> viewModel.saveEditedChapter(id, title, sub, content) },
            onDismiss = { viewModel.closeEditChapter() }
        )
    }

    if (isTimerDialogOpen) {
        StudyTimerDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.isTimerDialogOpen.value = false }
        )
    }

    if (isShareDialogOpen) {
        AppShareDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.isShareDialogOpen.value = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.gyansetu_new_icon_1791689602936),
                            contentDescription = "GyanSetu Icon",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ज्ञानसेतु AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Study • Lock • Timer",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Strict App Blocker Button
                    IconButton(
                        onClick = { viewModel.isAppBlockerDialogOpen.value = true },
                        modifier = Modifier.testTag("open_app_blocker_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "App Blocker Mode",
                            tint = RoseLock
                        )
                    }

                    // Study Photo Upload Button
                    IconButton(
                        onClick = { viewModel.isPhotoUploadDialogOpen.value = true },
                        modifier = Modifier.testTag("open_photo_upload_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Upload Study Photo",
                            tint = EmeraldSuccess
                        )
                    }

                    // Timer Button with active running badge
                    IconButton(
                        onClick = { viewModel.isTimerDialogOpen.value = true },
                        modifier = Modifier.testTag("open_timer_button")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Focus Timer",
                                tint = if (isTimerRunning) EmeraldSuccess else MaterialTheme.colorScheme.primary
                            )
                            if (isTimerRunning) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(EmeraldSuccess, CircleShape)
                                )
                            }
                        }
                    }

                    // Share App Button
                    IconButton(
                        onClick = { viewModel.isShareDialogOpen.value = true },
                        modifier = Modifier.testTag("open_share_dialog_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share App",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Global Tough Question Solver
                    IconButton(
                        onClick = onOpenGlobalSolver,
                        modifier = Modifier.testTag("open_global_solver_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Tough Question Solver",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // AI Key & Fair-Use Quota Settings
                    IconButton(
                        onClick = { viewModel.isApiKeySettingsOpen.value = true },
                        modifier = Modifier.testTag("open_api_key_settings_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "AI Quota & Key Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Theme Toggle
                    IconButton(
                        onClick = {
                            viewModel.isDarkTheme.value = if (isDarkTheme == true) false else true
                        },
                        modifier = Modifier.testTag("toggle_dark_theme_icon")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme == true) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openBatchUpload() },
                icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                text = { Text("अपलोड 6 Chapters") },
                modifier = Modifier.testTag("fab_batch_upload")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("chapters_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Banner Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨ AI स्मार्ट स्टडी साथी",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Timer pill if running
                            if (isTimerRunning) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.2f)),
                                    modifier = Modifier.clickable { viewModel.isTimerDialogOpen.value = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = String.format("%02d:%02d", remainingSec / 60, remainingSec % 60),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldSuccess
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "6 चैप्टर एक साथ अपलोड करें, नोट्स में पासवर्ड लॉक लगाएं, कठिन सवाल आसान भाषा में समझें और टाइमर से फोकस करें!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.openBatchUpload() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_upload_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("6 Chapters", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = { viewModel.isTimerDialogOpen.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_timer_button")
                            ) {
                                Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("स्टडी टाइमर", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = onOpenGlobalSolver,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_solve_button")
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("कठिन सवाल", maxLines = 1)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.openLargeNotesUpload() },
                                modifier = Modifier
                                    .weight(1.1f)
                                    .testTag("hero_2gb_upload_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("📄 2GB नोट्स", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = { viewModel.isAppBlockerDialogOpen.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_blocker_button")
                            ) {
                                Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = RoseLock, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ऐप लॉक", maxLines = 1, color = RoseLock)
                            }

                            OutlinedButton(
                                onClick = { viewModel.isPhotoUploadDialogOpen.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_photo_button")
                            ) {
                                Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("फोटो जोड़ें", maxLines = 1, color = EmeraldSuccess)
                            }
                        }
                    }
                }
            }

            // 2. Study Stats Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = AmberTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$streakDays दिन स्ट्रीक",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberTertiary
                            )
                        }

                        Text(
                            text = "⏱️ आज की पढ़ाई: $todayMinutes m",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        val lockedCount = chapters.count { it.isLocked }
                        Text(
                            text = "🔒 $lockedCount लॉक",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoseLock,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 3. Search & Subject Filter Bar
            item {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        label = { Text("चैप्टर या विषय खोजें (Search)...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val subjects = listOf("All", "Science", "Physics", "Chemistry", "Mathematics", "History", "Civics")
                    val filterScroll = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(filterScroll),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjects.forEach { sub ->
                            val isSelected = if (sub == "All") selectedSubjectFilter == null else selectedSubjectFilter == sub
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectedSubjectFilter.value = if (sub == "All") null else sub
                                },
                                label = { Text(sub, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 3.5 AI Fair-Use Resource Quota Status
            item {
                val remainingQuota = (maxAiQuota - dailyAiUsage).coerceAtLeast(0)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (remainingQuota > 5) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else RoseLock.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (remainingQuota > 5) MaterialTheme.colorScheme.primary else RoseLock,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (remainingQuota > 0) "दैनिक AI फेयर-यूज़ कोटा: $remainingQuota/$maxAiQuota अनुरोध शेष"
                                    else "⚠️ आज का AI कोटा समाप्त! (अपनी Key जोड़ें)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingQuota > 0) MaterialTheme.colorScheme.onSurface else RoseLock
                                )
                                Text(
                                    text = "संसाधनों के दुरुपयोग को रोकने के लिए दैनिक सुरक्षा सक्रिय है.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        TextButton(
                            onClick = { viewModel.isApiKeySettingsOpen.value = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Key बदलें", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // 4. Chapters Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "अध्ययन सूची (Study Chapters)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${chapters.size} Chapters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 5. Chapters List Items
            if (chapters.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "कोई चैप्टर नहीं मिला.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "'अपलोड 6 Chapters' बटन दबाकर नए नोट्स जोड़ें!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(chapters, key = { it.id }) { chapter ->
                    val isUnlockedInSession = !chapter.isLocked || unlockedChapterIds.contains(chapter.id)

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onChapterClicked(chapter) }
                            .testTag("chapter_card_${chapter.id}"),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Chapter # & Lock Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${chapter.chapterNumber}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Text(
                                            text = chapter.subject,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                if (chapter.isLocked) {
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isUnlockedInSession)
                                                EmeraldSuccess.copy(alpha = 0.15f)
                                            else
                                                RoseLock.copy(alpha = 0.15f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isUnlockedInSession) Icons.Default.LockOpen else Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = if (isUnlockedInSession) EmeraldSuccess else RoseLock,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isUnlockedInSession) "सत्र में अनलॉक" else "पासवर्ड लॉक",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUnlockedInSession) EmeraldSuccess else RoseLock
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = chapter.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (chapter.isLocked && !isUnlockedInSession) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(RoseLock.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = RoseLock,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "यह नोट्स पासवर्ड से सुरक्षित है। देखने के लिए टैप करें और पिन डालें.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RoseLock,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                if (chapter.attachedFileName.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (chapter.attachedFileName.endsWith(".pdf", ignoreCase = true)) Icons.Default.PictureAsPdf else Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${chapter.attachedFileName} (${viewModel.formatFileSize(chapter.attachedFileSize)})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                Text(
                                    text = chapter.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Bottom actions on card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Dedicated Mistake Solver Quick Button
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.openMistakeSolverForChapter(chapter)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.testTag("mistake_btn_${chapter.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("मिस्टेक पूछें", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Quick Relock button if currently unlocked in session
                                    if (chapter.isLocked && isUnlockedInSession) {
                                        OutlinedButton(
                                            onClick = { viewModel.relockChapterImmediately(chapter) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.testTag("relock_button_${chapter.id}")
                                        ) {
                                            Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = RoseLock, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("पुनः लॉक", style = MaterialTheme.typography.labelSmall, color = RoseLock)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    // Share chapter notes (Protected against unauthorized access)
                                    IconButton(
                                        onClick = {
                                            if (chapter.isLocked && !isUnlockedInSession) {
                                                viewModel.onChapterClicked(chapter)
                                            } else {
                                                viewModel.shareChapterNotes(context, chapter)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share Notes", modifier = Modifier.size(16.dp))
                                    }

                                    // Edit chapter (Protected against unauthorized access)
                                    IconButton(
                                        onClick = {
                                            if (chapter.isLocked && !isUnlockedInSession) {
                                                viewModel.onChapterClicked(chapter)
                                            } else {
                                                viewModel.openEditChapter(chapter)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Chapter", modifier = Modifier.size(16.dp))
                                    }

                                    // Delete chapter (Protected against unauthorized deletion)
                                    IconButton(
                                        onClick = {
                                            if (chapter.isLocked && !isUnlockedInSession) {
                                                viewModel.onChapterClicked(chapter)
                                            } else {
                                                viewModel.deleteChapter(chapter.id)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete chapter",
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.onChapterClicked(chapter) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("open_chapter_button_${chapter.id}")
                                ) {
                                    Text(
                                        text = if (chapter.isLocked && !isUnlockedInSession) "अनलॉक करें 🔓" else "अध्ययन करें 📖",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
