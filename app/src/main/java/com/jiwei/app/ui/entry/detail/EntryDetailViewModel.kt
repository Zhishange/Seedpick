package com.jiwei.app.ui.entry.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jiwei.app.domain.model.EntryWithTags
import com.jiwei.app.domain.model.UiState
import com.jiwei.app.domain.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailEvent {
    data class NavigateToEntry(val entryId: String) : DetailEvent()
    data class PromptCreateEntry(val title: String) : DetailEvent()
}

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val entryRepository: EntryRepository
) : ViewModel() {

    private val entryId: String = savedStateHandle["entryId"] ?: ""

    private val _uiState = MutableStateFlow<UiState<EntryWithTags>>(UiState.Loading)
    val uiState: StateFlow<UiState<EntryWithTags>> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DetailEvent>()
    val events: SharedFlow<DetailEvent> = _events.asSharedFlow()

    init {
        loadEntry()
    }

    private fun loadEntry() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val entryWithDetails = entryRepository.getEntryWithDetails(entryId)
                if (entryWithDetails != null) {
                    _uiState.value = UiState.Success(entryWithDetails)
                } else {
                    _uiState.value = UiState.Error("条目不存在")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun togglePin() {
        viewModelScope.launch {
            try {
                entryRepository.togglePin(entryId)
                loadEntry()
            } catch (e: Exception) {
            }
        }
    }

    fun onLinkClicked(linkTitle: String) {
        viewModelScope.launch {
            try {
                val entry = entryRepository.findEntryByTitle(linkTitle)
                if (entry != null) {
                    _events.emit(DetailEvent.NavigateToEntry(entry.id))
                } else {
                    _events.emit(DetailEvent.PromptCreateEntry(linkTitle))
                }
            } catch (_: Exception) {
                _events.emit(DetailEvent.PromptCreateEntry(linkTitle))
            }
        }
    }
}
