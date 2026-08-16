package com.foddy.app.domain.usecase.address

import com.foddy.app.domain.model.Address
import com.foddy.app.domain.repository.AddressRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class GetAddressesUseCase @Inject constructor(
    private val repository: AddressRepository,
    private val auth: FirebaseAuth
) {
    operator fun invoke(): Flow<List<Address>> {
        val userId = auth.currentUser?.uid ?: return emptyFlow()
        return repository.getAddresses(userId)
    }
}
