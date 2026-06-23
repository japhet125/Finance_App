package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LanguageSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        val backButton = findViewById<Button>(R.id.btnBack)
        val languageSpinner = findViewById<Spinner>(R.id.spLanguage)
        val saveButton = findViewById<Button>(R.id.btnSaveLanguage)

        backButton.setOnClickListener {
            finish()
        }

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.languages,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        languageSpinner.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->

                    val language =
                        document.getString("language") ?: "en"

                    if (language == "fr") {
                        languageSpinner.setSelection(1)
                    } else {
                        languageSpinner.setSelection(0)
                    }
                }
        }

        saveButton.setOnClickListener {

            val selectedLanguage =
                languageSpinner.selectedItem.toString()

            val languageCode =
                if (selectedLanguage == "Français") {
                    "fr"
                } else {
                    "en"
                }

            if (userId != null) {
                db.collection("users")
                    .document(userId)
                    .update("language", languageCode)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Language updated",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = android.content.Intent(
                            this,
                            DashboardActivity::class.java
                        )

                        intent.flags =
                            android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK

                        startActivity(intent)
                        finish()
                    }
            }
        }
    }
}