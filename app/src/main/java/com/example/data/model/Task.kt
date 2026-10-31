package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority(val displayName: String, val level: Int) {
    LOW("منخفضة", 1),
    MEDIUM("متوسطة", 2),
    HIGH("مرتفعة", 3);

    companion object {
        fun fromString(value: String): TaskPriority {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class TaskCategory(val displayName: String) {
    WORK("عمل"),
    PERSONAL("شخصي"),
    STUDY("دراسة"),
    OTHER("أخرى");

    companion object {
        fun fromString(value: String): TaskCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: WORK
        }
    }
}

enum class RepeatMode(val displayName: String) {
    NONE("بدون تكرار"),
    DAILY("يومياً"),
    WEEKLY("أسبوعياً"),
    MONTHLY("شهرياً");

    companion object {
        fun fromString(value: String): RepeatMode {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: NONE
        }
    }
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.WORK,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val durationMinutes: Int = 25,
    val completedAt: Long = System.currentTimeMillis()
)
