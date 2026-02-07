package com.example.aeye
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class Firestore {
    private val db= FirebaseFirestore.getInstance()

    /**
     * Get all cycle logs for a user.
     */
    fun getCycleLogs(
        userId: String,
        onSuccess: (List<CycleLog>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("menstrual_logs")
            .document(userId)
            .collection("cycles")
            .get()
            .addOnSuccessListener { documents ->
                val cycleLogs = documents.mapNotNull { it.toObject(CycleLog::class.java) }
                onSuccess(cycleLogs)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching cycle logs: ${e.message}")
                onFailure(e)
            }
    }

    /**
     * Get the most recent cycle log for a user.
     */
    fun getLatestCycleLog(
        userId: String,
        onSuccess: (CycleLog) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("menstrual_logs")
            .document(userId)
            .collection("cycles")
            .orderBy("startDate")
            .limitToLast(1)
            .get()
            .addOnSuccessListener { documents ->
                val latestLog = documents.firstOrNull()?.toObject(CycleLog::class.java)
                if (latestLog != null) {
                    onSuccess(latestLog)
                } else {
                    onFailure(Exception("No cycle logs found"))
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching latest cycle log: ${e.message}")
                onFailure(e)
            }
    }

    /**
     * Convert the latest cycle log into chatbot-friendly format.
     */
    fun getChatbotData(
        userId: String,
        onResult: (Symptoms, CycleData) -> Unit,
        onError: (Exception) -> Unit
    ) {
        getLatestCycleLog(
            userId = userId,
            onSuccess = { log ->
                val symptoms = Symptoms(
                    mood = log.mood,
                    energy = log.energyLevel,
                    painLevel = log.painLevel,
                    hydration = log.hydration.toInt()
                )

                val cycleData = CycleData(
                    userId = userId,
                    cycleStartDate = log.startDate,
                    cycleEndDate = log.endDate,
                    symptoms = symptoms
                )

                onResult(symptoms, cycleData)
            },
            onFailure = onError
        )
    }
}


