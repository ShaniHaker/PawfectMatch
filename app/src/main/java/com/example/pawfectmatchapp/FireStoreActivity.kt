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
    private val articlesCollection = db.collection("Articles") // 🔹 קולקשן למאמרים


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

        //one time for uploading article data to firestore
       // uploadArticlesToFirestore()
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


    private fun uploadArticlesToFirestore() {
        val articles = listOf(
            mapOf(
                "title" to "13 most important questions to ask when adopting a dog",
                "summary" to "Adopting a new dog is a big responsibility — There’s no shame in not being quite ready yet! Consider these questions before you adopt a dog.\nBe honest with yourself — Having a dog can be time-consuming and expensive. Are you able to meet their needs?",
                "articleUrl" to "https://www.betterpet.com/learn/questions-to-ask-when-adopting-a-dog",
                "imageUrl" to "https://firebasestorage.googleapis.com/v0/b/pawfect-match-21a44.firebasestorage.app/o/Articles%20Photos%2F67b39b4b5ea53f6e950a5937_AdobeStock_233995176-scaled-p-1080.avif?alt=media&token=a14f657d-efd3-4b20-93c4-b43fabd61d5e"
            ),
            mapOf(
                "title" to "How to Take Care of a Dog: 8 Vet-Recommended Tips",
                "summary" to "As a responsible pet parent, ensuring your canine companion’s well-being is key to fostering a long, healthy, and happy life for your beloved dog.\nProper dog care encompasses a range of essential aspects, including providing adequate nutrition, regular exercise, proper grooming, a comfortable living environment, regular veterinary check-ups, effective training, reliable identification, and safe handling practices.",
                "articleUrl" to "https://thevets.com/resources/pet-health-care/how-to-take-care-of-a-dog",
                "imageUrl" to "https://firebasestorage.googleapis.com/v0/b/pawfect-match-21a44.firebasestorage.app/o/Articles%20Photos%2Fperson-taking-care-of-a-dog-2.jpg?alt=media&token=dbc75734-4928-4e8e-9be4-66d272bcb675"
            ),
            mapOf(
                "title" to "11 Requirements to Adopt a Dog From a Shelter: Application, Fees & Considerations",
                "summary" to "Adopting a dog from a shelter is a rewarding experience that gives a second chance to a deserving animal and helps open resources for other pets in need. However, each rescue or shelter organization is likely to have guidelines that you need to follow before you can take your new dog home with you. Here’s a list of the most common requirements that you might need to meet during the adoption process.",
                "articleUrl" to "https://www.dogster.com/lifestyle/requirements-to-adopt-a-dog-from-a-shelter",
                "imageUrl" to "https://firebasestorage.googleapis.com/v0/b/pawfect-match-21a44.firebasestorage.app/o/Articles%20Photos%2F67b39b4b5ea53f6e950a5937_AdobeStock_233995176-scaled-p-1080.avif?alt=media&token=a14f657d-efd3-4b20-93c4-b43fabd61d5e"
            )
        )

        for (article in articles) {
            articlesCollection.add(article)
                .addOnSuccessListener { println("✅ מאמר נוסף בהצלחה: ${article["title"]}") }
                .addOnFailureListener { e -> println("❌ שגיאה בהעלאת מאמר: ${e.message}") }
        }
    }
}
