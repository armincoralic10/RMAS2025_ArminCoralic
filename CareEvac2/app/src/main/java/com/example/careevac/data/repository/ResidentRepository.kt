package com.example.careevac.data.repository

import com.example.careevac.model.Resident
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ResidentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val residentsCollection = db.collection("residents")

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
    }

    fun getAllResidents(): Flow<List<Resident>> = callbackFlow {
        val listener = residentsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val residents = mutableListOf<Resident>()

                for (doc in snapshot.documents) {
                    val resident = doc.toObject(Resident::class.java)
                    if (resident != null) {
                        val residentWithId = resident.copy(id = doc.id)
                        residents.add(residentWithId)
                    }
                }

                trySend(residents)
            }
        }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun markAsEvacuated(residentId: String): Boolean {
        return try {
            residentsCollection
                .document(residentId)
                .update("isEvacuated", true)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun resetAllEvacuations(): Boolean {
        return try {
            val snapshot = residentsCollection.get().await()
            val batch = db.batch()

            for (doc in snapshot.documents) {
                batch.update(doc.reference, "isEvacuated", false)
            }

            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}