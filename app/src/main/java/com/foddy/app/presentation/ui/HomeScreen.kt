package com.foddy.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.foddy.app.data.DummyData
import com.foddy.app.presentation.components.CategoryChip
import com.foddy.app.presentation.components.FoodCard
import com.foddy.app.presentation.components.RestaurantListItem
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.ui.components.ShimmerItem
import com.foddy.app.presentation.ui.components.ShimmerRestaurantItem
import com.foddy.app.presentation.ui.state.UiState
import com.foddy.app.presentation.ui.theme.Primary
import com.foddy.app.presentation.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    menuViewModel: MenuViewModel = hiltViewModel(),
    recommendationViewModel: RecommendationViewModel = hiltViewModel(),
    restaurantViewModel: RestaurantViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val menuItems by menuViewModel.foodItems.collectAsStateWithLifecycle()
    val aiRecs by recommendationViewModel.aiRecs.collectAsStateWithLifecycle()
    val isLoadingAi by recommendationViewModel.isLoading.collectAsStateWithLifecycle()
    val restaurantUiState by restaurantViewModel.uiState.collectAsStateWithLifecycle()
    val cartItems by cartViewModel.cartItems.collectAsStateWithLifecycle()
    val userState by userViewModel.uiState.collectAsStateWithLifecycle()
    
    val unreadCount by notificationViewModel.unreadCount.collectAsStateWithLifecycle()
    
    LaunchedEffect(userState.user) {
        val preferences = userState.user?.let { 
            "Tên: ${it.name}, Địa chỉ: ${it.address}. Thích các món ăn đa dạng." 
        } ?: "Thích đồ ăn cay, gà rán và trà sữa"
        recommendationViewModel.fetchRecommendations(preferences)
    }
    
    val flashSaleItems = remember(menuItems) {
        menuItems.filter { it.isFlashSale && it.available }
    }
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    val categories = remember { DummyData.categories.map { it.name } }

    val filteredRestaurants = remember(restaurantUiState, selectedCategory) {
        if (restaurantUiState is UiState.Success) {
            val data = (restaurantUiState as UiState.Success).data
            val list = data.restaurants
            if (selectedCategory == "Tất cả") {
                list
            } else {
                list.filter { it.category == selectedCategory }
            }
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            HomeHeader(
                address = userState.user?.address ?: "Chưa cập nhật địa chỉ",
                cartCount = cartItems.size,
                unreadNotifications = unreadCount,
                onAddressClick = { navController.navigate(Screen.AddressManagement.route) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onRefresh = {
                    restaurantViewModel.refresh()
                    menuViewModel.observeMenu()
                    val preferences = userState.user?.let { 
                        "Tên: ${it.name}, Địa chỉ: ${it.address}. Thích các món ăn đa dạng." 
                    } ?: "Thích đồ ăn cay, gà rán và trà sữa"
                    recommendationViewModel.fetchRecommendations(preferences)
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F8F8)),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Search Bar
            item {
                Box(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Screen.Search.route) },
                        placeholder = { Text("Bạn muốn ăn gì hôm nay?") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            disabledBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        enabled = false
                    )
                }
            }

            // Banner Carousel
            item {
                BannerCarousel()
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Categories
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            text = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // AI Recommendations Section
            if (aiRecs.isNotEmpty() || isLoadingAi) {
                item {
                    SectionHeader(title = "Gợi ý AI ✨", subtitle = "Dựa trên sở thích của bạn")
                }
                
                item {
                    if (isLoadingAi) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(3) { 
                                ShimmerItem(modifier = Modifier.width(160.dp).height(160.dp).clip(RoundedCornerShape(16.dp))) 
                            }
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            aiRecs.forEach { rec ->
                                items(rec.foodItems, key = { it.id }) { item ->
                                    FoodCard(item) {
                                        navController.navigate(Screen.RestaurantDetail.createRoute(item.restaurantId))
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Flash Sale Section
            if (flashSaleItems.isNotEmpty()) {
                item {
                    SectionHeader(title = "Flash Sale ⚡", subtitle = "Ưu đãi cực hời", titleColor = Color.Red)
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(flashSaleItems, key = { it.id }) { item ->
                            FoodCard(item) {
                                navController.navigate(Screen.RestaurantDetail.createRoute(item.restaurantId))
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Nearby Restaurants Header
            item {
                SectionHeader(title = "Quán ăn gần bạn", subtitle = "Giao hàng siêu tốc")
            }
            
            // Nearby Restaurants List
            when (val state = restaurantUiState) {
                is UiState.Loading -> {
                    items(3) { 
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            ShimmerRestaurantItem() 
                        }
                    }
                }
                is UiState.Success -> {
                    items(filteredRestaurants, key = { it.id }) { restaurant ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            RestaurantListItem(restaurant) {
                                navController.navigate(Screen.RestaurantDetail.createRoute(restaurant.id))
                            }
                        }
                    }
                    
                    if (state.data.isLoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(text = "Đã có lỗi xảy ra: ${state.message}", modifier = Modifier.padding(16.dp))
                    }
                    item {
                        Button(onClick = { restaurantViewModel.refresh() }, modifier = Modifier.padding(16.dp)) {
                            Text("Thử lại")
                        }
                    }
                }
                else -> {}
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun HomeHeader(
    address: String,
    cartCount: Int,
    unreadNotifications: Int = 0,
    onAddressClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAddressClick() }
            ) {
                Text(text = "Giao hàng đến", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = address.ifEmpty { "Chọn địa chỉ giao hàng" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Làm mới", tint = Primary)
                }

                BadgedBox(
                    badge = { if (cartCount > 0) Badge { Text("$cartCount") } }
                ) {
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng", tint = Color.Black)
                    }
                }
                
                BadgedBox(
                    badge = { if (unreadNotifications > 0) Badge { Text("$unreadNotifications") } }
                ) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Thông báo", tint = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color.Black,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = titleColor)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        if (onSeeAllClick != null) {
            Text(
                text = "Xem tất cả",
                modifier = Modifier.clickable { onSeeAllClick() },
                style = MaterialTheme.typography.labelLarge,
                color = Primary
            )
        }
    }
}

@Composable
fun BannerCarousel() {
    val banners = listOf(
        "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&q=80&w=1000",
        "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?auto=format&fit=crop&q=80&w=1000"
    )
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(banners) { bannerUrl ->
            AsyncImage(
                model = bannerUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(300.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
