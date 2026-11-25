package com.sos.studentonstudy.data.remote

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Columns read for a request, with its author's public profile embedded (see supabase/schema.sql). */
internal const val REQUEST_COLUMNS =
    "id,user_id,helper_id,title,details,tags,estimated_time,deadline,budget,attachment_name,image_path,created_at," +
        "author:profiles!requests_user_id_fkey(id,name,email,major,university,linkedin_url)"

internal const val REQUEST_IMAGES_BUCKET = "request-images"

/** A row of `public.requests` as read by the app. */
@Serializable
data class RequestDto(
    val id: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("helper_id") val helperId: String? = null,
    val title: String,
    val details: String,
    val tags: List<String> = emptyList(),
    @SerialName("estimated_time") val estimatedTime: Int = 0,
    /** ISO date, e.g. "2026-11-20". */
    val deadline: String,
    val budget: Long = 0,
    @SerialName("attachment_name") val attachmentName: String? = null,
    /** Object path inside the [REQUEST_IMAGES_BUCKET] bucket, e.g. "<user uuid>/<file>.jpg". */
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("created_at") val createdAt: Instant,
    val author: ProfileDto? = null
)

/** The columns the app writes when creating or editing a request. */
@Serializable
data class RequestWriteDto(
    @SerialName("user_id") val userId: String,
    val title: String,
    val details: String,
    val tags: List<String>,
    @SerialName("estimated_time") val estimatedTime: Int,
    val deadline: String,
    val budget: Long,
    @SerialName("attachment_name") val attachmentName: String?,
    @SerialName("image_path") val imagePath: String?
)

/** Room keeps deadlines as UTC midnight in millis; Postgres keeps a `date`. */
internal fun deadlineToIso(utcMidnightMillis: Long): String =
    Instant.fromEpochMilliseconds(utcMidnightMillis).toLocalDateTime(TimeZone.UTC).date.toString()

internal fun deadlineFromIso(iso: String): Long =
    LocalDate.parse(iso).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
