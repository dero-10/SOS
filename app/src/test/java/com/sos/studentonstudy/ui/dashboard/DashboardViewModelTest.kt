package com.sos.studentonstudy.ui.dashboard

import app.cash.turbine.test
import com.sos.studentonstudy.data.local.UserEntity
import com.sos.studentonstudy.testutil.FakeAuthRepository
import com.sos.studentonstudy.testutil.FakeRequestRepository
import com.sos.studentonstudy.testutil.MainDispatcherRule
import com.sos.studentonstudy.testutil.request
import com.sos.studentonstudy.ui.daysFromToday
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    @Test fun `no own requests shows zero counts`() = runTest {
        val repo = FakeRequestRepository()
        val auth = FakeAuthRepository()
        DashboardViewModel(auth, repo).uiState.test {
            val state = expectMostRecentItem()
            assertEquals(0, state.total)
            assertEquals(0, state.urgent)
            assertEquals(0, state.dueThisWeek)
            assertTrue(state.recent.isEmpty())
            assertFalse(state.loading)
            assertEquals(1, repo.refreshCalls)
        }
    }

    @Test fun `dashboard counts urgent and week deadlines and shows user`() = runTest {
        val auth = FakeAuthRepository()
        auth.user.value = UserEntity(name = "Michael", email = "michael@example.com", passwordHash = "", balance = 75_000)
        val repo = FakeRequestRepository(initialOwn = listOf(
            request(1, "Today", deadline = daysFromToday(0)),
            request(2, "Tomorrow", deadline = daysFromToday(1)),
            request(3, "Later this week", deadline = daysFromToday(7)),
            request(4, "Next week", deadline = daysFromToday(8))
        ))
        DashboardViewModel(auth, repo).uiState.test {
            val state = expectMostRecentItem()
            assertEquals("Michael", state.name)
            assertEquals(75_000L, state.balance)
            assertEquals(4, state.total)
            assertEquals(2, state.urgent)
            assertEquals(3, state.dueThisWeek)
            assertEquals(listOf("Next week", "Later this week", "Tomorrow"), state.recent.map { it.request.title })
        }
    }

    @Test fun `dashboard reacts when own requests change`() = runTest {
        val repo = FakeRequestRepository()
        DashboardViewModel(FakeAuthRepository(), repo).uiState.test {
            assertEquals(0, expectMostRecentItem().total)
            repo.ownItems.value = listOf(request(1, "Urgent", deadline = daysFromToday(1)))
            val state = expectMostRecentItem()
            assertEquals(1, state.total)
            assertEquals(1, state.urgent)
        }
    }
}
