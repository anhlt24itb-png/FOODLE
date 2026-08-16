package com.foddy.app.data.repository

import com.foddy.app.data.local.UserDao
import com.foddy.app.data.mapper.toDomain
import com.foddy.app.data.mapper.toEntity
import com.foddy.app.domain.model.User
import com.foddy.app.domain.model.UserRole
import com.foddy.app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _currentUser = MutableStateFlow<User?>(null)
    private val _isInitialized = MutableStateFlow(false)
    private var userListener: ListenerRegistration? = null

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                syncUserProfile(firebaseUser.uid)
            } else {
                _currentUser.value = null
                _isInitialized.value = true
                userListener?.remove()
                userListener = null
            }
        }
    }

    override fun getCurrentUser(): Flow<User?> = _currentUser.asStateFlow()
    
    override fun isInitialized(): Flow<Boolean> = _isInitialized.asStateFlow()

    private fun syncUserProfile(uid: String) {
        userListener?.remove()
        userListener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to user profile")
                    repositoryScope.launch {
                        val localUser = userDao.getUserById(uid)
                        if (localUser != null) {
                            _currentUser.value = localUser.toDomain()
                        }
                        _isInitialized.value = true
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val user = snapshot.toObject(User::class.java)?.copy(id = snapshot.id, isLoggedIn = true)
                        
                        if (user != null) {
                            _currentUser.value = user
                            repositoryScope.launch {
                                try {
                                    userDao.insertUser(user.toEntity())
                                } catch (e: Exception) {
                                    Timber.e(e, "Error saving user to local DB")
                                }
                            }
                        } else {
                            Timber.e("Failed to parse user from snapshot: ${snapshot.id}")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Critical error parsing user profile from Firestore")
                    }
                } else {
                    // Tạo profile nếu chưa có
                    firebaseAuth.currentUser?.let { firebaseUser ->
                        val newUser = User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            name = firebaseUser.displayName ?: "User",
                            role = UserRole.CUSTOMER.name,
                            isLoggedIn = true
                        )
                        saveUserToFirestore(newUser)
                        // Tạo địa chỉ mặc định cho người dùng mới để có thể đặt hàng ngay
                        createInitialAddress(firebaseUser.uid)
                    }
                }
                _isInitialized.value = true
            }
    }

    private fun createInitialAddress(userId: String) {
        val defaultAddress = mapOf(
            "id" to "addr_${System.currentTimeMillis()}",
            "userId" to userId,
            "label" to "Nhà riêng",
            "fullAddress" to "Số 1 Võ Văn Ngân, Linh Chiểu, Thủ Đức, TP.HCM",
            "lat" to 10.8507,
            "lng" to 106.7719,
            "isDefault" to true
        )
        firestore.collection("addresses").document(defaultAddress["id"] as String).set(defaultAddress)
            .addOnFailureListener { Timber.e(it, "Failed to create initial address for user $userId") }
    }

    private fun createInitialRestaurant(ownerId: String) {
        val restaurants = listOf(
            mapOf(
                "id" to "res_001",
                "name" to "Quán Bún Bò Huế",
                "address" to "123 Đường Lê Lợi, Huế",
                "phone" to "0905123456",
                "image" to "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500",
                "rating" to 4.8,
                "reviewCount" to 120,
                "ownerId" to ownerId,
                "open" to true,
                "category" to "Bún/Phở",
                "deliveryTime" to "15-20 min",
                "shippingFee" to 15000.0
            ),
            mapOf(
                "id" to "res_002",
                "name" to "Pizza Foddy Demo",
                "address" to "45 Hoàn Kiếm, Hà Nội",
                "phone" to "0123456789",
                "image" to "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500",
                "rating" to 4.5,
                "reviewCount" to 85,
                "ownerId" to "demo_owner_1",
                "open" to true,
                "category" to "Pizza",
                "deliveryTime" to "25-30 min",
                "shippingFee" to 20000.0
            ),
            mapOf(
                "id" to "res_003",
                "name" to "The Coffee House Demo",
                "address" to "88 Nguyễn Huệ, TP.HCM",
                "phone" to "0987654321",
                "image" to "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=500",
                "rating" to 4.7,
                "reviewCount" to 210,
                "ownerId" to "demo_owner_2",
                "open" to true,
                "category" to "Cà phê",
                "deliveryTime" to "10-15 min",
                "shippingFee" to 12000.0
            ),
            mapOf(
                "id" to "res_004",
                "name" to "Gà Rán KFC Demo",
                "address" to "12 Cầu Giấy, Hà Nội",
                "phone" to "0444555666",
                "image" to "https://images.unsplash.com/photo-1562967914-6c82c4623df6?w=500",
                "rating" to 4.2,
                "reviewCount" to 340,
                "ownerId" to "demo_owner_3",
                "open" to true,
                "category" to "Gà rán",
                "deliveryTime" to "20-25 min",
                "shippingFee" to 18000.0
            )
        )

        restaurants.forEach { rest ->
            firestore.collection("restaurants").document(rest["id"] as String).set(rest)
                .addOnFailureListener { Timber.e(it, "Failed to create restaurant ${rest["id"]}") }
        }
    }

    private fun saveUserToFirestore(user: User) {
        firestore.collection("users").document(user.id).set(user.toMap())
            .addOnFailureListener { Timber.e(it, "Failed to save user to Firestore") }
    }

    override suspend fun register(name: String, email: String, password: String, role: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Registration failed")
            
            firebaseUser.updateProfile(userProfileChangeRequest { displayName = name }).await()

            val currentTime = System.currentTimeMillis()
            
            // 1. Khởi tạo User Model với giá trị mặc định theo yêu cầu
            val user = User(
                id = firebaseUser.uid,
                email = email,
                name = name,
                role = role,
                isLoggedIn = true,
                status = "ACTIVE",
                isActive = true,
                createdAt = currentTime,
                restaurantId = if (role == UserRole.RESTAURANT_OWNER.name) firebaseUser.uid else null
            )

            // Lưu vào collection "users"
            saveUserToFirestore(user)

            // 2. Tạo dữ liệu bổ trợ theo Vai trò
            if (role == UserRole.CUSTOMER.name) {
                createInitialAddress(firebaseUser.uid)
            }

            when (role) {
                UserRole.RESTAURANT_OWNER.name -> {
                    val newRestaurant = com.foddy.app.domain.model.Restaurant(
                        id = firebaseUser.uid,
                        name = "Nhà hàng của $name",
                        ownerId = firebaseUser.uid,
                        rating = 0.0,
                        reviewCount = 0,
                        open = false,
                        shippingFee = 15000.0,
                        deliveryTime = "20-30 min",
                        status = "PENDING",
                        createdAt = currentTime,
                        address = "Chưa cập nhật",
                        phone = user.phone
                    )
                    firestore.collection("restaurants").document(firebaseUser.uid).set(newRestaurant).await()
                    
                    // Thêm 1 món ăn demo để quán không bị trống (Dùng collection "foods")
                    val demoFood = com.foddy.app.domain.model.FoodItem(
                        id = "food_${System.currentTimeMillis()}",
                        name = "Món ăn chào mừng",
                        description = "Chào mừng bạn đến với nhà hàng mới!",
                        price = 50000.0,
                        imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                        restaurantId = firebaseUser.uid,
                        available = true,
                        stock = 99
                    )
                    firestore.collection("foods").document(demoFood.id).set(demoFood).await()
                }
                UserRole.DRIVER.name -> {
                    val newDriver = com.foddy.app.domain.model.Driver(
                        id = firebaseUser.uid,
                        name = name,
                        online = false,
                        currentOrderId = "",
                        rating = 5.0,
                        totalOrders = 0,
                        earnings = 0.0,
                        status = "ACTIVE",
                        createdAt = currentTime
                    )
                    firestore.collection("drivers").document(firebaseUser.uid).set(newDriver).await()
                }
            }

            userDao.insertUser(user.toEntity())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Login failed")

            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = if (doc.exists()) {
                doc.toObject(User::class.java)?.copy(id = doc.id, isLoggedIn = true) 
                    ?: throw Exception("User profile data error")
            } else {
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "User",
                    isLoggedIn = true
                )
                saveUserToFirestore(newUser)
                newUser
            }

            userDao.insertUser(user.toEntity())
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateName(name: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("Not logged in")
            firebaseAuth.currentUser?.updateProfile(userProfileChangeRequest { displayName = name })?.await()
            firestore.collection("users").document(uid).update("name", name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAvatar(imageUrl: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("Not logged in")
            firebaseAuth.currentUser?.updateProfile(userProfileChangeRequest { photoUri = android.net.Uri.parse(imageUrl) })?.await()
            firestore.collection("users").document(uid).update("avatar", imageUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePhone(phone: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("Not logged in")
            firestore.collection("users").document(uid).update("phone", phone).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: return Result.success(Unit)
            firestore.collection("users").document(uid).update("fcmToken", token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        userListener?.remove()
        userListener = null
        firebaseAuth.signOut()
        userDao.clearUser()
        _currentUser.value = null
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google Sign-In failed")
            
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = if (!doc.exists()) {
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "User",
                    avatar = firebaseUser.photoUrl?.toString() ?: "",
                    role = UserRole.CUSTOMER.name,
                    isLoggedIn = true
                )
                saveUserToFirestore(newUser)
                newUser
            } else {
                doc.toObject(User::class.java)?.copy(id = doc.id, isLoggedIn = true) 
                    ?: throw Exception("User profile data error")
            }
            
            userDao.insertUser(user.toEntity())
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
