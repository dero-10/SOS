package com.sos.studentonstudy.testutil

import com.sos.studentonstudy.data.local.ChatPreview
import com.sos.studentonstudy.data.local.HelperEntity

fun helper(id: Long, name: String) = HelperEntity(
    id = id, name = name, rating = 4.5, helps = 10, requests = 12, reviews = 8,
    education = "Computer Science, UPH", skills = "Kotlin,Compose", linkedinUrl = "", online = true, portfolio = ""
)

fun chat(helperId: Long, name: String, text: String = "Hi", at: Long = 1_000L) =
    ChatPreview(helperId = helperId, helperName = name, lastText = text, isRequest = false, createdAt = at)
