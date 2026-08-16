package com.foddy.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.foddy.app.domain.model.Address
import com.foddy.app.presentation.viewmodel.AddressViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressScreen(
    navController: NavController,
    viewModel: AddressViewModel = hiltViewModel()
) {
    var label by remember { mutableStateOf("") }
    var fullAddress by remember { mutableStateOf("") }
    var receiverName by remember { mutableStateOf("") }
    var receiverPhone by remember { mutableStateOf("") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm địa chỉ mới") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Tên địa chỉ (VD: Nhà, Công ty)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fullAddress,
                onValueChange = { fullAddress = it },
                label = { Text("Địa chỉ chi tiết") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = receiverName,
                onValueChange = { receiverName = it },
                label = { Text("Tên người nhận") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = receiverPhone,
                onValueChange = { receiverPhone = it },
                label = { Text("Số điện thoại người nhận") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    val address = Address(
                        userId = userId,
                        label = label,
                        fullAddress = fullAddress,
                        receiverName = receiverName,
                        receiverPhone = receiverPhone
                    )
                    viewModel.addAddress(address)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = label.isNotBlank() && fullAddress.isNotBlank()
            ) {
                Text("Lưu địa chỉ")
            }
        }
    }
}
