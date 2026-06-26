package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Spinner
import android.widget.ArrayAdapter

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val fullName = findViewById<EditText>(R.id.etFullName)
        val email = findViewById<EditText>(R.id.etEmail)
        val phone = findViewById<EditText>(R.id.etPhone)
        val password = findViewById<EditText>(R.id.etPassword)
        val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val createButton = findViewById<Button>(R.id.btnCreateAccount)
        val address =
            findViewById<EditText>(R.id.etAddress)

        val apt =
            findViewById<EditText>(R.id.etApt)

        val city =
            findViewById<EditText>(R.id.etCity)

        val state =
            findViewById<EditText>(R.id.etState)

        val zipCode =
            findViewById<EditText>(R.id.etZipCode)

        val countrySpinner =
            findViewById<Spinner>(R.id.spCountry)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.supported_countries,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        countrySpinner.adapter = adapter

        val languageSpinner =
            findViewById<Spinner>(R.id.spLanguage)

        val languageAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.supported_languages,
            android.R.layout.simple_spinner_item
        )

        languageAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        languageSpinner.adapter = languageAdapter


        createButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val confirmPasswordText = confirmPassword.text.toString().trim()
            val fullNameText = fullName.text.toString().trim()
            val phoneText = phone.text.toString().trim()

            val addressText = address.text.toString().trim()
            val aptText = apt.text.toString().trim()
            val cityText = city.text.toString().trim()
            val stateText = state.text.toString().trim()
            val zipText = zipCode.text.toString().trim()
            val countryText =
                countrySpinner.selectedItem.toString()


            if (
                fullName.text.isBlank() ||
                emailText.isBlank() ||
                phone.text.isBlank() ||
                passwordText.isBlank() ||
                confirmPasswordText.isBlank()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (passwordText != confirmPasswordText) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                        val selectedLanguage =
                            languageSpinner.selectedItem.toString()

                        val languageCode =
                            if (selectedLanguage == "Français") "fr" else "en"

                        auth.createUserWithEmailAndPassword(emailText, passwordText)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid

                                    if (userId != null) {
                                        val userProfile = hashMapOf(
                                            "userId" to userId,
                                            "fullName" to fullNameText,
                                            "email" to emailText,
                                            "phone" to phoneText,
                                            "phoneVerified" to false,
                                            "phoneVerifiedAt" to 0L,

                                            "address" to addressText,
                                            "apt" to aptText,
                                            "city" to cityText,
                                            "state" to stateText,
                                            "zipCode" to zipText,
                                            "country" to countryText,
                                            "language" to languageCode,
                                            "accountStatus" to "active",
                                            "completedLoans" to 0,
                                            "borrowerLevel" to "New",

                                            "creditScore" to 500,
                                            "identityVerified" to false,
                                            "identityStatus" to "pending",
                                            "role" to "user",
                                            "emailVerified" to false,
                                            "createdAt" to System.currentTimeMillis()

                                        )


                                        db.collection("users")
                                            .document(userId)
                                            .set(userProfile)
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                                                auth.currentUser?.sendEmailVerification()

                                                Toast.makeText(
                                                    this,
                                                    "Verification email sent. Please check your inbox.",
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                FirebaseAuth.getInstance().signOut()

                                                val intent = Intent(this, LoginActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }

                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "Profile save failed: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Signup failed: ${task.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }


                    }

            }
        }
    }
