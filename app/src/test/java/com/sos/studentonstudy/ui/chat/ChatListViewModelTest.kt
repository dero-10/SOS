package com.sos.studentonstudy.ui.chat

import app.cash.turbine.test
import com.sos.studentonstudy.testutil.FakeChatRepository
import com.sos.studentonstudy.testutil.MainDispatcherRule
import com.sos.studentonstudy.testutil.chat
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChatListViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    @Test fun `no conversations gives an empty list`() = runTest {
        val vm = ChatListViewModel(FakeChatRepository())
        vm.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state.chats.isEmpty())
            assertFalse(state.hasChats)
            assertFalse(state.loading)
        }
    }

    @Test fun `conversations are listed`() = runTest {
        val vm = ChatListViewModel(FakeChatRepository(listOf(chat(1, "Chris"), chat(2, "Faith"))))
        vm.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf("Chris", "Faith"), state.chats.map { it.helperName })
            assertTrue(state.hasChats)
        }
    }

    @Test fun `search filters by helper name but keeps hasChats`() = runTest {
        val vm = ChatListViewModel(FakeChatRepository(listOf(chat(1, "Chris"), chat(2, "Faith"))))
        vm.uiState.test {
            vm.setQuery("fai")
            val state = expectMostRecentItem()
            assertEquals(listOf("Faith"), state.chats.map { it.helperName })
            assertTrue(state.hasChats)
        }
    }

    @Test fun `search with no match is empty but hasChats stays true`() = runTest {
        val vm = ChatListViewModel(FakeChatRepository(listOf(chat(1, "Chris"))))
        vm.uiState.test {
            vm.setQuery("zzz")
            val state = expectMostRecentItem()
            assertTrue(state.chats.isEmpty())
            assertTrue(state.hasChats)
        }
    }
}
