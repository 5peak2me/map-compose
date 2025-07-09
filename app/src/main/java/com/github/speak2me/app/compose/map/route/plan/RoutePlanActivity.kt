package com.github.speak2me.app.compose.map.route.plan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.speak2me.app.compose.map.route.plan.search.Search
import com.github.speak2me.app.compose.map.route.plan.search.SearchScreen

class RoutePlanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                NavHost(
                    navController = navController,
                    startDestination = "map"
                ) {
                    composable("map") {
                        // 从导航状态中获取传递过来的经纬度
                        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                        val location = savedStateHandle?.get<android.location.Location>(KEY_MAP_LOCATION)
                        
                        RoutePlan2(
                            onSearchClick = {
                                navController.navigate(Search(0.0, 0.0))
                            },
                            initialSearchLocation = location
                        )
                        
                        // 清除传递的数据，避免重复使用
                        LaunchedEffect(location) {
                            if (location != null) {
                                savedStateHandle.remove<android.location.Location>(KEY_MAP_LOCATION)
                            }
                        }
                    }
                    composable<Search> {
                        SearchScreen(onBackClick = navController::popBackStack) {
                            navController.previousBackStackEntry?.savedStateHandle?.set(KEY_MAP_LOCATION, it)
//                            navController.popBackStack()
                            navController.popBackStack("map", inclusive = false)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "RoutePlanActivity"
        private const val KEY_MAP_LOCATION = "map_location"

        // 标记是否是从我的路线跳转的
        private const val NAV_FROM_MY_ROUTE = "nav_from_my_route"

        // 表示路线的id
        private const val ROUTE_ID = "route_id"
        private const val ROUTE_TYPE = "route_type"
        private const val SAVE_PATH_LIST = "save_path_list"
    }

}