package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.database.InjazDatabase
import com.example.data.repository.TaskRepository

class InjazApplication : Application() {

    val database by lazy { InjazDatabase.getDatabase(this) }
    val repository by lazy { TaskRepository(database.taskDao(), database.pomodoroDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                "تنبيهات المهام",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات تذكير بمواعيد المهام واستحقاقها"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_REMINDERS_ID = "injaz_task_reminders"
    }
}
