package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseLock
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun UploadLargeNotesDialog(
    onSaveNote: (
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
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val maxAllowedBytes = 2L * 1024 * 1024 * 1024 // 2 GB

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("General") }
    var content by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("") }
    var fileUriString by remember { mutableStateOf("") }
    var fileSize by remember { mutableLongStateOf(0L) }
    var fileMimeType by remember { mutableStateOf("") }

    var isLocked by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordHint by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Launcher for picking any document / notes / PDF up to 2GB
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                fileUriString = uri.toString()
                fileMimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                // Query file name and size safely
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex) ?: "Notes_Document"
                        }
                        if (sizeIndex != -1) {
                            fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                }

                // If title is empty, prefill with file name without extension
                if (title.isBlank() && fileName.isNotBlank()) {
                    val dotIdx = fileName.lastIndexOf('.')
                    title = if (dotIdx > 0) fileName.substring(0, dotIdx) else fileName
                }

                // Check 2GB Limit
                if (fileSize > maxAllowedBytes) {
                    errorMessage = "⚠️ फाइल 2GB सीमा से अधिक है! (${formatBytes(fileSize)}). कृपया 2GB से छोटी फाइल चुनें."
                } else {
                    errorMessage = null
                    // Try to read snippet if it's text or markdown
                    if (fileMimeType.contains("text", ignoreCase = true) || fileName.endsWith(".txt") || fileName.endsWith(".md")) {
                        try {
                            context.contentResolver.openInputStream(uri)?.use { stream ->
                                val reader = BufferedReader(InputStreamReader(stream))
                                val sb = StringBuilder()
                                var line: String?
                                var count = 0
                                while (reader.readLine().also { line = it } != null && count < 200) {
                                    sb.appendLine(line)
                                    count++
                                }
                                val extracted = sb.toString().take(10000)
                                if (extracted.isNotBlank() && content.isBlank()) {
                                    content = extracted
                                }
                            }
                        } catch (_: Exception) { }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "फाइल लोड करने में त्रुटि: ${e.message}"
            }
        }
    }

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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "2GB Upload",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2GB नोट्स अपलोड (Large Notes)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "PDF, Docs, टेक्स्ट या नोट्स फाइल (2GB तक समर्थित)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_upload_large_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // File Attachment Section Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "संलग्न फाइल (Document Attachment):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = {
                                    filePickerLauncher.launch(arrayOf("*/*", "application/pdf", "text/*"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("select_file_2gb_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (fileName.isEmpty()) "फाइल चुनें (Max 2GB)" else "फाइल बदलें")
                            }
                        }

                        if (fileName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (fileName.endsWith(".pdf", ignoreCase = true))
                                                Icons.Default.PictureAsPdf
                                            else
                                                Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = fileName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "साइज: ${formatBytes(fileSize)} / 2.00 GB",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = if (fileSize <= maxAllowedBytes) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = if (fileSize <= maxAllowedBytes) EmeraldSuccess else RoseLock,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Usage Progress out of 2GB
                                    val progressFraction = (fileSize.toFloat() / maxAllowedBytes.toFloat()).coerceIn(0f, 1f)
                                    LinearProgressIndicator(
                                        progress = { progressFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (fileSize <= maxAllowedBytes) EmeraldSuccess else RoseLock
                                    )
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        color = RoseLock,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 120) title = it },
                    label = { Text("नोट्स का शीर्षक (Title) *") },
                    placeholder = { Text("उदा. Class 12 Physics - Ray Optics Complete Notes") },
                    supportingText = { Text("${title.length}/120") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("large_note_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subject Input
                OutlinedTextField(
                    value = subject,
                    onValueChange = { if (it.length <= 50) subject = it },
                    label = { Text("विषय (Subject)") },
                    placeholder = { Text("उदा. Physics / Chemistry / Biology / Maths") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("large_note_subject_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Notes Description / Summary Input
                OutlinedTextField(
                    value = content,
                    onValueChange = { if (it.length <= 15000) content = it },
                    label = { Text("नोट्स का विवरण या मुख्य सूत्र (Notes Summary / Formulae)") },
                    placeholder = { Text("चैप्टर के मुख्य टॉपिक, फॉर्मूले या नोट्स यहाँ लिखें (ताकि AI सवाल में गलतियाँ आसानी से पकड़ सके)") },
                    supportingText = { Text("${content.length}/15000") },
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("large_note_content_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Security & Password Lock Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLocked) RoseLock.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (isLocked) RoseLock else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "पासवर्ड सुरक्षा (Password Lock)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLocked) RoseLock else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "इस 2GB नोट्स को लॉक करें",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isLocked,
                                onCheckedChange = { isLocked = it },
                                modifier = Modifier.testTag("large_note_lock_switch")
                            )
                        }

                        if (isLocked) {
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { if (it.length <= 20) passwordInput = it },
                                label = { Text("पासवर्ड या 4-अंकों का पिन *") },
                                placeholder = { Text("उदा. 1234 या SecretPass") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("large_note_pin_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = passwordHint,
                                onValueChange = { if (it.length <= 60) passwordHint = it },
                                label = { Text("पासवर्ड हिंट (वैकल्पिक)") },
                                placeholder = { Text("उदा. मेरा रोल नंबर") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("large_note_hint_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🔒 नोट्स सेव होने पर कोई भी इसे बिना पासवर्ड नहीं खोल सकेगा!",
                                style = MaterialTheme.typography.bodySmall,
                                color = RoseLock,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_large_note_button")
                    ) {
                        Text("रद्द करें")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "कृपया नोट्स का शीर्षक लिखें!"
                                return@Button
                            }
                            if (isLocked && passwordInput.isBlank()) {
                                errorMessage = "लॉक चालू है, कृपया पासवर्ड या पिन दर्ज करें!"
                                return@Button
                            }
                            if (fileSize > maxAllowedBytes) {
                                errorMessage = "फाइल 2GB से अधिक है! कृपया 2GB से छोटी फाइल चुनें."
                                return@Button
                            }

                            onSaveNote(
                                title,
                                subject,
                                content,
                                fileName,
                                fileUriString,
                                fileSize,
                                fileMimeType,
                                isLocked,
                                passwordInput,
                                passwordHint
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_large_note_button")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("सेव करें (Save)")
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
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
