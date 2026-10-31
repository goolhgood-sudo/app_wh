package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.PomodoroDao
import com.example.data.dao.TaskDao
import com.example.data.model.PomodoroSession
import com.example.data.model.Task

@Database(entities = [Task::class, PomodoroSession::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class InjazDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun pomodoroDao(): PomodoroDao

    companion object {
        @Volatile
        private var INSTANCE: InjazDatabase? = null

        fun getDatabase(context: Context): InjazDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InjazDatabase::class.java,
                    "injaz_tasks.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
