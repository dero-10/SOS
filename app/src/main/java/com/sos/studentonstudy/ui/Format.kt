package com.sos.studentonstudy.ui

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val MINUTE_MILLIS = 60 * 1000L
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS

internal fun formatIdr(amount: Long): String = "IDR " + NumberFormat.getNumberInstance(Locale.US).format(amount)

/** Dates are stored as UTC midnight (what the Material 3 date picker returns), so format them in UTC. */
internal fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(millis)

/** UTC midnight of the local calendar day [days] from today. */
fun daysFromToday(days: Int): Long {
    val local = Calendar.getInstance()
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }
    return utc.timeInMillis + days * DAY_MILLIS
}

/** A request is urgent when its deadline is today or tomorrow. */
internal fun isUrgent(deadline: Long): Boolean = deadline <= daysFromToday(1)

/** Short relative time for chat rows: "Now", "5m", "10h", "5d", "1w". */
internal fun formatTimeAgo(millis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = (now - millis).coerceAtLeast(0)
    return when {
        diff < MINUTE_MILLIS -> "Now"
        diff < HOUR_MILLIS -> "${diff / MINUTE_MILLIS}m"
        diff < DAY_MILLIS -> "${diff / HOUR_MILLIS}h"
        diff < 7 * DAY_MILLIS -> "${diff / DAY_MILLIS}d"
        else -> "${diff / (7 * DAY_MILLIS)}w"
    }
}

/** "2d ago" style label used on helper profiles. */
internal fun formatAgo(millis: Long): String = formatTimeAgo(millis).let { if (it == "Now") "Just now" else "$it ago" }
