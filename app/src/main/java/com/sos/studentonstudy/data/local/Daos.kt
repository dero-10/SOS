package com.sos.studentonstudy.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observe(id: Long): Flow<UserEntity?>

    @Query("UPDATE users SET name = :name, major = :major, university = :university WHERE id = :id")
    suspend fun updateProfile(id: Long, name: String, major: String, university: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}

/** A request with its author's name and how many replies it has. */
data class RequestWithMeta(
    @Embedded val request: RequestEntity,
    val authorName: String,
    val replyCount: Int
)

private const val REQUEST_WITH_META =
    "SELECT r.*, u.name AS authorName, (SELECT COUNT(*) FROM replies WHERE requestId = r.id) AS replyCount " +
        "FROM requests r JOIN users u ON u.id = r.userId"

@Dao
interface RequestDao {
    /** Public requests from everyone; privately sent requests are excluded. */
    @Query("$REQUEST_WITH_META WHERE r.helperId IS NULL ORDER BY r.createdAt DESC")
    fun observeFeed(): Flow<List<RequestWithMeta>>

    @Query("$REQUEST_WITH_META WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    fun observeOwnWithMeta(userId: Long): Flow<List<RequestWithMeta>>

    @Query("$REQUEST_WITH_META WHERE r.id = :id")
    fun observeWithMeta(id: Long): Flow<RequestWithMeta?>

    @Query("SELECT * FROM requests WHERE id = :id")
    suspend fun getById(id: Long): RequestEntity?

    @Insert
    suspend fun insert(request: RequestEntity): Long

    @Update
    suspend fun update(request: RequestEntity)

    @Delete
    suspend fun delete(request: RequestEntity)
}

@Dao
interface HelperDao {
    @Insert
    suspend fun insert(helper: HelperEntity): Long

    @Insert
    suspend fun insertHistory(history: HelpHistoryEntity)

    @Query("SELECT * FROM helpers ORDER BY online DESC, id ASC")
    fun observeAll(): Flow<List<HelperEntity>>

    @Query("SELECT * FROM helpers WHERE id = :id")
    fun observe(id: Long): Flow<HelperEntity?>

    @Query("SELECT * FROM help_history WHERE helperId = :helperId ORDER BY completedAt DESC")
    fun observeHistory(helperId: Long): Flow<List<HelpHistoryEntity>>
}

/** A reply joined with its author; [rating] and [helps] are only set for helpers. */
data class ReplyWithAuthor(
    val id: Long,
    val requestId: Long,
    val message: String,
    val link: String?,
    val createdAt: Long,
    val authorName: String,
    val rating: Double?,
    val helps: Int?,
    val isHelper: Boolean
)

/** A reply the logged-in user wrote, with the request it belongs to. */
data class MyReply(val replyId: Long, val requestId: Long, val requestTitle: String, val createdAt: Long)

@Dao
interface ReplyDao {
    @Insert
    suspend fun insert(reply: ReplyEntity): Long

    @Query(
        "SELECT r.id, r.requestId, r.message, r.link, r.createdAt, COALESCE(h.name, u.name, 'Unknown') AS authorName, " +
            "h.rating AS rating, h.helps AS helps, (r.helperId IS NOT NULL) AS isHelper " +
            "FROM replies r LEFT JOIN helpers h ON h.id = r.helperId LEFT JOIN users u ON u.id = r.userId " +
            "WHERE r.requestId = :requestId ORDER BY r.createdAt ASC, r.id ASC"
    )
    fun observeForRequest(requestId: Long): Flow<List<ReplyWithAuthor>>

    @Query(
        "SELECT r.id AS replyId, r.requestId AS requestId, q.title AS requestTitle, r.createdAt AS createdAt " +
            "FROM replies r JOIN requests q ON q.id = r.requestId WHERE r.userId = :userId ORDER BY r.createdAt DESC"
    )
    fun observeByUser(userId: Long): Flow<List<MyReply>>
}

/** Last message of one conversation, for the chat list. */
data class ChatPreview(val helperId: Long, val helperName: String, val lastText: String, val isRequest: Boolean, val createdAt: Long)

@Dao
interface ChatDao {
    @Insert
    suspend fun insert(message: ChatMessageEntity): Long

    @Query(
        "SELECT h.id AS helperId, h.name AS helperName, m.text AS lastText, m.isRequest AS isRequest, m.createdAt AS createdAt " +
            "FROM chat_messages m JOIN helpers h ON h.id = m.helperId " +
            "WHERE m.userId = :userId AND m.id = (SELECT MAX(id) FROM chat_messages WHERE userId = :userId AND helperId = m.helperId) " +
            "ORDER BY m.createdAt DESC"
    )
    fun observePreviews(userId: Long): Flow<List<ChatPreview>>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND helperId = :helperId ORDER BY createdAt ASC, id ASC")
    fun observeMessages(userId: Long, helperId: Long): Flow<List<ChatMessageEntity>>
}
