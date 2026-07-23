package com.jiwei.app.ui.entry.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.TagEntity
import com.jiwei.app.domain.model.UiState
import com.jiwei.app.domain.repository.EntryRepository
import com.jiwei.app.domain.repository.TagRepository
import com.jiwei.app.domain.usecase.CreateEntry
import com.jiwei.app.domain.usecase.UpdateEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EntryEditUiState(
    val entryId: String = "",
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val tagSuggestions: List<TagEntity> = emptyList(),
    val isNewEntry: Boolean = true,
    val isPinned: Boolean = false,
    val saveState: UiState<EntryEntity> = UiState.Loading
)

@HiltViewModel
class EntryEditViewModel @Inject constructor(
    private val createEntry: CreateEntry,
    private val updateEntry: UpdateEntry,
    private val entryRepository: EntryRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryEditUiState())
    val uiState: StateFlow<EntryEditUiState> = _uiState.asStateFlow()

    fun loadEntry(entryId: String) {
        viewModelScope.launch {
            val entry = entryRepository.getEntryById(entryId)
            if (entry != null) {
                val tags = tagRepository.getTagsForEntry(entryId).map { it.name }
                _uiState.value = _uiState.value.copy(
                    entryId = entryId,
                    title = entry.title,
                    content = entry.content,
                    tags = tags,
                    isNewEntry = false,
                    isPinned = entry.isPinned
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun addTag(tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isNotBlank() && trimmed !in _uiState.value.tags) {
            _uiState.value = _uiState.value.copy(
                tags = _uiState.value.tags + trimmed
            )
        }
    }

    fun removeTag(tag: String) {
        _uiState.value = _uiState.value.copy(
            tags = _uiState.value.tags - tag
        )
    }

    fun searchTags(query: String) {
        viewModelScope.launch {
            val suggestions = if (query.isBlank()) {
                tagRepository.getAllTags()
            } else {
                tagRepository.searchTags(query)
            }
            _uiState.value = _uiState.value.copy(tagSuggestions = suggestions)
        }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveState = UiState.Loading)
            try {
                val state = _uiState.value
                val entry = if (state.isNewEntry) {
                    val created = createEntry(state.title, state.content)
                    tagRepository.updateEntryTags(created.id, state.tags)
                    created
                } else {
                    updateEntry(
                        entryId = state.entryId,
                        title = state.title,
                        content = state.content,
                        tags = state.tags,
                        isPinned = state.isPinned
                    )
                }
                _uiState.value = _uiState.value.copy(saveState = UiState.Success(entry))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveState = UiState.Error(e.message ?: "保存失败")
                )
            }
        }
    }
}
