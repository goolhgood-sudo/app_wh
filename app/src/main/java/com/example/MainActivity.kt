package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddTaskBottomSheet
import com.example.ui.components.EditTaskDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.PomodoroScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Orange100
import com.example.ui.theme.Orange500
import com.example.ui.theme.Orange600
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.PomodoroViewModel
import com.example.ui.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MakkiTasksApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakkiTasksApp(
    taskViewModel: TaskViewModel = viewModel(),
    pomodoroViewModel: PomodoroViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {
    // Current Navigation Tab: 0 = Analytics (Default), 1 = Tasks, 2 = Pomodoro
    var currentTab by remember { mutableIntStateOf(0) }

    val isAddSheetOpen by taskViewModel.isAddSheetOpen.collectAsState()
    val editingTask by taskViewModel.editingTask.collectAsState()
    val isFabVisible by taskViewModel.isFabVisible.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Hardware Back Button navigation
    if (currentTab != 0) {
        BackHandler {
            currentTab = 0
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        bottomBar = {
            NavigationBar(
                containerColor = SurfacePureWhite,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .border(0.8.dp, BorderLight)
                    .testTag("main_navigation_bar")
            ) {
                // Tab 0: Analytics
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "التحليلات",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "التحليلات",
                            fontSize = 12.sp,
                            fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Orange600,
                        selectedTextColor = Orange600,
                        indicatorColor = Orange100,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_analytics_tab")
                )

                // Tab 1: Tasks
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "المهام",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "المهام",
                            fontSize = 12.sp,
                            fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Orange600,
                        selectedTextColor = Orange600,
                        indicatorColor = Orange100,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tasks_tab")
                )

                // Tab 2: Pomodoro Focus
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "التركيز",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "التركيز",
                            fontSize = 12.sp,
                            fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Orange600,
                        selectedTextColor = Orange600,
                        indicatorColor = Orange100,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_pomodoro_tab")
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible && currentTab != 2,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight * 2 },
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(150)),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight * 2 },
                    animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)
                ) + fadeOut(animationSpec = tween(150))
            ) {
                FloatingActionButton(
                    onClick = { taskViewModel.openAddSheet() },
                    containerColor = Orange500,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp
                    ),
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .size(58.dp)
                        .testTag("main_add_task_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة مهمة جديدة",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(durationMillis = 100),
                label = "screen_crossfade"
            ) { tab ->
                when (tab) {
                    0 -> AnalyticsScreen(
                        analyticsViewModel = analyticsViewModel,
                        onNavigateToTasks = { currentTab = 1 },
                        onNavigateToPomodoro = { currentTab = 2 },
                        onCompleteTask = { task -> taskViewModel.toggleTaskCompletion(task) },
                        onScrollChanged = { isScrolling -> taskViewModel.setFabVisible(!isScrolling) }
                    )
                    1 -> TasksScreen(
                        taskViewModel = taskViewModel,
                        onStartPomodoroForTask = { task ->
                            pomodoroViewModel.selectTask(task)
                            currentTab = 2
                        }
                    )
                    2 -> PomodoroScreen(
                        pomodoroViewModel = pomodoroViewModel,
                        taskViewModel = taskViewModel
                    )
                }
            }
        }

        // Add Task Bottom Sheet
        if (isAddSheetOpen) {
            AddTaskBottomSheet(
                sheetState = sheetState,
                onDismiss = { taskViewModel.closeAddSheet() },
                onAddTask = { title, description, category, priority, dueDate, repeatMode ->
                    taskViewModel.addTask(
                        title = title,
                        description = description,
                        category = category,
                        priority = priority,
                        dueDate = dueDate,
                        repeatMode = repeatMode
                    )
                }
            )
        }

        // Edit Task Dialog
        editingTask?.let { task ->
            EditTaskDialog(
                task = task,
                onDismiss = { taskViewModel.setEditingTask(null) },
                onSave = { updatedTask -> taskViewModel.updateTask(updatedTask) },
                onDelete = { taskToDelete ->
                    taskViewModel.deleteTask(taskToDelete)
                    taskViewModel.setEditingTask(null)
                }
            )
        }
    }
}
