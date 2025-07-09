package com.github.speak2me.app.compose.map.route.plan.components

import androidx.activity.compose.LocalActivity
import androidx.annotation.ColorInt
import androidx.annotation.ColorLong
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.github.speak2me.app.compose.map.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// region ==================== 对话框规范接口 ====================
interface DialogSpec {
    @Composable
    fun Content(onDismiss: () -> Unit)
}
// endregion

// region ==================== 对话框上下文 ====================
data class DialogContext(
    val selectedSportType: SportType?,
    val onSportTypeSelected: (SportType) -> Unit,
    val selectedRouteMode: RouteMode,
    val onRouteModeSelected: (RouteMode) -> Unit,
    val hasTracks: Boolean,
    val onSwapStartEnd: () -> Unit,
    val onReverseRoute: () -> Unit,
    val isReverseRouteUsed: Boolean = false,
)
// endregion

// region ==================== 枚举定义 ====================
fun SportType?.orDefault(): SportType {
    return this ?: SportType.OUTDOOR_RUNNING
}

enum class SportType(
    @field:DrawableRes val icon: Int,
    val text: String,
    @field:ColorLong val color: Long,
    val profile: String,
) {
    OUTDOOR_RUNNING(R.drawable.ic_run_30, "户外跑", 0xFFEB6458, "outdoor"),
    TRAIL_RUNNING(R.drawable.ic_trail_running_30, "越野跑", 0xFFEB6458, "trail"),
    OUTDOOR_HIKING(R.drawable.ic_on_foot_30, "户外徒步", 0xFFFFA100, "hike"),
    WALKING(R.drawable.ic_walk_30, "健走", 0xFFF5793A, "walk"),
    OUTDOOR_CYCLING(R.drawable.ic_cycling_30, "户外骑行", 0xFF1CBF7E, "bike");
}

enum class RouteMode(private val text: String, @field:DrawableRes val icon: Int) {
    ROUTE_PLANNING("路线规划", R.drawable.menu0),
    DIRECT_DRAWING("直接绘制", R.drawable.menu2),
    ADD_LOCATION("添加位置", R.drawable.menu3);

    fun getTextBySportType(sportType: SportType, hasTracks: Boolean = true): String {
        return when (this) {
            ROUTE_PLANNING -> "${sportType.text}$text"
            ADD_LOCATION -> if (hasTracks) text else "$text（请先创建路线）"
            else -> text
        }
    }

    fun getIconBySportType(sportType: SportType): Int {
        return when (this) {
            ROUTE_PLANNING -> if (sportType == SportType.OUTDOOR_CYCLING) R.drawable.menu1 else R.drawable.menu0
            DIRECT_DRAWING -> R.drawable.menu2
            ADD_LOCATION -> R.drawable.menu3
        }
    }
}
// endregion

// DialogState 定义
sealed interface DialogState {
    data object Disclaimer : DialogState
    data object SportType : DialogState
    data object RouteMode : DialogState
    data object RouteMenu : DialogState
    data object SaveRoute : DialogState
    data object ExitRoute : DialogState
}

// 通用运动类型选择对话框组件
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportTypeDialog(
    onDismiss: () -> Unit,
    selectedSportType: SportType?,
    onSportTypeSelected: (SportType) -> Unit,
) {
    val activity = LocalActivity.current
    DialogLayout(
        title = "选择运动",
        subtitle = "将规划适合该运动的路线",
        onCloseClick = {
            if (selectedSportType != null) onDismiss() else activity?.finish()
        }
    ) {
        SportType.entries.forEachIndexed { index, sportType ->
            DialogListTile(
                drawable = sportType.icon,
                text = sportType.text,
                color = sportType.color.toInt(),
                checked = if (selectedSportType != null) selectedSportType == sportType else null,
                onClick = {
                    onSportTypeSelected(sportType)
                    onDismiss()
                }
            )
            if (index != SportType.entries.lastIndex) HorizontalDivider(color = Color.LightGray)
        }
    }
}

class SportTypeDialogSpec(
    private val controller: DialogController,
) : DialogSpec {
    @Composable
    override fun Content(onDismiss: () -> Unit) {
        SportTypeDialog(
            onDismiss = onDismiss,
            selectedSportType = controller.context.selectedSportType,
            onSportTypeSelected = controller.context.onSportTypeSelected,
        )
    }
}

