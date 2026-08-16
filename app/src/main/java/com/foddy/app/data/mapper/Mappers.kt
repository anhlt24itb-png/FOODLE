package com.foddy.app.data.mapper

import com.foddy.app.data.local.FoodItemEntity
import com.foddy.app.data.local.RestaurantEntity
import com.foddy.app.data.model.UserEntity
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.Restaurant
import com.foddy.app.domain.model.User
import com.foddy.app.domain.model.UserRole

fun FoodItem.toEntity(): FoodItemEntity {
    return FoodItemEntity(
        id = id,
        name = name,
        description = description,
        price = price,
        discountPrice = discountPrice,
        imageUrl = imageUrl,
        rating = rating,
        calories = calories,
        isFlashSale = isFlashSale,
        restaurantId = restaurantId,
        category = category,
        available = available,
        soldCount = soldCount
    )
}

fun FoodItemEntity.toDomain(): FoodItem {
    return try {
        FoodItem(
            id = id,
            name = name,
            description = description,
            price = price,
            discountPrice = discountPrice,
            imageUrl = imageUrl,
            rating = rating,
            calories = calories,
            isFlashSale = isFlashSale,
            restaurantId = restaurantId,
            category = category,
            available = available,
            soldCount = soldCount
        )
    } catch (e: Exception) {
        com.foddy.app.util.AppLogger.e(e, "Error mapping FoodItemEntity to FoodItem")
        FoodItem(id = id, name = name, restaurantId = restaurantId)
    }
}

fun Restaurant.toEntity(): RestaurantEntity {
    return RestaurantEntity(
        id = id,
        name = name,
        address = address,
        phone = phone,
        image = image,
        rating = rating,
        reviewCount = reviewCount,
        ownerId = ownerId,
        open = open,
        lat = lat,
        lng = lng,
        category = category,
        deliveryTime = deliveryTime,
        distance = distance,
        shippingFee = shippingFee,
        promoTags = promoTags.joinToString(",")
    )
}

fun RestaurantEntity.toDomain(): Restaurant {
    return try {
        Restaurant(
            id = id,
            name = name,
            address = address,
            phone = phone,
            image = image,
            rating = rating,
            reviewCount = reviewCount,
            ownerId = ownerId,
            open = open,
            lat = lat,
            lng = lng,
            category = category,
            deliveryTime = deliveryTime,
            distance = distance,
            shippingFee = shippingFee,
            promoTags = if (promoTags.isEmpty()) emptyList() else promoTags.split(",")
        )
    } catch (e: Exception) {
        com.foddy.app.util.AppLogger.e(e, "Error mapping RestaurantEntity to Restaurant")
        Restaurant(id = id, name = name, ownerId = ownerId)
    }
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        name = name,
        phoneNumber = phone,
        address = address,
        profilePictureUrl = avatar,
        role = role
    )
}

fun UserEntity.toDomain(): User {
    return try {
        User(
            id = id,
            email = email,
            name = name,
            phone = phoneNumber,
            address = address,
            avatar = profilePictureUrl,
            role = role,
            isLoggedIn = true
        )
    } catch (e: Exception) {
        com.foddy.app.util.AppLogger.e(e, "Error mapping UserEntity to User")
        User(id = id, email = email, name = name, role = role)
    }
}
