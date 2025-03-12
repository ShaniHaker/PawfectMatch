package com.example.pawfectmatchapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ProfileSetUpActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        profileImageView = findViewById(R.id.profileImageView)
        nameEditText = findViewById(R.id.nameEditText)
        saveButton = findViewById(R.id.saveButton)
        progressBar = findViewById(R.id.progressBarSaving)

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val userId = user.uid

        // Select image from gallery
        profileImageView.setOnClickListener {
            pickImageFromGallery()
        }

        // Save profile
        saveButton.setOnClickListener {
            val userName = nameEditText.text.toString().trim()
            if (userName.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading and disable button
            saveButton.isEnabled = false
            saveButton.text = "Saving..."
            saveButton.setBackgroundColor(resources.getColor(R.color.teal_200))
            progressBar.visibility = View.VISIBLE

            if (selectedImageUri != null) {
                uploadImageToStorage(userId, userName, selectedImageUri!!)
            } else {
                uploadDefaultImageToStorage(userId, userName)
            }
        }
    }

    /**
     * Opens the gallery to select an image
     */
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            profileImageView.setImageURI(selectedImageUri)
        }
    }

    /**
     * Uploads the selected image to Firebase Storage
     */
    private fun uploadImageToStorage(userId: String, userName: String, imageUri: Uri) {
        val imageRef = storageRef.child("users/$userId/profile.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveUserToFirestore(userId, userName, downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                resetUIAfterUpload()
            }
    }

    /**
     * Uploads a default profile image from drawable to Firebase Storage
     */
    private fun uploadDefaultImageToStorage(userId: String, userName: String) {
        val imageRef = storageRef.child("users/$userId/profile.jpg")

        val bitmap = (resources.getDrawable(R.drawable.account) as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveUserToFirestore(userId, userName, downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload default image", Toast.LENGTH_SHORT).show()
                resetUIAfterUpload()
            }
    }

    /**
     * Saves the user's name and profile image URL in Firestore
     */
    private fun saveUserToFirestore(userId: String, userName: String, imageUrl: String) {
        val userRef = db.collection("users").document(userId)

        val userData = hashMapOf(
            "name" to userName,
            "profileImageUrl" to imageUrl
        )

        userRef.set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                goToMainActivity()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
                resetUIAfterUpload()
            }
    }

    /**
     * Resets the UI after the upload is complete
     */
    private fun resetUIAfterUpload() {
        saveButton.isEnabled = true
        saveButton.text = "Save Profile"
        saveButton.setBackgroundColor(resources.getColor(R.color.teal_700)) // Restore original color
        progressBar.visibility = View.GONE
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
