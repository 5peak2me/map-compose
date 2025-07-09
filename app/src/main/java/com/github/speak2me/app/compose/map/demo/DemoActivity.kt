package com.github.speak2me.app.compose.map.demo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.demo.amap.AMapActivity
import com.github.speak2me.app.compose.map.demo.baidu.BaiduMapActivity
import com.github.speak2me.app.compose.map.demo.google.GoogleMapActivity
import com.github.speak2me.app.compose.map.demo.google.test.MainActivity
import com.github.speak2me.app.compose.map.demo.tencent.TencentMapActivity
import com.github.speak2me.app.compose.map.route.plan.RoutePlanActivity
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme

class DemoActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                        .systemBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.padding(10.dp))
                        Text(
                            text = getString(R.string.main_activity_title),
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.padding(10.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, RoutePlanActivity::class.java))
                            }) {
                            Text("路线规划")
                        }

                        Spacer(modifier = Modifier.padding(10.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, AMapActivity::class.java))
                            }) {
                            Text("AMap")
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, BaiduMapActivity::class.java))
                            }) {
                            Text("BaiduMap")
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, GoogleMapActivity::class.java))
                            }) {
                            Text("GoogleMap")
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, TencentMapActivity::class.java))
                            }) {
                            Text("TencentMap")
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, MainActivity::class.java))
                            }) {
                            Text("点击示例")
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                    }
                }
            }
        }
    }

}