// 编辑模式选择对话框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteModeDialog(
    onDismiss: () -> Unit,
    selectedSportType: SportType,
    selectedRouteMode: RouteMode,
    onRouteModeSelected: (RouteMode) -> Unit,
    hasTracks: Boolean,
) {
    DialogLayout(
        title = "选择绘制工具",
        subtitle = "更换后不影响已规划的路线",
        onCloseClick = onDismiss
    ) {
        RouteMode.entries.forEachIndexed { index, mode ->
            val isOptionEnabled = if (mode == RouteMode.ADD_LOCATION) hasTracks else true
            DialogListTile(
                drawable = mode.getIconBySportType(selectedSportType),
                text = mode.getTextBySportType(selectedSportType, hasTracks),
                checked = selectedRouteMode == mode,
                enabled = isOptionEnabled,
                onClick = {
                    onRouteModeSelected(mode)
                    onDismiss()
                }
            )
            if (index != RouteMode.entries.lastIndex) HorizontalDivider(color = Color.LightGray)
        }
    }
}

class RouteModeDialogSpec(
    private val context: DialogContext,
) : DialogSpec {
    @Composable
    override fun Content(onDismiss: () -> Unit) {
        RouteModeDialog(
            onDismiss = onDismiss,
            selectedRouteMode = context.selectedRouteMode,
            selectedSportType = context.selectedSportType.orDefault(),
            onRouteModeSelected = context.onRouteModeSelected,
            hasTracks = context.hasTracks
        )
    }
}

// 路线操作对话框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMenuDialog(
    onDismiss: () -> Unit,
    selectedSportType: SportType,
    onShowSportTypeDialog: () -> Unit,
    onSwapStartEnd: () -> Unit,
    onReverseRoute: () -> Unit,
    hasTracks: Boolean,
    isReverseRouteUsed: Boolean = false,
) {
    DialogLayout(title = "选择运动", onCloseClick = onDismiss) {
        // 显示当前运动类型
        DialogListTile(
            drawable = selectedSportType.icon,
            text = selectedSportType.text,
            color = selectedSportType.color.toInt(),
            indicator = true,
            onClick = onShowSportTypeDialog
        )
        HorizontalDivider(color = Color.LightGray)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MenuItem(
                modifier = Modifier.weight(1f),
                enabled = hasTracks,
                imageVector = Icons.Default.SwapHoriz,
                text = "起止点互换",
                onClick = {
                    onDismiss()
                    onSwapStartEnd()
                }
            )
            MenuItem(
                modifier = Modifier.weight(1f),
                enabled = hasTracks && !isReverseRouteUsed,
                imageVector = Icons.Default.Refresh,
                text = "原路返回",
                onClick = {
                    onDismiss()
                    onReverseRoute()
                }
            )
        }
    }
}

@Composable
private fun MenuItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = false,
    imageVector: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = imageVector,
            tint = if (enabled) Color.Black else Color.Gray,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .background(Color.LightGray, CircleShape)
                .padding(4.dp)
        )
        Text(
            text = text,
            modifier = Modifier.padding(top = 4.dp),
            color = if (enabled) Color.Black else Color.Gray
        )
    }
}

class RouteMenuDialogSpec(
    private val controller: DialogController,
) : DialogSpec {
    @Composable
    override fun Content(onDismiss: () -> Unit) {
        RouteMenuDialog(
            onDismiss = onDismiss,
            selectedSportType = controller.context.selectedSportType.orDefault(),
            onShowSportTypeDialog = controller::showSportTypeDialog,
            onSwapStartEnd = controller.context.onSwapStartEnd,
            onReverseRoute = controller.context.onReverseRoute,
            hasTracks = controller.context.hasTracks,
            isReverseRouteUsed = controller.context.isReverseRouteUsed
        )
    }
}

// region ==================== 对话框控制器 ====================

class DialogController(var context: DialogContext) {
    private val _currentDialog = MutableStateFlow<DialogSpec?>(null)
    val currentDialog: StateFlow<DialogSpec?> = _currentDialog.asStateFlow()

    // 对话框堆栈，用于管理对话框层级
    private val _dialogStack = mutableListOf<DialogState>()

    // 当前对话框状态
    private val _currentState = MutableStateFlow<DialogState?>(null)

    fun showDialog(state: DialogState) {
        // 将当前对话框推入堆栈
        _currentState.value?.let { currentState ->
            _dialogStack.add(currentState)
        }

        // 显示新对话框
        _currentState.value = state
        _currentDialog.value = createDialogSpec(state)
    }

