package com.jiwei.app.ui.tag

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jiwei.app.domain.model.TreeNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManageScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEntry: (String) -> Unit,
    viewModel: TagManageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("标签管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tag tree section
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxWidth()
            ) {
                Text(
                    "标签",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.tagTree, key = { it.tagId }) { node ->
                        TagTreeItem(
                            node = node,
                            depth = 0,
                            expandedIds = uiState.expandedTagIds,
                            selectedTagId = uiState.selectedTagId,
                            onToggleExpand = { viewModel.toggleExpand(it) },
                            onSelectTag = { id, name -> viewModel.selectTag(id, name) }
                        )
                    }
                }
            }

            HorizontalDivider()

            // Entries for selected tag
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxWidth()
            ) {
                if (uiState.selectedTagName != null) {
                    Text(
                        "「${uiState.selectedTagName}」下的条目",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp)
                    )

                    if (uiState.entriesForSelectedTag.isEmpty()) {
                        Text(
                            "暂无条目",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.entriesForSelectedTag, key = { it.id }) { entry ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                        .clickable { onNavigateToEntry(entry.id) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            entry.title.ifBlank { "无标题" },
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            entry.content.take(80),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "选择一个标签查看条目",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TagTreeItem(
    node: TreeNode,
    depth: Int,
    expandedIds: Set<String>,
    selectedTagId: String?,
    onToggleExpand: (String) -> Unit,
    onSelectTag: (String, String) -> Unit
) {
    val isExpanded = node.tagId in expandedIds
    val isSelected = node.tagId == selectedTagId
    val hasChildren = node.children.isNotEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectTag(node.tagId, node.tagName) }
            .padding(start = (16 + depth * 24).dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasChildren) {
            IconButton(
                onClick = { onToggleExpand(node.tagId) },
                modifier = Modifier.width(32.dp)
            ) {
                Icon(
                    if (isExpanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                    contentDescription = if (isExpanded) "折叠" else "展开"
                )
            }
        } else {
            Spacer(modifier = Modifier.width(32.dp))
        }

        Icon(
            Icons.Filled.Label,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            node.tagName,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }

    if (isExpanded && hasChildren) {
        node.children.forEach { child ->
            TagTreeItem(
                node = child,
                depth = depth + 1,
                expandedIds = expandedIds,
                selectedTagId = selectedTagId,
                onToggleExpand = onToggleExpand,
                onSelectTag = onSelectTag
            )
        }
    }
}
