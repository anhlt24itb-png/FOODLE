package com.foddy.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.foddy.app.domain.model.Address
import com.foddy.app.presentation.viewmodel.AddressViewModel
import com.foddy.app.presentation.ui.state.UiState
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressManagementScreen(
    navController: NavController,
    viewModel: AddressViewModel = hiltViewModel()
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "eEhBaQc6OtQHltzRIp1KmJUnGPn1"
    val addressState by viewModel.addresses.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAddresses(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Địa chỉ giao hàng") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                navController.navigate("add_address")
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = addressState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize())
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Text("Chưa có địa chỉ nào", modifier = Modifier.fillMaxSize().wrapContentSize())
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.data) { address ->
                                AddressItem(
                                    address = address,
                                    onSetDefault = { viewModel.setDefault(userId, address.id) }
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Text("Lỗi: ${state.message}", modifier = Modifier.fillMaxSize().wrapContentSize())
                }
                else -> {}
            }
        }
    }
}

@Composable
fun AddressItem(address: Address, onSetDefault: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(address.label, style = MaterialTheme.typography.titleMedium)
                Text(address.fullAddress, style = MaterialTheme.typography.bodyMedium)
                if (address.isDefault) {
                    Text(
                        "Mặc định",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            if (!address.isDefault) {
                TextButton(onClick = onSetDefault) {
                    Text("Đặt mặc định")
                }
            }
        }
    }
}
