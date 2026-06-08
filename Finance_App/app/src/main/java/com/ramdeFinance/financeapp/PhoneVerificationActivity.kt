package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PhoneVerificationActivity : AppCompatActivity() {

    private val demoCode = "123456"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verification)

        val otpInput = findViewById<EditText>(R.id.etOtpCode)
        val verifyButton = findViewById<Button>(R.id.btnVerifyOtp)
        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        verifyButton.setOnClickListener {

            val enteredCode = otpInput.text.toString().trim()

            if (enteredCode != demoCode) {
                Toast.makeText(
                    this,
                    "Invalid verification code",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val userId =
                FirebaseAuth.getInstance().currentUser?.uid

            if (userId == null) {
                Toast.makeText(
                    this,
                    "User not found",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val updates = mapOf(
                "phoneVerified" to true,
                "phoneVerifiedAt" to System.currentTimeMillis()
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Phone verified successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Verification failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}