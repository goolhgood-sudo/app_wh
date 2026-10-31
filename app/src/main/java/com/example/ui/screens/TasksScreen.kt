package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LoadingLogoView
import com.example.data.model.RepeatMode
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
import com.example.ui.theme.Sky600
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TasksScreen(
    taskViewModel: TaskViewModel,
    onStartPomodoroForTask: (Task) -> Unit
) {
    val tasks by taskViewModel.filteredTasks.collectAsState()
    val allTasks by taskViewModel.allTasks.collectAsState()
    val filterState by taskViewModel.filterState.collectAsState()
    val isLoading by taskViewModel.isLoading.collectAsState()

    var showPriorityMenu by remember { mutableStateOf(false) }

    val hasCompleted = remember(allTasks) { allTasks.any { it.isCompleted } }
    val completedCount = remember(allTasks) { allTasks.count { it.isCompleted } }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolling by androidx.compose.runtime.remember {
        androidx.compose.runtime.derivedStateOf { listState.isScrollInProgress }
    }

    androidx.compose.runtime.LaunchedEffect(isScrolling) {
        taskViewModel.setFabVisible(!isScrolling)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(horizontal = 16.dp)
                .testTag("tasks_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "قائمة المهام",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${tasks.size} مهمة معروضة",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (completedCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Orange50)
                                .border(1.dp, Orange400.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("total_flames_badge")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "شعلات الإنجاز",
                                tint = Orange600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$completedCount شعلة 🔥",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Orange600
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (hasCompleted) {
                        IconButton(
                            onClick = { taskViewModel.deleteCompletedTasks() },
                            modifier = Modifier.testTag("clear_completed_tasks_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "مسح المكتملة",
                                tint = Orange600
                            )
                        }
                    }

                    IconButton(
                        onClick = { /* Filter logic would go here or in a menu */ },
                        modifier = Modifier.testTag("filter_tasks_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "تصفية",
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar (White container with subtle border)
            OutlinedTextField(
                value = filterState.query,
                onValueChange = { taskViewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_search_bar"),
                placeholder = { Text("بحث في المهام...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (filterState.query.isNotEmpty()) {
                        IconButton(onClick = { taskViewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح البحث",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500,
                    unfocusedBorderColor = BorderLight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfacePureWhite,
                    unfocusedContainerColor = SurfacePureWhite
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category & Filter Row (Scrollable horizontally)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "All" filter
                FilterChip(
                    selected = filterState.category == null,
                    onClick = { taskViewModel.setCategoryFilter(null) },
                    label = { Text("الكل", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Orange500,
                        selectedLabelColor = Color.White,
                        containerColor = SurfacePureWhite,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filterState.category == null,
                        borderColor = if (filterState.category == null) Orange500 else BorderLight
                    )
                )

                TaskCategory.entries.forEach { cat ->
                    val isSelected = filterState.category == cat
                    val catColor = when (cat) {
                        TaskCategory.WORK -> CatWork
                        TaskCategory.PERSONAL -> CatPersonal
                        TaskCategory.STUDY -> CatStudy
                        TaskCategory.OTHER -> Color(0xFF8B5CF6)
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { taskViewModel.setCategoryFilter(if (isSelected) null else cat) },
                        label = { Text(cat.displayName, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = catColor,
                            selectedLabelColor = Color.White,
                            containerColor = SurfacePureWhite,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) catColor else BorderLight
                        )
                    )
                }

                // Priority filter toggle button
                Box {
                    FilterChip(
                        selected = filterState.priority != null,
                        onClick = { showPriorityMenu = true },
                        label = {
                            Text(
                                filterState.priority?.displayName ?: "الأولوية",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange50,
                            selectedLabelColor = Orange600,
                            containerColor = SurfacePureWhite,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filterState.priority != null,
                            borderColor = if (filterState.priority != null) Orange500 else BorderLight
                        )
                    )

                    DropdownMenu(
                        expanded = showPriorityMenu,
                        onDismissRequest = { showPriorityMenu = false },
                        modifier = Modifier.background(SurfacePureWhite)
                    ) {
                        DropdownMenuItem(
                            text = { Text("جميع الأولويات", color = TextPrimary) },
                            onClick = {
                                taskViewModel.setPriorityFilter(null)
                                showPriorityMenu = false
                            }
                        )
                        TaskPriority.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.displayName, color = TextPrimary) },
                                onClick = {
                                    taskViewModel.setPriorityFilter(p)
                                    showPriorityMenu = false
                                }
                            )
                        }
                    }
                }

                // Pending only toggle
                FilterChip(
                    selected = filterState.onlyPending,
                    onClick = { taskViewModel.toggleOnlyPendingFilter() },
                    label = { Text("المعلقة فقط", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Sky100,
                        selectedLabelColor = Sky600,
                        containerColor = SurfacePureWhite,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filterState.onlyPending,
                        borderColor = if (filterState.onlyPending) Sky500 else BorderLight
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tasks List
            if (tasks.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = Sky400,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد مهام تطابق هذا الفلتر",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "اضغط على زر (+) البرتقالي لإضافة مهمة جديدة",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleComplete = { taskViewModel.toggleTaskCompletion(task) },
                            onEdit = { taskViewModel.setEditingTask(task) },
                            onDelete = { taskViewModel.deleteTask(task) },
                            onStartPomodoro = { onStartPomodoroForTask(task) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(90.dp)) // Space for bottom bar and FAB
                    }
                }
            }
        }

        // Overlay Loading
        if (isLoading) {
            LoadingLogoView(message = "جارٍ تحميل المهام وتنظيم الإنجازات...")
        }
    }
}

@Composable
private fun TaskItemCard(
    task: Task,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStartPomodoro: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val isOverdue = task.dueDate != null && task.dueDate < System.currentTimeMillis() && !task.isCompleted

    val checkBgColor by animateColorAsState(
        targetValue = if (task.isCompleted) Orange500 else Color.Transparent,
        label = "check_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) BackgroundLight else SurfacePureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 1.5.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isOverdue) PriorityHigh else BorderLight
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Circle (Orange check)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(checkBgColor)
                    .border(
                        2.dp,
                        if (task.isCompleted) Orange500 else BorderLight,
                        CircleShape
                    )
                    .clickable { onToggleComplete() }
                    .testTag("task_checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "مكتملة",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEdit() }
            ) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (task.isCompleted) TextMuted else TextPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Badges row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category pill
                    val catColor = when (task.category) {
                        TaskCategory.WORK -> CatWork
                        TaskCategory.PERSONAL -> CatPersonal
                        TaskCategory.STUDY -> CatStudy
                        TaskCategory.OTHER -> Color(0xFF8B5CF6)
                    }
                    Text(
                        text = task.category.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = catColor,
                        modifier = Modifier
                            .background(catColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    )

                    // Priority dot
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when (task.priority) {
                                        TaskPriority.HIGH -> PriorityHigh
                                        TaskPriority.MEDIUM -> PriorityMedium
                                        TaskPriority.LOW -> PriorityLow
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.priority.displayName,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Due date badge
                    if (task.dueDate != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isOverdue) Orange600 else Sky600,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = formatTaskDueDate(task.dueDate),
                                fontSize = 11.sp,
                                color = if (isOverdue) Orange600 else Sky600,
                                fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Recurring badge
                    if (task.repeatMode != RepeatMode.NONE) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = task.repeatMode.displayName,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Quick Actions: Finish with Flame, Pomodoro & Menu button
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!task.isCompleted) {
                    // زر إنهاء المهمة وكسب الشعلة
                    Button(
                        onClick = onToggleComplete,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange50,
                            contentColor = Orange600
                        ),
                        border = BorderStroke(1.dp, Orange400.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("finish_task_button_${task.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "إنهاء المهمة وكسب شعلة",
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

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onStartPomodoro,
                        modifier = Modifier.size(36.dp).testTag("start_pomodoro_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "بدء تركيز",
                            tint = Orange500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // شعلة الإنجاز المكتملة
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Orange50)
                            .border(1.dp, Orange400.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable { onToggleComplete() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("flame_badge_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "شعلة الإنجاز",
                            tint = Orange600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "شعلة 🔥",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Orange600
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "خيارات",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(SurfacePureWhite)
                    ) {
                        DropdownMenuItem(
                            text = { Text("تعديل", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Sky600) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("حذف المهمة", color = PriorityHigh) },
                            leadingIcon = { Icon(Icons.Default.Clear, contentDescription = null, tint = PriorityHigh) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatTaskDueDate(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val today = Calendar.getInstance()
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.forLanguageTag("ar"))
    val timeStr = timeFormat.format(millis)

    return when {
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "اليوم $timeStr"
        cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR) -> "غداً $timeStr"
        else -> {
            val dateFormat = SimpleDateFormat("d MMM", Locale.forLanguageTag("ar"))
            "${dateFormat.format(millis)} $timeStr"
        }
    }
}
