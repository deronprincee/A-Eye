package com.example.aeye.data

import com.example.aeye.data.model.TestResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class Firestore(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    private fun uid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

    suspend fun addResult(result: TestResult) {

        val doc = db.collection("users")
            .document(uid())
            .collection("results")
            .document()

        val payload = hashMapOf(
            "testType" to result.testType,
            "distanceCm" to result.distanceCm,
            "finalLogmar" to result.finalLogmar,
            "snellenApprox" to result.snellenApprox,
            "totalCorrectLetters" to result.totalCorrectLetters,
            "totalLetters" to result.totalLetters,
            "pxPerMm" to result.pxPerMm,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        doc.set(payload).await()
    }

    fun observeResults(): Flow<List<TestResult>> = callbackFlow {

        val listener = db.collection("users")
            .document(uid())
            .collection("results")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val results = snapshot?.documents?.map { doc ->

                    val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time
                    val createdAtMillis = doc.getLong("createdAtMillis")

                    TestResult(
                        id = doc.id,
                        testType = doc.getString("testType") ?: "",
                        finalLogmar = doc.getDouble("finalLogmar"),
                        snellenApprox = doc.getString("snellenApprox"),
                        createdAtMillis = createdAt ?: createdAtMillis
                    )
                } ?: emptyList()

                trySend(results)
            }

        awaitClose { listener.remove() }
    }
    suspend fun deleteResult(resultId: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("results")
            .document(resultId)
            .delete()
            .await()
    }
}