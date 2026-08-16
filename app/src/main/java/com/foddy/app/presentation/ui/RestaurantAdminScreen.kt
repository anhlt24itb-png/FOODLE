package com.foddy.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.presentation.viewmodel.AIViewModel
import com.foddy.app.presentation.viewmodel.MenuViewModel
import com.foddy.app.presentation.viewmodel.OrderViewModel
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.viewmodel.UserViewModel
import com.foddy.app.presentation.ui.theme.Primary
import com.foddy.app.core.Resource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantAdminScreen(
    navController: NavController,
    menuViewModel: MenuViewModel = hiltViewModel(),
    orderViewModel: OrderViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    restaurantViewModel: com.foddy.app.presentation.viewmodel.RestaurantViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Đơn hàng", "Thực đơn", "Thông tin quán", "Phân tích AI")
    val snackbarHostState = remember { SnackbarHostState() }
    
    val menuActionState by menuViewModel.actionState.collectAsStateWithLifecycle()
    val orderState by orderViewModel.orderState.collectAsStateWithLifecycle()
    val restaurantActionState by restaurantViewModel.actionState.collectAsStateWithLifecycle()

    LaunchedEffect(menuActionState) {
        when (menuActionState) {
            is com.foddy.app.presentation.ui.state.UiState.Success -> {
                snackbarHostState.showSnackbar((menuActionState as com.foddy.app.presentation.ui.state.UiState.Success<String>).data)
                menuViewModel.resetActionState()
            }
            is com.foddy.app.presentation.ui.state.UiState.Error -> {
                snackbarHostState.showSnackbar((menuActionState as com.foddy.app.presentation.ui.state.UiState.Error).message)
                menuViewModel.resetActionState()
            }
            else -> {}
        }
    }

    LaunchedEffect(orderState) {
        when (orderState) {
            is com.foddy.app.presentation.ui.state.UiState.Success -> {
                snackbarHostState.showSnackbar((orderState as com.foddy.app.presentation.ui.state.UiState.Success<String>).data)
                orderViewModel.resetOrderState()
            }
            is com.foddy.app.presentation.ui.state.UiState.Error -> {
                snackbarHostState.showSnackbar((orderState as com.foddy.app.presentation.ui.state.UiState.Error).message)
                orderViewModel.resetOrderState()
            }
            else -> {}
        }
    }

    LaunchedEffect(restaurantActionState) {
        when (restaurantActionState) {
            is com.foddy.app.presentation.ui.state.UiState.Success -> {
                snackbarHostState.showSnackbar((restaurantActionState as com.foddy.app.presentation.ui.state.UiState.Success<String>).data)
                restaurantViewModel.resetActionState()
            }
            is com.foddy.app.presentation.ui.state.UiState.Error -> {
                snackbarHostState.showSnackbar((restaurantActionState as com.foddy.app.presentation.ui.state.UiState.Error).message)
                restaurantViewModel.resetActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { 
            Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                CenterAlignedTopAppBar(
                    title = { 
                        Text("Quản lý Nhà hàng", fontWeight = FontWeight.ExtraBold) 
                    },
                    actions = {
                        IconButton(onClick = { 
                            when (selectedTab) {
                                0 -> orderViewModel.listenToRestaurantOrders(userViewModel.user.value.restaurantId ?: "res_001")
                                1 -> menuViewModel.observeMenu(userViewModel.user.value.restaurantId ?: "res_001")
                                2 -> orderViewModel.listenToRestaurantOrders(userViewModel.user.value.restaurantId ?: "res_001")
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Làm mới", tint = Primary)
                        }
                        IconButton(onClick = { 
                            userViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true } 
                            } 
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Đăng xuất", tint = Color.Gray)
                        }
                    }
                )
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Primary,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Primary
                            )
                        }
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color(0xFFF8F9FA))
        ) {
            when (selectedTab) {
                0 -> OrderManagementSection(orderViewModel)
                1 -> MenuManagementSection(menuViewModel)
                2 -> RestaurantInfoSection(userViewModel, restaurantViewModel)
                3 -> AIAnalyticsSection(orderViewModel)
            }
        }
    }
}

