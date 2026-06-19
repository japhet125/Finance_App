package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.RadioButton
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import android.widget.CheckBox
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts



class LoanRequestActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var pendingLoanRequest: MutableMap<String, Any>

    private val agreementLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data

                pendingLoanRequest["agreementAccepted"] =
                    data?.getBooleanExtra("agreementAccepted", false) ?: false

                pendingLoanRequest["agreementName"] =
                    data?.getStringExtra("agreementName") ?: ""

                pendingLoanRequest["agreementAcceptedAt"] =
                    data?.getLongExtra("agreementAcceptedAt", System.currentTimeMillis())
                        ?: System.currentTimeMillis()

                pendingLoanRequest["agreementVersion"] =
                    data?.getStringExtra("agreementVersion") ?: "1.0"

                db.collection("loan_requests")
                    .add(pendingLoanRequest)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Loan request submitted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_request)
        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        var userLanguage = "en"

        val amount = findViewById<EditText>(R.id.etLoanAmount)
        val reason = findViewById<EditText>(R.id.etLoanReason)
        val paymentPlanGroup = findViewById<RadioGroup>(R.id.rgPaymentPlan)
        val monthlyOption =
            findViewById<RadioButton>(R.id.rbMonthly12)
        val autoPayCheckBox =
            findViewById<CheckBox>(R.id.cbAutoPay)
        var userCountry = "USA"
        val submitButton = findViewById<Button>(R.id.btnSubmitLoan)

        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->

                    userLanguage =
                        document.getString("language") ?: "en"

                    updateLanguage(userLanguage)
                }
        }

        if (currentUserId != null) {
            db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    userCountry = document.getString("country") ?: "USA"
                }
        }

        amount.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val amountValue =
                    s.toString().trim().toDoubleOrNull() ?: 0.0

                if (userCountry == "USA" && amountValue < 2000.0) {

                    monthlyOption.isEnabled = false

                    if (monthlyOption.isChecked) {
                        paymentPlanGroup.clearCheck()
                    }

                } else {
                    monthlyOption.isEnabled = true
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        submitButton.setOnClickListener {

            val amountText = amount.text.toString().trim()
            val reasonText = reason.text.toString().trim()
            val amountValue = amountText.toDoubleOrNull()

            if (amountText.isBlank() || reasonText.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amountValue == null || amountValue <= 0.0) {
                Toast.makeText(this, "Enter a valid loan amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (paymentPlanGroup.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Please select a payment plan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val paymentFrequency: String
            val paymentTerm: Int
            val interestRate: Double

            when (paymentPlanGroup.checkedRadioButtonId) {
                R.id.rbWeekly10 -> {
                    paymentFrequency = "weekly"
                    paymentTerm = 10
                    interestRate = 0.25
                }



                R.id.rbWeekly14 -> {
                    paymentFrequency = "weekly"
                    paymentTerm = 14
                    interestRate = 0.25
                }

                R.id.rbMonthly12 -> {
                    paymentFrequency = "monthly"
                    paymentTerm = 12
                    interestRate = 0.35
                }
                R.id.rbOneTime -> {
                    paymentFrequency = "one_time"
                    paymentTerm = 1
                    interestRate = 0.25
                }

                else -> {
                    Toast.makeText(this, "Invalid payment plan", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            val millisecondsPerDay = 24L * 60L * 60L * 1000L

            val dueDate = when (paymentFrequency) {
                "weekly" -> {
                    System.currentTimeMillis() + (paymentTerm * 7L * millisecondsPerDay)
                }

                "monthly" -> {
                    System.currentTimeMillis() + (paymentTerm * 30L * millisecondsPerDay)
                }

                "one_time" -> {
                    System.currentTimeMillis() + (30L * millisecondsPerDay)
                }

                else -> {
                    System.currentTimeMillis()
                }
            }


            val totalRepayment = amountValue + (amountValue * interestRate)
            val paymentAmount = totalRepayment / paymentTerm
            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            val userRef = db.collection("users").document(userId)

            userRef.get().addOnSuccessListener { userDocument ->
                val userCountry =
                    userDocument.getString("country") ?: "USA"
                val emailVerified =
                    userDocument.getBoolean("emailVerified") ?: false

                val phoneVerified =
                    userDocument.getBoolean("phoneVerified") ?: false

                if (!emailVerified) {
                    Toast.makeText(
                        this,
                        if (userLanguage == "fr") {
                            "Vérification de l'e-mail requise. Allez dans Profil → Vérification de l'e-mail."
                        } else {
                            "Email verification required. Go to Profile → Email Verification."
                        },
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                if (!phoneVerified) {
                    Toast.makeText(
                        this,
                        if (userLanguage == "fr") {
                            "Vérification du téléphone requise. Allez dans Profil → Vérification du téléphone."
                        } else {
                            "Phone verification required. Go to Profile → Phone Verification."
                        },
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                if (
                    userCountry == "USA" &&
                    amountValue < 2000 &&
                    paymentFrequency == "monthly"
                ) {

                    Toast.makeText(
                        this,
                        "Monthly payment is only available for USA loans of $2,000 or more.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                val identityVerified =
                    userDocument.getBoolean("identityVerified") ?: false

                val borrowerLevel =
                    userDocument.getString("borrowerLevel") ?: "New"

                if (!identityVerified) {
                    Toast.makeText(
                        this,
                        if (userLanguage == "fr") {
                            "Vérification d'identité requise. Allez dans Profil → Vérification d'identité pour continuer."
                        } else {
                            "Identity verification required. Go to Profile → Identity Verification to continue."
                        },
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }


                val maxAllowed = when (borrowerLevel) {

                    "Platinum" -> 1500.0
                    "Gold" -> 1000.0
                    "Silver" -> 750.0
                    "Bronze" -> 500.0
                    else -> 250.0
                }

                if (amountValue > maxAllowed) {
                    Toast.makeText(
                        this,
                        if (userLanguage == "fr") {
                            "Prêt refusé. Votre niveau emprunteur permet jusqu'à $${maxAllowed.toInt()}."
                        } else {
                            "Loan denied. Your borrower level allows up to $${maxAllowed.toInt()}."
                        },
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }
                val autoPayEnabled =
                    autoPayCheckBox.isChecked

                db.collection("loan_requests")
                    .whereEqualTo("userId", userId)
                    .whereIn("status", listOf("approved", "overdue"))
                    .get()
                    .addOnSuccessListener { overdueLoans ->

                        if (!overdueLoans.isEmpty) {
                            Toast.makeText(
                                this,
                                if (userLanguage == "fr") {
                                    "Veuillez rembourser votre prêt actuel avant de demander un nouveau prêt."
                                } else {
                                    "Please pay off your current loan before requesting a new one."
                                },
                                Toast.LENGTH_LONG
                            ).show()
                            return@addOnSuccessListener
                        }
                        if (userId != null) {

                            val loanRequest = hashMapOf(
                                "userId" to userId,
                                "amount" to String.format("%.2f", amountValue),
                                "principalAmount" to String.format("%.2f", amountValue),
                                "reason" to reasonText,
                                "autoPayEnabled" to autoPayEnabled,
                                "autoPayStatus" to if (autoPayEnabled) "pending_approval" else "disabled",
                                "nextPaymentDate" to 0L,
                                "nextPaymentAmount" to String.format("%.2f", paymentAmount),
                                "status" to "pending",
                                "paymentFrequency" to paymentFrequency,
                                "paymentTerm" to paymentTerm,
                                "interestRate" to if (paymentFrequency == "monthly") 35 else 25,
                                "totalRepayment" to String.format("%.2f", totalRepayment),
                                "paymentAmount" to String.format("%.2f", paymentAmount),
                                "remainingBalance" to String.format("%.2f", totalRepayment),
                                "createdAt" to System.currentTimeMillis(),
                                "dueDate" to dueDate,
                            )
                            val planDisplay = when (paymentFrequency) {
                                "one_time" -> "One-Time Payment"
                                "monthly" -> "Monthly (12 Payments)"
                                "weekly" -> "Weekly ($paymentTerm Payments)"
                                else -> paymentFrequency
                            }

                            val dueDateDisplay =
                                java.text.SimpleDateFormat(
                                    "MMM dd, yyyy",
                                    java.util.Locale.getDefault()
                                ).format(java.util.Date(dueDate))

                            val summaryMessage =
                                if (userLanguage == "fr") {
                                    """
Niveau emprunteur : $borrowerLevel

Limite actuelle :
$${maxAllowed.toInt()}

Identité :
${if (identityVerified) "Vérifiée" else "Non vérifiée"}

Montant du prêt :
$${String.format("%.2f", amountValue)}

Plan :
$planDisplay

Taux d'intérêt :
${(interestRate * 100).toInt()}%

Remboursement total :
$${String.format("%.2f", totalRepayment)}

Montant du paiement :
$${String.format("%.2f", paymentAmount)}

Date d'échéance :
$dueDateDisplay

Voulez-vous soumettre cette demande de prêt ?
""".trimIndent()
                                } else {
                                    """
Borrower Level: $borrowerLevel

Current Limit:
$${maxAllowed.toInt()}

Identity:
${if (identityVerified) "Verified" else "Not Verified"}

Loan Amount:
$${String.format("%.2f", amountValue)}

Plan:
$planDisplay

Interest Rate:
${(interestRate * 100).toInt()}%

Total Repayment:
$${String.format("%.2f", totalRepayment)}

Payment Amount:
$${String.format("%.2f", paymentAmount)}

Due Date:
$dueDateDisplay

Do you want to submit this loan request?
""".trimIndent()
                                }

                            AlertDialog.Builder(this)
                                .setTitle(
                                    if (userLanguage == "fr")
                                        "Résumé du prêt Baobab"
                                    else
                                        "Baobab Loan Summary"
                                )
                                .setMessage(summaryMessage)

                                .setNegativeButton(
                                    if (userLanguage == "fr") "Annuler" else "Cancel"
                                ) { dialog, _ ->
                                    dialog.dismiss()
                                }

                                .setPositiveButton(
                                    if (userLanguage == "fr") "Confirmer" else "Confirm"
                                ) { _, _ ->

                                    pendingLoanRequest = loanRequest.toMutableMap()

                                    val intent = Intent(this, LoanAgreementActivity::class.java)
                                    agreementLauncher.launch(intent)
                                }

                                .show()
                        }
                    }
            }


        }
    }
    private fun updateLanguage(language: String) {
        val backButton = findViewById<Button>(R.id.btnBack)
        val submitButton = findViewById<Button>(R.id.btnSubmitLoan)
        val amountInput = findViewById<EditText>(R.id.etLoanAmount)
        val reasonInput = findViewById<EditText>(R.id.etLoanReason)
        val weekly10 = findViewById<RadioButton>(R.id.rbWeekly10)
        val weekly14 = findViewById<RadioButton>(R.id.rbWeekly14)
        val monthly12 = findViewById<RadioButton>(R.id.rbMonthly12)
        val oneTime = findViewById<RadioButton>(R.id.rbOneTime)
        val autoPay = findViewById<CheckBox>(R.id.cbAutoPay)

        if (language == "fr") {
            backButton.text = "Retour"
            submitButton.text = "Soumettre la demande"
            amountInput.hint = "Montant du prêt"
            reasonInput.hint = "Raison du prêt"
            weekly10.text = "Hebdomadaire 10 paiements"
            weekly14.text = "Hebdomadaire 14 paiements"
            monthly12.text = "Mensuel 12 paiements"
            oneTime.text = "Paiement unique"
            autoPay.text = "Activer le paiement automatique"
        } else {
            backButton.text = "Back"
            submitButton.text = "Submit Loan"
            amountInput.hint = "Loan Amount"
            reasonInput.hint = "Loan Reason"
            weekly10.text = "Weekly 10 Payments"
            weekly14.text = "Weekly 14 Payments"
            monthly12.text = "Monthly 12 Payments"
            oneTime.text = "One-Time Payment"
            autoPay.text = "Enable Auto Pay"
        }
    }

}