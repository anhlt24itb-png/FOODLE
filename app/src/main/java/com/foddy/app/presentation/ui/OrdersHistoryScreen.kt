package com.foddy.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.presentation.ui.state.UiState
import com.foddy.app.presentation.viewmodel.OrderViewModel
import com.foddy.app.presentation.ui.theme.LightGray
import com.foddy.app.presentation.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersHistoryScreen(
    navController: NavController, 
    orderViewModel: OrderViewModel = hiltViewModel(),
    userViewModel: com.foddy.app.presentation.viewmodel.UserViewModel = hiltViewModel()
) {
    val ordersState by orderViewModel.userOrders.collectAsStateWithLifecycle()
    val user by userViewModel.user.collectAsStateWithLifecycle()

    LaunchedEffect(user.id) {
        if (user.id.isNotEmpty()) {
            orderViewModel.listenToUserOrders(user.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
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
            when (val state = ordersState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }

                is UiState.Success -> {
                    val orders = state.data
                    if (orders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Bạn chưa có đơn hàng nào", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(orders) { order ->
                                OrderHistoryCard(order)
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Lỗi: ${state.message}", color = Color.Red)
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: OrderRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = order.restaurantName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${order.totalPrice.toInt()}đ", fontWeight = FontWeight.Bold, color = Primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Đơn hàng: #${order.id.takeLast(6)}", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when(order.status.uppercase()) {
                        "PENDING" -> "Đang chờ"
                        "CONFIRMED" -> "Đã xác nhận"
                        "PREPARING" -> "Đang chuẩn bị"
                        "DELIVERING" -> "Đang giao"
                        "COMPLETED" -> "Hoàn thành"
                        "CANCELLED" -> "Đã hủy"
                        else -> order.status
                    },
                    color = when(order.status.uppercase()) {
                        "COMPLETED" -> Color(0xFF4CAF50)
                        "DELIVERING", "PREPARING", "CONFIRMED" -> Color(0xFF2196F3)
                        "PENDING" -> Color(0xFFFF9800)
                        "CANCELLED" -> Color.Red
                        else -> Color.Gray
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = { /* TODO: Đặt lại */ },
                    colors = ButtonDefaults.buttonColors(containerColor = LightGray, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Đặt lại", fontSize = 12.sp)
                }
            }
        }
    }
}
