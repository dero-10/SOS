package com.sos.studentonstudy.ui.explore

import app.cash.turbine.test
import com.sos.studentonstudy.testutil.FakeHelperRepository
import com.sos.studentonstudy.testutil.FakeRequestRepository
import com.sos.studentonstudy.testutil.MainDispatcherRule
import com.sos.studentonstudy.testutil.request
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExploreViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private fun vm(repo: FakeRequestRepository) = ExploreViewModel(repo, FakeHelperRepository())

    @Test fun `empty feed shows no requests and finishes loading`() = runTest {
        val repo = FakeRequestRepository()
        vm(repo).uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state.requests.isEmpty())
            assertFalse(state.loading)
            assertEquals(1, repo.refreshCalls)
        }
    }

    @Test fun `feed is displayed newest first`() = runTest {
        val repo = FakeRequestRepository(initialFeed = listOf(
            request(1, "Older", createdAt = 100), request(2, "Newest", createdAt = 300)
        ))
        vm(repo).uiState.test {
            assertEquals(listOf("Newest", "Older"), expectMostRecentItem().requests.map { it.request.title })
        }
    }

    @Test fun `tag and search filter request feed`() = runTest {
        val repo = FakeRequestRepository(initialFeed = listOf(
            request(1, "Kotlin app", tags = "Mobile,Kotlin"),
            request(2, "Website", tags = "Web", details = "Kotlin examples"),
            request(3, "Java app", tags = "Mobile,Java")
        ))
        val viewModel = vm(repo)
        viewModel.uiState.test {
            assertEquals(3, expectMostRecentItem().requests.size)
            viewModel.setTag("Mobile")
            assertEquals(listOf("Kotlin app", "Java app"), expectMostRecentItem().requests.map { it.request.title })
            viewModel.setQuery("kOtLiN")
            assertEquals(listOf("Kotlin app"), expectMostRecentItem().requests.map { it.request.title })
            viewModel.setQuery("missing")
            assertTrue(expectMostRecentItem().requests.isEmpty())
        }
    }

    @Test fun `feed updates when repository emits new request`() = runTest {
        val repo = FakeRequestRepository()
        vm(repo).uiState.test {
            assertTrue(expectMostRecentItem().requests.isEmpty())
            repo.feedItems.value = listOf(request(3, "New request"))
            assertEquals("New request", expectMostRecentItem().requests.single().request.title)
        }
    }
}
