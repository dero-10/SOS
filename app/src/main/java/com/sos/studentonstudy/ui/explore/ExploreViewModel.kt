package com.sos.studentonstudy.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.local.RequestEntity
import com.sos.studentonstudy.data.local.tagList
import com.sos.studentonstudy.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ExploreTab(val label: String) { Request("Request"), Helper("Helper") }

enum class RequestSort(val label: String) { Newest("Newest"), Budget("Highest budget"), Deadline("Nearest deadline") }

data class ExploreUiState(
    val tab: ExploreTab = ExploreTab.Request,
    val query: String = "",
    val tag: String? = null,
    val sort: RequestSort = RequestSort.Newest,
    val requests: List<RequestEntity> = emptyList(),
    val loading: Boolean = true
)

private data class ExploreFilters(val tab: ExploreTab, val query: String, val tag: String?, val sort: RequestSort)

class ExploreViewModel(repository: RequestRepository) : ViewModel() {
    private val tab = MutableStateFlow(ExploreTab.Request)
    private val query = MutableStateFlow("")
    private val tag = MutableStateFlow<String?>(null)
    private val sort = MutableStateFlow(RequestSort.Newest)

    private val filters = combine(tab, query, tag, sort) { tab, query, tag, sort -> ExploreFilters(tab, query, tag, sort) }

    val uiState: StateFlow<ExploreUiState> = combine(repository.requests, filters) { requests, f ->
        val visible = requests
            .filter { f.tag == null || f.tag in it.tagList() }
            .filter { f.query.isBlank() || it.title.contains(f.query.trim(), ignoreCase = true) || it.details.contains(f.query.trim(), ignoreCase = true) }
            .let { list ->
                when (f.sort) {
                    RequestSort.Newest -> list.sortedByDescending { it.createdAt }
                    RequestSort.Budget -> list.sortedByDescending { it.budget }
                    RequestSort.Deadline -> list.sortedBy { it.deadline }
                }
            }
        ExploreUiState(f.tab, f.query, f.tag, f.sort, visible, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState())

    fun setTab(value: ExploreTab) { tab.value = value }
    fun setQuery(value: String) { query.value = value }
    fun setTag(value: String?) { tag.value = value }
    fun setSort(value: RequestSort) { sort.value = value }
}
