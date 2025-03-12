package com.example.pawfectmatchapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (auth.currentUser == null) {
                        signIn()
                    } else {
                        transactToNextScreen()
                    }
                } else {
                    signIn()
                }
            }
        } else {
            signIn()
        }
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.logo)
            .setTheme(R.style.FirebaseLoginTheme)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                // transport user to the activity of profile set up after log in is done
                goToProfileSetup(user.uid)
            } else {
                transactToNextScreen()
            }
        } else {
            Toast.makeText(this, "Error: Failed logging in", Toast.LENGTH_LONG).show()
            signIn()
        }
    }

    private fun transactToNextScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToProfileSetup(userId: String) {
        // choose image from gallery and name
        val intent = Intent(this, ProfileSetUpActivity::class.java)
        intent.putExtra("USER_ID", userId) // sending UID
        startActivity(intent)
        finish()
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                signIn()
            }
    }
}
