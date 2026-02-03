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
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return null
            val uid = firebaseUser.uid

            val docSnapshot = db.collection("users").document(uid).get().await()

            if (docSnapshot.exists()) {
                val isActive = docSnapshot.getBoolean("isActive") ?: true

                if (!isActive) {
                    auth.signOut()
                    return null
                }

                val user = User(
                    uid = uid,
                    email = docSnapshot.getString("email") ?: email,
                    fullName = docSnapshot.getString("fullName") ?: "",
                    role = docSnapshot.getString("role") ?: "staff",
                    institution = docSnapshot.getString("institution") ?: "",
                    department = docSnapshot.getString("department") ?: "",
                    isActive = true,
                    isEmailVerified = firebaseUser.isEmailVerified
                )

                // Ažuriraj isEmailVerified ako je promijenjen
                if (firebaseUser.isEmailVerified && !docSnapshot.getBoolean("isEmailVerified")!!) {
                    db.collection("users")
                        .document(uid)
                        .update("isEmailVerified", true)
                        .await()
                }

                return user
            }

            User(
                uid = uid,
                email = email,
                fullName = firebaseUser.displayName ?: "",
                role = "staff",
                isActive = true,
                isEmailVerified = firebaseUser.isEmailVerified
            )

        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error: ${e.message}")
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

    suspend fun sendEmailVerification(): Boolean {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Email verification error: ${e.message}")
            false
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            false
        }
    }

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
                institution = doc.getString("institution") ?: "",
                department = doc.getString("department") ?: "",
                isActive = doc.getBoolean("isActive") ?: true,
                isEmailVerified = doc.getBoolean("isEmailVerified") ?: false
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = db.collection("users").get().await()
            snapshot.documents.mapNotNull { doc ->
                User(
                    uid = doc.id,
                    email = doc.getString("email") ?: "",
                    fullName = doc.getString("fullName") ?: "",
                    role = doc.getString("role") ?: "staff",
                    institution = doc.getString("institution") ?: "",
                    department = doc.getString("department") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    isEmailVerified = doc.getBoolean("isEmailVerified") ?: false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun toggleUserStatus(userId: String, isActive: Boolean): Boolean {
        if (userId.isBlank()) {
            Log.e("AuthRepo", "❌ Toggle failed: userId is blank")
            return false
        }

        return try {
            Log.d("AuthRepo", "🔄 Starting toggle for user: $userId to isActive=$isActive")

            // Provjeri trenutnog korisnika i njegovu rolu
            val currentUid = auth.currentUser?.uid
            Log.d("AuthRepo", "👤 Current user UID: $currentUid")

            if (currentUid != null) {
                val currentUserDoc = db.collection("users").document(currentUid).get().await()
                val currentUserRole = currentUserDoc.getString("role")
                Log.d("AuthRepo", "🔑 Current user role: $currentUserRole")
            }

            // Provjeri da li dokument postoji
            val docRef = db.collection("users").document(userId)
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                Log.e("AuthRepo", "❌ Document does not exist: $userId")
                return false
            }

            val currentValue = snapshot.getBoolean("isActive")
            Log.d("AuthRepo", "📄 Document exists, current isActive: $currentValue")

            // Pokušaj update
            Log.d("AuthRepo", "💾 Attempting to update isActive to: $isActive")
            docRef.update("isActive", isActive).await()

            Log.d("AuthRepo", "✅ Update call completed")

            // Verifikuj promjenu
            val verifySnapshot = docRef.get().await()
            val newValue = verifySnapshot.getBoolean("isActive")
            Log.d("AuthRepo", "🔍 Verified new value in Firebase: $newValue")

            if (newValue == isActive) {
                Log.d("AuthRepo", "✅ SUCCESS: Value changed correctly!")
                true
            } else {
                Log.e("AuthRepo", "❌ FAILED: Value did not change! Expected: $isActive, Got: $newValue")
                false
            }

        } catch (e: Exception) {
            Log.e("AuthRepo", "❌ Exception during toggle for $userId: ${e.message}", e)
            Log.e("AuthRepo", "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            false
        }
    }

    suspend fun changeUserRole(userId: String, newRole: String): Boolean {
        if (userId.isBlank()) return false
        return try {
            db.collection("users")
                .document(userId)
                .update("role", newRole)
                .await()
            true
        } catch (e: Exception) {
            Log.e("AuthRepo", "Change role failed: ${e.message}", e)
            false
        }
    }

    suspend fun deleteUser(userId: String): Boolean {
        if (userId.isBlank()) return false
        return try {
            db.collection("users")
                .document(userId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("AuthRepo", "Delete user failed: ${e.message}", e)
            false
        }
    }

    fun logout() {
        auth.signOut()
    }
}