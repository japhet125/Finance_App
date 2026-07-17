package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LanguageSettingsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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

        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val language =
                        document.getString("language") ?: getCurrentLanguage()

                    languageSpinner.setSelection(
                        if (language == "fr") 1 else 0
                    )
                }
        }

        saveButton.setOnClickListener {
            val languageCode =
                if (languageSpinner.selectedItemPosition == 1) {
                    "fr"
                } else {
                    "en"
                }

            saveLanguage(
                userId = userId,
                languageCode = languageCode
            )
        }
    }

    private fun saveLanguage(
        userId: String?,
        languageCode: String
    ) {
        saveButtonEnabled(false)

        if (userId == null) {
            applyLanguage(languageCode)
            return
        }

        db.collection("users")
            .document(userId)
            .update("language", languageCode)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.language_updated),
                    Toast.LENGTH_SHORT
                ).show()

                applyLanguage(languageCode)
            }
            .addOnFailureListener { error ->
                saveButtonEnabled(true)

                Toast.makeText(
                    this,
                    error.message ?: getString(R.string.language_update_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun applyLanguage(languageCode: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageCode)
        )

        val intent = Intent(
            this,
            DashboardActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    private fun getCurrentLanguage(): String {
        return AppCompatDelegate
            .getApplicationLocales()[0]
            ?.language
            ?: resources.configuration.locales[0].language
    }

    private fun saveButtonEnabled(enabled: Boolean) {
        findViewById<Button>(R.id.btnSaveLanguage).apply {
            isEnabled = enabled
            alpha = if (enabled) 1f else 0.6f
        }
    }
}