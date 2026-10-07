package com.sos.studentonstudy.data.local

import androidx.room.Dao
import androidx.room.Delete
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

@Dao
interface RequestDao {
    @Query("SELECT * FROM requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeAll(userId: Long): Flow<List<RequestEntity>>

    @Query("SELECT * FROM requests WHERE id = :id")
    suspend fun getById(id: Long): RequestEntity?

    @Insert
    suspend fun insert(request: RequestEntity): Long

    @Update
    suspend fun update(request: RequestEntity)

    @Delete
    suspend fun delete(request: RequestEntity)
}
