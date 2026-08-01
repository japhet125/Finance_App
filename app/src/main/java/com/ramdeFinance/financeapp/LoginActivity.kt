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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val backButton =
            findViewById<Button>(R.id.btnBack)

        val emailInput =
            findViewById<EditText>(R.id.etLoginEmail)

        val passwordInput =
            findViewById<EditText>(R.id.etLoginPassword)

        val loginButton =
            findViewById<Button>(R.id.btnLoginNow)

        val forgotPasswordButton =
            findViewById<Button>(R.id.btnForgotPassword)

        backButton.setOnClickListener {
            finish()
        }

        forgotPasswordButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ForgotPasswordActivity::class.java
                )
            )
        }

        loginButton.setOnClickListener {
            val email =
                emailInput.text.toString().trim()

            val password =
                passwordInput.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(
                    this,
                    getString(R.string.enter_email_and_password),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            loginUser(
                email = email,
                password = password
            )
        }
    }

    private fun loginUser(
        email: String,
        password: String
    ) {
        auth.signInWithEmailAndPassword(
            email,
            password
        ).addOnCompleteListener(this) { task ->

            if (!task.isSuccessful) {
                Toast.makeText(
                    this,
                    getString(
                        R.string.login_failed_message,
                        task.exception?.message.orEmpty()
                    ),
                    Toast.LENGTH_LONG
                ).show()

                return@addOnCompleteListener
            }

            val user = auth.currentUser

            if (user == null) {
                Toast.makeText(
                    this,
                    getString(R.string.user_not_found),
                    Toast.LENGTH_SHORT
                ).show()

                return@addOnCompleteListener
            }

            user.reload()
                .addOnSuccessListener {
                    validateSignedInUser()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(
                        this,
                        getString(
                            R.string.login_failed_message,
                            error.message.orEmpty()
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun validateSignedInUser() {
        val user = auth.currentUser

        if (user == null) {
            return
        }

        if (!user.isEmailVerified) {
            Toast.makeText(
                this,
                getString(R.string.verify_email_before_login),
                Toast.LENGTH_LONG
            ).show()

            auth.signOut()
            return
        }

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                val accountStatus =
                    document.getString("accountStatus")
                        ?: "active"

                if (accountStatus == "suspended") {
                    Toast.makeText(
                        this,
                        getString(R.string.account_suspended),
                        Toast.LENGTH_LONG
                    ).show()

                    auth.signOut()
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .document(user.uid)
                    .update("emailVerified", true)

                Toast.makeText(
                    this,
                    getString(R.string.login_successful),
                    Toast.LENGTH_SHORT
                ).show()

                openDashboard()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    getString(
                        R.string.login_failed_message,
                        error.message.orEmpty()
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun openDashboard() {
        val intent =
            Intent(
                this,
                DashboardActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        startActivity(intent)
        finish()
    }
}