package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.InjazApplication
import com.example.MainActivity
import com.example.R
import com.example.alarm.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TASK_REMINDER = "com.example.ACTION_TASK_REMINDER"
        const val ACTION_COMPLETE_TASK = "com.example.ACTION_COMPLETE_TASK"
        const val ACTION_SNOOZE_TASK = "com.example.ACTION_SNOOZE_TASK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val taskId = intent.getLongExtra(AlarmScheduler.EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE) ?: "مهمتك"
        val isAdvance = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_ADVANCE_REMINDER, false)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        when (action) {
            ACTION_TASK_REMINDER -> {
                showReminderNotification(context, notificationManager, taskId, taskTitle, isAdvance)
            }
            ACTION_COMPLETE_TASK -> {
                notificationManager.cancel(taskId.toInt())
                val app = context.applicationContext as? InjazApplication
                if (app != null && taskId > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val task = app.repository.getTaskById(taskId)
                        if (task != null) {
                            app.repository.toggleTaskCompletion(task)
                        }
                    }
                }
            }
            ACTION_SNOOZE_TASK -> {
                notificationManager.cancel(taskId.toInt())
                val app = context.applicationContext as? InjazApplication
                if (app != null && taskId > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val task = app.repository.getTaskById(taskId)
                        if (task != null) {
                            val snoozedTime = System.currentTimeMillis() + (10 * 60 * 1000)
                            AlarmScheduler.scheduleTaskReminders(context, task.copy(dueDate = snoozedTime))
                        }
                    }
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // Re-schedule all active reminders after reboot
                val app = context.applicationContext as? InjazApplication
                if (app != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val pending = app.database.taskDao().getPendingTasksWithDueDate()
                        for (t in pending) {
                            AlarmScheduler.scheduleTaskReminders(context, t)
                        }
                    }
                }
            }
        }
    }

    private fun showReminderNotification(
        context: Context,
        notificationManager: NotificationManager,
        taskId: Long,
        taskTitle: String,
        isAdvance: Boolean
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Complete action
        val completeIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 10 + 3).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 10m action
        val snoozeIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_TASK
            putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId)
            putExtra(AlarmScheduler.EXTRA_TASK_TITLE, taskTitle)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 10 + 4).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "تذكير: مهام المكي"
        val content = taskTitle

        val builder = NotificationCompat.Builder(context, InjazApplication.CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "إتمام ✅", completePendingIntent)
            .addAction(0, "تأجيل ⏰", snoozePendingIntent)

        notificationManager.notify(taskId.toInt(), builder.build())
    }
}
