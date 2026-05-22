package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val logoutButton = findViewById<Button>(R.id.btnLogout)

        logoutButton.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            finish()
        }
        val welcomeText = findViewById<TextView>(R.id.txtWelcome)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid

        if (userId != null) {

            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {

                        val fullName = document.getString("fullName")

                        welcomeText.text = "Welcome, $fullName"
                    }
                }
        }
        val totalRequestedText = findViewById<TextView>(R.id.txtTotalRequested)
        val pendingLoansText = findViewById<TextView>(R.id.txtPendingLoans)
        val approvedAmountText = findViewById<TextView>(R.id.txtApprovedAmount)
        val rejectedLoansText = findViewById<TextView>(R.id.txtRejectedLoans)

        val currencyFormat = NumberFormat.getCurrencyInstance()

        if (userId != null) {
            db.collection("loan_requests")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->

                    var totalRequested = 0.0
                    var approvedAmount = 0.0
                    var pendingCount = 0
                    var rejectedCount = 0

                    for (document in documents) {
                        val amount = document.getString("amount")?.toDoubleOrNull() ?: 0.0
                        val status = document.getString("status") ?: "pending"

                        totalRequested += amount

                        when (status) {
                            "pending" -> pendingCount++
                            "approved" -> approvedAmount += amount
                            "rejected" -> rejectedCount++
                        }
                    }

                    totalRequestedText.text = "Total Requested: ${currencyFormat.format(totalRequested)}"
                    pendingLoansText.text = "Pending Requests: $pendingCount"
                    approvedAmountText.text = "Approved Amount: ${currencyFormat.format(approvedAmount)}"
                    rejectedLoansText.text = "Rejected Requests: $rejectedCount"
                }
        }
        val loanButton = findViewById<Button>(R.id.btnLoanRequest)

        loanButton.setOnClickListener {

            val intent = Intent(this, LoanRequestActivity::class.java)
            startActivity(intent)
        }
        val historyButton = findViewById<Button>(R.id.btnLoanHistory)

        historyButton.setOnClickListener {

            val intent = Intent(this, LoanHistoryActivity::class.java)
            startActivity(intent)
        }
    }
}