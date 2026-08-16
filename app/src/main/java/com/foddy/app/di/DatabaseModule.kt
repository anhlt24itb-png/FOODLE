package com.foddy.app.di

import android.content.Context
import androidx.room.Room
import com.foddy.app.data.local.AppDatabase
import com.foddy.app.data.local.CartDao
import com.foddy.app.data.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "foddy_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideCartDao(database: AppDatabase): CartDao {
        return database.cartDao()
    }

    @Provides
    fun provideFoodItemDao(database: AppDatabase): com.foddy.app.data.local.FoodItemDao {
        return database.foodItemDao()
    }

    @Provides
    fun provideRestaurantDao(database: AppDatabase): com.foddy.app.data.local.RestaurantDao {
        return database.restaurantDao()
    }

    @Provides
    fun provideOrderDao(database: AppDatabase): com.foddy.app.data.local.OrderDao {
        return database.orderDao()
    }
}
