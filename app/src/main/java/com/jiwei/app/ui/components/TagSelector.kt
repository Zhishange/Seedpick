package com.jiwei.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jiwei.app.data.local.entity.TagEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelector(
    selectedTags: List<String>,
    suggestions: List<TagEntity>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onSearchTags: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tagInput by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = tagInput,
            onValueChange = {
                tagInput = it
                onSearchTags(it)
                showSuggestions = it.isNotBlank()
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("添加标签（输入 / 创建层级标签）") },
            singleLine = true,
            label = { Text("标签") }
        )

        if (showSuggestions && suggestions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                suggestions.take(5).forEach { tag ->
                    FilterChip(
                        selected = tag.name in selectedTags,
                        onClick = {
                            if (tag.name !in selectedTags) {
                                onAddTag(tag.name)
                            }
                            tagInput = ""
                            showSuggestions = false
                        },
                        label = { Text(tag.name, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }

        // Selected tags
        if (selectedTags.isNotEmpty()) {
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                selectedTags.forEach { tag ->
                    androidx.compose.material3.InputChip(
                        selected = true,
                        onClick = { onRemoveTag(tag) },
                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = {
                            androidx.compose.material3.Text(
                                "x",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}
