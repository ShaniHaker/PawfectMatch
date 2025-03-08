package com.example.pawfectmatchapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pawfectmatchapp.databinding.ActivityFireStoreBinding
import com.example.pawfectmatchapp.utilities.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FireStoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFireStoreBinding

    private val db = Firebase.firestore
    private val dogsCollection = db.collection(Constants.DB.DOGS_COLLECTION_REF) // 🔹 קולקציה לכלבים
    private val usersCollection = db.collection("Users") // 🔹 קולקציה למשתמשים

    private var userFavorites: List<String> = emptyList() // 🔹 רשימת הכלבים המועדפים של המשתמש

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFireStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ המשתמש כבר נבדק ונוסף בפיירסטור דרך MainActivity, לכן אין צורך לבדוק אותו שוב כאן!
        // currentUser?.let { user -> createUserIfNotExist(user.uid, user.email) }

        // ✅ הבאת המועדפים של המשתמש (אפשר להשאיר כדי ללמוד איך זה עובד)
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            getFavoriteDogs(user.uid) { favorites ->
                userFavorites = favorites
            }
        }

        // ✅ אפשר להשאיר את זה כדי ללמוד איך להעלות נתונים לפיירסטור אבל זה לא הכרחי
        // addDogsCollectionToFireStore()
    }

    /**
     * ✅ פונקציה שמביאה את רשימת הכלבים המועדפים של המשתמש
     */
    fun getFavoriteDogs(userId: String, callback: (List<String>) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val favorites = document.get("favorites") as? List<String> ?: emptyList()
                    callback(favorites)
                } else {
                    callback(emptyList())
                }
            }
            .addOnFailureListener { e ->
                println("❌ שגיאה בהבאת מועדפים: ${e.message}")
                callback(emptyList())
            }
    }

    /**
     * ✅ פונקציה להוספת כלב למועדפים של המשתמש
     */
    fun addFavoriteDog(userId: String, dogId: String) {
        val userDoc = usersCollection.document(userId)

        userDoc.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favorites = document.get("favorites") as? MutableList<String> ?: mutableListOf()
                if (!favorites.contains(dogId)) {
                    favorites.add(dogId)
                    userDoc.update("favorites", favorites)
                        .addOnSuccessListener { println("✅ הכלב נוסף למועדפים") }
                        .addOnFailureListener { e -> println("❌ שגיאה בהוספת הכלב למועדפים: ${e.message}") }
                }
            }
        }
    }

    /**
     * ✅ פונקציה להסרת כלב מהמועדפים של המשתמש
     */
    fun removeFavoriteDog(userId: String, dogId: String) {
        val userDoc = usersCollection.document(userId)

        userDoc.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favorites = document.get("favorites") as? MutableList<String> ?: mutableListOf()
                if (favorites.contains(dogId)) {
                    favorites.remove(dogId)
                    userDoc.update("favorites", favorites)
                        .addOnSuccessListener { println("✅ הכלב הוסר מהמועדפים") }
                        .addOnFailureListener { e -> println("❌ שגיאה בהסרת הכלב מהמועדפים: ${e.message}") }
                }
            }
        }
    }

    /**
     * 🛠️ פונקציה שמעלה נתונים על כלבים לפיירסטור (לא חובה לריצה, אבל טוב לתרגול)
     */
    private fun addDogsCollectionToFireStore() {
        addDog("Buddy", "Golden Retriever", 3, "Male", "Friendly and playful dog",
            "https://example.com/buddy.jpg")

        addDog("Max", "Labrador Retriever", 4, "Male", "Loyal and energetic companion",
            "https://example.com/max.jpg")

        addDog("Bella", "Siberian Husky", 2, "Female", "Loves the cold and very intelligent",
            "https://example.com/bella.jpg")
    }

    /**
     * ✅ פונקציה להוספת כלב בודד לפיירסטור
     */
    private fun addDog(name: String, breed: String, age: Int, gender: String, description: String, imageUrl: String) {
        val animal = hashMapOf(
            "name" to name,
            "breed" to breed,
            "age" to age,
            "gender" to gender,
            "description" to description,
            "imageUrl" to imageUrl
        )

        dogsCollection.add(animal)
            .addOnSuccessListener { println("✅ הכלב נוסף בהצלחה: $name") }
            .addOnFailureListener { e -> println("❌ שגיאה בהוספת הכלב: ${e.message}") }
    }
}
