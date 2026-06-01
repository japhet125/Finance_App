package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.view.View

class BankAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank_account)

        val backButton = findViewById<Button>(R.id.btnBack)

        val bankName = findViewById<EditText>(R.id.etBankName)
        val accountType = findViewById<EditText>(R.id.etAccountType)
        val routingNumber = findViewById<EditText>(R.id.etRoutingNumber)
        val accountNumber = findViewById<EditText>(R.id.etAccountNumber)
        val bankCountrySpinner = findViewById<Spinner>(R.id.spinnerBankCountry)
        val currency = findViewById<EditText>(R.id.etCurrency)
        val saveButton = findViewById<Button>(R.id.btnSaveBank)

        val countries = listOf(
            "USA",
            "Burkina Faso",
            "Mali",
            "Niger",
            "Côte d'Ivoire"
        )

        val countryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            countries
        )

        countryAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        bankCountrySpinner.adapter = countryAdapter

        bankCountrySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedCountry = countries[position]

                    val selectedCurrency =
                        if (selectedCountry == "USA") {
                            "USD"
                        } else {
                            "XOF"
                        }

                    currency.setText(selectedCurrency)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

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

                    bankName.setText(document.getString("bankName") ?: "")
                    accountType.setText(document.getString("accountType") ?: "")
                    routingNumber.setText(document.getString("routingNumber") ?: "")
                    accountNumber.setText(document.getString("accountNumber") ?: "")
                    val savedCountry = document.getString("bankCountry") ?: "USA"
                    val savedIndex = countries.indexOf(savedCountry)

                    if (savedIndex >= 0) {
                        bankCountrySpinner.setSelection(savedIndex)
                    }

                    currency.setText(document.getString("currency") ?: "USD")
                }
        }

        saveButton.setOnClickListener {

            val bankNameText = bankName.text.toString().trim()
            val accountTypeText = accountType.text.toString().trim()
            val routingText = routingNumber.text.toString().trim()
            val accountText = accountNumber.text.toString().trim()
            val bankCountryText =
                bankCountrySpinner.selectedItem.toString()

            val currencyText =
                currency.text.toString().trim()

            if (
                bankNameText.isBlank() ||
                accountTypeText.isBlank() ||
                accountText.isBlank() ||
                currencyText.isBlank()
            ) {
                Toast.makeText(
                    this,
                    "Please fill all required fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (userId != null) {
                val bankInfo = mapOf(
                    "bankName" to bankNameText,
                    "accountType" to accountTypeText,
                    "routingNumber" to routingText,
                    "accountNumber" to accountText,
                    "currency" to currencyText,
                    "bankInfoSubmitted" to true,
                    "bankCountry" to bankCountryText,
                    "bankInfoUpdatedAt" to System.currentTimeMillis()
                )

                db.collection("users")
                    .document(userId)
                    .update(bankInfo)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Bank account saved",
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