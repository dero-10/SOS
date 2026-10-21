package com.sos.studentonstudy.data

import com.sos.studentonstudy.data.local.ChatMessageEntity
import com.sos.studentonstudy.data.local.EstimatedTime
import com.sos.studentonstudy.data.local.HelpHistoryEntity
import com.sos.studentonstudy.data.local.HelperEntity
import com.sos.studentonstudy.data.local.ReplyEntity
import com.sos.studentonstudy.data.local.RequestEntity
import com.sos.studentonstudy.data.local.SosDatabase
import com.sos.studentonstudy.data.local.UserEntity
import com.sos.studentonstudy.data.repository.hashPassword
import com.sos.studentonstudy.ui.daysFromToday
import java.util.UUID

private const val MINUTE = 60 * 1000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR
private const val LINKEDIN = "https://www.linkedin.com"

/**
 * Fills an empty database with the demo account (demo@sos.com / password123), a community of other
 * students and helpers, replies and chats, so every Figma screen has realistic content on first launch.
 */
suspend fun seedDatabase(db: SosDatabase) {
    if (db.userDao().count() > 0) return
    val now = System.currentTimeMillis()

    val demoId = db.userDao().insert(
        UserEntity(
            name = "Patrick Jane",
            email = "demo@sos.com",
            passwordHash = hashPassword("password123"),
            major = "Product Design",
            university = "Universitas Pelita Harapan",
            linkedinUrl = LINKEDIN
        )
    )
    val helpers = seedHelpers(db)
    seedDemoRequests(db, demoId, helpers, now)
    seedCommunityRequests(db, helpers, now)
    seedChats(db, demoId, helpers, now)
}

private suspend fun seedHelpers(db: SosDatabase): Map<String, Long> {
    val dao = db.helperDao()
    val ids = mutableMapOf<String, Long>()

    suspend fun helper(
        name: String, rating: Double, helps: Int, requests: Int, reviews: Int,
        education: String, skills: String, online: Boolean
    ): Long {
        val id = dao.insert(
            HelperEntity(
                name = name, rating = rating, helps = helps, requests = requests, reviews = reviews,
                education = education, skills = skills, linkedinUrl = LINKEDIN, online = online,
                portfolio = "Portfolio${name.replace(" ", "")}.com"
            )
        )
        ids[name] = id
        return id
    }

    // Order matches the Figma Helper tab: Chris, Faith, John (online), Mary, User 2 (online today).
    val chris = helper("Chris", 4.9, 23, 14, 18, "Product Design, Universitas Pelita Harapan", "Design,UI/UX,Graphic Design,Adobe Illustrator,Adobe Photoshop", true)
    val faith = helper("Faith", 4.8, 49, 8, 40, "Visual communication design, Universitas Pelita Harapan", "Design,Graphic Design,Illustration", true)
    val john = helper("John", 4.7, 51, 12, 44, "Product design, University of Somerset", "Design,3D Modeling,Adobe Photoshop", true)
    val mary = helper("Mary", 4.9, 16, 5, 12, "Product design, Universitas Pelita Harapan", "Design,Product design,3D Modeling", false)
    val user2 = helper("User 2", 4.9, 23, 9, 17, "Product design, Universitas Pelita Harapan", "Design,UI/UX", false)
    val abby = helper("Abby", 4.8, 31, 6, 25, "English Literature, Universitas Pelita Harapan", "Writing", false)
    val william = helper("William", 4.6, 27, 10, 20, "Informatics, Universitas Pelita Harapan", "Programming", false)
    val coco = helper("Coco", 4.7, 19, 4, 15, "Mathematics, Universitas Pelita Harapan", "Math", false)
    val billy = helper("Billy", 4.5, 12, 7, 9, "Fine Arts, Universitas Pelita Harapan", "Illustration,Graphic Design", false)

    val now = System.currentTimeMillis()
    suspend fun history(helperId: Long, title: String, tags: String, daysAgo: Int, stars: Int = 5) =
        dao.insertHistory(HelpHistoryEntity(helperId = helperId, title = title, stars = stars, tags = tags, completedAt = now - daysAgo * DAY))

    history(chris, "Modern Landing Page Design", "Design,UI/UX", 2)
    history(chris, "Build landing page", "Design,UI/UX", 7)
    history(chris, "Logo design review", "Design,Graphic Design", 14)
    history(faith, "Poster layout feedback", "Design,Graphic Design", 3)
    history(faith, "Children's book illustration", "Design,Illustration", 10)
    history(john, "3D product mockup", "Design,3D Modeling", 4)
    history(john, "Photo retouching", "Design,Adobe Photoshop", 12, stars = 4)
    history(mary, "Product sketch critique", "Design,Product design", 5)
    history(user2, "Mobile app wireframes", "Design,UI/UX", 6)
    history(abby, "Essay proofreading", "Writing", 2)
    history(william, "Fix Kotlin coroutine bug", "Programming", 3)
    history(coco, "Calculus problem set", "Math", 8)
    history(billy, "Character illustration", "Illustration", 9, stars = 4)
    return ids
}

