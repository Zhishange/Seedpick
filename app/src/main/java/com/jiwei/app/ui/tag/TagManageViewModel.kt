package com.jiwei.app.ui.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.TagEntity
import com.jiwei.app.domain.model.TreeNode
import com.jiwei.app.domain.repository.TagRepository
import com.jiwei.app.domain.usecase.BuildTagTree
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagManageUiState(
    val tagTree: List<TreeNode> = emptyList(),
    val expandedTagIds: Set<String> = emptySet(),
    val selectedTagId: String? = null,
    val selectedTagName: String? = null,
    val entriesForSelectedTag: List<EntryEntity> = emptyList()
)

@HiltViewModel
class TagManageViewModel @Inject constructor(
    private val tagRepository: TagRepository,
    private val buildTagTree: BuildTagTree
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagManageUiState())
    val uiState: StateFlow<TagManageUiState> = _uiState.asStateFlow()

    init {
        loadTags()
    }

    fun loadTags() {
        viewModelScope.launch {
            val allTags = tagRepository.getAllTags()
            val tree = buildTagTree(allTags)
            _uiState.value = _uiState.value.copy(tagTree = tree)
        }
    }

    fun toggleExpand(tagId: String) {
        val current = _uiState.value.expandedTagIds
        _uiState.value = _uiState.value.copy(
            expandedTagIds = if (tagId in current) current - tagId else current + tagId
        )
    }

    fun selectTag(tagId: String, tagName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedTagId = tagId,
                selectedTagName = tagName
            )
            val entries = tagRepository.getEntriesForTagRecursive(tagId)
            _uiState.value = _uiState.value.copy(entriesForSelectedTag = entries)
        }
    }
}
