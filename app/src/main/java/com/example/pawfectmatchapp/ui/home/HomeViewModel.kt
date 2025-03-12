package com.example.pawfectmatchapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawfectmatchapp.models.DogData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val _dogs = MutableLiveData<List<DogData>>()
    val dogs: LiveData<List<DogData>> = _dogs

    private val _favoriteDogs = MutableLiveData<List<String>>() // LiveData for favorites
    val favoriteDogs: LiveData<List<String>> = _favoriteDogs

    init {
        fetchDogsFromFirestore()
        fetchFavoriteDogs() // Fetches favorite dogs from Firestore
    }

    fun fetchDogsFromFirestore() {
        db.collection("Dogs").get()
            .addOnSuccessListener { documents ->
                val dogList = mutableListOf<DogData>()
                Log.d("FirestoreDebug", "Fetching all dogs (${documents.size()})")

                for (document in documents) {
                    val name = document.getString("name") ?: "No Name"
                    val breed = document.getString("breed") ?: "Unknown"
                    val gender = document.getString("gender") ?: "Unknown"
                    val description = document.getString("description") ?: "No Description"

                    Log.d("FirestoreDebug", "Loaded dog: Name = $name | Breed = $breed | Gender = $gender | Description = $description")

                    val dog = DogData.Builder()
                        .setId(document.id)
                        .setName(name)
                        .setAge(document.getLong("age")?.toInt() ?: 0)
                        .setBreed(breed)
                        .setGender(gender)
                        .setDescription(description)
                        .setImageUrl(document.getString("imageUrl") ?: "")
                        .build()
                    dogList.add(dog)
                }
                _dogs.value = dogList
                Log.d("FirestoreDebug", "Finished loading ${dogList.size} dogs")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDebug", "Error fetching dogs: $e")
            }
    }

    fun fetchFavoriteDogs() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("FirestoreDebug", "Error listening to favorites: ${error.message}")
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val favorites = document.get("favorites") as? List<String> ?: emptyList()
                    Log.d("FirestoreDebug", "Favorites loaded (${favorites.size} dogs) - $favorites")
                    _favoriteDogs.value = favorites // LiveData updates in real-time
                }
            }
    }

    fun getAvailableBreeds(): List<String> {
        val breeds = dogs.value?.map { it.breed }?.distinct()?.sorted() ?: emptyList()
        Log.d("FilterDebug", "Available breeds: $breeds")
        return breeds
    }

    fun getAvailableAges(): List<Int> {
        val ages = dogs.value?.map { it.age }?.distinct()?.sorted() ?: emptyList()
        Log.d("FilterDebug", "Available ages: $ages")
        return ages
    }
}