/** Patrick's own requests, one of which already has replies. */
private suspend fun seedDemoRequests(db: SosDatabase, userId: Long, helpers: Map<String, Long>, now: Long) {
    val dao = db.requestDao()
    val ids = mutableMapOf<String, Long>()

    suspend fun request(title: String, details: String, tags: String, time: Int, deadlineDays: Int, budget: Long, createdAt: Long, helperId: Long? = null, attachment: String? = null) {
        ids[title] = dao.insert(
            RequestEntity(
                userId = userId, title = title, details = details, tags = tags, estimatedTime = time,
                deadline = daysFromToday(deadlineDays), budget = budget, createdAt = createdAt,
                helperId = helperId, attachmentName = attachment
            )
        )
    }

    request(
        "Need help with mobile UI design",
        "I want to create a clean and modern homepage layout but don't know how to structure it properly.",
        "Design,UI/UX", EstimatedTime.FAST, 0, 15_000, now - 3 * HOUR
    )
    request(
        "Make an illustration for a project",
        "Looking for a simple flat illustration for my presentation cover.",
        "Design,Illustration", EstimatedTime.EXTENDED, 7, 50_000, now - 2 * HOUR
    )
    request(
        "Review my Room database schema",
        "Please check whether my entities and relations make sense for a CRUD app.",
        "Programming", EstimatedTime.MODERATE, 3, 25_000, now - HOUR
    )
    // Sent privately to Chris from the chat flow; matches the first conversation in the chat list.
    val chatText = "Hi Chris, could you review my UI/UX assignment before Friday? The brief is attached."
    request(
        chatText.take(60), chatText, "", EstimatedTime.FAST, 2, 50_000, now - MINUTE,
        helperId = helpers.getValue("Chris"), attachment = "ui ux assignment.pdf"
    )

    val replyDao = db.replyDao()
    val repliedRequestId = ids.getValue("Need help with mobile UI design")
    listOf(
        Triple("Chris", "Hello, I can help you! Take a look at my portfolio here", 2 * HOUR),
        Triple("Faith", "I'd be happy to help you, this is my portfolio if you'd like to see my previous works first.", 90 * MINUTE),
        Triple("John", "This is my portfolio.", HOUR),
        Triple("User 2", "Hello, I can help you! Take a look at my portfolio here", 30 * MINUTE)
    ).forEach { (name, message, ago) ->
        replyDao.insert(
            ReplyEntity(requestId = repliedRequestId, helperId = helpers.getValue(name), message = message, link = "Portfolio${name.replace(" ", "")}.com", createdAt = now - ago)
        )
    }
}

