package com.foddy.app.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object RestaurantDetail : Screen("restaurant_detail/{restaurantId}") {
        fun createRoute(restaurantId: String) = "restaurant_detail/$restaurantId"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object DriverSelection : Screen("driver_selection")
    object OrderTracking : Screen("order_tracking/{orderId}") {
        fun createRoute(orderId: String) = "order_tracking/$orderId"
    }
    object OrdersHistory : Screen("orders_history")
    object Profile : Screen("profile")
    object Search : Screen("search")
    object Notifications : Screen("notifications")
    // New Roles
    object RestaurantAdmin : Screen("restaurant_admin")
    object DriverApp : Screen("driver_app")
    object RoleSelection : Screen("role_selection")
    object AIAssistant : Screen("ai_assistant")
    
    // User Features
    object AddressManagement : Screen("address_management")
    object Favorites : Screen("favorites")
    object ProfileEdit : Screen("profile_edit")
    object AddAddress : Screen("add_address")
    object OrderReview : Screen("order_review/{orderId}") {
        fun createRoute(orderId: String) = "order_review/$orderId"
    }
}
