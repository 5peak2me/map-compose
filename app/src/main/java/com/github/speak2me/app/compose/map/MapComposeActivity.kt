package com.github.speak2me.app.compose.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme

class MapComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapComposeTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapComposeScreen(
                        modifier = Modifier.fillMaxSize(),
                        titleBarColor = Color(window.statusBarColor),
                        bottomBarColor = Color(window.navigationBarColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun MapComposeScreen(
    modifier: Modifier = Modifier,
    titleBarColor: Color,
    bottomBarColor: Color,
) {
    Column(modifier = modifier) {
        MapComposeTopBar(
            color = titleBarColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )
        MapCompose(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        MapComposeBottomBar(
            color = bottomBarColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        )
    }
}

@Composable
private fun MapComposeTopBar(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(color),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "离线地图",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun MapComposeBottomBar(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(color))
}
