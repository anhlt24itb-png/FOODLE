package com.foddy.app.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.viewmodel.UserViewModel
import com.foddy.app.presentation.ui.theme.LightGray
import com.foddy.app.presentation.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val userProfile by userViewModel.user.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userProfile.name) }
    var editPhone by remember { mutableStateOf(userProfile.phone) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    // Chuyển sang Base64 để lưu trực tiếp vào Firestore (Thay thế cho Storage)
                    val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    val dataUrl = "data:image/jpeg;base64,$base64String"
                    
                    userViewModel.updateAvatar(dataUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (showEditDialog) {
        // ... (giữ nguyên dialog)
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Chỉnh sửa hồ sơ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Họ và tên") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            autoCorrect = false
                        )
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                editPhone = newValue
                            }
                        },
                        label = { Text("Số điện thoại") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    userViewModel.updateName(editName)
                    userViewModel.updatePhone(editPhone)
                    showEditDialog = false
                }) {
                    Text("Lưu")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hồ sơ của tôi") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = userProfile.avatar.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200" },
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(50.dp)),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Primary,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape),
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp).size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = userProfile.name.ifEmpty { "Khách" }, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = userProfile.email.ifEmpty { "Chưa có email" }, fontSize = 14.sp, color = Color.Gray)
            if (userProfile.phone.isNotEmpty()) {
                Text(text = userProfile.phone, fontSize = 14.sp, color = Color.Gray)
            }
            
            Button(
                onClick = { 
                    editName = userProfile.name
                    editPhone = userProfile.phone
                    showEditDialog = true 
                },
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightGray, contentColor = Color.Black)
            ) {
                Text("Chỉnh sửa thông tin")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ProfileMenuItem(icon = Icons.Default.Favorite, title = "Món yêu thích") {
                navController.navigate(Screen.Favorites.route)
            }
            ProfileMenuItem(icon = Icons.Default.History, title = "Lịch sử đơn hàng") {
                navController.navigate(Screen.OrdersHistory.route)
            }
            ProfileMenuItem(icon = Icons.Default.LocationOn, title = "Địa chỉ của tôi") {
                navController.navigate(Screen.AddressManagement.route)
            }
            ProfileMenuItem(icon = Icons.Default.Payment, title = "Phương thức thanh toán") { }
            ProfileMenuItem(icon = Icons.Default.Settings, title = "Cài đặt") { }
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(
                onClick = {
                    userViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, modifier = Modifier.weight(1f), fontSize = 16.sp)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
    HorizontalDivider(color = LightGray)
}
