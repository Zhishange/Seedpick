package com.jiwei.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jiwei.app.ui.components.EntryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToGraph: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEntry: (String) -> Unit,
    onNewEntry: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("积微", style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Filled.Search, "搜索")
                    }
                    IconButton(onClick = onNavigateToTags) {
                        Icon(Icons.Filled.Tag, "标签")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.FilterList, "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewEntry) {
                Icon(Icons.Filled.Add, "新建条目")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter bar
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.showPinnedOnly,
                        onClick = { viewModel.togglePinnedFilter() },
                        label = { Text("收藏") },
                        leadingIcon = {
                            Icon(Icons.Filled.Star, "收藏", Modifier.padding(start = 0.dp))
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedTagId == null,
                        onClick = { viewModel.selectTag(null) },
                        label = { Text("全部") }
                    )
                }
                items(uiState.allTags.take(15)) { tag ->
                    FilterChip(
                        selected = tag.id == uiState.selectedTagId,
                        onClick = { viewModel.selectTag(tag.id) },
                        label = { Text(tag.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val displayEntries = if (uiState.showPinnedOnly) {
                    uiState.pinnedEntries
                } else {
                    uiState.pinnedEntries + uiState.unpinnedEntries
                }

                if (displayEntries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "还没有知识条目",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "点击右下角 + 创建第一个条目",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp
                    ) {
                        items(displayEntries, key = { it.id }) { entry ->
                            // Get tags from entry through UI state
                            EntryCard(
                                title = entry.title,
                                content = entry.content,
                                tags = emptyList(), // Tags would need cross-ref lookup
                                isPinned = entry.isPinned,
                                onClick = { onNavigateToEntry(entry.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
