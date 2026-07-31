package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LanguageSettingsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var saveButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        val backButton =
            findViewById<MaterialButton>(R.id.btnBack)

        val languageSpinner =
            findViewById<Spinner>(R.id.spLanguage)

        saveButton =
            findViewById(R.id.btnSaveLanguage)

        backButton.setOnClickListener {
            finish()
        }

        setupLanguageSpinner(languageSpinner)

        val userId = auth.currentUser?.uid

        loadSavedLanguage(
            userId = userId,
            languageSpinner = languageSpinner
        )

        saveButton.setOnClickListener {
            val languageCode =
                if (languageSpinner.selectedItemPosition == 1) {
                    LANGUAGE_FRENCH
                } else {
                    LANGUAGE_ENGLISH
                }

            saveLanguage(
                userId = userId,
                languageCode = languageCode
            )
        }
    }

    private fun setupLanguageSpinner(
        languageSpinner: Spinner
    ) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.languages,
            R.layout.item_spinner_selected
        )

        adapter.setDropDownViewResource(
            R.layout.item_spinner_dropdown
        )

        languageSpinner.adapter = adapter
    }

    private fun loadSavedLanguage(
        userId: String?,
        languageSpinner: Spinner
    ) {
        if (userId == null) {
            setSpinnerSelection(
                languageSpinner = languageSpinner,
                languageCode = getCurrentLanguage()
            )

            return
        }

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val languageCode =
                    document.getString("language")
                        ?: getCurrentLanguage()

                setSpinnerSelection(
                    languageSpinner = languageSpinner,
                    languageCode = languageCode
                )
            }
            .addOnFailureListener {
                setSpinnerSelection(
                    languageSpinner = languageSpinner,
                    languageCode = getCurrentLanguage()
                )
            }
    }

    private fun setSpinnerSelection(
        languageSpinner: Spinner,
        languageCode: String
    ) {
        val position =
            if (languageCode == LANGUAGE_FRENCH) {
                1
            } else {
                0
            }

        languageSpinner.setSelection(position)
    }

    private fun saveLanguage(
        userId: String?,
        languageCode: String
    ) {
        setSaveButtonEnabled(false)

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
                setSaveButtonEnabled(true)

                Toast.makeText(
                    this,
                    error.message
                        ?: getString(
                            R.string.language_update_failed
                        ),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun applyLanguage(
        languageCode: String
    ) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(
                languageCode
            )
        )

        val intent =
            Intent(
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

    private fun setSaveButtonEnabled(
        enabled: Boolean
    ) {
        saveButton.isEnabled = enabled
        saveButton.alpha =
            if (enabled) {
                1f
            } else {
                0.6f
            }
    }

    private companion object {
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_FRENCH = "fr"
    }
}