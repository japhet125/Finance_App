package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val backButton = findViewById<Button>(R.id.btnBack)
        val editProfileButton = findViewById<Button>(R.id.btnEditProfile)
        val verifyPhoneButton = findViewById<Button>(R.id.btnVerifyPhone)
        val verificationLevelText =
            findViewById<TextView>(R.id.txtVerificationLevel)

        val nameText = findViewById<TextView>(R.id.txtProfileName)
        val emailText = findViewById<TextView>(R.id.txtProfileEmail)
        val phoneText = findViewById<TextView>(R.id.txtProfilePhone)
        val roleText = findViewById<TextView>(R.id.txtProfileRole)
        val creditScoreText = findViewById<TextView>(R.id.txtProfileCreditScore)
        val emailVerificationText = findViewById<TextView>(R.id.txtEmailVerification)
        val phoneVerificationText = findViewById<TextView>(R.id.txtPhoneVerification)

        backButton.setOnClickListener {
            finish()
        }

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        verifyPhoneButton.setOnClickListener {
            startActivity(Intent(this, PhoneVerificationActivity::class.java))
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .addSnapshotListener { document, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName") ?: "N/A"
                        val email = document.getString("email") ?: "N/A"
                        val phone = document.getString("phone") ?: "N/A"
                        val role = document.getString("role") ?: "user"
                        val creditScore = document.getLong("creditScore") ?: 500
                        val emailVerified = document.getBoolean("emailVerified") ?: false
                        val phoneVerified = document.getBoolean("phoneVerified") ?: false
                        if (emailVerified && phoneVerified) {

                            verificationLevelText.text =
                                "🟢 Verification Level: Fully Verified"

                        } else {

                            verificationLevelText.text =
                                "🟡 Verification Level: Pending"
                        }

                        nameText.text = "Name: $fullName"
                        emailText.text = "Email: $email"
                        phoneText.text = "Phone: $phone"
                        roleText.text = "Role: $role"
                        creditScoreText.text = "Credit Score: $creditScore"

                        emailVerificationText.text =
                            if (emailVerified) {
                                "✓ Email Verified"
                            } else {
                                "✗ Email Not Verified"
                            }

                        if (phoneVerified) {
                            phoneVerificationText.text = "✓ Phone Verified"
                            verifyPhoneButton.visibility = View.GONE
                        } else {
                            phoneVerificationText.text = "✗ Phone Not Verified"
                            verifyPhoneButton.visibility = View.VISIBLE
                        }
                    }
                }
        }
    }
}