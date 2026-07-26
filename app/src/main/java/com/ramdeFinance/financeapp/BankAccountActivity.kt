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

class BankAccountActivity : AppCompatActivity() {

    private val countryCodes = listOf(
        COUNTRY_USA,
        COUNTRY_BURKINA_FASO,
        COUNTRY_MALI,
        COUNTRY_NIGER,
        COUNTRY_COTE_DIVOIRE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank_account)

        val backButton =
            findViewById<Button>(R.id.btnBack)

        val bankNameInput =
            findViewById<EditText>(R.id.etBankName)

        val accountTypeInput =
            findViewById<EditText>(R.id.etAccountType)

        val routingNumberInput =
            findViewById<EditText>(R.id.etRoutingNumber)

        val accountNumberInput =
            findViewById<EditText>(R.id.etAccountNumber)

        val bankCountrySpinner =
            findViewById<Spinner>(R.id.spinnerBankCountry)

        val currencyInput =
            findViewById<EditText>(R.id.etCurrency)

        val saveButton =
            findViewById<Button>(R.id.btnSaveBank)

        backButton.setOnClickListener {
            finish()
        }

        setupCountrySpinner(
            bankCountrySpinner = bankCountrySpinner,
            currencyInput = currencyInput
        )

        val userId =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid

        val db =
            FirebaseFirestore.getInstance()

        if (userId != null) {
            loadSavedBankAccount(
                db = db,
                userId = userId,
                bankNameInput = bankNameInput,
                accountTypeInput = accountTypeInput,
                routingNumberInput = routingNumberInput,
                accountNumberInput = accountNumberInput,
                bankCountrySpinner = bankCountrySpinner,
                currencyInput = currencyInput
            )
        }

        saveButton.setOnClickListener {
            saveBankAccount(
                db = db,
                userId = userId,
                bankNameInput = bankNameInput,
                accountTypeInput = accountTypeInput,
                routingNumberInput = routingNumberInput,
                accountNumberInput = accountNumberInput,
                bankCountrySpinner = bankCountrySpinner,
                currencyInput = currencyInput
            )
        }
    }

    private fun setupCountrySpinner(
        bankCountrySpinner: Spinner,
        currencyInput: EditText
    ) {
        val localizedCountries =
            countryCodes.map { countryCode ->
                getLocalizedCountryName(countryCode)
            }

        val countryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            localizedCountries
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
                    val selectedCountry =
                        countryCodes[position]

                    val selectedCurrency =
                        if (selectedCountry == COUNTRY_USA) {
                            CURRENCY_USD
                        } else {
                            CURRENCY_XOF
                        }

                    currencyInput.setText(selectedCurrency)
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    // No action required.
                }
            }
    }

    private fun loadSavedBankAccount(
        db: FirebaseFirestore,
        userId: String,
        bankNameInput: EditText,
        accountTypeInput: EditText,
        routingNumberInput: EditText,
        accountNumberInput: EditText,
        bankCountrySpinner: Spinner,
        currencyInput: EditText
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                bankNameInput.setText(
                    document.getString("bankName").orEmpty()
                )

                accountTypeInput.setText(
                    document.getString("accountType").orEmpty()
                )

                routingNumberInput.setText(
                    document.getString("routingNumber").orEmpty()
                )

                accountNumberInput.setText(
                    document.getString("accountNumber").orEmpty()
                )

                val savedCountry =
                    document.getString("bankCountry")
                        ?: COUNTRY_USA

                val savedCountryIndex =
                    countryCodes.indexOf(savedCountry)

                if (savedCountryIndex >= 0) {
                    bankCountrySpinner.setSelection(
                        savedCountryIndex
                    )
                }

                val defaultCurrency =
                    if (savedCountry == COUNTRY_USA) {
                        CURRENCY_USD
                    } else {
                        CURRENCY_XOF
                    }

                currencyInput.setText(
                    document.getString("currency")
                        ?: defaultCurrency
                )
            }
    }

    private fun saveBankAccount(
        db: FirebaseFirestore,
        userId: String?,
        bankNameInput: EditText,
        accountTypeInput: EditText,
        routingNumberInput: EditText,
        accountNumberInput: EditText,
        bankCountrySpinner: Spinner,
        currencyInput: EditText
    ) {
        if (userId == null) {
            Toast.makeText(
                this,
                getString(R.string.user_not_signed_in),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val selectedCountryPosition =
            bankCountrySpinner.selectedItemPosition

        if (
            selectedCountryPosition < 0 ||
            selectedCountryPosition >= countryCodes.size
        ) {
            Toast.makeText(
                this,
                getString(R.string.select_bank_country),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val bankNameText =
            bankNameInput.text
                .toString()
                .trim()

        val accountTypeText =
            accountTypeInput.text
                .toString()
                .trim()

        val routingNumberText =
            routingNumberInput.text
                .toString()
                .trim()

        val accountNumberText =
            accountNumberInput.text
                .toString()
                .trim()

        val currencyText =
            currencyInput.text
                .toString()
                .trim()

        if (
            bankNameText.isBlank() ||
            accountTypeText.isBlank() ||
            accountNumberText.isBlank() ||
            currencyText.isBlank()
        ) {
            Toast.makeText(
                this,
                getString(R.string.bank_required_fields),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val selectedCountry =
            countryCodes[selectedCountryPosition]

        val bankInfo = mapOf(
            "bankName" to bankNameText,
            "accountType" to accountTypeText,
            "routingNumber" to routingNumberText,
            "accountNumber" to accountNumberText,
            "currency" to currencyText,
            "bankInfoSubmitted" to true,
            "bankCountry" to selectedCountry,
            "bankInfoUpdatedAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .update(bankInfo)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.bank_account_saved),
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    getString(
                        R.string.bank_account_save_failed,
                        error.localizedMessage.orEmpty()
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun getLocalizedCountryName(
        countryCode: String
    ): String {
        return when (countryCode) {
            COUNTRY_USA ->
                getString(R.string.country_united_states)

            COUNTRY_BURKINA_FASO ->
                getString(R.string.country_burkina_faso)

            COUNTRY_MALI ->
                getString(R.string.country_mali)

            COUNTRY_NIGER ->
                getString(R.string.country_niger)

            COUNTRY_COTE_DIVOIRE ->
                getString(R.string.country_cote_divoire)

            else -> countryCode
        }
    }

    companion object {
        private const val COUNTRY_USA = "USA"
        private const val COUNTRY_BURKINA_FASO =
            "Burkina Faso"
        private const val COUNTRY_MALI = "Mali"
        private const val COUNTRY_NIGER = "Niger"
        private const val COUNTRY_COTE_DIVOIRE =
            "Côte d'Ivoire"

        private const val CURRENCY_USD = "USD"
        private const val CURRENCY_XOF = "XOF"
    }
}