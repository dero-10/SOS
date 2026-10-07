package com.sos.studentonstudy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, RequestEntity::class], version = 2, exportSchema = false)
abstract class SosDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun requestDao(): RequestDao

    companion object {
        fun create(context: Context): SosDatabase =
            Room.databaseBuilder(context.applicationContext, SosDatabase::class.java, "sos.db")
                // Pre-release app: rebuild instead of migrating when the schema changes.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
