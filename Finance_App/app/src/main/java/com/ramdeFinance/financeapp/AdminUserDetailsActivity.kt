package com.ramdefinance.financeapp

import android.content.Intent
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
        val suspendButton = findViewById<Button>(R.id.btnSuspendUser)
        val reactivateButton = findViewById<Button>(R.id.btnReactivateUser)
        val viewLoansButton =
            findViewById<Button>(R.id.btnViewUserLoans)

        backButton.setOnClickListener {
            finish()
        }
        val promoteButton = findViewById<Button>(R.id.btnPromoteAdmin)
        val demoteButton = findViewById<Button>(R.id.btnDemoteUser)

        val userId = intent.getStringExtra("USER_ID")

        if (userId == null) {
            finish()
            return
        }
        val txtAccountStatus =
            findViewById<TextView>(R.id.txtAccountStatus)

        val db = FirebaseFirestore.getInstance()
        suspendButton.setOnClickListener {
            db.collection("users")
                .document(userId)
                .update("accountStatus", "suspended")
                .addOnSuccessListener {

                    txtAccountStatus.text = "Account Status: Suspended"

                    suspendButton.isEnabled = false
                    reactivateButton.isEnabled = true
                    val auditLog = hashMapOf(
                        "actorId" to "admin",
                        "action" to "user_suspended",
                        "targetType" to "user",
                        "targetId" to userId,
                        "message" to "User $userId was suspended.",
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("audit_logs").add(auditLog)
                    val notification = hashMapOf(
                        "userId" to userId,
                        "title" to "Account Suspended",
                        "message" to "Your account has been suspended. Please contact support.",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )

                    db.collection("notifications").add(notification)

                }


        }

        promoteButton.setOnClickListener {
            db.collection("users")
                .document(userId)
                .update("role", "admin")
                .addOnSuccessListener {
                    txtUserRole.text = "Role: admin"

                    promoteButton.isEnabled = false
                    demoteButton.isEnabled = true
                    val auditLog = hashMapOf(
                        "actorId" to "admin",
                        "action" to "user_promoted_admin",
                        "targetType" to "user",
                        "targetId" to userId,
                        "message" to "User $userId was promoted to admin.",
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("audit_logs").add(auditLog)
                    val notification = hashMapOf(
                        "userId" to userId,
                        "title" to "Admin Access Granted",
                        "message" to "Your account has been promoted to admin.",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )

                    db.collection("notifications").add(notification)

                }

        }


        demoteButton.setOnClickListener {
            db.collection("users")
                .document(userId)
                .update("role", "user")
                .addOnSuccessListener {
                    txtUserRole.text = "Role: user"

                    promoteButton.isEnabled = true
                    demoteButton.isEnabled = false
                    val auditLog = hashMapOf(
                        "actorId" to "admin",
                        "action" to "user_demoted",
                        "targetType" to "user",
                        "targetId" to userId,
                        "message" to "User $userId was demoted to user.",
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("audit_logs").add(auditLog)
                    val notification = hashMapOf(
                        "userId" to userId,
                        "title" to "Admin Access Removed",
                        "message" to "Your account has been changed back to user access.",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )

                    db.collection("notifications").add(notification)

                }

        }


        reactivateButton.setOnClickListener {
            db.collection("users")
                .document(userId)
                .update("accountStatus", "active")
                .addOnSuccessListener {

                    txtAccountStatus.text = "Account Status: Active"

                    suspendButton.isEnabled = true
                    reactivateButton.isEnabled = false
                    val auditLog = hashMapOf(
                        "actorId" to "admin",
                        "action" to "user_reactivated",
                        "targetType" to "user",
                        "targetId" to userId,
                        "message" to "User $userId was reactivated.",
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("audit_logs").add(auditLog)

                    val notification = hashMapOf(
                        "userId" to userId,
                        "title" to "Account Reactivated",
                        "message" to "Your account has been reactivated.",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )

                    db.collection("notifications").add(notification)

                }

        }


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
                val accountStatus =
                    document.getString("accountStatus") ?: "active"

                txtUserAddress.text =
                    "Address: $address Apt $apt, $city, $state $zipCode, $country"

                val role = document.getString("role") ?: "user"

                txtUserRole.text = "Role: $role"

                if (role == "admin") {
                    promoteButton.isEnabled = false
                    demoteButton.isEnabled = true
                } else {
                    promoteButton.isEnabled = true
                    demoteButton.isEnabled = false
                }

                txtUserCredit.text =
                    "Credit Score: ${document.getLong("creditScore") ?: 500}"

                txtUserIdentity.text =
                    "Identity Status: ${document.getString("identityStatus") ?: "pending"}"


                txtAccountStatus.text =
                    "Account Status: ${accountStatus.replaceFirstChar { it.uppercase() }}"
                if (accountStatus == "suspended") {

                    suspendButton.isEnabled = false
                    reactivateButton.isEnabled = true

                } else {

                    suspendButton.isEnabled = true
                    reactivateButton.isEnabled = false
                }
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
        viewLoansButton.setOnClickListener {

            val intent = Intent(
                this,
                AdminUserLoansActivity::class.java
            )

            intent.putExtra("USER_ID", userId)

            startActivity(intent)
        }
        val viewTransactionsButton =
            findViewById<Button>(R.id.btnViewUserTransactions)
        viewTransactionsButton.setOnClickListener {
            val intent = Intent(
                this,
                AdminUserTransactionsActivity::class.java
            )

            intent.putExtra("USER_ID", userId)

            startActivity(intent)
        }
    }
}