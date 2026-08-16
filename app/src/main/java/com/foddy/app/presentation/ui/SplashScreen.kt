package com.foddy.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavController
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.ui.theme.OrangeGradient
import com.foddy.app.presentation.viewmodel.UserViewModel
import com.foddy.app.domain.model.UserRole
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val uiState by userViewModel.uiState.collectAsStateWithLifecycle()
    val isInitialized by userViewModel.isInitialized.collectAsStateWithLifecycle()

    LaunchedEffect(isInitialized, uiState.isLoading) {
        if (isInitialized && !uiState.isLoading) {
            delay(1000) // Small delay for branding
            val user = uiState.user
            if (user != null && user.isLoggedIn) {
                // Navigate based on role
                when (user.userRole) {
                    UserRole.DRIVER -> navController.navigate(Screen.DriverApp.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                    UserRole.RESTAURANT_OWNER -> navController.navigate(Screen.RestaurantAdmin.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                    UserRole.ADMIN -> navController.navigate(Screen.RestaurantAdmin.route) { // Placeholder for ADMIN
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                    else -> navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            } else {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    // Safety timeout: if loading takes more than 8 seconds, move to onboarding
    LaunchedEffect(Unit) {
        delay(8000)
        if (uiState.isLoading) {
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(OrangeGradient)),
        contentAlignment = Alignment.Center
    ) {
        // Logo or branding here
    }
}
