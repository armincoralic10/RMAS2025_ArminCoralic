package com.example.careevac.data.repository

import com.example.careevac.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun login(email: String, password: String): User? {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return null

            val userDoc = db.collection("users").document(uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: return null
            val userWithId = user.copy(uid = uid)

            if (!userWithId.isActive) {
                auth.signOut()
                return null
            }

            userWithId
        } catch (e: Exception) {
            null
        }
    }

    suspend fun register(email: String, password: String, fullName: String): String? {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return null

            val user = User(
                uid = uid,
                email = email,
                fullName = fullName,
                role = "staff",
                institution = "Demo Ustanova",
                isActive = true
            )

            db.collection("users").document(uid).set(user).await()
            uid
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val uid = auth.currentUser?.uid ?: return null
            val userDoc = db.collection("users").document(uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: return null
            user.copy(uid = uid)
        } catch (e: Exception) {
            null
        }
    }
}