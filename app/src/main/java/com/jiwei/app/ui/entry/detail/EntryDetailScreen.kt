package com.jiwei.app.ui.entry.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jiwei.app.domain.model.UiState
import com.jiwei.app.util.MarkdownParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToEntry: (String) -> Unit,
    onCreateEntry: (String) -> Unit,
    onEntryDeleted: () -> Unit,
    viewModel: EntryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingLinkTitle by remember { mutableStateOf<String?>(null) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除条目") },
            text = { Text("确定要删除这个条目吗？删除后无法恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onEntryDeleted()
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    val linkTitle = pendingLinkTitle
    if (linkTitle != null) {
        AlertDialog(
            onDismissRequest = { pendingLinkTitle = null },
            title = { Text("条目不存在") },
            text = { Text("「${linkTitle}」尚未创建，是否立即创建？") },
            confirmButton = {
                TextButton(onClick = {
                    pendingLinkTitle = null
                    onCreateEntry(linkTitle)
                }) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingLinkTitle = null }) {
                    Text("取消")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.NavigateToEntry -> onNavigateToEntry(event.entryId)
                is DetailEvent.PromptCreateEntry -> pendingLinkTitle = event.title
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    when (val state = uiState) {
                        is UiState.Success -> {
                            IconButton(onClick = { viewModel.togglePin() }) {
                                Icon(
                                    if (state.data.entry.isPinned) Icons.Filled.Star else Icons.Outlined.Star,
                                    "收藏",
                                    tint = if (state.data.entry.isPinned)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                onNavigateToEdit(state.data.entry.id)
                            }) {
                                Icon(Icons.Filled.Edit, "编辑")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Filled.Delete, "删除")
                            }
                        }
                        else -> {}
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val entryWithTags = state.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Title
                    Text(
                        text = entryWithTags.entry.title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tags
                    if (entryWithTags.tags.isNotEmpty()) {
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
                        ) {
                            entryWithTags.tags.forEach { tag ->
                                androidx.compose.material3.AssistChip(
                                    onClick = { },
                                    label = { Text(tag.name, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Content with rendered Markdown
                    val renderedContent = remember(entryWithTags.entry.content) {
                        MarkdownParser.parse(entryWithTags.entry.content)
                    }

                    androidx.compose.foundation.text.ClickableText(
                        text = renderedContent,
                        style = MaterialTheme.typography.bodyLarge,
                        onClick = { offset ->
                            renderedContent.getStringAnnotations(
                                tag = "bidirectional_link",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                viewModel.onLinkClicked(annotation.item)
                            }
                        }
                    )

                    // Backlinks section
                    if (entryWithTags.backlinks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "被以下条目引用",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        entryWithTags.backlinks.forEach { backlink ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToEntry(backlink.id) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        backlink.title,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        backlink.content.take(100),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
