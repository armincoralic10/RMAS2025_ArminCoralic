package com.example.careevac.data.repository

import android.util.Log
import com.example.careevac.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun login(email: String, password: String): User? {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return null
            val uid = firebaseUser.uid

            val docSnapshot = db.collection("users").document(uid).get().await()

            if (docSnapshot.exists()) {
                val isActive = docSnapshot.getBoolean("isActive") ?: true

                if (!isActive) {
                    auth.signOut()
                    throw Exception("Nalog je deaktiviran.")
                }

                return User(
                    uid = uid,
                    email = docSnapshot.getString("email") ?: email,
                    fullName = docSnapshot.getString("fullName") ?: "",
                    role = docSnapshot.getString("role") ?: "staff",
                    institution = docSnapshot.getString("institution") ?: "",
                    department = docSnapshot.getString("department") ?: "",
                    isActive = true,
                    isEmailVerified = firebaseUser.isEmailVerified
                )
            }

            return User(
                uid = uid,
                email = email,
                fullName = firebaseUser.displayName ?: "",
                role = "staff",
                isActive = true,
                isEmailVerified = firebaseUser.isEmailVerified
            )

        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error: ${e.message}")
            return null
        }
    }

    suspend fun register(email: String, password: String, fullName: String): String? {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return null

            try {
                auth.currentUser?.sendEmailVerification()?.await()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val user = User(
                uid = uid,
                email = email,
                fullName = fullName,
                role = "staff",
                isActive = true,
                isEmailVerified = false
            )

            db.collection("users").document(uid).set(user).await()
            uid
        } catch (e: Exception) {
            Log.e("AuthRepository", "Register error: ${e.message}")
            null
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = db.collection("users").get().await()
            snapshot.documents.map { doc ->
                User(
                    uid = doc.id,
                    email = doc.getString("email") ?: "",
                    fullName = doc.getString("fullName") ?: "",
                    role = doc.getString("role") ?: "staff",
                    institution = doc.getString("institution") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    isEmailVerified = false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun toggleUserStatus(userId: String, isActive: Boolean): Boolean {
        if (userId.isBlank()) return false
        return try {
            db.collection("users")
                .document(userId)
                .update("isActive", isActive)
                .await()
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Toggle status error: ${e.message}")
            false
        }
    }

    suspend fun changeUserRole(userId: String, newRole: String): Boolean {
        if (userId.isBlank()) return false
        return try {
            db.collection("users").document(userId).update("role", newRole).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteUser(userId: String): Boolean {
        if (userId.isBlank()) return false
        return try {
            db.collection("users").document(userId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun logout() { auth.signOut() }

    fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified ?: false

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            User(
                uid = uid,
                email = doc.getString("email") ?: "",
                fullName = doc.getString("fullName") ?: "",
                role = doc.getString("role") ?: "staff",
                isActive = doc.getBoolean("isActive") ?: true
            )
        } catch (e: Exception) { null }
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) { false }
    }
}