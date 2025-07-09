package com.github.speak2me.app.compose.map.route.plan.search

import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.speak2me.app.compose.map.route.plan.components.InternalTextField
import com.github.speak2me.app.compose.map.route.plan.components.Loading
import kotlinx.serialization.Serializable

@Serializable
internal data class Search(val latitude: Double, val longitude: Double)

@Composable
internal fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onBackClick: () -> Unit,
    onItemClick: (Location) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        onBackClick = onBackClick,
        uiState = uiState,
        searchQuery = viewModel.searchQuery,
        onClearClick = viewModel::onSearchQueryClear,
        onValueChange = viewModel::onSearchQueryChange,
        onItemClick = onItemClick
    )
}

@Composable
private fun SearchScreen(
    uiState: SearchUiState,
    searchQuery: String,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    onValueChange: (String) -> Unit,
    onItemClick: (Location) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        TitleBar(
            value = searchQuery,
            onBackClick = onBackClick,
            onClearClick = onClearClick,
            onValueChange = onValueChange
        )
        when (uiState) {
            SearchUiState.Empty -> EmptyView()
            SearchUiState.Loading -> Loading()
            is SearchUiState.Success -> {
//                LocalSoftwareKeyboardController.current?.hide()
                LazyColumn {
                    itemsIndexed(uiState.data, key = { index, item -> "${item}_$index" }) { index, item ->
                        SearchItem(
                            query = searchQuery,
                            title = item.toString(),
                            subtitle = item.toString(),
                            onClick = {
                                onItemClick(item)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchItem(
    modifier: Modifier = Modifier,
    query: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val ranges = query.lowercase().findRangesIn(title.lowercase())
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 48.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Text(text = buildAnnotatedString {
            append(title)
            addStyle(
                style = SpanStyle(color = MaterialTheme.colorScheme.onPrimary),
                start = 0,
                end = title.length
            )
            if (query.isNotEmpty()) {
                for (range in ranges) {
                    addStyle(
                        style = SpanStyle(MaterialTheme.colorScheme.onPrimary),
                        start = range.first,
                        end = range.last + 1
                    )
                }
            }
        })
        Text(subtitle)
    }
}

private fun String.findRangesIn(text: String): List<IntRange> =
    toRegex().findAll(text).map { it.range }.toList()

@Composable
private fun TitleBar(
    value: String,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    onValueChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
//        focusRequester.requestFocus()
//        keyboard?.show()
    }
    Row(modifier = Modifier.padding(start = 12.dp, end = 16.dp, bottom = 8.dp, top = 6.dp)) {
        IconButton(onClick = onBackClick) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = null
            )
        }
        InternalTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1F),
            value = value,
            hint = "请输入搜索关键词",
            trailingIcon = {
                if (value.isEmpty()) null else {
                    IconButton(
                        modifier = Modifier
                            .background(Color.LightGray, CircleShape)
                            .size(20.dp),
                        onClick = onClearClick
                    ) {
                        Icon(
                            modifier = Modifier.size(10.dp),
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                        )
                    }
                }
            },
            onValueChange = onValueChange
        )
    }
}

@Composable
private fun EmptyView() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, BiasAlignment.Vertical(-0.2f))
    ) {
        Image(imageVector = Icons.Default.HourglassEmpty, contentDescription = "search fail")
        Text(
            text = "暂无搜索结果",
        )
    }
}

@Preview
@Composable
private fun SearchScreenPreview() {
    SearchScreen(
        uiState = SearchUiState.Success(listOf()),
//        uiState = SearchUiState.Empty,
        searchQuery = "合肥",
        onBackClick = {},
        onClearClick = {},
        onValueChange = {},
        onItemClick = {},
    )
}
