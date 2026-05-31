package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdminUserDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user_details)

        val backButton = findViewById<Button>(R.id.btnBack)

        val txtUserName = findViewById<TextView>(R.id.txtUserName)
        val txtUserEmail = findViewById<TextView>(R.id.txtUserEmail)
        val txtUserPhone = findViewById<TextView>(R.id.txtUserPhone)
        val txtUserAddress = findViewById<TextView>(R.id.txtUserAddress)
        val txtUserRole = findViewById<TextView>(R.id.txtUserRole)
        val txtUserCredit = findViewById<TextView>(R.id.txtUserCredit)
        val txtUserIdentity = findViewById<TextView>(R.id.txtUserIdentity)
        val txtTotalLoans = findViewById<TextView>(R.id.txtTotalLoans)
        val txtOutstandingBalance = findViewById<TextView>(R.id.txtOutstandingBalance)

        backButton.setOnClickListener {
            finish()
        }

        val userId = intent.getStringExtra("USER_ID")

        if (userId == null) {
            finish()
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                txtUserName.text =
                    "Name: ${document.getString("fullName") ?: "N/A"}"

                txtUserEmail.text =
                    "Email: ${document.getString("email") ?: "N/A"}"

                txtUserPhone.text =
                    "Phone: ${document.getString("phone") ?: "N/A"}"

                val address = document.getString("address") ?: ""
                val apt = document.getString("apt") ?: ""
                val city = document.getString("city") ?: ""
                val state = document.getString("state") ?: ""
                val zipCode = document.getString("zipCode") ?: ""
                val country = document.getString("country") ?: ""

                txtUserAddress.text =
                    "Address: $address Apt $apt, $city, $state $zipCode, $country"

                txtUserRole.text =
                    "Role: ${document.getString("role") ?: "user"}"

                txtUserCredit.text =
                    "Credit Score: ${document.getLong("creditScore") ?: 500}"

                txtUserIdentity.text =
                    "Identity Status: ${document.getString("identityStatus") ?: "pending"}"
            }

        db.collection("loan_requests")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->

                var totalLoans = 0
                var balance = 0.0

                for (doc in documents) {
                    totalLoans++

                    balance += doc.getString("remainingBalance")
                        ?.toDoubleOrNull() ?: 0.0
                }

                txtTotalLoans.text = "Total Loans: $totalLoans"

                txtOutstandingBalance.text =
                    "Outstanding Balance: $${String.format("%.2f", balance)}"
            }
    }
}