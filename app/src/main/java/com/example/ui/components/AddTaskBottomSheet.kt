package com.example.ui.components

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.RepeatMode
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.nlp.ArabicNlpParser
import com.example.nlp.ParsedTaskResult
import com.example.speech.VoiceInputHelper
import com.example.speech.VoiceState
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CatPersonal
import com.example.ui.theme.CatStudy
import com.example.ui.theme.CatWork
import com.example.ui.theme.Orange100
import com.example.ui.theme.Orange50
import com.example.ui.theme.Orange500
import com.example.ui.theme.Orange600
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.Sky100
import com.example.ui.theme.Sky500
import com.example.ui.theme.Sky50
import com.example.ui.theme.Sky600
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAddTask: (
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        dueDate: Long?,
        repeatMode: RepeatMode
    ) -> Unit
) {
    val context = LocalContext.current
    val voiceHelper = remember { VoiceInputHelper(context) }
    val voiceState by voiceHelper.voiceState.collectAsState()
    val soundLevel by voiceHelper.soundLevel.collectAsState()

    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.WORK) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var dueDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedRepeatMode by remember { mutableStateOf(RepeatMode.NONE) }
    var nlpResult by remember { mutableStateOf<ParsedTaskResult?>(null) }
    var hasUserManuallyEditedDate by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.stopListening()
        }
    }

    // Permission launcher for microphone
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceHelper.startListening { spokenText ->
                taskTitle = spokenText
            }
        }
    }

    // Live NLP parsing whenever taskTitle changes
    LaunchedEffect(taskTitle) {
        if (taskTitle.isNotBlank()) {
            val parsed = ArabicNlpParser.parse(taskTitle)
            nlpResult = parsed
            if (!hasUserManuallyEditedDate && parsed.dueDate != null) {
                dueDateMillis = parsed.dueDate
                selectedPriority = parsed.priority
                selectedCategory = parsed.category
            }
        } else {
            nlpResult = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            voiceHelper.stopListening()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = SurfacePureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تذكير سريع جديد",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Orange600
                )
                IconButton(
                    onClick = {
                        voiceHelper.stopListening()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("close_add_task_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main input field with Mic Button
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                placeholder = {
                    Text(
                        text = "مثال: اجتماع عمل غداً 5 مساءً عاجل",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                },
                trailingIcon = {
                    val isListening = voiceState is VoiceState.Listening
                    val pulseScale by animateFloatAsState(
                        targetValue = if (isListening) 1f + soundLevel * 0.4f else 1f,
                        animationSpec = tween(150),
                        label = "mic_scale"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(44.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(if (isListening) PriorityHigh else Orange500)
                            .clickable {
                                if (isListening) {
                                    voiceHelper.stopListening()
                                } else {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (granted) {
                                        voiceHelper.startListening { spokenText ->
                                            taskTitle = spokenText
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .testTag("voice_input_mic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "تسجيل بالصوت",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500,
                    unfocusedBorderColor = BorderLight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = BackgroundLight,
                    unfocusedContainerColor = BackgroundLight
                ),
                shape = RoundedCornerShape(16.dp),
                maxLines = 3
            )

            // Voice Listening Status Banner
            AnimatedVisibility(visible = voiceState is VoiceState.Listening) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Orange50, RoundedCornerShape(12.dp))
                        .border(1.dp, Orange100, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Orange500)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جارٍ الاستماع باللغة العربية... تحدث الآن",
                        color = Orange600,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Reminder Time Selector
            Text(
                text = "موعد التذكير",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (dueDateMillis != null) Orange50 else BackgroundLight)
                        .border(
                            2.dp,
                            if (dueDateMillis != null) Orange500 else BorderLight,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            showDateTimePicker(context, dueDateMillis) { chosenMillis ->
                                dueDateMillis = chosenMillis
                                hasUserManuallyEditedDate = true
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("due_date_time_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "التاريخ",
                            tint = if (dueDateMillis != null) Orange600 else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (dueDateMillis != null) formatDateTime(dueDateMillis!!) else "اختر الوقت والتاريخ...",
                            fontSize = 15.sp,
                            color = if (dueDateMillis != null) TextPrimary else TextSecondary,
                            fontWeight = if (dueDateMillis != null) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                if (dueDateMillis != null) {
                    IconButton(
                        onClick = {
                            dueDateMillis = null
                            hasUserManuallyEditedDate = true
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BackgroundLight)
                            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "مسح الموعد", tint = TextSecondary)
                    }
                }
            }

            // Smart NLP Detection Tag
            AnimatedVisibility(visible = nlpResult?.matchedDateSummary != null) {
                nlpResult?.matchedDateSummary?.let { summary ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(Sky50, RoundedCornerShape(12.dp))
                            .border(1.dp, Sky100, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "استخراج ذكي",
                            tint = Sky600,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تم ضبط الوقت تلقائياً: $summary",
                            color = Sky600,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Expandable/Secondary options
            var showAdvanced by remember { mutableStateOf(false) }
            Text(
                text = if (showAdvanced) "خيارات متقدمة (أقل)" else "خيارات إضافية (تصنيف، أولوية...)",
                fontSize = 12.sp,
                color = Sky600,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { showAdvanced = !showAdvanced }
                    .padding(vertical = 4.dp)
            )

            AnimatedVisibility(visible = showAdvanced) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Category Chips
                    Text(
                        text = "التصنيف",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TaskCategory.entries.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Orange500,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Priority Chips
                    Text(
                        text = "الأولوية",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TaskPriority.entries.forEach { prio ->
                            val isSelected = selectedPriority == prio
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPriority = prio },
                                label = { Text(prio.displayName, fontSize = 11.sp) },
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recurrence Row
            Text(
                text = "التكرار",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RepeatMode.entries.forEach { mode ->
                    val isSelected = selectedRepeatMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedRepeatMode = mode },
                        label = { Text(mode.displayName, fontSize = 12.sp) },
                        leadingIcon = if (mode != RepeatMode.NONE) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Sky100,
                            selectedLabelColor = Sky600,
                            containerColor = BackgroundLight,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Sky500 else BorderLight
                        ),
                        modifier = Modifier.testTag("repeat_chip_${mode.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button (Vibrant Orange)
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        val finalTitle = nlpResult?.cleanTitle?.ifBlank { taskTitle.trim() } ?: taskTitle.trim()
                        onAddTask(
                            finalTitle,
                            taskDescription,
                            selectedCategory,
                            selectedPriority,
                            dueDateMillis,
                            selectedRepeatMode
                        )
                        voiceHelper.stopListening()
                        onDismiss()
                    }
                },
                enabled = taskTitle.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_add_task_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange500,
                    disabledContainerColor = BorderLight
                )
            ) {
                Text(
                    text = "حفظ المهمة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun showDateTimePicker(context: Context, currentMillis: Long?, onSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply {
        if (currentMillis != null) timeInMillis = currentMillis
    }

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val timePicker = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    onSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            timePicker.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
    datePicker.show()
}

private fun formatDateTime(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val today = Calendar.getInstance()
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

    val timeFormat = SimpleDateFormat("h:mm a", Locale("ar"))
    val timeStr = timeFormat.format(millis)

    return when {
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "اليوم، $timeStr"
        cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR) -> "غداً، $timeStr"
        else -> {
            val dateFormat = SimpleDateFormat("d MMM، h:mm a", Locale("ar"))
            dateFormat.format(millis)
        }
    }
}
