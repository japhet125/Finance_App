package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_forgot_password)

        val emailInput =
            findViewById<EditText>(R.id.etResetEmail)

        val sendButton =
            findViewById<Button>(R.id.btnSendReset)

        val backButton =
            findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        sendButton.setOnClickListener {

            val email =
                emailInput.text.toString().trim()

            if (email.isBlank()) {

                Toast.makeText(
                    this,
                    "Enter your email",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            FirebaseAuth.getInstance()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Password reset email sent",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener { e ->

                    Toast.makeText(
                        this,
                        e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}