@Composable
fun AIAnalyticsSection(orderViewModel: OrderViewModel) {
    val aiViewModel: AIViewModel = hiltViewModel()
    val orders by orderViewModel.restaurantOrders.collectAsStateWithLifecycle()
    val insights by aiViewModel.businessInsights.collectAsStateWithLifecycle()
    val isAnalyzing by aiViewModel.isAnalyzingBusiness.collectAsStateWithLifecycle()

    LaunchedEffect(orders) {
        if (orders.isNotEmpty() && insights == null) {
            aiViewModel.getBusinessInsights(orders)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Phân tích thông minh", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (isAnalyzing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Primary)
                    Text("Đang phân tích dữ liệu kinh doanh...", modifier = Modifier.padding(top = 8.dp), fontSize = 12.sp, color = Color.Gray)
                } else {
                    Text(
                        text = insights ?: "Chưa có dữ liệu phân tích. Hãy nhận thêm đơn hàng để AI có thể giúp bạn.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Revenue Chart
        Card(
            modifier = Modifier.fillMaxWidth().height(220.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Doanh thu 7 ngày qua (VNĐ)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedRevenueChart()
            }
        }

        // Recommendations
        Text("Khuyến nghị từ AI", fontWeight = FontWeight.Bold)
        
        AIInsightItem(
            icon = Icons.Default.TrendingUp,
            title = "Tăng cường Flash Sale",
            desc = "Giảm giá 10% cho Pizza vào khung giờ 14h-16h để tối ưu doanh thu thấp điểm."
        )
        
        AIInsightItem(
            icon = Icons.Default.Inventory,
            title = "Quản lý kho",
            desc = "Nhu cầu Gà rán dự kiến tăng cao vào tối Thứ 6. Hãy chuẩn bị thêm nguyên liệu."
        )
    }
}

@Composable
fun RestaurantInfoSection(
    userViewModel: UserViewModel,
    restaurantViewModel: com.foddy.app.presentation.viewmodel.RestaurantViewModel
) {
    val userProfile by userViewModel.user.collectAsStateWithLifecycle()
    val restaurantId = userProfile.restaurantId ?: ""
    var restaurant by remember { mutableStateOf<com.foddy.app.domain.model.Restaurant?>(null) }
    val actionState by restaurantViewModel.actionState.collectAsStateWithLifecycle()
    
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val base64 = com.foddy.app.core.util.ImageUtils.compressImageToBase64(context, it, 800, 600, 70)
            if (base64 != null) imageUrl = base64
        }
    }

    LaunchedEffect(restaurantId) {
        if (restaurantId.isNotEmpty()) {
            val res = restaurantViewModel.getRestaurantById(restaurantId)
            restaurant = res
            res?.let {
                name = it.name
                address = it.address
                phone = it.phone
                category = it.category
                imageUrl = it.image
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Thông tin nhà hàng", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Restaurant Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray.copy(alpha = 0.1f))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Gray)
                            Text("Chọn ảnh đại diện quán", color = Color.Gray)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên nhà hàng") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Loại hình (VD: Bún bò, Pizza, Cafe)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại liên hệ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Địa chỉ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Button(
                    onClick = {
                        val updatedRes = (restaurant ?: com.foddy.app.domain.model.Restaurant(id = restaurantId, ownerId = userProfile.id)).copy(
                            name = name,
                            address = address,
                            phone = phone,
                            category = category,
                            image = imageUrl
                        )
                        restaurantViewModel.updateRestaurant(updatedRes)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = actionState !is com.foddy.app.presentation.ui.state.UiState.Loading
                ) {
                    if (actionState is com.foddy.app.presentation.ui.state.UiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Cập nhật thông tin quán", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AnimatedRevenueChart() {
    val revenueData = listOf(1200000f, 1500000f, 1100000f, 1800000f, 2200000f, 1900000f, 2500000f)
    val maxRevenue = revenueData.maxOrNull() ?: 1f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        revenueData.forEachIndexed { index, value ->
            val barHeight = (value / maxRevenue) * 120
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(barHeight.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Primary, Primary.copy(alpha = 0.5f))
                            )
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "T${index + 2}", 
                    fontSize = 10.sp, 
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Composable
fun AIInsightItem(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Primary.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun OrderManagementSection(orderViewModel: OrderViewModel, userViewModel: UserViewModel = hiltViewModel()) {
    val rawOrders by orderViewModel.restaurantOrders.collectAsStateWithLifecycle()
    val userProfile by userViewModel.user.collectAsStateWithLifecycle()
    val restaurantId = userProfile.restaurantId ?: "res_001"

    LaunchedEffect(restaurantId) {
        if (restaurantId.isNotBlank()) {
            orderViewModel.listenToRestaurantOrders(restaurantId)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Debug Info - Giúp bạn biết ID đang hoạt động
        Surface(
            color = Color.DarkGray,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Đang quản lý ID: $restaurantId | Tổng: ${rawOrders.size} đơn",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        if (rawOrders.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox, 
                        contentDescription = null, 
                        modifier = Modifier.size(48.dp), 
                        tint = Color.LightGray
                    )
                    Text("Chưa có đơn hàng nào cho quán này", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val sortedOrders = rawOrders.sortedByDescending { it.createdAt }
                items(
                    items = sortedOrders,
                    key = { it.id }
                ) { order ->
                    RestaurantOrderCard(order) { newStatus ->
                        orderViewModel.updateOrderStatus(order.id, newStatus)
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantOrderCard(order: OrderRequest, onStatusUpdate: (String) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Đơn #${order.id.takeLast(6)}", fontWeight = FontWeight.Bold, color = Color.Gray)
                Surface(
                    color = when(order.status) {
                        "PENDING" -> Color(0xFFFFF3E0)
                        "CONFIRMED" -> Color(0xFFE3F2FD)
                        "PREPARING" -> Color(0xFFE3F2FD)
                        "DELIVERING" -> Color(0xFFE8F5E9)
                        "COMPLETED" -> Color(0xFFF5F5F5)
                        else -> Color(0xFFF5F5F5)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when(order.status) {
                            "PENDING" -> "MỚI"
                            "CONFIRMED" -> "ĐÃ XÁC NHẬN"
                            "PREPARING" -> "ĐANG CHẾ BIẾN"
                            "DELIVERING" -> "ĐANG GIAO"
                            "COMPLETED" -> "HOÀN THÀNH"
                            else -> order.status.uppercase()
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when(order.status) {
                            "PENDING" -> Color(0xFFEF6C00)
                            "CONFIRMED" -> Color(0xFF1976D2)
                            "PREPARING" -> Color(0xFF1976D2)
                            "DELIVERING" -> Color(0xFF2E7D32)
                            "COMPLETED" -> Color.Gray
                            else -> Color.Gray
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                order.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.quantity}x ${item.foodItem.name}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("${(item.foodItem.price * item.quantity).toInt()}đ", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng cộng", fontSize = 12.sp, color = Color.Gray)
                    Text("${order.totalPrice.toInt()}đ", fontWeight = FontWeight.ExtraBold, color = Primary, fontSize = 18.sp)
                }
                
                if (order.status == "PENDING") {
                    Button(
                        onClick = { onStatusUpdate("CONFIRMED") },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nhận đơn", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (order.status == "CONFIRMED") {
                    Button(
                        onClick = { onStatusUpdate("PREPARING") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Bắt đầu chế biến", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (order.status == "PREPARING") {
                    OutlinedButton(
                        onClick = { /* Wait for driver */ },
                        shape = RoundedCornerShape(8.dp),
                        enabled = false,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Đang chờ tài xế...", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuManagementSection(menuViewModel: MenuViewModel, userViewModel: UserViewModel = hiltViewModel()) {
    val menuItems by menuViewModel.foodItems.collectAsStateWithLifecycle()
    val userProfile by userViewModel.user.collectAsStateWithLifecycle()
    val restaurantId = userProfile.restaurantId ?: "res_001" 
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val uploadState by menuViewModel.uploadState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Khởi chạy việc lắng nghe menu khi vào màn hình
    LaunchedEffect(restaurantId) {
        if (restaurantId.isNotEmpty()) {
            menuViewModel.observeMenu(restaurantId)
        }
    }
    var editingItem by remember { mutableStateOf<FoodItem?>(null) }
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("99") }
    var isFlashSale by remember { mutableStateOf(false) }
    var isAvailable by remember { mutableStateOf(true) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    LaunchedEffect(uploadState) {
        if (uploadState is Resource.Success && (uploadState.data ?: "").isNotEmpty()) {
            val newImageUrl = uploadState.data ?: ""
            if (editingItem != null) {
                val updatedItem = editingItem!!.copy(
                    name = name,
                    description = description,
                    price = price.toDoubleOrNull() ?: 0.0,
                    stock = stock.toIntOrNull() ?: 99,
                    discountPrice = if (isFlashSale) (price.toDoubleOrNull() ?: 0.0) * 0.8 else null,
                    imageUrl = newImageUrl,
                    isFlashSale = isFlashSale,
                    available = isAvailable
                )
                menuViewModel.updateFoodItem(updatedItem, oldImageUrl = editingItem!!.imageUrl)
            } else {
                val newItem = FoodItem(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    price = price.toDoubleOrNull() ?: 0.0,
                    stock = stock.toIntOrNull() ?: 99,
                    discountPrice = if (isFlashSale) (price.toDoubleOrNull() ?: 0.0) * 0.8 else null,
                    imageUrl = newImageUrl,
                    restaurantId = restaurantId,
                    rating = 5.0,
                    calories = 300,
                    isFlashSale = isFlashSale,
                    available = isAvailable
                )
                menuViewModel.addFoodItem(newItem)
            }
            menuViewModel.resetUploadState()
            showAddDialog = false
            editingItem = null
            name = ""; description = ""; price = ""; isFlashSale = false; isAvailable = true; selectedImageUri = null
        }
    }

    if (showAddDialog || editingItem != null) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                editingItem = null
                name = ""; description = ""; price = ""; isFlashSale = false; isAvailable = true; selectedImageUri = null
                menuViewModel.resetUploadState()
            },
            title = { Text(if (editingItem != null) "Sửa món ăn" else "Thêm món ăn mới", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Image Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.1f))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (editingItem != null && editingItem!!.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = editingItem!!.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Gray)
                                Text("Chọn ảnh món ăn", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = name, 
                        onValueChange = { name = it }, 
                        label = { Text("Tên món") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Mô tả") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = price, 
                        onValueChange = { price = it }, 
                        label = { Text("Giá tiền (đ)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = stock, 
                        onValueChange = { stock = it }, 
                        label = { Text("Số lượng trong kho") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Surface(
                        color = Color.Gray.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Checkbox(checked = isFlashSale, onCheckedChange = { isFlashSale = it })
                                Text("Kích hoạt Flash Sale (Giảm 20%)", fontSize = 14.sp)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Checkbox(checked = isAvailable, onCheckedChange = { isAvailable = it })
                                Text("Còn hàng (Availability)", fontSize = 14.sp)
                            }
                        }
                    }

                    if (uploadState is Resource.Loading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Primary)
                        Text("Đang xử lý...", fontSize = 12.sp, color = Primary)
                    }
                    
                    if (uploadState is Resource.Error) {
                        Text(uploadState.message ?: "Lỗi tải ảnh", color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        var finalImageUrl = if (editingItem != null) editingItem!!.imageUrl else "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"
                        
                        // Chuyển ảnh sang Base64 với nén tối ưu nếu người dùng chọn ảnh mới
                        selectedImageUri?.let { uri ->
                            val compressedBase64 = com.foddy.app.core.util.ImageUtils.compressImageToBase64(
                                context = context,
                                uri = uri,
                                maxWidth = 600,
                                maxHeight = 600,
                                quality = 70
                            )
                            if (compressedBase64 != null) {
                                finalImageUrl = compressedBase64
                            }
                        }

                        if (editingItem != null) {
                            val updatedItem = editingItem!!.copy(
                                name = name,
                                description = description,
                                price = price.toDoubleOrNull() ?: 0.0,
                                stock = stock.toIntOrNull() ?: 99,
                                discountPrice = if (isFlashSale) (price.toDoubleOrNull() ?: 0.0) * 0.8 else null,
                                imageUrl = finalImageUrl,
                                isFlashSale = isFlashSale,
                                available = isAvailable
                            )
                            menuViewModel.updateFoodItem(updatedItem)
                        } else {
                            val newItem = FoodItem(
                                id = java.util.UUID.randomUUID().toString(),
                                name = name,
                                description = description,
                                price = price.toDoubleOrNull() ?: 0.0,
                                stock = stock.toIntOrNull() ?: 99,
                                discountPrice = if (isFlashSale) (price.toDoubleOrNull() ?: 0.0) * 0.8 else null,
                                imageUrl = finalImageUrl,
                                restaurantId = restaurantId,
                                rating = 5.0,
                                calories = 300,
                                isFlashSale = isFlashSale,
                                available = isAvailable
                            )
                            menuViewModel.addFoodItem(newItem)
                        }
                        
                        showAddDialog = false
                        editingItem = null
                        name = ""; description = ""; price = ""; isFlashSale = false; isAvailable = true; selectedImageUri = null
                    },
                    enabled = name.isNotBlank() && price.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Lưu món") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    editingItem = null
                    name = ""; description = ""; price = ""; isFlashSale = false; isAvailable = true; selectedImageUri = null
                    menuViewModel.resetUploadState()
                }) { Text("Hủy") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(Modifier.weight(1f), "Tổng món", menuItems.size.toString(), Color(0xFF2196F3))
                AdminStatCard(Modifier.weight(1f), "Đang giảm giá", menuItems.count { it.isFlashSale }.toString(), Color(0xFFF44336))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(menuItems) { item ->
                    AdminFoodCard(
                        item = item, 
                        onDelete = { menuViewModel.removeFoodItem(item) },
                        onEdit = {
                            editingItem = item
                            name = item.name
                            description = item.description
                            price = item.price.toInt().toString()
                            stock = item.stock.toString()
                            isFlashSale = item.isFlashSale
                            isAvailable = item.available
                        },
                        onToggleAvailability = {
                            menuViewModel.updateFoodItem(item.copy(available = !item.available))
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
        
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}

@Composable
fun AdminStatCard(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AdminFoodCard(
    item: FoodItem, 
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleAvailability: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().alpha(if (item.available) 1f else 0.6f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${item.price.toInt()}đ", color = Primary, fontWeight = FontWeight.Bold)
                    if (item.isFlashSale) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Flash Sale", 
                                color = Color.Red, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (!item.available) {
                    Text("Hết hàng", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Text("Kho: ${item.stock}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Text("Mô tả: ${item.description}", color = Color.Gray, fontSize = 12.sp, maxLines = 1)
            }
            
            Row {
                Switch(
                    checked = item.available, 
                    onCheckedChange = { onToggleAvailability() },
                    scale = 0.7f
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.clip(CircleShape).background(Color(0xFFFFEBEE))
                ) { 
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp)) 
                }
            }
        }
    }
}

// Helper to scale switch down
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    scale: Float
) {
    Box(modifier = Modifier.scale(scale)) {
        androidx.compose.material3.Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
