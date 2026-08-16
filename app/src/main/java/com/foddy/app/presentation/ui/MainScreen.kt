package com.foddy.app.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.ui.components.BottomNavigationBar

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.OrdersHistory.route,
        Screen.Profile.route,
        Screen.AIAssistant.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
            composable(Screen.Login.route) { LoginScreen(navController) }
            composable(Screen.Register.route) { RegisterScreen(navController) }
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Notifications.route) { NotificationScreen(navController) }
            composable(Screen.RestaurantDetail.route) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""
                RestaurantDetailScreen(navController, restaurantId)
            }
            composable(Screen.Cart.route) { CartScreen(navController) }
            composable(Screen.Checkout.route) { CheckoutScreen(navController) }
            composable(
                route = Screen.OrderTracking.route,
                deepLinks = listOf(
                    navDeepLink { uriPattern = "foddy://order/{orderId}" }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderTrackingScreen(navController, orderId)
            }
            composable(Screen.OrdersHistory.route) { OrdersHistoryScreen(navController) }
            composable(Screen.Profile.route) { ProfileScreen(navController) }
            composable(Screen.AIAssistant.route) { AIAssistantScreen(navController) }
            composable(Screen.Search.route) { SearchScreen(navController) }
            
            // Roles
            composable(Screen.RestaurantAdmin.route) { RestaurantAdminScreen(navController) }
            composable(Screen.DriverApp.route) { DriverAppScreen(navController) }
            composable(Screen.RoleSelection.route) { RoleSelectionScreen(navController) }

            // User Features
            composable(Screen.AddressManagement.route) { AddressManagementScreen(navController) }
            composable(Screen.AddAddress.route) { AddAddressScreen(navController) }
            composable(Screen.Favorites.route) { FavoritesScreen(navController) }
            // composable(Screen.ProfileEdit.route) { ProfileEditScreen(navController) }
            composable(Screen.OrderReview.route) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderReviewScreen(navController, orderId)
            }
        }
    }
}
