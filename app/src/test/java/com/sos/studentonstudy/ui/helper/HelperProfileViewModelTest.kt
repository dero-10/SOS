package com.sos.studentonstudy.ui.helper

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.sos.studentonstudy.testutil.FakeHelperRepository
import com.sos.studentonstudy.testutil.MainDispatcherRule
import com.sos.studentonstudy.testutil.helper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HelperProfileViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private fun vm(id: Long, repo: FakeHelperRepository) =
        HelperProfileViewModel(SavedStateHandle(mapOf(HelperProfileViewModel.HELPER_ID_ARG to id)), repo)

    @Test fun `known helper is loaded`() = runTest {
        val vm = vm(2, FakeHelperRepository(listOf(helper(1, "Chris"), helper(2, "Faith"))))
        vm.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("Faith", state.helper?.name)
            assertFalse(state.loading)
        }
    }

    @Test fun `unknown helper id gives no helper`() = runTest {
        val vm = vm(99, FakeHelperRepository(listOf(helper(1, "Chris"))))
        vm.uiState.test {
            val state = expectMostRecentItem()
            assertNull(state.helper)
            assertFalse(state.loading)
        }
    }

    @Test fun `helper without history has an empty recent list`() = runTest {
        val vm = vm(1, FakeHelperRepository(listOf(helper(1, "Chris"))))
        vm.uiState.test {
            assertTrue(expectMostRecentItem().recentHelps.isEmpty())
        }
    }
}
