package com.example.pawfectmatchapp.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawfectmatchapp.models.DogData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class DashboardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _favoriteDogs = MutableLiveData<List<DogData>>()
    val favoriteDogs: LiveData<List<DogData>> = _favoriteDogs

    init {
        fetchFavoriteDogs()
    }

    private fun fetchFavoriteDogs() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("DashboardViewModel", "error in receiving favorites: ${error.message}")
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val favoriteDogIds = document.get("favorites") as? List<String> ?: emptyList()
                    fetchDogsByIds(favoriteDogIds)
                }
            }
    }

    private fun fetchDogsByIds(dogIds: List<String>) {
        Log.d("DashboardViewModel", "received ${dogIds.size} dog id's: $dogIds") //checking id's

        if (dogIds.isEmpty()) {
            _favoriteDogs.value = emptyList()
            Log.d("DashboardViewModel", "no dogs to show!")
            return
        }

        val dogsList = mutableListOf<DogData>()
        val batchSize = 10
        val batches = dogIds.chunked(batchSize)

        batches.forEach { batch ->
            Log.d("DashboardViewModel", "Firestore ${batch.size}: $batch") // prints id's before sending

            db.collection("Dogs").whereIn(FieldPath.documentId(), batch).get()
                .addOnSuccessListener { documents ->
                    Log.d("DashboardViewModel", "✅ received ${documents.size()} results!")

                    for (document in documents) {
                        val dog = DogData.Builder()
                            .setId(document.id)
                            .setName(document.getString("name") ?: "")
                            .setAge(document.getLong("age")?.toInt() ?: 0)
                            .setBreed(document.getString("breed") ?: "")
                            .setGender(document.getString("gender") ?: "")
                            .setDescription(document.getString("description") ?: "")
                            .setImageUrl(document.getString("imageUrl") ?: "")
                            .build()
                        dogsList.add(dog)

                    }

                    _favoriteDogs.value = dogsList
                    Log.d("DashboardViewModel", "were added ${dogsList.size} dogs in favorites!")
                }
                .addOnFailureListener { e ->
                    Log.e("DashboardViewModel", "error in receiving from-Firestore: ${e.message}")
                    _favoriteDogs.value = emptyList()
                }
        }
    }



    fun updateFavoriteStatus(dog: DogData, isFavorite: Boolean) {
        val currentUser = auth.currentUser ?: return
        val userDoc = db.collection("users").document(currentUser.uid)

        userDoc.get().addOnSuccessListener { document ->
            val currentFavorites = document.get("favorites") as? MutableList<String> ?: mutableListOf()

            if (isFavorite) {
                if (!currentFavorites.contains(dog.id)) currentFavorites.add(dog.id)
            } else {
                currentFavorites.remove(dog.id)
            }

            userDoc.update("favorites", currentFavorites)
                .addOnSuccessListener {
                    fetchFavoriteDogs()
                }
                .addOnFailureListener {

                }
        }
    }
    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

}
