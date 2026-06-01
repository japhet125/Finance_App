package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val backButton = findViewById<Button>(R.id.btnBack)
        val fullName = findViewById<EditText>(R.id.etEditFullName)
        val phone = findViewById<EditText>(R.id.etEditPhone)
        val address = findViewById<EditText>(R.id.etEditAddress)
        val apt = findViewById<EditText>(R.id.etEditApt)
        val city = findViewById<EditText>(R.id.etEditCity)
        val state = findViewById<EditText>(R.id.etEditState)
        val zipCode = findViewById<EditText>(R.id.etEditZipCode)
        val country = findViewById<EditText>(R.id.etEditCountry)
        val saveButton = findViewById<Button>(R.id.btnSaveProfile)

        backButton.setOnClickListener {
            finish()
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    fullName.setText(document.getString("fullName") ?: "")
                    phone.setText(document.getString("phone") ?: "")
                    address.setText(document.getString("address") ?: "")
                    apt.setText(document.getString("apt") ?: "")
                    city.setText(document.getString("city") ?: "")
                    state.setText(document.getString("state") ?: "")
                    zipCode.setText(document.getString("zipCode") ?: "")
                    country.setText(document.getString("country") ?: "")
                }
        }

        saveButton.setOnClickListener {
            val fullNameText = fullName.text.toString().trim()
            val phoneText = phone.text.toString().trim()
            val addressText = address.text.toString().trim()
            val aptText = apt.text.toString().trim()
            val cityText = city.text.toString().trim()
            val stateText = state.text.toString().trim()
            val zipText = zipCode.text.toString().trim()
            val countryText = country.text.toString().trim()

            if (
                fullNameText.isBlank() ||
                phoneText.isBlank() ||
                addressText.isBlank() ||
                cityText.isBlank() ||
                stateText.isBlank() ||
                zipText.isBlank() ||
                countryText.isBlank()
            ) {
                Toast.makeText(
                    this,
                    "Please fill required fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (userId != null) {
                val updates = mapOf(
                    "fullName" to fullNameText,
                    "phone" to phoneText,
                    "address" to addressText,
                    "apt" to aptText,
                    "city" to cityText,
                    "state" to stateText,
                    "zipCode" to zipText,
                    "country" to countryText,
                    "profileUpdatedAt" to System.currentTimeMillis()
                )

                db.collection("users")
                    .document(userId)
                    .update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Profile updated",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Update failed: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }
    }
}