package com.github.speak2me.app.compose.map.route.plan.search

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

internal class SearchViewModel(
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private val location = savedStateHandle.toRoute<Search>()

    var searchQuery by mutableStateOf("1")
        private set

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<SearchUiState> = snapshotFlow { searchQuery }
        .debounce(300L) // 减少延迟，提升响应速度
        .distinctUntilChanged() // 避免重复查询
        .onStart { emit("") } // 初始状态
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(SearchUiState.Success())
            } else {
                val locations = listOf(
                    Location("合肥南站", 31.798383, 117.291131),
                    Location("Beijing", 39.90403, 116.407525),
                    Location("Shanghai", 31.222222, 121.458056),
                    Location("Guangzhou", 23.129109, 113.280637),
                    Location("Shenzhen", 22.547, 114.0579),
                )
                flowOf(locations).map { results ->
                    if (results.isEmpty()) {
                        SearchUiState.Empty
                    } else {
                        SearchUiState.Success(results)
                    }
                }
                .onStart { emit(SearchUiState.Loading) }
            }
        }
        .catch {
            emit(SearchUiState.Empty)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState.Success()
        )

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    fun onSearchQueryClear() {
        searchQuery = ""
    }

}

fun Location(name: String, latitude: Double, longitude: Double) = Location(name).apply {
    this.latitude = latitude
    this.longitude = longitude
}

internal sealed interface SearchUiState {
    data object Loading : SearchUiState
    data object Empty : SearchUiState
    data class Success(val data: List<Location> = emptyList()) : SearchUiState
}