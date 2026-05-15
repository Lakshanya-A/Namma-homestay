package com.example.nammahomestay.data.repository

import android.net.Uri
import com.example.nammahomestay.data.model.Homestay
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

object HomestayRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val collection = db.collection("homestays")

    // 📸 UPLOAD MULTIPLE IMAGES TO FIREBASE STORAGE
    fun uploadImages(
        uris: List<Uri>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uploadTasks = uris.map { uri ->
            val fileRef = storage.child("homestays/${UUID.randomUUID()}.jpg")
            fileRef.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                fileRef.downloadUrl
            }
        }

        Tasks.whenAllSuccess<Uri>(uploadTasks)
            .addOnSuccessListener { urls ->
                onSuccess(urls.map { it.toString() })
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // ➕ ADD HOMESTAY TO FIRESTORE
    fun addHomestay(homestay: Homestay, onComplete: (Boolean) -> Unit) {
        val docRef = collection.document()
        val homestayWithId = homestay.copy(id = docRef.id)
        docRef.set(homestayWithId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // 📥 GET ALL HOMESTAYS
    fun getHomestays(onResult: (List<Homestay>) -> Unit) {
        collection.get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    doc.toObject(Homestay::class.java)
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // 📥 GET HOMESTAYS BY HOST ID
    fun getHomestaysByHost(hostId: String, onResult: (List<Homestay>) -> Unit) {
        collection.whereEqualTo("hostId", hostId)
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    doc.toObject(Homestay::class.java)
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
