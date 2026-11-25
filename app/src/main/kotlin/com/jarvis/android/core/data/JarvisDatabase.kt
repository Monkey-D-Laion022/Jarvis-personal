package com.jarvis.android.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChatMessageEntity::class], version = 1, exportSchema = false)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        fun create(context: Context): JarvisDatabase =
            Room.databaseBuilder(context.applicationContext, JarvisDatabase::class.java, "jarvis.db")
                .build()
    }
}
