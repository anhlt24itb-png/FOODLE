package com.foddy.app.data.repository

import com.foddy.app.domain.model.Address
import com.foddy.app.domain.repository.AddressRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
//nơi trực tiếp đi gom dữ liệu rồi đổ về cho giao diện hiển thị lên màn hình.
class AddressRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AddressRepository {

    override fun getAddresses(userId: String): Flow<List<Address>> = callbackFlow {
        val listener = firestore.collection("addresses")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val addresses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Address::class.java)?.copy(id = doc.id)
                }
                trySend(addresses ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addAddress(address: Address): Result<Unit> = try {
        firestore.collection("addresses").add(address).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateAddress(address: Address): Result<Unit> = try {
        firestore.collection("addresses").document(address.id).set(address).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAddress(addressId: String): Result<Unit> = try {
        firestore.collection("addresses").document(addressId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun setDefaultAddress(userId: String, addressId: String): Result<Unit> = try {
        val batch = firestore.batch()
        val allAddresses = firestore.collection("addresses")
            .whereEqualTo("userId", userId)
            .get().await()
        
        for (doc in allAddresses) {
            batch.update(doc.reference, "isDefault", doc.id == addressId)
        }
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
