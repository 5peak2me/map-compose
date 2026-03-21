package com.github.speak2me.app.compose.map.demo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.github.speak2me.app.compose.map.offline.MapComposeActivity
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.demo.amap.AMapActivity
import com.github.speak2me.app.compose.map.demo.baidu.BaiduMapActivity
import com.github.speak2me.app.compose.map.demo.google.GoogleMapActivity
import com.github.speak2me.app.compose.map.demo.google.test.MainActivity
import com.github.speak2me.app.compose.map.demo.tencent.TencentMapActivity
import com.github.speak2me.app.compose.map.demo.utils.LocationPermissionHelper.openLocationSettings
import com.github.speak2me.app.compose.map.route.plan.RoutePlanActivity
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme

class MapDemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = getString(R.string.main_activity_title),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, RoutePlanActivity::class.java))
                            }) {
                            Text("路线规划")
                        }
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, MapComposeActivity::class.java))
                            }) {
                            Text("离线地图")
                        }
                        MapMenu()
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, MainActivity::class.java))
                            }) {
                            Text("点击示例")
                        }
                        Button(
                            onClick = {
                                openLocationSettings(context)
                            }) {
                            Text("权限设置")
                        }
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, com.github.speak2me.app.compose.map.MainActivity::class.java))
                            }) {
                            Text("示例")
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun MapMenu() {
    val context = LocalContext.current
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_map_gaode),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = {
                    context.startActivity(Intent(context, AMapActivity::class.java))
                })
        )
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_map_baidu),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = {
                    context.startActivity(Intent(context, BaiduMapActivity::class.java))
                })
        )
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_map_google),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = {
                    context.startActivity(Intent(context, GoogleMapActivity::class.java))
                })
        )
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_map_tencent),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = {
                    context.startActivity(Intent(context, TencentMapActivity::class.java))
                })
        )
    }
}
