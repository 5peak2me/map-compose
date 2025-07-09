package com.github.speak2me.app.compose.map.route.plan

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amap.api.maps.model.LatLng
import com.github.speak2me.app.compose.map.route.plan.components.DialogHost
import com.github.speak2me.app.compose.map.route.plan.components.rememberDialogState
import com.github.speak2me.app.compose.map.route.plan.components.RouteMode
import com.github.speak2me.app.compose.map.route.plan.components.Overview1
import com.github.speak2me.app.compose.map.route.plan.components.Overview2
import com.github.speak2me.app.compose.map.route.plan.components.Overview3
import com.github.speak2me.app.compose.map.route.plan.components.Loading
import com.github.speak2me.app.compose.map.route.plan.components.MapView
import com.github.speak2me.app.compose.map.route.plan.components.HintMessage
import com.github.speak2me.app.compose.map.route.plan.components.OverviewState
import com.github.speak2me.app.compose.map.route.plan.components.SportType
import com.github.speak2me.app.compose.map.route.plan.components.UndoRedo
import com.github.speak2me.app.compose.map.route.plan.components.orDefault
import com.github.speak2me.app.compose.map.route.plan.components.showEditRouteDialog
import com.github.speak2me.app.compose.map.route.plan.components.showRouteMenuDialog
import com.github.speak2me.app.compose.map.route.plan.components.showSportTypeDialog
import com.github.speak2me.app.compose.map.route.plan.data.model.Waypoint
import com.github.speak2me.app.compose.map.route.plan.permission.useLocationPermission

@Composable
fun RoutePlan2(
    modifier: Modifier = Modifier,
    viewModel: RoutePlan2ViewModel = viewModel(),
    onSearchClick: () -> Unit = {},
    initialSearchLocation: Location? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialSearchLocation) {
        initialSearchLocation?.let {
            viewModel.onSearchLocationChanged(it)
        }
    }

    RoutePlan2(
        modifier = modifier,
        uiState = uiState,
        onSportTypeSelected = viewModel::updateSportType,
        onRouteModeSelected = viewModel::updateRouteMode,
        onSwapStartEnd = viewModel::swapStartEnd,
        onReverseRoute = viewModel::reverseRoute,
        onPermissionGranted = viewModel::onLocationPermissionGranted,
        onLocateClick = viewModel::startLocationUpdates,
        onMapClick = viewModel::onMapClick,
        onWaypointClick = viewModel::onWaypointClick,
        onSearchClick = onSearchClick,
        onScreenshotTaken = viewModel::onScreenshotTaken,
        undo = viewModel::undo,
        redo = viewModel::redo,
        onSaveClick = viewModel::saveRoute,
        onAddWaypoint = viewModel::addWaypoint,
        onDeleteWaypoint = viewModel::deleteWaypoint,
        onUpdateWaypoint = viewModel::updateWaypoint,
        onChartScroll = viewModel::onChartScroll,
        onCancelWaypoint = viewModel::cancelWaypoint
    )
}

