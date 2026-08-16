package com.foddy.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.foddy.app.domain.model.OrderChatMessage
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.model.User
import com.foddy.app.presentation.ui.state.UiState
import com.foddy.app.presentation.ui.theme.Primary
import com.foddy.app.presentation.viewmodel.OrderViewModel
import com.foddy.app.presentation.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverAppScreen(
    navController: NavController,
    orderViewModel: OrderViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    var isOnline by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showChatDialog by remember { mutableStateOf(false) }
    var activeChatOrderId by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val allOrders by orderViewModel.pendingOrders.collectAsStateWithLifecycle()
    val isSimulating by orderViewModel.isSimulating.collectAsStateWithLifecycle()
    val orderState by orderViewModel.orderState.collectAsStateWithLifecycle()
    val user by userViewModel.user.collectAsStateWithLifecycle()
    val chatMessages by orderViewModel.chatMessages.collectAsStateWithLifecycle()
    
    val currentDriverId = user.id
    val tabs = listOf("Đơn mới", "Đang giao", "Cá nhân")

    LaunchedEffect(Unit) {
        orderViewModel.listenToPendingOrders()
    }

    LaunchedEffect(orderState) {
        when (orderState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar((orderState as UiState.Success<String>).data)
                orderViewModel.resetOrderState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((orderState as UiState.Error).message)
                orderViewModel.resetOrderState()
            }
            else -> {}
        }
    }

    val filteredOrders by remember(allOrders, isOnline, currentDriverId, selectedTab) {
        derivedStateOf {
            if (!isOnline && selectedTab != 2) emptyList()
            else {
                when (selectedTab) {
                    0 -> allOrders.filter {
                        (it.status == "PENDING" || it.status == "CONFIRMED" || it.status == "PREPARING") &&
                                (it.driverId.isNullOrEmpty() || it.driverId == "DRIVER_REQUIRED")
                    }
                    1 -> allOrders.filter {
                        (it.status == "PREPARING" || it.status == "DELIVERING") &&
                                !it.driverId.isNullOrEmpty() && it.driverId == currentDriverId
                    }
                    else -> emptyList()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Trung tâm Tài xế", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = { orderViewModel.listenToPendingOrders() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Làm mới", tint = Primary)
                        }
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { isOnline = it },
                            modifier = Modifier.scale(0.8f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4CAF50)
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (selectedTab == 2) {
                DriverProfileSection(user, userViewModel, navController)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isOnline) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Bạn đang ngoại tuyến. Bật trực tuyến để nhận đơn.")
                            }
                        }
                    } else if (filteredOrders.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Không có đơn hàng nào phù hợp.")
                            }
                        }
                    } else {
                        items(filteredOrders) { order ->
                            val buttonText = when (order.status) {
                                "PENDING", "CONFIRMED" -> "Nhận đơn"
                                "PREPARING" -> "Đã lấy hàng"
                                "DELIVERING" -> "Hoàn thành"
                                else -> "Nhận đơn"
                            }
                            
                            OrderRequestCard(
                                orderId = order.id,
                                restaurant = order.restaurantName,
                                address = order.address,
                                price = "${order.totalPrice}đ",
                                status = if (selectedTab == 0) "ĐƠN MỚI" else "ĐANG GIAO",
                                buttonText = buttonText,
                                isSimulating = isSimulating,
                                onSimulateToggle = { 
                                    if (isSimulating) orderViewModel.stopLocationSimulation()
                                    else orderViewModel.startLocationSimulation(order.id)
                                },
                                onChatClick = if (selectedTab == 1) {
                                    {
                                        activeChatOrderId = order.id
                                        orderViewModel.trackOrder(order.id)
                                        showChatDialog = true
                                    }
                                } else null,
                                onAccept = {
                                    when (order.status) {
                                        "PENDING", "CONFIRMED" -> orderViewModel.acceptOrder(order.id, currentDriverId, user.name)
                                        "PREPARING" -> orderViewModel.updateOrderStatus(order.id, "DELIVERING")
                                        "DELIVERING" -> orderViewModel.updateOrderStatus(order.id, "COMPLETED")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showChatDialog) {
        val currentOrder by orderViewModel.currentOrder.collectAsStateWithLifecycle()
        ChatDialog(
            onDismiss = { showChatDialog = false },
            messages = chatMessages,
            currentUserId = currentDriverId,
            onSendMessage = { text ->
                currentOrder?.userId?.let { customerId ->
                    orderViewModel.sendChatMessage(activeChatOrderId, currentDriverId, customerId, text)
                }
            }
        )
    }
}

@Composable
fun DriverProfileSection(user: User, userViewModel: UserViewModel, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = user.avatar.ifEmpty { "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=200" },
            contentDescription = null,
            modifier = Modifier.size(100.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = user.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Tài xế chuyên nghiệp", color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoItem(icon = Icons.Default.Call, label = "Số điện thoại", value = user.phone.ifEmpty { "Chưa có" })
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                ProfileInfoItem(icon = Icons.Default.LocationOn, label = "Khu vực", value = user.address.ifEmpty { "Toàn thành phố" })
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                userViewModel.logout()
                navController.navigate("login") { popUpTo(0) { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Đăng xuất", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun OrderRequestCard(
    orderId: String,
    restaurant: String,
    address: String,
    price: String,
    status: String,
    buttonText: String,
    isSimulating: Boolean = false,
    onSimulateToggle: () -> Unit = {},
    onChatClick: (() -> Unit)? = null,
    onAccept: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (status.contains("ĐƠN MỚI")) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status,
                        color = if (status.contains("ĐƠN MỚI")) Color.Red else Color(0xFF1976D2),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(text = "#$orderId", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = restaurant, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = address, fontSize = 14.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = price, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                Row {
                    if (onChatClick != null) {
                        IconButton(onClick = onChatClick) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = Primary)
                        }
                    }
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(buttonText)
                    }
                }
            }
            
            if (status != "ĐƠN MỚI") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("Mô phỏng vị trí: ", fontSize = 12.sp)
                    Switch(
                        checked = isSimulating,
                        onCheckedChange = { onSimulateToggle() },
                        modifier = Modifier.scale(0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatDialog(
    onDismiss: () -> Unit,
    messages: List<OrderChatMessage>,
    currentUserId: String,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.8f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chat với khách hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        val isMe = message.senderId == currentUserId
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                color = if (isMe) Primary else Color(0xFFF1F1F1),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 16.dp
                                )
                            ) {
                                Text(
                                    text = message.message,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    color = if (isMe) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nhập tin nhắn...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText)
                                messageText = ""
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi", tint = Primary)
                    }
                }
            }
        }
    }
}
