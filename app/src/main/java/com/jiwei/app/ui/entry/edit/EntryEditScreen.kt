package com.jiwei.app.ui.entry.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jiwei.app.domain.model.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditScreen(
    entryId: String?,
    onNavigateBack: () -> Unit,
    onEntrySaved: (String) -> Unit,
    viewModel: EntryEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    var tagInput by remember { mutableStateOf("") }
    var showTagSuggestions by remember { mutableStateOf(false) }

    LaunchedEffect(entryId) {
        if (entryId != null && entryId != "null") {
            viewModel.loadEntry(entryId)
        }
    }

    LaunchedEffect(uiState.saveState) {
        when (val state = uiState.saveState) {
            is UiState.Success -> {
                onEntrySaved(state.data.id)
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isNewEntry) "新建条目" else "编辑条目") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.save() }) {
                        Icon(Icons.Filled.Check, "保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title input
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("标题") },
                textStyle = MaterialTheme.typography.titleLarge,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start
                ) {
                    uiState.tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { viewModel.removeTag(tag) },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "移除标签",
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                // Tag input with suggestions
                Box {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = {
                            tagInput = it
                            viewModel.searchTags(it)
                            showTagSuggestions = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("添加标签（输入 / 创建层级标签）") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                viewModel.addTag(tagInput)
                                tagInput = ""
                                showTagSuggestions = false
                            }) {
                                Icon(Icons.Filled.Add, "添加标签")
                            }
                        }
                    )
                }

                if (showTagSuggestions && uiState.tagSuggestions.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        uiState.tagSuggestions.take(5).forEach { tag ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.addTag(tag.name)
                                    tagInput = ""
                                    showTagSuggestions = false
                                },
                                label = { Text(tag.name) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Markdown toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                ToolbarButton(Icons.Filled.Title, "标题") {
                    viewModel.updateContent(uiState.content + "\n## ")
                }
                ToolbarButton(Icons.Filled.FormatBold, "加粗") {
                    viewModel.updateContent(uiState.content + "**粗体**")
                }
                ToolbarButton(Icons.Filled.FormatItalic, "斜体") {
                    viewModel.updateContent(uiState.content + "*斜体*")
                }
                ToolbarButton(Icons.Filled.FormatListBulleted, "列表") {
                    viewModel.updateContent(uiState.content + "\n- ")
                }
                ToolbarButton(Icons.Filled.Code, "代码") {
                    viewModel.updateContent(uiState.content + "\n```\n\n```")
                }
                ToolbarButton(Icons.Filled.Link, "链接") {
                    viewModel.updateContent(uiState.content + "[[条目名称]]")
                }
                ToolbarButton(Icons.Filled.Image, "图片") {
                    viewModel.updateContent(uiState.content + "![描述]()")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content editor
            var textFieldValue by remember(uiState.content) {
                mutableStateOf(TextFieldValue(uiState.content))
            }

            BasicTextField(
                value = TextFieldValue(
                    text = uiState.content,
                    selection = textFieldValue.selection
                ),
                onValueChange = { newValue ->
                    viewModel.updateContent(newValue.text)
                    textFieldValue = newValue
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (uiState.content.isEmpty()) {
                        Text(
                            "输入 Markdown 内容...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(icon, label, tint = MaterialTheme.colorScheme.primary)
    }
}
