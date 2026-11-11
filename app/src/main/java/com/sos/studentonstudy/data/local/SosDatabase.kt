package com.sos.studentonstudy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        RequestEntity::class,
        HelperEntity::class,
        HelpHistoryEntity::class,
        ReplyEntity::class,
        ChatMessageEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class SosDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun requestDao(): RequestDao
    abstract fun helperDao(): HelperDao
    abstract fun replyDao(): ReplyDao
    abstract fun chatDao(): ChatDao

    companion object {
        fun create(context: Context): SosDatabase =
            Room.databaseBuilder(context.applicationContext, SosDatabase::class.java, "sos.db")
                // Versions 1-3 were development builds without exported schemas, so they can't be migrated.
                // From version 4 on every change needs a Migration (see Migrations.kt) so user data survives.
                .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1, 2, 3)
                .addMigrations(*ALL_MIGRATIONS)
                .build()
    }
}
