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
                    Log.e("DashboardViewModel", "❌ שגיאה בקבלת המועדפים: ${error.message}")
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val favoriteDogIds = document.get("favorites") as? List<String> ?: emptyList()
                    fetchDogsByIds(favoriteDogIds)
                }
            }
    }

    private fun fetchDogsByIds(dogIds: List<String>) {
        Log.d("DashboardViewModel", "🔍 קיבלנו ${dogIds.size} מזהי כלבים: $dogIds") // ✅ בודק את ה-IDים שנשלחים

        if (dogIds.isEmpty()) {
            _favoriteDogs.value = emptyList()
            Log.d("DashboardViewModel", "⚠️ אין כלבים להצגה!")
            return
        }

        val dogsList = mutableListOf<DogData>()
        val batchSize = 10
        val batches = dogIds.chunked(batchSize)

        batches.forEach { batch ->
            Log.d("DashboardViewModel", "📌 מבצע שאילתת Firestore עבור קבוצה בגודל ${batch.size}: $batch") // 🔍 הדפסת ה-IDים לפני שליחה

            db.collection("Dogs").whereIn(FieldPath.documentId(), batch).get()
                .addOnSuccessListener { documents ->
                    Log.d("DashboardViewModel", "✅ התקבלו ${documents.size()} תוצאות מהשאילתה!") // ✅ כמה כלבים התקבלו

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

                        Log.d("DashboardViewModel", "🐶 נוסף כלב: ${dog.id} - ${dog.name}") // ✅ וידוא שכלב נוסף
                    }

                    _favoriteDogs.value = dogsList
                    Log.d("DashboardViewModel", "📢 בסוף התהליך נוספו ${dogsList.size} כלבים לרשימת המועדפים!") // 🔍 כמה כלבים יש בסוף
                }
                .addOnFailureListener { e ->
                    Log.e("DashboardViewModel", "❌ שגיאה בקבלת הכלבים מ-Firestore: ${e.message}")
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
                    println("✅ עדכון מועדפים בפיירסטור הצליח")
                    fetchFavoriteDogs()
                }
                .addOnFailureListener {
                    println("❌ שגיאה בעדכון מועדפים")
                }
        }
    }
}
