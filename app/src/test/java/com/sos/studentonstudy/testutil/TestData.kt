package com.sos.studentonstudy.testutil

import com.sos.studentonstudy.data.local.ChatPreview
import com.sos.studentonstudy.data.local.HelperEntity
import com.sos.studentonstudy.data.local.RequestEntity
import com.sos.studentonstudy.data.local.RequestWithMeta

fun helper(id: Long, name: String) = HelperEntity(
    id = id, name = name, rating = 4.5, helps = 10, requests = 12, reviews = 8,
    education = "Computer Science, UPH", skills = "Kotlin,Compose", linkedinUrl = "", online = true, portfolio = ""
)

fun chat(helperId: Long, name: String, text: String = "Hi", at: Long = 1_000L) =
    ChatPreview(helperId = helperId, helperName = name, lastText = text, isRequest = false, createdAt = at)

fun request(
    id: Long,
    title: String,
    tags: String = "Kotlin",
    details: String = "Help with coursework",
    budget: Long = 50_000L,
    deadline: Long = 0L,
    createdAt: Long = id
) = RequestWithMeta(
    request = RequestEntity(
        id = id, userId = 1L, title = title, details = details, tags = tags,
        estimatedTime = 1, deadline = deadline, budget = budget, createdAt = createdAt
    ),
    authorName = "Student", replyCount = 0
)