    fun dismiss() {
        // 从堆栈中弹出上一个对话框
        val previousState = _dialogStack.removeLastOrNull()

        if (previousState != null) {
            // 显示上一个对话框
            _currentState.value = previousState
            _currentDialog.value = createDialogSpec(previousState)
        } else {
            // 堆栈为空，关闭所有对话框
            _currentState.value = null
            _currentDialog.value = null
        }
    }

    private fun createDialogSpec(state: DialogState): DialogSpec {
        return when (state) {
            DialogState.Disclaimer -> TODO()
            DialogState.SportType -> SportTypeDialogSpec(this)
            DialogState.RouteMode -> RouteModeDialogSpec(context)
            DialogState.RouteMenu -> RouteMenuDialogSpec(this)
            DialogState.ExitRoute -> TODO()
            DialogState.SaveRoute -> TODO()
        }
    }
}
// endregion

// region ==================== 对外暴露的 Hook 函数 ====================

@Composable
fun rememberDialogState(
    selectedSportType: SportType?,
    onSportTypeSelected: (SportType) -> Unit,
    selectedRouteMode: RouteMode,
    onRouteModeSelected: (RouteMode) -> Unit,
    hasTracks: Boolean,
    onSwapStartEnd: () -> Unit,
    onReverseRoute: () -> Unit,
    isReverseRouteUsed: Boolean = false,
): DialogController {

    val context = remember(selectedSportType, selectedRouteMode, hasTracks, isReverseRouteUsed) {
        DialogContext(
            selectedSportType = selectedSportType,
            onSportTypeSelected = onSportTypeSelected,
            selectedRouteMode = selectedRouteMode,
            onRouteModeSelected = onRouteModeSelected,
            hasTracks = hasTracks,
            onSwapStartEnd = onSwapStartEnd,
            onReverseRoute = onReverseRoute,
            isReverseRouteUsed = isReverseRouteUsed
        )
    }

    return remember {
        DialogController(context)
    }.also {
        it.context = context
    }
}
// endregion

// region ==================== 扩展函数 ====================

fun DialogController.showSportTypeDialog() = showDialog(DialogState.SportType)
fun DialogController.showEditRouteDialog() = showDialog(DialogState.RouteMode)
fun DialogController.showRouteMenuDialog() = showDialog(DialogState.RouteMenu)
// endregion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogHost(controller: DialogController) {
    val currentDialog by controller.currentDialog.collectAsState()

    currentDialog?.let { dialog ->
        ModalBottomSheet(
            onDismissRequest = controller::dismiss,
            sheetState = rememberModalBottomSheetState(),
            dragHandle = null,
            properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
            content = { dialog.Content(onDismiss = controller::dismiss) }
        )
    }
}

// <editor-fold desc="对话框布局" defaultstate="collapsed">
@Composable
private fun DialogLayout(
    title: String,
    subtitle: String? = null,
    onCloseClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.padding(vertical = 18.dp, horizontal = 28.dp)) {
        DialogTitle(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            title = title,
            onCloseClick = onCloseClick
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 18.dp),
                text = subtitle,
            )
        }
        content(this)
    }
}
// </editor-fold>

// <editor-fold desc="对话框标题" defaultstate="collapsed">
@Composable
private fun DialogTitle(
    modifier: Modifier = Modifier,
    title: String,
    onCloseClick: () -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp),
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Close, contentDescription = "关闭")
        }
    }
}
// </editor-fold>

// <editor-fold desc="对话框列表项" defaultstate="collapsed">
@Composable
private fun DialogListTile(
    @DrawableRes drawable: Int,
    text: String,
    @ColorInt color: Int = -1,
    checked: Boolean? = null,
    indicator: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val colorState = @Composable {
        if (enabled) Color.Black else Color.Black.copy(alpha = 0.3f)
    }
    Row(
        modifier = Modifier
            .height(52.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            imageVector = ImageVector.vectorResource(drawable),
            contentDescription = null,
            tint = if (color == -1) colorState() else Color(color),
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            text = text,
            color = colorState(),
        )
        when {
            indicator -> {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = null,
                )
            }

            checked != null -> {
                RadioButton(
                    modifier = Modifier.size(36.dp),
                    selected = checked,
                    enabled = enabled,
                    onClick = null,
                )
            }
        }
    }
}
// </editor-fold>
