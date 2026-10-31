package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.RepeatMode
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority

class Converters {
    @TypeConverter
    fun fromPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): TaskPriority = TaskPriority.fromString(value)

    @TypeConverter
    fun fromCategory(category: TaskCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): TaskCategory = TaskCategory.fromString(value)

    @TypeConverter
    fun fromRepeatMode(repeatMode: RepeatMode): String = repeatMode.name

    @TypeConverter
    fun toRepeatMode(value: String): RepeatMode = RepeatMode.fromString(value)
}
