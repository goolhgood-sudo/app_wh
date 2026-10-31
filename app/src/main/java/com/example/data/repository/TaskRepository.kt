package com.example.data.repository

import com.example.data.dao.PomodoroDao
import com.example.data.dao.TaskDao
import com.example.data.model.PomodoroSession
import com.example.data.model.RepeatMode
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TaskRepository(
    private val taskDao: TaskDao,
    private val pomodoroDao: PomodoroDao
) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allSessions: Flow<List<PomodoroSession>> = pomodoroDao.getAllSessions()

    suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun deleteCompletedTasks() = taskDao.deleteCompletedTasks()

    suspend fun toggleTaskCompletion(task: Task) {
        val newStatus = !task.isCompleted
        val completedTime = if (newStatus) System.currentTimeMillis() else null

        if (newStatus && task.repeatMode != RepeatMode.NONE && task.dueDate != null) {
            // Mark current task as completed
            taskDao.updateTask(task.copy(isCompleted = true, completedAt = completedTime))

            // Calculate next due date
            val nextDueDate = calculateNextDueDate(task.dueDate, task.repeatMode)

            // Spawn next recurring task instance
            val nextTask = task.copy(
                id = 0,
                dueDate = nextDueDate,
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                completedAt = null
            )
            taskDao.insertTask(nextTask)
        } else {
            taskDao.updateTask(task.copy(isCompleted = newStatus, completedAt = completedTime))
        }
    }

    private fun calculateNextDueDate(currentDueDate: Long, repeatMode: RepeatMode): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentDueDate
        }
        when (repeatMode) {
            RepeatMode.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            RepeatMode.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RepeatMode.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            RepeatMode.NONE -> {}
        }
        return calendar.timeInMillis
    }

    suspend fun recordPomodoroSession(taskId: Long?, taskTitle: String?, durationMinutes: Int = 25): Long {
        val session = PomodoroSession(
            taskId = taskId,
            taskTitle = taskTitle,
            durationMinutes = durationMinutes,
            completedAt = System.currentTimeMillis()
        )
        return pomodoroDao.insertSession(session)
    }

    fun getTodaySessionsCount(startOfDay: Long): Flow<Int> = pomodoroDao.getTodaySessionsCount(startOfDay)

    fun getTotalFocusMinutes(): Flow<Int?> = pomodoroDao.getTotalFocusMinutes()
}
