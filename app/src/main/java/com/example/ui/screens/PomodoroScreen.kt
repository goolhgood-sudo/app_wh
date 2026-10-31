package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.Orange500
import com.example.ui.theme.Orange600
import com.example.ui.theme.PomodoroBreak
import com.example.ui.theme.PomodoroWork
import com.example.ui.theme.Sky100
import com.example.ui.theme.Sky500
import com.example.ui.theme.Sky600
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PomodoroMode
import com.example.ui.viewmodel.PomodoroViewModel
import com.example.ui.viewmodel.TaskViewModel
import java.util.Locale

@Composable
fun PomodoroScreen(
    pomodoroViewModel: PomodoroViewModel,
    taskViewModel: TaskViewModel
) {
    val currentMode by pomodoroViewModel.currentMode.collectAsState()
    val timeLeft by pomodoroViewModel.timeLeftSeconds.collectAsState()
    val totalDuration by pomodoroViewModel.totalDurationSeconds.collectAsState()
    val isRunning by pomodoroViewModel.isRunning.collectAsState()
    val selectedTask by pomodoroViewModel.selectedTask.collectAsState()
    val todaySessions by pomodoroViewModel.todayCompletedSessions.collectAsState()
    val totalFocusMin by pomodoroViewModel.totalFocusMinutes.collectAsState()
    val allTasks by taskViewModel.allTasks.collectAsState()

    var showTaskSelector by remember { mutableStateOf(false) }

    val pendingTasks = allTasks.filter { !it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
            .testTag("pomodoro_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
            text = "مؤقت التركيز",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "عزّز إنتاجيتك بجلسات عمل مقسمة ومدروسة",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mode Switcher Pills (Pure White with Light Border)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfacePureWhite)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PomodoroMode.entries.forEach { mode ->
                val isCurrent = currentMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isCurrent) {
                                if (mode == PomodoroMode.WORK) PomodoroWork else PomodoroBreak
                            } else Color.Transparent
                        )
                        .clickable { pomodoroViewModel.switchMode(mode) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${mode.displayName} (${mode.defaultMinutes}د)",
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCurrent) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Linked Task Selector Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTaskSelector = true }
                .testTag("pomodoro_task_selector"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Task,
                        contentDescription = null,
                        tint = if (selectedTask != null) Sky600 else Orange500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (selectedTask != null) selectedTask!!.title else "اختر مهمة للتركيز عليها",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedTask != null) TextPrimary else TextSecondary
                        )
                        if (selectedTask != null) {
                            Text(
                                text = "تصنيف: ${selectedTask!!.category.displayName}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                if (selectedTask != null) {
                    IconButton(
                        onClick = { pomodoroViewModel.selectTask(null) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء التحديد",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = showTaskSelector,
                onDismissRequest = { showTaskSelector = false },
                modifier = Modifier.background(SurfacePureWhite)
            ) {
                if (pendingTasks.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("لا توجد مهام معلقة", color = TextSecondary) },
                        onClick = { showTaskSelector = false }
                    )
                } else {
                    pendingTasks.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.title, color = TextPrimary) },
                            onClick = {
                                pomodoroViewModel.selectTask(t)
                                showTaskSelector = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Circular Timer Gauge (Sky Blue Track + Vibrant Orange Arc)
        val progressFraction = if (totalDuration > 0) timeLeft.toFloat() / totalDuration.toFloat() else 0f
        val animatedProgress by animateFloatAsState(
            targetValue = progressFraction,
            animationSpec = tween(500),
            label = "timer_progress"
        )

        val minutes = timeLeft / 60
        val seconds = timeLeft % 60
        val timeString = String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)

        val activeColor = if (currentMode == PomodoroMode.WORK) PomodoroWork else PomodoroBreak

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background Track
                drawCircle(
                    color = Sky100,
                    radius = size.minDimension / 2 - 16.dp.toPx(),
                    style = Stroke(width = 16.dp.toPx())
                )
                // Progress Arc
                drawArc(
                    color = activeColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeString,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isRunning) {
                        if (currentMode == PomodoroMode.WORK) "حان وقت الإنجاز والتركيز" else "استرخِ وتنفس بعمق"
                    } else {
                        "جاهز للبدء"
                    },
                    fontSize = 13.sp,
                    color = if (isRunning) activeColor else TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Controls (Reset, Play/Pause, Skip)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            IconButton(
                onClick = { pomodoroViewModel.reset() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SurfacePureWhite)
                    .testTag("pomodoro_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "إعادة الضبط",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Main Play/Pause Button (Vibrant Orange)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(activeColor)
                    .clickable {
                        if (isRunning) pomodoroViewModel.pause() else pomodoroViewModel.start()
                    }
                    .testTag("pomodoro_play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "إيقاف مؤقت" else "بدء",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Skip Button
            IconButton(
                onClick = { pomodoroViewModel.skip() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SurfacePureWhite)
                    .testTag("pomodoro_skip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "تخطي الجلسة",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Daily Summary Stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$todaySessions", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Orange600)
                    Text(text = "جلسات اليوم", fontSize = 12.sp, color = TextSecondary)
                }
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(BorderLight))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${totalFocusMin ?: 0} دقيقة", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Sky600)
                    Text(text = "إجمالي التركيز", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }
}
