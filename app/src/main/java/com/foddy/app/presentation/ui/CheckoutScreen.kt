package com.foddy.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.foddy.app.domain.model.PaymentMethod
import com.foddy.app.presentation.components.FoddyTopBar
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.ui.theme.Primary
import com.foddy.app.presentation.viewmodel.CartViewModel
import com.foddy.app.presentation.viewmodel.CheckoutUiEffect
import com.foddy.app.presentation.viewmodel.CheckoutViewModel
import com.foddy.app.presentation.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    checkoutViewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by checkoutViewModel.uiState.collectAsStateWithLifecycle()
    val user by userViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        checkoutViewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is CheckoutUiEffect.NavigateToSuccess -> {
                    navController.navigate(Screen.OrderTracking.createRoute(uiState.orderId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
                is CheckoutUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FoddyTopBar(
                title = "Thanh toán",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Button(
                    onClick = { checkoutViewModel.placeOrder() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                        .navigationBarsPadding(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading && uiState.cartItems.isNotEmpty()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Đặt hàng - ${uiState.totalPrice.toInt()}đ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F8F8)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Address Section
            item {
                CheckoutSection(
                    title = "Địa chỉ giao hàng", 
                    icon = Icons.Default.LocationOn,
                    onEditClick = { navController.navigate(Screen.AddressManagement.route) }
                ) {
                    Column {
                        Text(text = user.user?.name ?: "Người dùng", fontWeight = FontWeight.Bold)
                        Text(text = user.user?.phone ?: "Chưa có số điện thoại", color = Color.Gray)
                        Text(text = uiState.deliveryAddress.ifBlank { "Hà Nội, Việt Nam" }, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = uiState.deliveryAddress,
                            onValueChange = { checkoutViewModel.onAddressChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Hoặc nhập địa chỉ thủ công...") },
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            maxLines = 2
                        )
                    }
                }
            }

            // Items Section
            item {
                Text(text = "Đơn hàng của bạn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            items(uiState.cartItems, key = { it.foodItem.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${item.quantity}x ${item.foodItem.name}")
                    Text(text = "${(item.foodItem.price * item.quantity).toInt()}đ", fontWeight = FontWeight.Bold)
                }
            }

            // Payment Method
            item {
                CheckoutSection(title = "Phương thức thanh toán", icon = Icons.Default.Payments) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentOption(
                            title = "Tiền mặt (COD)",
                            isSelected = uiState.selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY,
                            onClick = { checkoutViewModel.selectPaymentMethod(PaymentMethod.CASH_ON_DELIVERY) }
                        )
                        PaymentOption(
                            title = "Chuyển khoản VietQR",
                            isSelected = uiState.selectedPaymentMethod == PaymentMethod.BANK_TRANSFER,
                            onClick = { checkoutViewModel.selectPaymentMethod(PaymentMethod.BANK_TRANSFER) }
                        )
                    }
                }
            }

            // Summary
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryRow("Tạm tính", "${uiState.totalPrice.toInt()}đ")
                    SummaryRow("Phí giao hàng", "15000đ")
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    SummaryRow("Tổng cộng", "${uiState.totalPrice.toInt() + 15000}đ", isBold = true, color = Primary)
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun CheckoutSection(
    title: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    onEditClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold)
            }
            if (onEditClick != null) {
                Text(
                    text = "Thay đổi",
                    color = Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onEditClick() }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun PaymentOption(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title)
        RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = Primary))
    }
}

@Composable
fun SummaryRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.Black) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.Gray)
        Text(
            text = value,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color,
            fontSize = if (isBold) 18.sp else 14.sp
        )
    }
}
