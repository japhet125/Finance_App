package com.ramdefinance.financeapp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MobileMoneyActivity : AppCompatActivity() {

    private val providersByCountry = mapOf(
        "Burkina Faso" to listOf("Orange Money", "Moov Money", "Wave"),
        "Mali" to listOf("Orange Money", "Moov Money"),
        "Niger" to listOf("Airtel Money", "Moov Money", "Zamani Cash"),
        "Côte d'Ivoire" to listOf("Orange Money", "MTN MoMo", "Moov Money", "Wave")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_money)

        val backButton = findViewById<Button>(R.id.btnBack)
        val countrySpinner = findViewById<Spinner>(R.id.spinnerMobileMoneyCountry)
        val providerSpinner = findViewById<Spinner>(R.id.spinnerMobileMoneyProvider)
        val phoneInput = findViewById<EditText>(R.id.etMobileMoneyPhone)
        val nameInput = findViewById<EditText>(R.id.etMobileMoneyName)
        val currencyInput = findViewById<EditText>(R.id.etMobileMoneyCurrency)
        val saveButton = findViewById<Button>(R.id.btnSaveMobileMoney)

        backButton.setOnClickListener {
            finish()
        }

        val countries = providersByCountry.keys.toList()

        val countryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            countries
        )

        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countrySpinner.adapter = countryAdapter

        fun updateProviderSpinner(country: String) {
            val providers = providersByCountry[country] ?: emptyList()

            val providerAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                providers
            )

            providerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            providerSpinner.adapter = providerAdapter
            currencyInput.setText("XOF")
        }

        countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateProviderSpinner(countries[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->

                    val savedCountry = document.getString("mobileMoneyCountry")
                    val savedProvider = document.getString("mobileMoneyProvider")

                    phoneInput.setText(document.getString("mobileMoneyPhone") ?: "")
                    nameInput.setText(document.getString("mobileMoneyName") ?: "")
                    currencyInput.setText(document.getString("mobileMoneyCurrency") ?: "XOF")

                    if (savedCountry != null && countries.contains(savedCountry)) {
                        val countryIndex = countries.indexOf(savedCountry)
                        countrySpinner.setSelection(countryIndex)

                        providerSpinner.post {
                            val providers = providersByCountry[savedCountry] ?: emptyList()
                            val providerIndex = providers.indexOf(savedProvider)

                            if (providerIndex >= 0) {
                                providerSpinner.setSelection(providerIndex)
                            }
                        }
                    }
                }
        }

        saveButton.setOnClickListener {

            val countryText = countrySpinner.selectedItem.toString()
            val providerText = providerSpinner.selectedItem.toString()
            val phoneText = phoneInput.text.toString().trim()
            val nameText = nameInput.text.toString().trim()
            val currencyText = currencyInput.text.toString().trim()

            if (phoneText.isBlank() || nameText.isBlank()) {
                Toast.makeText(
                    this,
                    "Please enter phone number and account holder name",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (userId != null) {
                val mobileMoneyInfo = mapOf(
                    "mobileMoneyCountry" to countryText,
                    "mobileMoneyProvider" to providerText,
                    "mobileMoneyPhone" to phoneText,
                    "mobileMoneyName" to nameText,
                    "mobileMoneyCurrency" to currencyText,
                    "mobileMoneySubmitted" to true,
                    "mobileMoneyUpdatedAt" to System.currentTimeMillis()
                )

                db.collection("users")
                    .document(userId)
                    .update(mobileMoneyInfo)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Mobile money information saved",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Save failed: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }
    }
}