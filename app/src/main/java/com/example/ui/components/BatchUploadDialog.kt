package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.ChapterDraft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseLock

@Composable
fun BatchUploadDialog(
    drafts: List<ChapterDraft>,
    activeTabIndex: Int,
    onTabSelect: (Int) -> Unit,
    onUpdateDraft: (Int, ChapterDraft) -> Unit,
    onAutoFillSample: () -> Unit,
    onSaveAll: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "एक साथ 6 चैप्टर अपलोड करें",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val filledCount = drafts.count { it.title.isNotBlank() && it.content.isNotBlank() }
                            Text(
                                text = "Batch Upload ($filledCount / 6 Chapters Filled)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_batch_upload_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto-fill preset banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "तुरंत टेस्ट करने के लिए 6 चैप्टर्स का सैंपल लोड करें",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        OutlinedButton(
                            onClick = onAutoFillSample,
                            modifier = Modifier.testTag("autofill_batch_button"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("लोड करें (Sample)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Horizontal Chapter Selector (Ch 1 through Ch 6)
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    drafts.forEachIndexed { index, draft ->
                        val isSelected = activeTabIndex == index
                        val isFilled = draft.title.isNotBlank() && draft.content.isNotBlank()

                        FilterChip(
                            selected = isSelected,
                            onClick = { onTabSelect(index) },
                            label = {
                                Text(
                                    text = "Ch ${draft.chapterNumber}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                if (draft.isLocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = RoseLock,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else if (isFilled) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Filled",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("batch_chapter_tab_$index")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active Draft Editor
                val currentDraft = drafts.getOrNull(activeTabIndex) ?: drafts[0]

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Title
                    OutlinedTextField(
                        value = currentDraft.title,
                        onValueChange = { newTitle ->
                            if (newTitle.length <= 100) {
                                onUpdateDraft(activeTabIndex, currentDraft.copy(title = newTitle))
                            }
                        },
                        label = { Text("Chapter ${currentDraft.chapterNumber} शीर्षक (Title)") },
                        supportingText = { Text("${currentDraft.title.length}/100", style = MaterialTheme.typography.labelSmall) },
                        placeholder = { Text("उदा: प्रकाश संश्लेषण (Photosynthesis)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("draft_title_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subject / Category
                    OutlinedTextField(
                        value = currentDraft.subject,
                        onValueChange = { newSub ->
                            if (newSub.length <= 50) {
                                onUpdateDraft(activeTabIndex, currentDraft.copy(subject = newSub))
                            }
                        },
                        label = { Text("विषय / Subject (उदा. Science, Physics, Maths)") },
                        supportingText = { Text("${currentDraft.subject.length}/50", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("draft_subject_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notes Content
                    OutlinedTextField(
                        value = currentDraft.content,
                        onValueChange = { newContent ->
                            if (newContent.length <= 15000) {
                                onUpdateDraft(activeTabIndex, currentDraft.copy(content = newContent))
                            }
                        },
                        label = { Text("अध्याय के नोट्स (Notes Content)") },
                        placeholder = { Text("यहाँ अपने नोट्स पेस्ट करें या टाइप करें...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("draft_content_input"),
                        supportingText = {
                            Text("${currentDraft.content.length}/15,000 अक्षर | ${currentDraft.content.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size} शब्द")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Lock Configuration
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentDraft.isLocked)
                                RoseLock.copy(alpha = 0.08f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (currentDraft.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (currentDraft.isLocked) RoseLock else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "इस चैप्टर में पासवर्ड लॉक लगाएं",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (currentDraft.isLocked) "नोट्स देखने से पहले पासवर्ड पूछा जाएगा" else "नोट्स बिना पासवर्ड के खुलेंगे",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = currentDraft.isLocked,
                                    onCheckedChange = { checked ->
                                        onUpdateDraft(activeTabIndex, currentDraft.copy(isLocked = checked))
                                    },
                                    modifier = Modifier.testTag("draft_lock_switch")
                                )
                            }

                            if (currentDraft.isLocked) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = currentDraft.password,
                                    onValueChange = { newPass ->
                                        onUpdateDraft(activeTabIndex, currentDraft.copy(password = newPass))
                                    },
                                    label = { Text("पासवर्ड / 4-अंकों का पिन (PIN)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("draft_password_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = currentDraft.passwordHint,
                                    onValueChange = { newHint ->
                                        onUpdateDraft(activeTabIndex, currentDraft.copy(passwordHint = newHint))
                                    },
                                    label = { Text("पासवर्ड हिंट (वैकल्पिक / Optional Hint)") },
                                    placeholder = { Text("उदा. मेरी जन्म तिथि या 1234") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("draft_hint_input")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Next Chapter shortcut
                    if (activeTabIndex < drafts.size - 1) {
                        OutlinedButton(
                            onClick = { onTabSelect(activeTabIndex + 1) },
                            modifier = Modifier.testTag("next_chapter_tab_button")
                        ) {
                            Text("अगला Chapter ${activeTabIndex + 2} ➔")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = onSaveAll,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("save_all_batch_button")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("सभी चैप्टर्स सेव करें (Save All)")
                    }
                }
            }
        }
    }
}
