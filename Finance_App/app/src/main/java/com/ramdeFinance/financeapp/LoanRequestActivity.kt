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


class LoanRequestActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_request)
        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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

                if (!emailVerified || !phoneVerified) {

                    Toast.makeText(
                        this,
                        "Please verify both your email and phone number before requesting a loan.",
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

                val creditScore = userDocument.getLong("creditScore") ?: 500

                val maxAllowed = when {
                    creditScore >= 700 -> 10000.0
                    creditScore >= 550 -> 2000.0
                    else -> 500.0
                }

                if (amountValue > maxAllowed) {
                    Toast.makeText(
                        this,
                        "Loan denied. Your credit score allows up to $${maxAllowed.toInt()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }
                val autoPayEnabled =
                    autoPayCheckBox.isChecked

                db.collection("loan_requests")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", "overdue")
                    .get()
                    .addOnSuccessListener { overdueLoans ->

                        if (!overdueLoans.isEmpty) {
                            Toast.makeText(
                                this,
                                "Loan denied. You have an overdue loan.",
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

                            val summaryMessage = """
Loan Amount: $${String.format("%.2f", amountValue)}

Plan: $planDisplay

Interest Rate: ${(interestRate * 100).toInt()}%

Total Repayment:
$${String.format("%.2f", totalRepayment)}

Payment Amount:
$${String.format("%.2f", paymentAmount)}

Due Date:
$dueDateDisplay

Do you want to submit this loan request?
""".trimIndent()

                            AlertDialog.Builder(this)
                                .setTitle("Baobab Loan Summary")
                                .setMessage(summaryMessage)

                                .setNegativeButton("Cancel") { dialog, _ ->
                                    dialog.dismiss()
                                }

                                .setPositiveButton("Confirm") { _, _ ->

                                    db.collection("loan_requests")
                                        .add(loanRequest)
                                        .addOnSuccessListener {

                                            Toast.makeText(
                                                this,
                                                "Loan request submitted",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            finish()
                                        }
                                        .addOnFailureListener { e ->

                                            Toast.makeText(
                                                this,
                                                "Error: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }

                                .show()
                        }
                    }
            }


        }
    }

}