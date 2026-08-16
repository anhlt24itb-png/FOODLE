package com.foddy.app.presentation.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.foddy.app.domain.model.Location
import com.foddy.app.domain.model.OrderChatMessage
import com.foddy.app.presentation.components.map.OSMView
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.viewmodel.OrderViewModel
import com.foddy.app.presentation.ui.theme.Primary
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import timber.log.Timber

@Composable
fun OrderStatusTimeline(currentStatus: String) {
    val statuses = listOf("PENDING", "CONFIRMED", "PREPARING", "DELIVERING", "COMPLETED")
    val currentStep = statuses.indexOf(currentStatus).coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        statuses.forEachIndexed { index, _ ->
            val isActive = index <= currentStep
            val color = if (isActive) Primary else Color.LightGray
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
            
            if (index < statuses.size - 1) {
                val lineColor = if (index < currentStep) Primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(1f)
                        .background(lineColor)
                )
            }
        }
    }
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val labels = listOf("Đã đặt", "Xác nhận", "Chế biến", "Giao hàng", "Đến nơi")
        labels.forEachIndexed { index, label ->
            Text(
                text = label,
                fontSize = 8.sp,
                color = if (index <= currentStep) Color.Black else Color.Gray,
                fontWeight = if (index <= currentStep) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    navController: NavController, 
    orderId: String,
    orderViewModel: OrderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentOrder by orderViewModel.currentOrder.collectAsStateWithLifecycle()
    val driverLocState by orderViewModel.driverLocation.collectAsStateWithLifecycle()
    val chatMessages by orderViewModel.chatMessages.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(orderId) {
        orderViewModel.trackOrder(orderId)
    }

    val driverName = currentOrder?.driverName ?: "Đang tìm tài xế..."
    val driverPhone = "0987654321"
    val driverAvatar = if (currentOrder?.driverId != null) 
        "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=200" 
        else "https://via.placeholder.com/150"
    
    var showChatDialog by remember { mutableStateOf(false) }
    var chatMessage by remember { mutableStateOf("") }

    // Real-time location from Driver
    val driverLocation = remember(driverLocState, currentOrder) {
        driverLocState?.let { Location(it.lat, it.lng) }
            ?: currentOrder?.driverLocation 
            ?: Location(21.0285, 105.8542)
    }
    val userLocation = currentOrder?.customerLocation ?: Location(21.027, 105.850)

    // Xử lý thông báo Real-time khi trạng thái thay đổi
    LaunchedEffect(currentOrder?.status) {
        Timber.d("Order status changed to: ${currentOrder?.status}")
        when(currentOrder?.status) {
            "CONFIRMED" -> {
                snackbarHostState.showSnackbar("Đơn hàng của bạn đã được nhà hàng xác nhận!")
            }
            "PREPARING" -> {
                snackbarHostState.showSnackbar("Nhà hàng đang chế biến món ăn cho bạn!")
            }
            "DELIVERING" -> {
                snackbarHostState.showSnackbar("Tài xế đã lấy hàng và đang trên đường giao!")
            }
            "COMPLETED" -> {
                snackbarHostState.showSnackbar("Đơn hàng đã hoàn thành. Cảm ơn bạn!")
            }
        }
    }

    // Dialog khi hoàn thành đơn hàng
    if (currentOrder?.status == "COMPLETED") {
        AlertDialog(
            onDismissRequest = { /* Không cho tắt */ },
            title = { Text("Tuyệt vời! 🎉") },
            text = { Text("Đơn hàng của bạn đã được giao thành công. Bạn có muốn đánh giá dịch vụ không?") },
            confirmButton = {
                Button(onClick = { 
                    navController.navigate(Screen.OrderReview.createRoute(orderId))
                }) { Text("Đánh giá ngay") }
            },
            dismissButton = {
                TextButton(onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }) { Text("Để sau") }
            }
        )
    }

    if (showChatDialog) {
        AlertDialog(
            onDismissRequest = { showChatDialog = false },
            title = { Text("Chat với tài xế") },
            text = {
                Column {
                    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                    LaunchedEffect(chatMessages.size) {
                        if (chatMessages.isNotEmpty()) {
                            listState.animateScrollToItem(chatMessages.size - 1)
                        }
                    }

                    Box(modifier = Modifier.height(250.dp).fillMaxWidth()) {
                        LazyColumn(state = listState) {
                            items(chatMessages) { message ->
                                val isMe = message.senderId == (currentOrder?.userId ?: "")
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                                ) {
                                    Surface(
                                        color = if (isMe) Primary else Color(0xFFF0F0F0),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.widthIn(max = 200.dp)
                                    ) {
                                        Text(
                                            text = message.message,
                                            modifier = Modifier.padding(8.dp),
                                            color = if (isMe) Color.White else Color.Black,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = chatMessage,
                        onValueChange = { chatMessage = it },
                        label = { Text("Tin nhắn") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (chatMessage.isNotBlank() && currentOrder != null) {
                        orderViewModel.sendChatMessage(
                            message = chatMessage,
                            senderId = currentOrder!!.userId,
                            receiverId = currentOrder!!.driverId ?: "",
                            orderId = orderId
                        )
                        chatMessage = ""
                    }
                }) {
                    Text("Gửi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChatDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Theo dõi đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        orderViewModel.trackOrder(orderId)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Using OSMView instead of GoogleMap
            OSMView(
                modifier = Modifier.fillMaxSize(),
                userLocation = userLocation,
                driverLocation = driverLocation
            )

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Trạng thái", fontSize = 12.sp, color = Color.Gray)
                            Text(text = when(currentOrder?.status) {
                                "PENDING" -> "Đang chờ quán nhận..."
                                "CONFIRMED" -> "Quán đã nhận đơn"
                                "PREPARING" -> "Quán đang chuẩn bị"
                                "DELIVERING" -> "Đang giao hàng"
                                "COMPLETED" -> "Đã hoàn thành"
                                else -> "Đang xử lý"
                            }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
                        }
                        Text(text = "15-20 phút", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Timeline
                    OrderStatusTimeline(currentStatus = currentOrder?.status ?: "pending")

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = driverAvatar,
                            contentDescription = "Driver",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(30.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = driverName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Tài xế của bạn", fontSize = 14.sp, color = Color.Gray)
                        }
                        Row {
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$driverPhone")
                                }
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Primary)
                            }
                            IconButton(onClick = { showChatDialog = true }) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
