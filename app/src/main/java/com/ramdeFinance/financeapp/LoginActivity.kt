package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val email = findViewById<EditText>(R.id.etLoginEmail)
        val password = findViewById<EditText>(R.id.etLoginPassword)
        val loginButton = findViewById<Button>(R.id.btnLoginNow)

        loginButton.setOnClickListener {

            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailText.isBlank() || passwordText.isBlank()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->

                    if (!task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Login failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }

                    val user = auth.currentUser

                    if (user == null) {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    user.reload().addOnSuccessListener {

                        if (!user.isEmailVerified) {
                            Toast.makeText(
                                this,
                                "Please verify your email before logging in.",
                                Toast.LENGTH_LONG
                            ).show()

                            auth.signOut()
                            return@addOnSuccessListener
                        }

                        db.collection("users")
                            .document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->

                                val accountStatus =
                                    document.getString("accountStatus") ?: "active"

                                if (accountStatus == "suspended") {
                                    Toast.makeText(
                                        this,
                                        "Your account has been suspended. Please contact support.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    auth.signOut()
                                } else {
                                    db.collection("users")
                                        .document(user.uid)
                                        .update("emailVerified", true)

                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                                    startActivity(Intent(this, DashboardActivity::class.java))
                                    finish()
                                }
                            }
                    }
                }
            val forgotPasswordButton =
                findViewById<Button>(R.id.btnForgotPassword)

            forgotPasswordButton.setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        ForgotPasswordActivity::class.java
                    )
                )
            }
        }
    }
}