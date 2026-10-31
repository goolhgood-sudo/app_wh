package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.ui.components.LoadingLogoView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CatPersonal
import com.example.ui.theme.CatStudy
import com.example.ui.theme.CatWork
import com.example.ui.theme.Orange100
import com.example.ui.theme.Orange400
import com.example.ui.theme.Orange50
import com.example.ui.theme.Orange500
import com.example.ui.theme.Orange600
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.Sky100
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Sky50
import com.example.ui.theme.Sky600
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnalyticsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    analyticsViewModel: AnalyticsViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToPomodoro: () -> Unit,
    onCompleteTask: (Task) -> Unit,
    onScrollChanged: (Boolean) -> Unit = {}
) {
    val state by analyticsViewModel.analyticsState.collectAsState()
    val isLoading by analyticsViewModel.isLoading.collectAsState()
    val todayDateFormatted = remember { SimpleDateFormat("EEEE، d MMMM", Locale.forLanguageTag("ar")).format(Date()) }

    Box(modifier = Modifier.fillMaxSize()) {
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        val isScrolling by remember {
            androidx.compose.runtime.derivedStateOf { listState.isScrollInProgress }
        }

        androidx.compose.runtime.LaunchedEffect(isScrolling) {
            onScrollChanged(isScrolling)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(horizontal = 16.dp)
                .testTag("analytics_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "لوحة الإنتاجية",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = todayDateFormatted,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Streak Badge (Vibrant Orange)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Orange50)
                        .border(1.dp, Orange400.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "سلسلة الإنجاز",
                        tint = Orange600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${state.currentStreakDays} أيام",
                        color = Orange600,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Circular Completion Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("completion_gauge_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "معدل الإنجاز الإجمالي",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val animatedProgress by animateFloatAsState(
                        targetValue = state.completionRate / 100f,
                        animationSpec = tween(800),
                        label = "progress"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(140.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Sky Blue track
                            drawCircle(
                                color = Sky100,
                                radius = size.minDimension / 2 - 12.dp.toPx(),
                                style = Stroke(width = 14.dp.toPx())
                            )
                            // Vibrant Orange progress arc
                            drawArc(
                                color = Orange500,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.completionRate.toInt()}%",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "مكتملة",
                                fontSize = 12.sp,
                                color = Sky600,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatPill(
                            title = "شعلات الإنجاز 🔥",
                            value = "${state.completedTasks}",
                            color = Orange600
                        )
                        StatPill(
                            title = "المعلقة",
                            value = "${state.pendingTasks}",
                            color = Sky600
                        )
                        StatPill(
                            title = "الإجمالي",
                            value = "${state.totalTasks}",
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Productivity & Focus Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Focus Time Card (Orange Theme)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToPomodoro() }
                        .testTag("focus_stat_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Orange50),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Orange600,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${state.focusMinutesTotal} دقيقة",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "جلسات التركيز (${state.completedPomodoroSessions} جلسة)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Efficiency Card (Sky Blue Theme)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTasks() }
                        .testTag("tasks_quick_link_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Sky100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Sky600,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${state.monthlyCompletionRate.toInt()}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "إنجاز الشهر الحالي",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Weekly Activity Chart Card (Sky Blue Bars)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_chart_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "نشاط الإنجاز الأسبوعي (آخر 7 أيام)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        state.weeklyStats.forEach { stat ->
                            val maxCount = state.weeklyStats.maxOfOrNull { it.totalCount } ?: 1
                            val fraction = (stat.completedCount.toFloat() / maxOf(maxCount, 1).toFloat()).coerceIn(0.1f, 1f)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${stat.completedCount}",
                                    fontSize = 11.sp,
                                    color = if (stat.completedCount > 0) Sky600 else TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .height((80 * fraction).dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (stat.completedCount > 0) Sky500 else Sky100)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stat.dayLabel.take(3),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "توزيع المهام حسب التصنيف",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TaskCategory.entries.forEach { cat ->
                        val count = state.categoryDistribution[cat] ?: 0
                        val percentage = if (state.totalTasks > 0) (count.toFloat() / state.totalTasks.toFloat()) else 0f
                        val catColor = when (cat) {
                            TaskCategory.WORK -> CatWork
                            TaskCategory.PERSONAL -> CatPersonal
                            TaskCategory.STUDY -> CatStudy
                            TaskCategory.OTHER -> Color(0xFF8B5CF6)
                        }

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat.displayName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("$count مهمة (${(percentage * 100).toInt()}%)", color = TextSecondary, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { percentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = catColor,
                                trackColor = BorderLight
                            )
                        }
                    }
                }
            }
        }

        // Urgent Pending Tasks Section
        if (state.urgentPendingTasks.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مهام هامة بانتظار الإنجاز",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "عرض الكل",
                            fontSize = 13.sp,
                            color = Orange600,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToTasks() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    state.urgentPendingTasks.forEach { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (task.priority) {
                                                        TaskPriority.HIGH -> PriorityHigh
                                                        TaskPriority.MEDIUM -> PriorityMedium
                                                        TaskPriority.LOW -> PriorityLow
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = task.category.displayName,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // زر إنهاء وكسب شعلة
                                Button(
                                    onClick = { onCompleteTask(task) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Orange50,
                                        contentColor = Orange600
                                    ),
                                    border = BorderStroke(1.dp, Orange400.copy(alpha = 0.6f)),
                                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("finish_urgent_task_${task.id}")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = "إنهاء وشعلة",
                                            tint = Orange600,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "إنهاء",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Orange600
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
                Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar & FAB
            }
        }

        if (isLoading) {
            LoadingLogoView(message = "جارٍ تجهيز الإحصائيات والشعلات...")
        }
    }
}

@Composable
private fun StatPill(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = title, fontSize = 12.sp, color = TextSecondary)
    }
}
