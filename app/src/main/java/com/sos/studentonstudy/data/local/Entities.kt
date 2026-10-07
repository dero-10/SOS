package com.sos.studentonstudy.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val major: String = "",
    val university: String = "",
    val balance: Long = STARTING_BALANCE
) {
    companion object {
        const val STARTING_BALANCE = 250_000L
    }
}

/** A help request posted by a student (the CRUD module). */
@Entity(
    tableName = "requests",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class RequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val details: String,
    /** Comma-separated tags, e.g. "Design,UI/UX". */
    val tags: String,
    val estimatedTime: Int,
    /** UTC midnight of the deadline day. */
    val deadline: Long,
    val budget: Long,
    val attachmentName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

fun RequestEntity.tagList(): List<String> = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

object EstimatedTime {
    const val FAST = 0
    const val MODERATE = 1
    const val EXTENDED = 2
    val all = listOf(FAST, MODERATE, EXTENDED)

    fun label(value: Int) = when (value) {
        FAST -> "Fast (< 1 hour)"
        MODERATE -> "Moderate (< 1 day)"
        else -> "Extended (>1 day)"
    }
}

object RequestTags {
    val all = listOf("Design", "UI/UX", "Graphic Design", "Illustration", "Programming", "Writing", "Math", "3D Modeling")
}
