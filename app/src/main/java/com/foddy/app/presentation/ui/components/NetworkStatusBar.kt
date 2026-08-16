package com.foddy.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun NetworkStatusBar(isOnline: Boolean) {
    var showDisconnected by remember { mutableStateOf(false) }
    var showConnected by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            showDisconnected = true
            showConnected = false
        } else if (showDisconnected) {
            showConnected = true
            delay(2000)
            showConnected = false
            showDisconnected = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = showDisconnected && !showConnected,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            StatusBarContent(
                message = "Không có kết nối mạng",
                backgroundColor = Color.Red.copy(alpha = 0.8f)
            )
        }

        AnimatedVisibility(
            visible = showConnected,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            StatusBarContent(
                message = "Đã khôi phục kết nối",
                backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun StatusBarContent(message: String, backgroundColor: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
