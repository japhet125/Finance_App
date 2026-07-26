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
        COUNTRY_BURKINA_FASO to
                listOf("Orange Money", "Moov Money", "Wave"),

        COUNTRY_MALI to
                listOf("Orange Money", "Moov Money"),

        COUNTRY_NIGER to
                listOf("Airtel Money", "Moov Money", "Zamani Cash"),

        COUNTRY_COTE_DIVOIRE to
                listOf(
                    "Orange Money",
                    "MTN MoMo",
                    "Moov Money",
                    "Wave"
                )
    )

    private val countryCodes = listOf(
        COUNTRY_BURKINA_FASO,
        COUNTRY_MALI,
        COUNTRY_NIGER,
        COUNTRY_COTE_DIVOIRE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_money)

        val backButton =
            findViewById<Button>(R.id.btnBack)

        val countrySpinner =
            findViewById<Spinner>(
                R.id.spinnerMobileMoneyCountry
            )

        val providerSpinner =
            findViewById<Spinner>(
                R.id.spinnerMobileMoneyProvider
            )

        val phoneInput =
            findViewById<EditText>(
                R.id.etMobileMoneyPhone
            )

        val nameInput =
            findViewById<EditText>(
                R.id.etMobileMoneyName
            )

        val currencyInput =
            findViewById<EditText>(
                R.id.etMobileMoneyCurrency
            )

        val saveButton =
            findViewById<Button>(
                R.id.btnSaveMobileMoney
            )

        backButton.setOnClickListener {
            finish()
        }

        currencyInput.setText(CURRENCY_XOF)

        setupCountrySpinner(
            countrySpinner = countrySpinner,
            providerSpinner = providerSpinner,
            currencyInput = currencyInput
        )

        val userId =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid

        val db =
            FirebaseFirestore.getInstance()

        if (userId != null) {
            loadSavedMobileMoney(
                db = db,
                userId = userId,
                countrySpinner = countrySpinner,
                providerSpinner = providerSpinner,
                phoneInput = phoneInput,
                nameInput = nameInput,
                currencyInput = currencyInput
            )
        }

        saveButton.setOnClickListener {
            saveMobileMoney(
                db = db,
                userId = userId,
                countrySpinner = countrySpinner,
                providerSpinner = providerSpinner,
                phoneInput = phoneInput,
                nameInput = nameInput,
                currencyInput = currencyInput
            )
        }
    }

    private fun setupCountrySpinner(
        countrySpinner: Spinner,
        providerSpinner: Spinner,
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

        countrySpinner.adapter = countryAdapter

        countrySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val countryCode =
                        countryCodes[position]

                    updateProviderSpinner(
                        countryCode = countryCode,
                        providerSpinner = providerSpinner
                    )

                    currencyInput.setText(CURRENCY_XOF)
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    // No action required.
                }
            }
    }

    private fun updateProviderSpinner(
        countryCode: String,
        providerSpinner: Spinner
    ) {
        val providers =
            providersByCountry[countryCode].orEmpty()

        val providerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            providers
        )

        providerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        providerSpinner.adapter = providerAdapter
    }

    private fun loadSavedMobileMoney(
        db: FirebaseFirestore,
        userId: String,
        countrySpinner: Spinner,
        providerSpinner: Spinner,
        phoneInput: EditText,
        nameInput: EditText,
        currencyInput: EditText
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                val savedCountry =
                    document.getString(
                        "mobileMoneyCountry"
                    )

                val savedProvider =
                    document.getString(
                        "mobileMoneyProvider"
                    )

                phoneInput.setText(
                    document.getString(
                        "mobileMoneyPhone"
                    ).orEmpty()
                )

                nameInput.setText(
                    document.getString(
                        "mobileMoneyName"
                    ).orEmpty()
                )

                currencyInput.setText(
                    document.getString(
                        "mobileMoneyCurrency"
                    ) ?: CURRENCY_XOF
                )

                if (
                    savedCountry != null &&
                    countryCodes.contains(savedCountry)
                ) {
                    val countryIndex =
                        countryCodes.indexOf(savedCountry)

                    countrySpinner.setSelection(
                        countryIndex
                    )

                    providerSpinner.post {
                        val providers =
                            providersByCountry[
                                savedCountry
                            ].orEmpty()

                        val providerIndex =
                            providers.indexOf(
                                savedProvider
                            )

                        if (providerIndex >= 0) {
                            providerSpinner.setSelection(
                                providerIndex
                            )
                        }
                    }
                }
            }
    }

    private fun saveMobileMoney(
        db: FirebaseFirestore,
        userId: String?,
        countrySpinner: Spinner,
        providerSpinner: Spinner,
        phoneInput: EditText,
        nameInput: EditText,
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

        val countryPosition =
            countrySpinner.selectedItemPosition

        if (
            countryPosition < 0 ||
            countryPosition >= countryCodes.size
        ) {
            Toast.makeText(
                this,
                getString(
                    R.string.select_mobile_money_country
                ),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val selectedProvider =
            providerSpinner.selectedItem
                ?.toString()
                .orEmpty()

        val phoneText =
            phoneInput.text
                .toString()
                .trim()

        val nameText =
            nameInput.text
                .toString()
                .trim()

        val currencyText =
            currencyInput.text
                .toString()
                .trim()
                .ifBlank { CURRENCY_XOF }

        if (
            phoneText.isBlank() ||
            nameText.isBlank()
        ) {
            Toast.makeText(
                this,
                getString(
                    R.string.mobile_money_required_fields
                ),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val selectedCountry =
            countryCodes[countryPosition]

        val mobileMoneyInfo = mapOf(
            "mobileMoneyCountry" to selectedCountry,
            "mobileMoneyProvider" to selectedProvider,
            "mobileMoneyPhone" to phoneText,
            "mobileMoneyName" to nameText,
            "mobileMoneyCurrency" to currencyText,
            "mobileMoneySubmitted" to true,
            "mobileMoneyUpdatedAt" to
                    System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .update(mobileMoneyInfo)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(
                        R.string.mobile_money_saved
                    ),
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    getString(
                        R.string.mobile_money_save_failed,
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
            COUNTRY_BURKINA_FASO ->
                getString(
                    R.string.country_burkina_faso
                )

            COUNTRY_MALI ->
                getString(R.string.country_mali)

            COUNTRY_NIGER ->
                getString(R.string.country_niger)

            COUNTRY_COTE_DIVOIRE ->
                getString(
                    R.string.country_cote_divoire
                )

            else -> countryCode
        }
    }

    companion object {
        private const val COUNTRY_BURKINA_FASO =
            "Burkina Faso"

        private const val COUNTRY_MALI =
            "Mali"

        private const val COUNTRY_NIGER =
            "Niger"

        private const val COUNTRY_COTE_DIVOIRE =
            "Côte d'Ivoire"

        private const val CURRENCY_XOF =
            "XOF"
    }
}