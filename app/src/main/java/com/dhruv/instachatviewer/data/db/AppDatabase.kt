package com.dhruv.instachatviewer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dhruv.instachatviewer.data.dao.ChatDao
import com.dhruv.instachatviewer.data.model.ChatEntity
import com.dhruv.instachatviewer.data.model.MessageEntity

@Database(entities = [ChatEntity::class, MessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "instachatviewer.db"
                ).build().also { INSTANCE = it }
            }
    }
}
