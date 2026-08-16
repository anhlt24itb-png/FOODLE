package com.foddy.app.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.ui.theme.Primary

@Composable
fun RoleSelectionScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Chào m?ng d?n FoddyApp",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            text = "B?n mu?n tham gia v?i vai trò gì?",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        RoleCard(
            title = "Khách hàng",
            description = "Ð?t món an yêu thích c?a b?n",
            icon = Icons.Default.Person,
            color = Primary
        ) {
            navController.navigate(Screen.Splash.route)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        RoleCard(
            title = "Tài x?",
            description = "Giao hàng và ki?m thêm thu nh?p",
            icon = Icons.Default.DeliveryDining,
            color = Color(0xFF4CAF50)
        ) {
            navController.navigate(Screen.DriverApp.route)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        RoleCard(
            title = "Ch? quán an",
            description = "Qu?n lý th?c don và don hàng",
            icon = Icons.Default.Store,
            color = Color(0xFF2196F3)
        ) {
            navController.navigate(Screen.RestaurantAdmin.route)
        }
    }
}

@Composable
fun RoleCard(title: String, description: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
                Text(text = description, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
