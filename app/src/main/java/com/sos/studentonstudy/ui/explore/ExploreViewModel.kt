package com.sos.studentonstudy.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.local.HelperEntity
import com.sos.studentonstudy.data.local.RequestWithMeta
import com.sos.studentonstudy.data.local.skillList
import com.sos.studentonstudy.data.local.tagList
import com.sos.studentonstudy.data.repository.HelperRepository
import com.sos.studentonstudy.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ExploreTab(val label: String) { Request("Request"), Helper("Helper") }

enum class RequestSort(val label: String) { Newest("Newest"), Budget("Highest budget"), Deadline("Nearest deadline") }

enum class HelperSort(val label: String) { Recommended("Recommended"), Rating("Top rated"), Helps("Most helps") }

data class ExploreUiState(
    val tab: ExploreTab = ExploreTab.Request,
    val query: String = "",
    val tag: String? = null,
    val sort: RequestSort = RequestSort.Newest,
    val helperSort: HelperSort = HelperSort.Recommended,
    val requests: List<RequestWithMeta> = emptyList(),
    val helpers: List<HelperEntity> = emptyList(),
    val loading: Boolean = true
)

private data class ExploreFilters(
    val tab: ExploreTab,
    val query: String,
    val tag: String?,
    val sort: RequestSort,
    val helperSort: HelperSort
)

class ExploreViewModel(requestRepository: RequestRepository, helperRepository: HelperRepository) : ViewModel() {
    private val tab = MutableStateFlow(ExploreTab.Request)
    private val query = MutableStateFlow("")
    private val tag = MutableStateFlow<String?>(null)
    private val sort = MutableStateFlow(RequestSort.Newest)
    private val helperSort = MutableStateFlow(HelperSort.Recommended)

    private val filters = combine(tab, query, tag, sort, helperSort) { tab, query, tag, sort, helperSort ->
        ExploreFilters(tab, query, tag, sort, helperSort)
    }

    val uiState: StateFlow<ExploreUiState> = combine(requestRepository.feed, helperRepository.helpers, filters) { requests, helpers, f ->
        val needle = f.query.trim()
        val visibleRequests = requests
            .filter { f.tag == null || f.tag in it.request.tagList() }
            .filter {
                needle.isEmpty() || it.request.title.contains(needle, ignoreCase = true) ||
                    it.request.details.contains(needle, ignoreCase = true)
            }
            .let { list ->
                when (f.sort) {
                    RequestSort.Newest -> list.sortedByDescending { it.request.createdAt }
                    RequestSort.Budget -> list.sortedByDescending { it.request.budget }
                    RequestSort.Deadline -> list.sortedBy { it.request.deadline }
                }
            }
        val visibleHelpers = helpers
            .filter { helper -> f.tag == null || helper.skillList().any { it.equals(f.tag, ignoreCase = true) } }
            .filter { helper ->
                needle.isEmpty() || helper.name.contains(needle, ignoreCase = true) ||
                    helper.education.contains(needle, ignoreCase = true) ||
                    helper.skillList().any { it.contains(needle, ignoreCase = true) }
            }
            .let { list ->
                when (f.helperSort) {
                    HelperSort.Recommended -> list // the DAO already puts online helpers first
                    HelperSort.Rating -> list.sortedByDescending { it.rating }
                    HelperSort.Helps -> list.sortedByDescending { it.helps }
                }
            }
        ExploreUiState(f.tab, f.query, f.tag, f.sort, f.helperSort, visibleRequests, visibleHelpers, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState())

    fun setTab(value: ExploreTab) { tab.value = value }
    fun setQuery(value: String) { query.value = value }
    fun setTag(value: String?) { tag.value = value }
    fun setSort(value: RequestSort) { sort.value = value }
    fun setHelperSort(value: HelperSort) { helperSort.value = value }
}
