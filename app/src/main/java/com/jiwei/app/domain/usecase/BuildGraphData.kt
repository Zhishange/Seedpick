package com.jiwei.app.domain.usecase

import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.EntryLinkEntity
import com.jiwei.app.domain.model.GraphData
import com.jiwei.app.domain.model.GraphEdge
import com.jiwei.app.domain.model.GraphNode
import javax.inject.Inject

class BuildGraphData @Inject constructor() {

    operator fun invoke(
        entries: List<EntryEntity>,
        links: List<EntryLinkEntity>
    ): GraphData {
        val entryMap = entries.associateBy { it.id }
        val linkedEntryIds = mutableSetOf<String>()

        val edges = links.mapNotNull { link ->
            val targetEntry = entries.firstOrNull { it.title == link.targetEntryTitle }
            if (targetEntry != null) {
                linkedEntryIds.add(link.sourceEntryId)
                linkedEntryIds.add(targetEntry.id)
                GraphEdge(sourceId = link.sourceEntryId, targetId = targetEntry.id)
            } else null
        }

        val connectedEntries = entries.filter { it.id in linkedEntryIds }
        val nodes = connectedEntries.map { entry ->
            GraphNode(
                entryId = entry.id,
                title = entry.title,
                x = (Math.random() * 800).toFloat(),
                y = (Math.random() * 800).toFloat()
            )
        }

        return GraphData(
            nodes = applyForceDirectedLayout(nodes, edges),
            edges = edges
        )
    }

    private fun applyForceDirectedLayout(
        nodes: List<GraphNode>,
        edges: List<GraphEdge>,
        iterations: Int = 100
    ): List<GraphNode> {
        if (nodes.isEmpty()) return nodes

        val positions = nodes.map { floatArrayOf(it.x, it.y) }.toMutableList()
        val nodeIds = nodes.map { it.entryId }

        val repulsionStrength = 5000f
        val attractionStrength = 0.01f
        val damping = 0.9f

        for (iter in 0 until iterations) {
            val forces = positions.map { floatArrayOf(0f, 0f) }.toMutableList()

            for (i in positions.indices) {
                for (j in i + 1 until positions.size) {
                    val dx = positions[i][0] - positions[j][0]
                    val dy = positions[i][1] - positions[j][1]
                    val dist = maxOf(kotlin.math.sqrt(dx * dx + dy * dy), 1f)
                    val force = repulsionStrength / (dist * dist)
                    val fx = force * dx / dist
                    val fy = force * dy / dist
                    forces[i][0] += fx
                    forces[i][1] += fy
                    forces[j][0] -= fx
                    forces[j][1] -= fy
                }
            }

            for (edge in edges) {
                val si = nodeIds.indexOf(edge.sourceId)
                val ti = nodeIds.indexOf(edge.targetId)
                if (si != -1 && ti != -1) {
                    val dx = positions[ti][0] - positions[si][0]
                    val dy = positions[ti][1] - positions[si][1]
                    val dist = maxOf(kotlin.math.sqrt(dx * dx + dy * dy), 1f)
                    val force = attractionStrength * dist
                    val fx = force * dx / dist
                    val fy = force * dy / dist
                    forces[si][0] += fx
                    forces[si][1] += fy
                    forces[ti][0] -= fx
                    forces[ti][1] -= fy
                }
            }

            for (i in positions.indices) {
                positions[i][0] = (positions[i][0] + forces[i][0] * damping)
                    .coerceIn(50f, 750f)
                positions[i][1] = (positions[i][1] + forces[i][1] * damping)
                    .coerceIn(50f, 750f)
            }
        }

        return nodes.mapIndexed { i, node ->
            node.copy(x = positions[i][0], y = positions[i][1])
        }
    }
}
