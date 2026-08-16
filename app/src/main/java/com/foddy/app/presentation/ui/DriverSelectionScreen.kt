package com.foddy.app.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import com.foddy.app.domain.model.Driver
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.viewmodel.OrderViewModel
import com.foddy.app.presentation.ui.theme.Primary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverSelectionScreen(navController: NavController, orderViewModel: OrderViewModel) {
    var isSearching by remember { mutableStateOf(true) }
    val currentOrder by orderViewModel.currentOrder.collectAsState()
    
    val drivers = listOf(
        Driver("1", "Trần Văn Gấu", "0912345678", "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=200", 4.9, "Xe máy Honda SH - 29A1 12345"),
        Driver("2", "Lê Văn Tám", "0988776655", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200", 4.7, "Xe máy Yamaha Exciter - 29C1 67890"),
        Driver("3", "Phạm Văn Mèo", "0900112233", "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=200", 4.8, "Xe máy Honda Vision - 29F1 55555")
    )

    LaunchedEffect(Unit) {
        delay(2000) 
        isSearching = false
    }

    // Tự động chuyển sang màn hình theo dõi nếu đơn hàng được chấp nhận (Đồng bộ Real-time với Driver App)
    LaunchedEffect(currentOrder?.status) {
        if (currentOrder?.status == "accepted" || currentOrder?.status == "delivering") {
            navController.navigate(Screen.OrderTracking.route) {
                popUpTo(Screen.Home.route)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chọn tài xế") }) }
    ) { padding ->
        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Đang kết nối với các tài xế gần bạn", color = Color.Gray)
                }
            }
        } else {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text(
                    text = "Các tài xế sẵn sàng nhận đơn",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(drivers) { driver ->
                        DriverItem(driver) {
                            // Gọi hàm acceptOrder mới để cập nhật trạng thái và lưu timestamps
                            currentOrder?.let { order ->
                                orderViewModel.acceptOrder(order.id, driver.id, driver.name)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverItem(driver: Driver, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = driver.avatar,
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(30.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = driver.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = driver.vehicleInfo, fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                    Text(text = driver.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Chọn")
            }
        }
    }
}
