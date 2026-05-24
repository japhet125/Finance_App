package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        val submitButton = findViewById<Button>(R.id.btnSubmitLoan)

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

                else -> {
                    Toast.makeText(this, "Invalid payment plan", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val totalRepayment = amountValue + (amountValue * interestRate)
            val paymentAmount = totalRepayment / paymentTerm
            val userId = auth.currentUser?.uid

            if (userId != null) {

                val loanRequest = hashMapOf(
                    "userId" to userId,
                    "amount" to String.format("%.2f", amountValue),
                    "principalAmount" to String.format("%.2f", amountValue),
                    "reason" to reasonText,
                    "status" to "pending",
                    "paymentFrequency" to paymentFrequency,
                    "paymentTerm" to paymentTerm,
                    "interestRate" to if (paymentFrequency == "monthly") 35 else 25,
                    "totalRepayment" to String.format("%.2f", totalRepayment),
                    "paymentAmount" to String.format("%.2f", paymentAmount),
                    "remainingBalance" to String.format("%.2f", totalRepayment),
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("loan_requests")
                    .add(loanRequest)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Loan request submitted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

}