package com.sos.studentonstudy.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room migrations. Version 4 is the baseline (its schema is exported to app/schemas/).
 * When the schema changes: bump `version` in [SosDatabase], write a `Migration(from, to)` that
 * alters the tables with plain SQL, and add it to [ALL_MIGRATIONS]. Never edit an old migration.
 *
 * Example for a future version 5 that adds a column:
 * ```
 * val MIGRATION_4_5 = object : Migration(4, 5) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE requests ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
 *     }
 * }
 * ```
 */
/** v5: users.remoteId / requests.remoteId link cached rows to their Supabase records. */
internal val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE users ADD COLUMN remoteId TEXT")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_remoteId ON users (remoteId)")
        db.execSQL("ALTER TABLE requests ADD COLUMN remoteId INTEGER")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_requests_remoteId ON requests (remoteId)")
    }
}

internal val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_4_5)
