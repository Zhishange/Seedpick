package com.jiwei.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.TagEntity
import com.jiwei.app.domain.model.UiState
import com.jiwei.app.domain.repository.EntryRepository
import com.jiwei.app.domain.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder { UPDATED_DESC, CREATED_DESC, TITLE_ASC }

data class HomeUiState(
    val pinnedEntries: List<EntryEntity> = emptyList(),
    val unpinnedEntries: List<EntryEntity> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val selectedTagId: String? = null,
    val showPinnedOnly: Boolean = false,
    val sortOrder: SortOrder = SortOrder.UPDATED_DESC,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val tags = tagRepository.getAllTags()
            _uiState.value = _uiState.value.copy(allTags = tags, isLoading = false)
        }

        viewModelScope.launch {
            entryRepository.getPinnedEntries().collect { entries ->
                val sorted = sortEntries(entries, _uiState.value.sortOrder)
                _uiState.value = _uiState.value.copy(pinnedEntries = sorted)
            }
        }

        viewModelScope.launch {
            entryRepository.getUnpinnedEntries().collect { entries ->
                val sorted = sortEntries(entries, _uiState.value.sortOrder)
                _uiState.value = _uiState.value.copy(unpinnedEntries = sorted)
            }
        }
    }

    fun togglePinnedFilter() {
        _uiState.value = _uiState.value.copy(
            showPinnedOnly = !_uiState.value.showPinnedOnly
        )
    }

    fun selectTag(tagId: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedTagId = tagId)
            if (tagId != null) {
                val childIds = tagRepository.getChildTagIds(tagId)
                val allTagIds = listOf(tagId) + childIds
                tagRepository.getEntriesForTagsFlow(allTagIds).collect { entries ->
                    val sorted = sortEntries(entries, _uiState.value.sortOrder)
                    _uiState.value = _uiState.value.copy(
                        pinnedEntries = sorted.filter { it.isPinned },
                        unpinnedEntries = sorted.filter { !it.isPinned }
                    )
                }
            } else {
                loadData()
            }
        }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = order)
        loadData()
    }

    private fun sortEntries(entries: List<EntryEntity>, order: SortOrder): List<EntryEntity> {
        return when (order) {
            SortOrder.UPDATED_DESC -> entries.sortedByDescending { it.updatedAt }
            SortOrder.CREATED_DESC -> entries.sortedByDescending { it.createdAt }
            SortOrder.TITLE_ASC -> entries.sortedBy { it.title }
        }
    }
}
