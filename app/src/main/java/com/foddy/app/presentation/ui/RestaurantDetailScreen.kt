package com.foddy.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.Restaurant
import com.foddy.app.presentation.components.FoddyTopBar
import com.foddy.app.presentation.viewmodel.*
import com.foddy.app.presentation.ui.theme.Primary
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.foddy.app.presentation.ui.state.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    navController: NavController,
    restaurantId: String?,
    cartViewModel: CartViewModel = hiltViewModel(),
    menuViewModel: MenuViewModel = hiltViewModel(),
    restaurantViewModel: RestaurantViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    var restaurant by remember { mutableStateOf<Restaurant?>(null) }
    val menuUiState by menuViewModel.uiState.collectAsStateWithLifecycle()
    val cartItems by cartViewModel.cartItems.collectAsStateWithLifecycle()
    val totalPrice by cartViewModel.totalPrice.collectAsStateWithLifecycle()
    val favorites by favoriteViewModel.favorites.collectAsStateWithLifecycle()
    val user by userViewModel.user.collectAsStateWithLifecycle()

    LaunchedEffect(user.id) {
        if (user.id.isNotEmpty()) {
            favoriteViewModel.loadFavorites(user.id)
        }
    }

    val isFavorite = remember(favorites, restaurantId) {
        favorites.any { it.targetId == restaurantId }
    }

    LaunchedEffect(restaurantId) {
        if (restaurantId != null) {
            val uiState = restaurantViewModel.uiState.value
            if (uiState is UiState.Success) {
                restaurant = uiState.data.restaurants.find { it.id == restaurantId }
            }
            
            if (restaurant == null) {
                restaurant = restaurantViewModel.getRestaurantById(restaurantId)
            }
            
            menuViewModel.observeMenu(restaurantId)
        }
    }

    Scaffold(
        topBar = {
            FoddyTopBar(
                title = restaurant?.name ?: "Nhà hàng",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = {
                        if (user.id.isNotEmpty() && restaurantId != null) {
                            favoriteViewModel.toggleFavorite(
                                user.id, 
                                restaurantId, 
                                "RESTAURANT",
                                restaurant?.name ?: "",
                                restaurant?.image ?: ""
                            )
                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Yêu thích",
                            tint = if (isFavorite) Color.Red else Color.Black
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding()
                    ) {
                        Button(
                            onClick = { navController.navigate("cart") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val count = cartItems.sumOf { it.quantity }
                                Text(
                                    text = "$count món",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Xem giỏ hàng",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${totalPrice.toInt()}đ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        restaurant?.let { res ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    AsyncImage(
                        model = res.image,
                        contentDescription = res.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = res.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                                Text(text = "${res.rating}", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Text(
                            text = "${res.category} • ${res.deliveryTime} • ${res.address}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AIReviewSummarySection()
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = "Thực đơn", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                items(
                    items = when (val state = menuUiState) {
                        is UiState.Success -> state.data
                        else -> emptyList()
                    },
                    key = { it.id }
                ) { foodItem ->
                    val isFoodFavorite = favorites.any { it.targetId == foodItem.id }
                    FoodDetailItem(
                        foodItem = foodItem,
                        onAddClick = { cartViewModel.addToCart(foodItem) },
                        onFavoriteClick = {
                            favoriteViewModel.toggleFavorite(
                                userId = user.id,
                                targetId = foodItem.id,
                                type = "FOOD",
                                name = foodItem.name,
                                image = foodItem.imageUrl
                            )
                        },
                        isFavorite = isFoodFavorite
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
    }
}

@Composable
fun AIReviewSummarySection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Primary.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Tóm tắt đánh giá bằng AI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "\"Khách hàng đánh giá cao tốc độ giao hàng và hương vị đậm đà. Một số ý kiến cho rằng phần ăn hơi nhỏ so với giá tiền.\"",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ReviewStat("Ưu điểm", "Vị ngon, Giao nhanh", Color(0xFF2E7D32))
                ReviewStat("Cần cải thiện", "Giá hơi cao", Color(0xFFC62828))
            }
        }
    }
}

@Composable
fun ReviewStat(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun FoodDetailItem(
    foodItem: FoodItem, 
    onAddClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavorite: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = foodItem.name, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onFavoriteClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (foodItem.description.isNotEmpty()) {
                    Text(
                        text = foodItem.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${foodItem.price.toInt()}đ",
                    style = MaterialTheme.typography.titleMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = foodItem.imageUrl,
                    contentDescription = foodItem.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Primary, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
    }
}
