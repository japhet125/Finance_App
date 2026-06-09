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
        val identityUploadButton =
            findViewById<Button>(R.id.btnIdentityUpload)

        identityUploadButton.setOnClickListener {
            startActivity(Intent(this, IdentityUploadActivity::class.java))
        }
        val identityStatusText =
            findViewById<TextView>(R.id.txtIdentityStatus)

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

                        val language = document.getString("language") ?: "en"

                        val fullName = document.getString("fullName") ?: "N/A"
                        val email = document.getString("email") ?: "N/A"
                        val phone = document.getString("phone") ?: "N/A"
                        val role = document.getString("role") ?: "user"
                        val creditScore = document.getLong("creditScore") ?: 500
                        val emailVerified = document.getBoolean("emailVerified") ?: false
                        val phoneVerified = document.getBoolean("phoneVerified") ?: false
                        val borrowerLevel = document.getString("borrowerLevel") ?: "New"
                        val identityStatus =
                            document.getString("identityStatus") ?: "not_submitted"



                        if (language == "fr") {
                            identityUploadButton.text = "Soumettre l'identité"

                            nameText.text = "Nom : $fullName"
                            emailText.text = "Email : $email"
                            phoneText.text = "Téléphone : $phone"
                            roleText.text = "Rôle : $role"
                            creditScoreText.text = "Score de crédit : $creditScore"

                            emailVerificationText.text =
                                if (emailVerified) "✓ Email vérifié" else "✗ Email non vérifié"

                            phoneVerificationText.text =
                                if (phoneVerified) "✓ Téléphone vérifié" else "✗ Téléphone non vérifié"

                            verificationLevelText.text =
                                "🏅 Niveau emprunteur : $borrowerLevel"

                            verifyPhoneButton.text =
                                "Vérifier le téléphone"

                            editProfileButton.text =
                                "Modifier le profil"

                            backButton.text =
                                "Retour"
                            identityStatusText.text =
                                when (identityStatus) {
                                    "approved" -> "✓ Identité vérifiée"
                                    "rejected" -> "✗ Identité rejetée"
                                    "pending" -> "⏳ Vérification en cours"
                                    else -> "Aucune identité soumise"
                                }
                            if (identityStatus == "approved") {
                                identityUploadButton.visibility = View.GONE
                            } else {
                                identityUploadButton.visibility = View.VISIBLE
                            }

                        } else {
                            identityUploadButton.text = "Submit Identity Verification"


                            nameText.text = "Name: $fullName"
                            emailText.text = "Email: $email"
                            phoneText.text = "Phone: $phone"
                            roleText.text = "Role: $role"
                            creditScoreText.text = "Credit Score: $creditScore"

                            emailVerificationText.text =
                                if (emailVerified) "✓ Email Verified" else "✗ Email Not Verified"

                            phoneVerificationText.text =
                                if (phoneVerified) "✓ Phone Verified" else "✗ Phone Not Verified"

                            verificationLevelText.text =
                                "🏅 Borrower Level: $borrowerLevel"

                            verifyPhoneButton.text =
                                "Verify Phone"

                            editProfileButton.text =
                                "Edit Profile"

                            backButton.text =
                                "Back"
                            identityStatusText.text =
                                when (identityStatus) {
                                    "approved" -> "✓ Identity Verified"
                                    "rejected" -> "✗ Identity Rejected"
                                    "pending" -> "⏳ Verification Pending"
                                    else -> "No Identity Submitted"
                                }
                            if (identityStatus == "approved") {
                                identityUploadButton.visibility = View.GONE
                            } else {
                                identityUploadButton.visibility = View.VISIBLE
                            }
                        }

                        if (phoneVerified) {
                            verifyPhoneButton.visibility = View.GONE
                        } else {
                            verifyPhoneButton.visibility = View.VISIBLE
                        }
                    }
                }
        }
    }
}