/** Requests from other students for the Explore feed, with helper replies. */
private suspend fun seedCommunityRequests(db: SosDatabase, helpers: Map<String, Long>, now: Long) {
    val users = db.userDao()
    val requests = db.requestDao()
    val replies = db.replyDao()

    suspend fun student(name: String): Long = users.insert(
        UserEntity(
            name = name,
            email = "${name.lowercase()}@seed.sos",
            // Nobody knows this password; seeded students can't be logged into.
            passwordHash = hashPassword(UUID.randomUUID().toString()),
            major = "Design",
            university = "Universitas Pelita Harapan"
        )
    )

    suspend fun post(author: String, title: String, details: String, tags: String, time: Int, deadlineDays: Int, budget: Long, ago: Long, replyFrom: List<String>) {
        val id = requests.insert(
            RequestEntity(
                userId = student(author), title = title, details = details, tags = tags, estimatedTime = time,
                deadline = daysFromToday(deadlineDays), budget = budget, createdAt = now - ago
            )
        )
        replyFrom.forEachIndexed { index, helper ->
            replies.insert(
                ReplyEntity(
                    requestId = id, helperId = helpers.getValue(helper),
                    message = "Hi, I can help with this. Here is my portfolio, feel free to check it first.",
                    link = "Portfolio${helper.replace(" ", "")}.com",
                    createdAt = now - ago + (index + 1) * 10 * MINUTE
                )
            )
        }
    }

    post("Joshua", "Need help with mobile UI design", "I want to create a clean and modern homepage layout but don't know how to structure it properly.", "Design,UI/UX", EstimatedTime.FAST, 3, 15_000, 5 * HOUR, listOf("Chris", "Faith", "John", "User 2"))
    post("Sarah", "How to make a UI Kit?", "I'm new to design systems and want to build a reusable UI kit in Figma for my class project.", "Design,UI/UX", EstimatedTime.MODERATE, 1, 15_000, 4 * HOUR, listOf("Faith", "Mary", "Chris"))
    post("Daniel", "Make an illustration for a project", "I need a flat illustration of a study group for my project cover page.", "Design,Illustration", EstimatedTime.EXTENDED, 0, 250_000, 3 * HOUR, listOf("Billy", "Mary"))
    post("Mia", "I need critiques for my final design project", "Looking for honest feedback on the layout and colors of my final design project.", "Design,Graphic Design", EstimatedTime.FAST, 1, 15_000, 2 * HOUR, listOf("Chris"))
    post("Kevin", "Make a landing page for my app", "I have the copy and branding ready and need a responsive landing page designed.", "Design,UI/UX", EstimatedTime.EXTENDED, 14, 100_000, 90 * MINUTE, listOf("John", "Chris", "Faith"))
    post("Nadia", "Proofread my scholarship essay", "Please check grammar and flow of my 800 word scholarship essay.", "Writing", EstimatedTime.MODERATE, 4, 30_000, 70 * MINUTE, listOf("Abby"))
    post("Rafi", "Explain Kotlin coroutines scopes", "I keep mixing up viewModelScope and GlobalScope in my Android project.", "Programming", EstimatedTime.FAST, 2, 20_000, 40 * MINUTE, listOf("William"))
}

/** One conversation per helper in the chat list; Chris holds the request sent from the chat flow. */
private suspend fun seedChats(db: SosDatabase, userId: Long, helpers: Map<String, Long>, now: Long) {
    val chat = db.chatDao()

    suspend fun message(helper: String, text: String, ago: Long, fromMe: Boolean, attachment: String? = null, isRequest: Boolean = false) {
        chat.insert(
            ChatMessageEntity(
                userId = userId, helperId = helpers.getValue(helper), text = text, attachmentName = attachment,
                fromMe = fromMe, isRequest = isRequest, createdAt = now - ago
            )
        )
    }

    message("Chris", "Hi Chris, could you review my UI/UX assignment before Friday? The brief is attached.", MINUTE, true, "ui ux assignment.pdf", isRequest = true)
    message("Abby", "Hi, can you proofread my essay?", 11 * HOUR, true)
    message("Abby", "Hi, this is the details of the assignment, please take a look.", 10 * HOUR, true)
    message("William", "Done! The bug was in your coroutine scope, I left comments in the file.", 5 * DAY + HOUR, false)
    message("William", "Okay, thank you for helping me!", 5 * DAY, true)
    message("Coco", "Here are the worked solutions for questions 1 to 5.", 7 * DAY + HOUR, false)
    message("Coco", "Okay, thank you for helping me!", 7 * DAY, true)
    message("Billy", "I've sent over the final character sketches.", 7 * DAY + 2 * HOUR, false)
    message("Billy", "Okay, thank you for helping me!", 7 * DAY + HOUR, true)
}
