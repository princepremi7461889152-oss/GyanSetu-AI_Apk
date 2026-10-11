package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
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
import com.example.ui.theme.RoseLock

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DistractingAppsBlockerDialog(
    blockedApps: Set<String>,
    onToggleApp: (String) -> Unit,
    onAddCustomApp: (String) -> Unit,
    onStartStrictStudyLock: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var customAppInput by remember { mutableStateOf("") }
    var customMinutesInput by remember { mutableStateOf("30") }

    val presetApps = listOf(
        "Instagram", "YouTube & Shorts", "Snapchat", "WhatsApp",
        "Free Fire / BGMI", "Facebook", "Netflix", "Chrome"
    )

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
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(RoseLock.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = RoseLock,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ध्यान भटकाने वाले ऐप्स ब्लॉक करें",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Strict Study App Blocker Mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Information Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = RoseLock.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = RoseLock,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "जब तक टाइमर खत्म नहीं होगा, तब तक पढ़ाई का स्ट्रिक्ट लॉक नहीं खुलेगा ताकि छात्र पढ़ाई के अलावा कोई ऐप न चला सके!",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoseLock,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ब्लॉक करने के लिए ऐप्स चुनें:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Preset Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (presetApps + blockedApps.filter { !presetApps.contains(it) }).distinct().forEach { appName ->
                        val isBlocked = blockedApps.contains(appName)
                        FilterChip(
                            selected = isBlocked,
                            onClick = { onToggleApp(appName) },
                            label = { Text(appName, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                if (isBlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = RoseLock,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoseLock.copy(alpha = 0.15f),
                                selectedLabelColor = RoseLock
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom App Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customAppInput,
                        onValueChange = { customAppInput = it },
                        label = { Text("अन्य ऐप का नाम लिखें...") },
                        placeholder = { Text("उदा. Telegram, गेम्स") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customAppInput.isNotBlank()) {
                                onAddCustomApp(customAppInput.trim())
                                customAppInput = ""
                            }
                        },
                        enabled = customAppInput.isNotBlank()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Timer Duration ("timer apne man se set kar sake")
                Text(
                    text = "टाइमर अपने मन से सेट करें (Study Minutes):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("15", "30", "45", "60", "90").forEach { mins ->
                        FilterChip(
                            selected = customMinutesInput == mins,
                            onClick = { customMinutesInput = mins },
                            label = { Text("$mins m", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customMinutesInput,
                    onValueChange = {
                        val num = it.filter { ch -> ch.isDigit() }
                        customMinutesInput = num
                    },
                    label = { Text("कस्टम मिनट (Custom Minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Start Strict Lock Mode Button
                val parsedMins = customMinutesInput.toIntOrNull() ?: 30
                Button(
                    onClick = { onStartStrictStudyLock(parsedMins) },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseLock),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_strict_lock_button")
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔒 $parsedMins मिनट का स्ट्रिक्ट लॉक चालू करें",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
