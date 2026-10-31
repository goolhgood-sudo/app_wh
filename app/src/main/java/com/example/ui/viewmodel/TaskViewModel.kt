package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.InjazApplication
import com.example.alarm.AlarmScheduler
import com.example.data.model.RepeatMode
import com.example.data.model.Task
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.nlp.ArabicNlpParser
import com.example.nlp.ParsedTaskResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskFilterState(
    val category: TaskCategory? = null,
    val priority: TaskPriority? = null,
    val query: String = "",
    val onlyPending: Boolean = false
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as InjazApplication).repository

    private val _filterState = MutableStateFlow(TaskFilterState())
    val filterState: StateFlow<TaskFilterState> = _filterState

    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask

    private val _isAddSheetOpen = MutableStateFlow(false)
    val isAddSheetOpen: StateFlow<Boolean> = _isAddSheetOpen

    private val _isFabVisible = MutableStateFlow(true)
    val isFabVisible: StateFlow<Boolean> = _isFabVisible

    fun setFabVisible(visible: Boolean) {
        if (_isFabVisible.value != visible) {
            _isFabVisible.value = visible
        }
    }

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            repository.allTasks.collect {
                _isLoading.value = false
            }
        }
    }

    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .flowOn(kotlinx.coroutines.Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val filteredTasks: StateFlow<List<Task>> = combine(repository.allTasks, _filterState) { list, filter ->
        list.filter { task ->
            val matchQuery = filter.query.isBlank() || task.title.contains(filter.query, ignoreCase = true) ||
                    task.description.contains(filter.query, ignoreCase = true)
            val matchCategory = filter.category == null || task.category == filter.category
            val matchPriority = filter.priority == null || task.priority == filter.priority
            val matchPending = !filter.onlyPending || !task.isCompleted
            matchQuery && matchCategory && matchPriority && matchPending
        }
    }
        .flowOn(kotlinx.coroutines.Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun setCategoryFilter(category: TaskCategory?) {
        _filterState.value = _filterState.value.copy(category = category)
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        _filterState.value = _filterState.value.copy(priority = priority)
    }

    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun toggleOnlyPendingFilter() {
        _filterState.value = _filterState.value.copy(onlyPending = !_filterState.value.onlyPending)
    }

    fun openAddSheet() {
        _isAddSheetOpen.value = true
    }

    fun closeAddSheet() {
        _isAddSheetOpen.value = false
    }

    fun setEditingTask(task: Task?) {
        _editingTask.value = task
    }

    fun parseArabicInput(text: String): ParsedTaskResult {
        return ArabicNlpParser.parse(text)
    }

    fun addTask(
        title: String,
        description: String = "",
        category: TaskCategory = TaskCategory.WORK,
        priority: TaskPriority = TaskPriority.MEDIUM,
        dueDate: Long? = null,
        repeatMode: RepeatMode = RepeatMode.NONE
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = Task(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                dueDate = dueDate,
                repeatMode = repeatMode,
                createdAt = System.currentTimeMillis()
            )
            val newId = repository.insertTask(task)
            if (dueDate != null) {
                AlarmScheduler.scheduleTaskReminders(getApplication(), task.copy(id = newId))
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            if (task.dueDate != null && !task.isCompleted) {
                AlarmScheduler.scheduleTaskReminders(getApplication(), task)
            } else {
                AlarmScheduler.cancelTaskReminders(getApplication(), task.id)
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            if (!task.isCompleted) {
                AlarmScheduler.cancelTaskReminders(getApplication(), task.id)
            } else if (task.dueDate != null) {
                AlarmScheduler.scheduleTaskReminders(getApplication(), task.copy(isCompleted = false))
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            AlarmScheduler.cancelTaskReminders(getApplication(), task.id)
            repository.deleteTask(task)
        }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            repository.deleteCompletedTasks()
        }
    }
}
