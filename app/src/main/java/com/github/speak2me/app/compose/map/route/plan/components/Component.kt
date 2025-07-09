package com.github.speak2me.app.compose.map.route.plan.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material.icons.filled.CompassCalibration
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.Layers
//import androidx.compose.material.icons.filled.Menu
//import androidx.compose.material.icons.filled.MyLocation
//import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.route.plan.RoutePlan2UiState
import com.tencent.gaya.foundation.api.interfaces.Visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InternalTextField(
    modifier: Modifier = Modifier,
    value: String,
    hint: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        modifier = modifier.height(48.dp),
        singleLine = true,
        onValueChange = onValueChange,
        cursorBrush = SolidColor(Color.Black),
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.DecorationBox(
                placeholder = {
                    Text(
                        hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = trailingIcon,
                value = value,
                singleLine = true,
                enabled = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                innerTextField = innerTextField,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                    textColor = RainbowTheme.colors.c0, // 不生效
//                    focusedIndicatorColor = Color.Transparent,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
            )
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun Loading(visible: Boolean = false) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter(onTouchEvent = { true })
        ) {
            Column(
                modifier = Modifier
                    .align(BiasAlignment(0f, -0.1f))
                    .background(Color(0xd0000000), RoundedCornerShape(8.dp))
                    .height(120.dp)
                    .widthIn(min = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White)
                Text(
                    text = "Loading...",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MapTitle(
    start: @Composable BoxScope.() -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .height(40.dp)
    ) {
        start(this)
        IconButton(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd), onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "close",
            )
        }
    }
}

@Composable
fun MapPanel(
    modifier: Modifier = Modifier,
    showCompass: Boolean = false,
    @DrawableRes selectedIcon: Int,
    onCompassClick: () -> Unit,
    onLayerClick: () -> Unit,
    onLocateClick: () -> Unit,
    onTypeClick: () -> Unit,
    onModeClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp, top = 80.dp)
            .wrapContentSize(),
    ) {
        AnimatedVisibility(visible = showCompass) {
            FloatingIconButton(
                icon = ImageVector.vectorResource(R.drawable.action0),
                onClick = onCompassClick
            )
        }
        FloatingIconButton(
            icon = ImageVector.vectorResource(R.drawable.action1),
            onClick = onLayerClick
        )
        FloatingIconButton(
            icon = ImageVector.vectorResource(R.drawable.action2),
            onClick = onLocateClick
        )
        FloatingIconButton(
            icon = ImageVector.vectorResource(R.drawable.action3),
            onClick = onTypeClick
        )
        FloatingIconButton(
            icon = ImageVector.vectorResource(selectedIcon),
            onClick = onModeClick
        )
        FloatingIconButton(
            icon = ImageVector.vectorResource(R.drawable.action5),
            onClick = onSearchClick
        )
    }
}

@Composable
fun UndoRedo(canUndo: Boolean, canRedo: Boolean, undo: () -> Unit, redo: () -> Unit) {
    Row {
        AnimatedVisibility(canUndo, enter = fadeIn(), exit = fadeOut()) {
            // 撤销按钮
            FloatingIconButton(
                icon = Icons.AutoMirrored.Filled.Undo,
                onClick = undo,
            )
        }
        AnimatedVisibility(canRedo, enter = fadeIn(), exit = fadeOut()) {
            // 恢复按钮
            FloatingIconButton(
                icon = Icons.AutoMirrored.Filled.Redo,
                onClick = redo,
            )
        }
    }
}

@Composable
fun FloatingIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color.Black,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = backgroundColor,
        elevation = FloatingActionButtonDefaults.elevation(4.dp),
        shape = CircleShape,
        modifier = modifier
            .padding(vertical = 5.dp)
            .size(48.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.Unspecified)
    }
}

// 提示组件
@Composable
fun HintMessage(
    modifier: Modifier = Modifier,
    selectedRouteMode: RouteMode,
    uiState: RoutePlan2UiState,
    hasRoute: Boolean,
    locationsCount: Int,
    selectedTrackPoint: Int? = null,
    overviewState: OverviewState,
) {
    AnimatedVisibility(
        modifier = if (hasRoute) modifier else {
            modifier.padding(bottom = 100.dp) // 没有图表时距离底部150dp
        },
        visible = (selectedRouteMode == RouteMode.ADD_LOCATION) || !hasRoute || locationsCount <= 1,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val text = when {
            !hasRoute -> when (locationsCount) {
                0 -> "请点击地图添加起点"
                1 -> "请点击地图添加终点"
                else -> ""
            }

            selectedRouteMode == RouteMode.ADD_LOCATION -> "请在轨迹上添加位置"
            overviewState == OverviewState.Overview2 -> "滑动海拔曲线确定轨迹"
            else -> ""
        }
        if (text.isNotBlank()) {
            Box(
                modifier = Modifier
                    .background(Color.Black, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
internal fun rememberKeyboardVisible(): State<Boolean> {
    val density = LocalDensity.current
    val imeBottomState = rememberUpdatedState(newValue = WindowInsets.ime.getBottom(density))

    return produceState(initialValue = false) {
        snapshotFlow { imeBottomState.value > 0 }
            .distinctUntilChanged()
            .collectLatest { value = it }
    }
}
