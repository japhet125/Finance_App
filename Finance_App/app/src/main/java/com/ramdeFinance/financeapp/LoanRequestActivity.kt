package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val amount = findViewById<EditText>(R.id.etLoanAmount)
        val reason = findViewById<EditText>(R.id.etLoanReason)
        val submitButton = findViewById<Button>(R.id.btnSubmitLoan)

        submitButton.setOnClickListener {

            val amountText = amount.text.toString().trim()
            val reasonText = reason.text.toString().trim()

            if (amountText.isBlank() || reasonText.isBlank()) {

                Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid

            if (userId != null) {

                val loanRequest = hashMapOf(
                    "userId" to userId,
                    "amount" to amountText,
                    "reason" to reasonText,
                    "status" to "pending",
                    "createdAt" to System.currentTimeMillis()
                )

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
        }
    }
}