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
    val linkedinUrl: String = "",
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
        ),
        ForeignKey(
            entity = HelperEntity::class,
            parentColumns = ["id"],
            childColumns = ["helperId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("userId"), Index("helperId")]
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
    val createdAt: Long = System.currentTimeMillis(),
    /** Set when the request was sent privately to one helper via chat; such requests stay out of the Explore feed. */
    val helperId: Long? = null
)

fun RequestEntity.tagList(): List<String> = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

/** A student who offers help (Explore → Helper tab and helper profile). */
@Entity(tableName = "helpers")
data class HelperEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rating: Double,
    val helps: Int,
    val requests: Int,
    val reviews: Int,
    /** e.g. "Product Design, Universitas Pelita Harapan". */
    val education: String,
    /** Comma-separated skills, e.g. "Design,UI/UX". */
    val skills: String,
    val linkedinUrl: String,
    /** True for "Online", false for "Online today". */
    val online: Boolean,
    val portfolio: String
)

fun HelperEntity.skillList(): List<String> = skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }

/** A finished help shown under "Recent Helps" on a helper profile. */
@Entity(
    tableName = "help_history",
    foreignKeys = [
        ForeignKey(
            entity = HelperEntity::class,
            parentColumns = ["id"],
            childColumns = ["helperId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("helperId")]
)
data class HelpHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val helperId: Long,
    val title: String,
    val stars: Int,
    val tags: String,
    val completedAt: Long
)

fun HelpHistoryEntity.tagList(): List<String> = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

/** A reply on a request, written by a helper (seeded) or by the logged-in user. Exactly one of helperId/userId is set. */
@Entity(
    tableName = "replies",
    foreignKeys = [
        ForeignKey(entity = RequestEntity::class, parentColumns = ["id"], childColumns = ["requestId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = HelperEntity::class, parentColumns = ["id"], childColumns = ["helperId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("requestId"), Index("helperId"), Index("userId")]
)
data class ReplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: Long,
    val helperId: Long? = null,
    val userId: Long? = null,
    val message: String,
    /** Portfolio link or attached file name. */
    val link: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/** One chat bubble between the logged-in user and a helper. */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = HelperEntity::class, parentColumns = ["id"], childColumns = ["helperId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId", "helperId")]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val helperId: Long,
    val text: String,
    val attachmentName: String? = null,
    val fromMe: Boolean,
    /** True for the bubble created when a request is sent to the helper. */
    val isRequest: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

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
