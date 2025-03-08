package com.example.pawfectmatchapp

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pawfectmatchapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // ✅ ודא שהמשתמש קיים ב-Firestore (ללא בדיקת התחברות)
        checkUserInFirestore()
    }

    /**
     * ✅ בודק אם המשתמש קיים בפיירסטור ואם לא - יוצר אותו
     */
    private fun checkUserInFirestore() {
        val currentUser = auth.currentUser ?: return

        val userDoc = db.collection("users").document(currentUser.uid)
        userDoc.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // 🔹 המשתמש לא קיים - נוסיף אותו
                val newUser = hashMapOf(
                    "email" to (currentUser.email ?: "Unknown"),
                    "favorites" to emptyList<String>() // ✅ התחל עם רשימת מועדפים ריקה
                )
                userDoc.set(newUser)
                    .addOnSuccessListener { println("✅ משתמש חדש נוסף לפיירסטור!") }
                    .addOnFailureListener { e -> println("❌ שגיאה בהוספת המשתמש: ${e.message}") }
            } else {
                println("✅ המשתמש כבר קיים בפיירסטור.")
            }
        }.addOnFailureListener {
            println("❌ שגיאה בגישה למסמך המשתמש")
        }
    }
}
