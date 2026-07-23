package com.jiwei.app.ui.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jiwei.app.domain.model.GraphData
import com.jiwei.app.domain.repository.EntryRepository
import com.jiwei.app.domain.repository.LinkRepository
import com.jiwei.app.domain.usecase.BuildGraphData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GraphUiState(
    val graphData: GraphData = GraphData(emptyList(), emptyList()),
    val selectedNodeId: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class GraphViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val linkRepository: LinkRepository,
    private val buildGraphData: BuildGraphData
) : ViewModel() {

    private val _uiState = MutableStateFlow(GraphUiState())
    val uiState: StateFlow<GraphUiState> = _uiState.asStateFlow()

    init {
        loadGraph()
    }

    fun loadGraph() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val entries = entryRepository.getAllEntries().let { first(it) }
            val links = linkRepository.getAllLinks()
            val graph = buildGraphData(entries, links)
            _uiState.value = GraphUiState(graphData = graph, isLoading = false)
        }
    }

    fun selectNode(nodeId: String?) {
        _uiState.value = _uiState.value.copy(selectedNodeId = nodeId)
    }
}
