package com.sos.studentonstudy.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sos.studentonstudy.data.local.RequestTags
import com.sos.studentonstudy.ui.EmptyState
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosPurple
import com.sos.studentonstudy.ui.SosSearchBar
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.SosViewModelFactory
import com.sos.studentonstudy.ui.TagChip
import com.sos.studentonstudy.ui.request.RequestCard

@Composable
fun ExploreScreen(
    onAddRequest: () -> Unit,
    onOpenRequest: (Long) -> Unit,
    viewModel: ExploreViewModel = viewModel(factory = SosViewModelFactory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sortMenuOpen by rememberSaveable { mutableStateOf(false) }
    var filterMenuOpen by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SosSearchBar(state.query, viewModel::setQuery, modifier = Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { sortMenuOpen = true }) {
                            Icon(Icons.Outlined.SwapVert, contentDescription = "Sort", tint = SosPurple)
                        }
                        DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                            RequestSort.entries.forEach { sort ->
                                DropdownMenuItem(
                                    text = { Text(sort.label, fontWeight = if (sort == state.sort) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        viewModel.setSort(sort)
                                        sortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row {
                    ExploreTab.entries.forEach { tab ->
                        TabItem(tab.label, selected = state.tab == tab, modifier = Modifier.weight(1f)) { viewModel.setTab(tab) }
                        if (tab != ExploreTab.entries.last()) Spacer(Modifier.width(46.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFB3B3B3), CircleShape)
                                .clickable { filterMenuOpen = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.FilterAlt, contentDescription = null, tint = SosText, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Filter", color = SosText, fontSize = 12.sp)
                        }
                        DropdownMenu(expanded = filterMenuOpen, onDismissRequest = { filterMenuOpen = false }) {
                            RequestTags.all.forEach { tag ->
                                DropdownMenuItem(text = { Text(tag) }, onClick = {
                                    viewModel.setTag(tag)
                                    filterMenuOpen = false
                                })
                            }
                        }
                    }
                    state.tag?.let { tag ->
                        Spacer(Modifier.width(8.dp))
                        TagChip(tag, onRemove = { viewModel.setTag(null) })
                    }
                }
            }
            when {
                state.tab == ExploreTab.Helper -> item {
                    EmptyState("Helpers are coming soon", "Browse requests for now in the Request tab.")
                }
                state.loading -> item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SosPurple)
                    }
                }
                state.requests.isEmpty() -> item {
                    EmptyState(
                        if (state.query.isBlank() && state.tag == null) "No requests yet" else "No matching requests",
                        "Tap + to post a new request."
                    )
                }
                else -> items(state.requests, key = { it.id }) { request ->
                    RequestCard(request, onClick = { onOpenRequest(request.id) })
                }
            }
        }
        if (state.tab == ExploreTab.Request) {
            FloatingActionButton(
                onClick = onAddRequest,
                containerColor = SosPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add request")
            }
        }
    }
}

@Composable
private fun TabItem(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = if (selected) SosPurple else SosMuted, fontSize = 15.sp, modifier = Modifier.padding(vertical = 6.dp))
        Box(Modifier.fillMaxWidth().height(2.dp).background(if (selected) SosPurple else SosMuted))
    }
}
