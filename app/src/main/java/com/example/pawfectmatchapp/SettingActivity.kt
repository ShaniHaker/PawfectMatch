package com.example.pawfectmatchapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pawfectmatchapp.databinding.ActivitySettingBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sign out button
        binding.btnLogout.setOnClickListener {
            signOutUser()
        }

        // Delete account button
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    /**
     * Signs out the user
     */
    private fun signOutUser() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
    }

    /**
     * Displays a confirmation dialog for account deletion
     */
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteUserAccount() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Deletes the user from Firestore, Storage, and Authentication, including error handling
     */
    private fun deleteUserAccount() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid

            // Retrieve profile image URL from Firestore
            val userDocRef = db.collection("users").document(userId)
            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val profileImageUrl = document.getString("profileImageUrl")

                    if (!profileImageUrl.isNullOrEmpty()) {
                        // Convert URL to correct storage reference
                        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileImageUrl)

                        // Delete profile image from storage
                        storageRef.delete().addOnSuccessListener {
                            Toast.makeText(this, "Profile image deleted", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Error deleting profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "No profile image found in Firestore", Toast.LENGTH_SHORT).show()
                    }

                    // Delete user document from Firestore
                    userDocRef.delete().addOnSuccessListener {
                        Toast.makeText(this, "User data deleted", Toast.LENGTH_SHORT).show()

                        // Delete user from Firebase Authentication
                        user.delete().addOnSuccessListener {
                            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Error deleting account: ${e.message}", Toast.LENGTH_LONG).show()
                        }

                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Error deleting user data: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error accessing Firestore", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * If Firebase requires re-authentication before account deletion
     */
    private fun reAuthenticateAndDelete(user: FirebaseUser) {
        val userEmail = user.email
        if (userEmail.isNullOrEmpty()) {
            Toast.makeText(this, "Re-authentication required but no email found!", Toast.LENGTH_LONG).show()
            return
        }

        val password = "UserPasswordHere" // The user needs to provide the correct password
        val credential = EmailAuthProvider.getCredential(userEmail, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // After successful re-authentication, attempt to delete the account again
                user.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Account deleted successfully after re-authentication", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Final deletion failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Re-authentication failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
