package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import android.view.View
import android.widget.PopupMenu
import android.widget.ImageButton
class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val menuButton = findViewById<ImageButton>(R.id.btnMenu)
        var isAdminUser = false
        menuButton.setOnClickListener {

            val popupMenu = PopupMenu(this, menuButton)

            popupMenu.menu.add("Request Loan")
            popupMenu.menu.add("Loan History")
            popupMenu.menu.add("Make Payment")
            popupMenu.menu.add("Transactions")
            popupMenu.menu.add("Profile")
            popupMenu.menu.add("Notifications")

            if (isAdminUser) {
                popupMenu.menu.add("Admin Dashboard")
            }

            popupMenu.menu.add("Logout")

            popupMenu.setOnMenuItemClickListener { item ->

                when (item.title.toString()) {

                    "Request Loan" -> {
                        startActivity(Intent(this, LoanRequestActivity::class.java))
                        true
                    }

                    "Loan History" -> {
                        startActivity(Intent(this, LoanHistoryActivity::class.java))
                        true
                    }

                    "Make Payment" -> {
                        startActivity(Intent(this, PaymentActivity::class.java))
                        true
                    }

                    "Transactions" -> {
                        startActivity(Intent(this, TransactionHistoryActivity::class.java))
                        true
                    }

                    "Profile" -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }

                    "Notifications" -> {
                        startActivity(Intent(this, NotificationsActivity::class.java))
                        true
                    }

                    "Admin Dashboard" -> {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                        true
                    }

                    "Logout" -> {
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }

        val welcomeText = findViewById<TextView>(R.id.txtWelcome)


        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        val creditScoreText =
            findViewById<TextView>(R.id.txtCreditScore)
        val unreadNotificationsText =
            findViewById<TextView>(R.id.txtUnreadNotifications)

        if (userId != null) {

            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {

                        val fullName = document.getString("fullName")

                        welcomeText.text = "Welcome, $fullName"

                        val role = document.getString("role")

                        if (role == "admin") {
                            isAdminUser = true
                        }
                        val creditScore =
                            document.getLong("creditScore") ?: 500

                        creditScoreText.text = "💳 Credit Score: $creditScore"

                    }

                }
        }
        if (userId != null) {
            db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snapshots, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    val unreadCount = snapshots?.size() ?: 0

                    unreadNotificationsText.text = "🔔 Unread Notifications: $unreadCount"
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
                .addSnapshotListener { snapshots, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    var totalRequested = 0.0
                    var approvedAmount = 0.0
                    var pendingCount = 0
                    var rejectedCount = 0

                    if (snapshots != null) {

                        for (document in snapshots.documents) {
                            val amount = document.getString("amount")?.toDoubleOrNull() ?: 0.0
                            val status = document.getString("status") ?: "pending"

                            totalRequested += amount

                            when (status) {
                                "pending" -> pendingCount++
                                "approved" -> approvedAmount += amount
                                "rejected" -> rejectedCount++
                            }
                        }
                    }

                    totalRequestedText.text = "💰 Total Requested: ${currencyFormat.format(totalRequested)}"
                    pendingLoansText.text = "⏳ Pending Requests: $pendingCount"
                    approvedAmountText.text = "✅ Approved Amount: ${currencyFormat.format(approvedAmount)}"
                    rejectedLoansText.text = "❌ Rejected Requests: $rejectedCount"
                }
        }

    }
}