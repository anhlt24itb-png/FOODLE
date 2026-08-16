package com.foddy.app.di

import com.foddy.app.data.repository.UserRepositoryImpl
import com.foddy.app.data.repository.OrderRepositoryImpl
import com.foddy.app.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): com.foddy.app.domain.repository.OrderRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: com.foddy.app.data.repository.NotificationRepositoryImpl
    ): com.foddy.app.domain.repository.NotificationRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: com.foddy.app.data.repository.CartRepositoryImpl
    ): com.foddy.app.domain.repository.CartRepository

    @Binds
    @Singleton
    abstract fun bindMenuRepository(
        menuRepositoryImpl: com.foddy.app.data.repository.MenuRepositoryImpl
    ): com.foddy.app.domain.repository.MenuRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: com.foddy.app.data.repository.PostRepositoryImpl
    ): com.foddy.app.domain.repository.PostRepository

    @Binds
    @Singleton
    abstract fun bindRestaurantRepository(
        restaurantRepositoryImpl: com.foddy.app.data.repository.RestaurantRepositoryImpl
    ): com.foddy.app.domain.repository.RestaurantRepository

    @Binds
    @Singleton
    abstract fun bindAIRepository(
        aiRepositoryImpl: com.foddy.app.data.repository.AIRepositoryImpl
    ): com.foddy.app.domain.repository.AIRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: com.foddy.app.data.repository.StorageRepositoryImpl
    ): com.foddy.app.domain.repository.StorageRepository

    @Binds
    @Singleton
    abstract fun bindAddressRepository(
        addressRepositoryImpl: com.foddy.app.data.repository.AddressRepositoryImpl
    ): com.foddy.app.domain.repository.AddressRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRepositoryImpl: com.foddy.app.data.repository.FavoriteRepositoryImpl
    ): com.foddy.app.domain.repository.FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        reviewRepositoryImpl: com.foddy.app.data.repository.ReviewRepositoryImpl
    ): com.foddy.app.domain.repository.ReviewRepository
}
