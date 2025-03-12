package com.example.pawfectmatchapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pawfectmatchapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.navigation_Feed
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Ensure the user exists in Firestore (without checking authentication)
        checkUserInFirestore()
    }

    /**
     * Creates the menu (Settings in the Toolbar menu)
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * Handles clicks on menu items
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Checks if the user exists in Firestore, and if not, creates it
     */
    private fun checkUserInFirestore() {
        val currentUser = auth.currentUser ?: return

        val userDoc = db.collection("users").document(currentUser.uid)
        userDoc.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // The user does not exist - add them
                val newUser = hashMapOf(
                    "email" to (currentUser.email ?: "Unknown"),
                    "favorites" to emptyList<String>(), // Start with an empty favorites list
                    "sharedInFeed" to false // Ensure this field exists by default
                )
                userDoc.set(newUser, SetOptions.merge()) // Prevents overwriting existing data
                    .addOnSuccessListener { println("New user added to Firestore") }
                    .addOnFailureListener { e -> println("Error adding user: ${e.message}") }
            } else {
                println("User already exists in Firestore")
            }
        }.addOnFailureListener {
            println("Error accessing user document")
        }
    }
}
