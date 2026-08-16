package com.foddy.app.domain.repository

import com.foddy.app.domain.model.Address
import kotlinx.coroutines.flow.Flow

interface AddressRepository {
    fun getAddresses(userId: String): Flow<List<Address>>
    suspend fun addAddress(address: Address): Result<Unit>
    suspend fun updateAddress(address: Address): Result<Unit>
    suspend fun deleteAddress(addressId: String): Result<Unit>
    suspend fun setDefaultAddress(userId: String, addressId: String): Result<Unit>
}