@SuppressLint("AutoboxingStateCreation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlan2(
    modifier: Modifier = Modifier,
    uiState: RoutePlan2UiState,
    onSportTypeSelected: (SportType) -> Unit,
    onRouteModeSelected: (RouteMode) -> Unit,
    onSwapStartEnd: () -> Unit,
    onReverseRoute: () -> Unit,
    onPermissionGranted: () -> Unit,
    onLocateClick: () -> Unit,
    onMapClick: (LatLng) -> Unit,
    onSearchClick: () -> Unit = {},
    onScreenshotTaken: (Bitmap?) -> Unit,
    undo: () -> Unit,
    redo: () -> Unit,
    onSaveClick: (String) -> Unit,
    onWaypointClick: (Waypoint) -> Unit,
    selectedWaypoint: Waypoint? = null,
    onAddWaypoint: (Waypoint) -> Unit,
    onDeleteWaypoint: (Waypoint) -> Unit,
    onUpdateWaypoint: (Waypoint) -> Unit,
    onChartScroll: (Float) -> Unit,
    selectedTrackPoint: Waypoint? = null,
    onCancelWaypoint: () -> Unit,
) {
    val context = LocalContext.current // 用于显示 Toast

    var name by remember { mutableStateOf("") }

    // 使用新的 rememberDialogState API
    val dialogController = rememberDialogState(
        selectedSportType = uiState.selectedSportType,
        onSportTypeSelected = onSportTypeSelected,
        selectedRouteMode = uiState.selectedRouteMode,
        onRouteModeSelected = onRouteModeSelected,
        hasTracks = uiState.hasTracks,
        onSwapStartEnd = onSwapStartEnd,
        onReverseRoute = onReverseRoute,
        isReverseRouteUsed = uiState.isReverseRouteUsed,
    )

    // 初始化首次进入的对话框
    LaunchedEffect(Unit) {
        if (uiState.selectedSportType == null) {
            dialogController.showSportTypeDialog()
        }
    }

    // 权限管理 - 使用 Hook 方式
    val locationPermissionState = useLocationPermission(
        onPermissionGranted = onPermissionGranted,
        onPermissionDenied = {
            // 权限被拒绝时的处理
        }
    )

    val selectedRouteModeIcon = remember(uiState.selectedRouteMode, uiState.selectedSportType) {
        uiState.selectedRouteMode.getIconBySportType(uiState.selectedSportType.orDefault())
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 地图视图
        MapView(
            uiState = uiState,
            selectedIcon = selectedRouteModeIcon,
            onMapClick = onMapClick,
            onWaypointClick = onWaypointClick,
            onTypeClick = dialogController::showRouteMenuDialog,
            onModeClick = dialogController::showEditRouteDialog,
            onSearchClick = onSearchClick,
            onLocateClick = onLocateClick,
            onScreenshotTaken = onScreenshotTaken,
        )

        // 底部内容 - 海拔图表
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter)
        ) {
            // 提示文字（当没有路线或正在加载时显示）
            HintMessage(
                selectedRouteMode = uiState.selectedRouteMode,
                hasRoute = uiState.hasTracks,
                uiState = uiState,
                locationsCount = uiState.locations.size,
                selectedTrackPoint = uiState.selectedTrackPoint,
                overviewState = uiState.overviewState
            )

            // 底部按钮栏 - 撤销、重做和保存按钮
            // 当操作位置点时不显示撤销和恢复按钮
            val isOperatingLocationPoint =
                uiState.selectedRouteMode == RouteMode.ADD_LOCATION
                        && uiState.selectedTrackPoint != null
                        && uiState.overviewState != OverviewState.Overview1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
                    .height(60.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isOperatingLocationPoint) {
                    UndoRedo(
                        canUndo = uiState.canUndo,
                        canRedo = uiState.canRedo,
                        undo = undo,
                        redo = redo
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 保存按钮 - 只在有轨迹时显示
                if (uiState.hasTracks) {
                    Button(onClick = {
                        onSaveClick(name)
                    }) {
                        Text("保存")
                    }
                }
            }

            when (uiState.overviewState) {
                OverviewState.Overview1 -> Overview1(tracks = uiState.route?.tracks ?: emptyList())

                OverviewState.Overview2 -> {
                    Overview2(
                        tracks = uiState.route?.tracks ?: emptyList(),
                        onChartScroll = onChartScroll,
                        selectedTrackPoint = selectedTrackPoint,
                        onAddWaypoint = onAddWaypoint,
                        onCancelWaypoint = onCancelWaypoint
                    )
                }

                OverviewState.Overview3 -> {
                    Overview3(
                        selectedWaypoint = selectedWaypoint,
                        onUpdateWaypoint = onUpdateWaypoint,
                        onDeleteWaypoint = onDeleteWaypoint,
                    )
                }
            }
        }

        // 加载指示器
        Loading(visible = uiState.isLoading)

        // 统一错误处理
        LaunchedEffect(uiState.error) {
            uiState.error?.let { error ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }

        DialogHost(dialogController)
    }
}
