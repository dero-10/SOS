package com.sos.studentonstudy.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.sos.studentonstudy.testutil.FakeChatRepository
import com.sos.studentonstudy.testutil.chat
import com.sos.studentonstudy.ui.chat.ChatListScreen
import com.sos.studentonstudy.ui.chat.ChatListViewModel
import org.junit.Rule
import org.junit.Test

class ChatListScreenTest {
    @get:Rule val compose = createComposeRule()

    private fun show(repo: FakeChatRepository) {
        compose.setContent { SosTheme { ChatListScreen(onOpenChat = {}, viewModel = ChatListViewModel(repo)) } }
    }

    @Test fun emptyStateIsShownWithoutConversations() {
        show(FakeChatRepository())
        compose.onNodeWithText("No conversations yet").assertIsDisplayed()
    }

    @Test fun conversationsAreListedByHelperName() {
        show(FakeChatRepository(listOf(chat(1, "Chris", "Can you help?"), chat(2, "Faith", "Thanks!"))))
        compose.onNodeWithText("Chris").assertIsDisplayed()
        compose.onNodeWithText("Faith").assertIsDisplayed()
        compose.onNodeWithText("Can you help?").assertIsDisplayed()
    }
}
