package com.jiwei.app.domain.model

data class GraphNode(
    val entryId: String,
    val title: String,
    val x: Float = 0f,
    val y: Float = 0f
)

data class GraphEdge(
    val sourceId: String,
    val targetId: String
)

data class GraphData(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>
)
