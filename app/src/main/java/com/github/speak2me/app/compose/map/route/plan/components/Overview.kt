package com.github.speak2me.app.compose.map.route.plan.components

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.route.plan.data.model.Trackpoint
import com.github.speak2me.app.compose.map.route.plan.data.model.Waypoint
import kotlinx.coroutines.launch

enum class OverviewState {
    Overview1,
    Overview2,
    Overview3;
}

@Composable
fun Overview1(
    tracks: List<Trackpoint>,
) {
    // 计算海拔统计信息（直接从轨迹点计算）
    var totalElevationGain = 0.0
    var totalElevationLoss = 0.0

    val totalDistance = tracks.lastOrNull()?.distance ?: return

    for (i in 1 until tracks.size) {
        val elevationDiff = tracks[i].altitude - tracks[i - 1].altitude
        if (elevationDiff > 0) {
            totalElevationGain += elevationDiff
        } else {
            totalElevationLoss += -elevationDiff
        }
    }

    Column(
        modifier = Modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
            .navigationBarsPadding()
    ) {
        // 标题和统计信息
        ElevationSummary(
            totalElevationGain = totalElevationGain,
            totalElevationLoss = totalElevationLoss,
            totalDistance = totalDistance,
        )
        ElevationChart(
            tracks = tracks,
        )
    }
}

@Composable
private fun ElevationSummary(
    totalElevationGain: Double,
    totalElevationLoss: Double,
    totalDistance: Double,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.SocialDistance, contentDescription = null)
            Text(
                text = "${(totalDistance / 1000).format(2)} km",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null)
            Text(
                text = "${totalElevationGain.toInt()}m",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Green
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null)
            Text(
                text = "${totalElevationLoss.toInt()}m",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
        }
    }
}

@Composable
fun Overview2(
    tracks: List<Trackpoint>,
    onChartScroll: (Float) -> Unit,
    selectedTrackPoint: Waypoint? = null,
    onAddWaypoint: (Waypoint) -> Unit,
    onCancelWaypoint: () -> Unit,
) {
    var selectedTrackIndex by remember { mutableIntStateOf(0) }
    var waypoint: Waypoint? by remember { mutableStateOf(null) }

    var name by rememberSaveable { mutableStateOf("") }

    ContentContainer {
        InternalTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name, hint = "请输入位置点"
        ) {
            name = it
        }
        // 显示选中点信息
        selectedTrackPoint?.let { info ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
            ) {
                Text(
                    text = "距离起点：${info.distance.toInt()}m, 海拔: ${info.altitude.toInt()}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Coordinate("${info.latitude.format(4)}, ${info.longitude.format(4)}")
            }
        }
        ElevationChart(
            tracks = tracks,
            onChartScroll = { progress ->
                onChartScroll(progress)
                // 根据进度计算选中的轨迹点索引
                selectedTrackIndex =
                    (progress * (tracks.size - 1)).toInt().coerceIn(0, tracks.size - 1)
                // 创建新的waypoint
                tracks.getOrNull(selectedTrackIndex)?.let { track ->
                    waypoint = Waypoint(
                        index = selectedTrackIndex,
                        name = "轨迹点${selectedTrackIndex + 1}",
                        latitude = track.latitude,
                        longitude = track.longitude,
                        altitude = track.altitude,
                        distance = track.distance
                    )
                }
            },
            isChartScrollable = true, // 只有在添加位置模式时才可滑动
        )
        Buttons(
            positiveText = "添加",
            positiveEnabled = name.isNotBlank(),
            onPositiveClick = { waypoint?.let { onAddWaypoint(it) } },
            negativeText = "取消",
            onNegativeClick = onCancelWaypoint
        )
    }
}

@Composable
fun Overview3(
    selectedWaypoint: Waypoint? = null,
    onUpdateWaypoint: (Waypoint) -> Unit,
    onDeleteWaypoint: (Waypoint) -> Unit,
) {
    var waypoint: Waypoint? by remember(selectedWaypoint) {
        mutableStateOf(selectedWaypoint)
    }

    var name by rememberSaveable(waypoint?.name) { mutableStateOf(waypoint?.name.orEmpty()) }

    ContentContainer {
        InternalTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name, hint = "请输入位置点"
        ) {
            name = it
        }
        // 显示选中点信息
        waypoint?.let { info ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
            ) {
                Text(
                    text = "距离起点：${info.distance.toInt()}m, 海拔: ${info.altitude.toInt()}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Coordinate("${info.latitude.format(4)}, ${info.longitude.format(4)}")
            }
        }

        Buttons(
            positiveText = "更新",
            onPositiveClick = { waypoint?.let { onUpdateWaypoint(it) } },
            negativeText = "删除",
            onNegativeClick = { waypoint?.let { onDeleteWaypoint(it) } }
        )
    }
}

// <editor-fold desc="坐标按钮" defaultstate="collapsed">
@Composable
private fun Coordinate(coordinate: String) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(coordinate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Icon(
            modifier = Modifier
                .size(14.dp)
                .clickable(onClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipData.newPlainText(coordinate, coordinate).toClipEntry()
                        )
                    }
                }),
            imageVector = ImageVector.vectorResource(R.drawable.route_ic_dialog_location_copy),
            contentDescription = null
        )
    }
}
// </editor-fold>

@Composable
private fun ContentContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
            .padding(start = 12.dp, end = 12.dp, top = 12.dp)
            .navigationBarsPadding(),
        content = content
    )
}

@Composable
private fun Buttons(
    positiveText: String,
    positiveEnabled: Boolean = true,
    onPositiveClick: () -> Unit,
    negativeText: String,
    negativeEnabled: Boolean = true,
    onNegativeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onPositiveClick,
            modifier = Modifier.weight(2f),
            enabled = positiveEnabled
        ) {
            Text(positiveText)
        }

        OutlinedButton(
            onClick = onNegativeClick,
            modifier = Modifier.weight(1f),
            enabled = negativeEnabled
        ) {
            Text(negativeText)
        }
    }
}
