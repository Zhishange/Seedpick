package com.jiwei.app.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jiwei.app.domain.model.GraphData
import com.jiwei.app.domain.model.GraphNode
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEntry: (String) -> Unit,
    viewModel: GraphViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastTapTime by remember { mutableStateOf(0L) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("知识图谱") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.3f, 3f)
                        offset = Offset(
                            offset.x + pan.x,
                            offset.y + pan.y
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val data = uiState.graphData
                            val adjustedX = (tapOffset.x - offset.x) / scale
                            val adjustedY = (tapOffset.y - offset.y) / scale

                            data.nodes.minByOrNull { node ->
                                val dx = node.x - adjustedX
                                val dy = node.y - adjustedY
                                sqrt(dx * dx + dy * dy)
                            }?.let { closest ->
                                val dx = closest.x - adjustedX
                                val dy = closest.y - adjustedY
                                val dist = sqrt(dx * dx + dy * dy)
                                if (dist < 30f) {
                                    val now = System.currentTimeMillis()
                                    if (now - lastTapTime < 300) {
                                        onNavigateToEntry(closest.entryId)
                                    }
                                    lastTapTime = now
                                    viewModel.selectNode(closest.entryId)
                                } else {
                                    viewModel.selectNode(null)
                                }
                            }
                        }
                    )
                }
        ) {
            GraphCanvas(
                data = uiState.graphData,
                selectedNodeId = uiState.selectedNodeId,
                scale = scale,
                offset = offset
            )
        }
    }
}

@Composable
private fun GraphCanvas(
    data: GraphData,
    selectedNodeId: String?,
    scale: Float,
    offset: Offset
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Bind forces to canvas bounds
        val bindScaleX = canvasWidth / 800f
        val bindScaleY = canvasHeight / 800f

        // Draw edges
        for (edge in data.edges) {
            val source = data.nodes.find { it.entryId == edge.sourceId }
            val target = data.nodes.find { it.entryId == edge.targetId }
            if (source != null && target != null) {
                drawLine(
                    color = Color(0xFFBDBDBD),
                    start = Offset(
                        source.x * bindScaleX * scale + offset.x,
                        source.y * bindScaleY * scale + offset.y
                    ),
                    end = Offset(
                        target.x * bindScaleX * scale + offset.x,
                        target.y * bindScaleY * scale + offset.y
                    ),
                    strokeWidth = 2f
                )
            }
        }

        // Draw nodes
        for (node in data.nodes) {
            val cx = node.x * bindScaleX * scale + offset.x
            val cy = node.y * bindScaleY * scale + offset.y
            val radius = 20f * scale
            val isSelected = node.entryId == selectedNodeId

            drawCircle(
                color = if (isSelected) Color(0xFF1565C0) else Color(0xFF2E5A32),
                radius = radius,
                center = Offset(cx, cy)
            )

            drawCircle(
                color = Color.White,
                radius = radius - 2f,
                center = Offset(cx, cy)
            )

            drawCircle(
                color = if (isSelected) Color(0xFF1565C0) else Color(0xFF2E5A32),
                radius = radius - 4f,
                center = Offset(cx, cy)
            )

            // Draw title
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 10f * scale
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText(
                    node.title.take(6),
                    cx,
                    cy + 4f * scale,
                    paint
                )
            }
        }
    }
}
