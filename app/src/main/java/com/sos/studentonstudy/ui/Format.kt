package com.sos.studentonstudy.ui

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

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
