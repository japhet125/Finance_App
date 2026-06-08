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
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        var userLanguage = "en"

        val menuButton = findViewById<ImageButton>(R.id.btnMenu)
        var isAdminUser = false

        menuButton.setOnClickListener {

            val popupMenu = PopupMenu(this, menuButton)
            val requestLoanText =
                if (userLanguage == "fr") "Demander un prêt" else "Request Loan"

            val loanHistoryText =
                if (userLanguage == "fr") "Historique des prêts" else "Loan History"

            val makePaymentText =
                if (userLanguage == "fr") "Effectuer un paiement" else "Make Payment"

            val profileText =
                if (userLanguage == "fr") "Profil" else "Profile"
            val transactionsText =
                if (userLanguage == "fr") "Transactions" else "Transactions"

            val notificationsText =
                if (userLanguage == "fr") "Notifications" else "Notifications"

            val bankAccountText =
                if (userLanguage == "fr") "Compte bancaire" else "Bank Account"

            val mobileMoneyText =
                if (userLanguage == "fr") "Mobile Money" else "Mobile Money"

            val adminDashboardText =
                if (userLanguage == "fr") "Tableau Admin" else "Admin Dashboard"

            val logoutText =
                if (userLanguage == "fr") "Déconnexion" else "Logout"





            popupMenu.menu.add(transactionsText)
            popupMenu.menu.add(requestLoanText)
            popupMenu.menu.add(loanHistoryText)
            popupMenu.menu.add(makePaymentText)
            popupMenu.menu.add(profileText)
            popupMenu.menu.add(notificationsText)
            popupMenu.menu.add(bankAccountText)
            popupMenu.menu.add(mobileMoneyText)

            if (isAdminUser) {
                popupMenu.menu.add(adminDashboardText)
            }
            popupMenu.menu.add(logoutText)

            popupMenu.setOnMenuItemClickListener { item ->

                when (item.title.toString()) {

                    requestLoanText -> {
                        startActivity(Intent(this, LoanRequestActivity::class.java))
                        true
                    }

                    loanHistoryText -> {
                        startActivity(Intent(this, LoanHistoryActivity::class.java))
                        true
                    }

                    makePaymentText -> {
                        startActivity(Intent(this, PaymentActivity::class.java))
                        true
                    }


                    profileText -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }

                    transactionsText -> {
                        startActivity(Intent(this, TransactionHistoryActivity::class.java))
                        true
                    }

                    notificationsText -> {
                        startActivity(Intent(this, NotificationsActivity::class.java))
                        true
                    }

                    bankAccountText -> {
                        startActivity(Intent(this, BankAccountActivity::class.java))
                        true
                    }

                    mobileMoneyText -> {
                        startActivity(Intent(this, MobileMoneyActivity::class.java))
                        true
                    }

                    adminDashboardText -> {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                        true
                    }

                    logoutText -> {
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
        val borrowerLevelText =
            findViewById<TextView>(R.id.txtBorrowerLevel)
        val unreadNotificationsText =
            findViewById<TextView>(R.id.txtUnreadNotifications)
        val autoPayStatusText =
            findViewById<TextView>(R.id.txtAutoPayStatus)

        val autoPayNextDateText =
            findViewById<TextView>(R.id.txtAutoPayNextDate)

        val autoPayAmountText =
            findViewById<TextView>(R.id.txtAutoPayAmount)

        if (userId != null) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->

                    db.collection("users")
                        .document(userId)
                        .update(
                            mapOf(
                                "fcmToken" to token,
                                "fcmTokenUpdatedAt" to System.currentTimeMillis()
                            )
                        )
                }
        }

        if (userId != null) {

            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    userLanguage = document.getString("language") ?: "en"

                    if (document.exists()) {


                        val fullName = document.getString("fullName")

                        welcomeText.text =
                            if (userLanguage == "fr") {
                                "Bienvenue, $fullName"
                            } else {
                                "Welcome, $fullName"
                            }

                        val role = document.getString("role")


                        if (role == "admin") {
                            isAdminUser = true
                        }
                        val creditScore =
                            document.getLong("creditScore") ?: 500

                        creditScoreText.text =
                            if (userLanguage == "fr") {
                                "💳 Score de crédit : $creditScore"
                            } else {
                                "💳 Credit Score: $creditScore"
                            }
                        val borrowerLevel =
                            document.getString("borrowerLevel") ?: "New"

                        borrowerLevelText.text =
                            if (userLanguage == "fr") {
                                "🏅 Niveau emprunteur : $borrowerLevel"
                            } else {
                                "🏅 Borrower Level: $borrowerLevel"
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

                                unreadNotificationsText.text =
                                    if (userLanguage == "fr") {
                                        "🔔 Notifications non lues : $unreadCount"
                                    } else {
                                        "🔔 Unread Notifications: $unreadCount"
                                    }
                            }
                    }

                    if (userId != null) {

                        db.collection("loan_requests")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("autoPayEnabled", true)
                            .whereEqualTo("autoPayStatus", "scheduled")
                            .limit(1)
                            .addSnapshotListener { snapshots, error ->

                                if (error != null) {
                                    return@addSnapshotListener
                                }

                                val loan =
                                    snapshots?.documents?.firstOrNull()

                                if (loan != null) {

                                    val nextDate =
                                        loan.getLong("nextPaymentDate") ?: 0L

                                    val amount =
                                        loan.getString("nextPaymentAmount") ?: "0.00"

                                    val dateText =
                                        java.text.SimpleDateFormat(
                                            "MMM dd, yyyy",
                                            java.util.Locale.getDefault()
                                        ).format(java.util.Date(nextDate))

                                    if (userLanguage == "fr") {

                                        autoPayStatusText.text =
                                            "Paiement automatique : Planifié"

                                        autoPayNextDateText.text =
                                            "Prochain paiement : $dateText"

                                        autoPayAmountText.text =
                                            "Montant : $$amount"

                                    } else {

                                        autoPayStatusText.text =
                                            "Status: Scheduled"

                                        autoPayNextDateText.text =
                                            "Next Payment: $dateText"

                                        autoPayAmountText.text =
                                            "Amount: $$amount"
                                    }

                                } else {

                                    autoPayStatusText.text =
                                        if (userLanguage == "fr") {
                                            "Aucun paiement automatique actif"
                                        } else {
                                            "Status: No Active Auto Pay"
                                        }
                                    autoPayNextDateText.text = ""

                                    autoPayAmountText.text = ""
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
                                        val amount =
                                            parseMoney(document.getString("amount") ?: "0")
                                        val status = document.getString("status") ?: "pending"

                                        totalRequested += amount

                                        when (status) {
                                            "pending" -> pendingCount++
                                            "approved" -> approvedAmount += amount
                                            "rejected" -> rejectedCount++
                                        }
                                    }
                                }

                                if (userLanguage == "fr") {

                                    totalRequestedText.text =
                                        "💰 Total demandé : ${currencyFormat.format(totalRequested)}"

                                    pendingLoansText.text =
                                        "⏳ Demandes en attente : $pendingCount"

                                    approvedAmountText.text =
                                        "✅ Montant approuvé : ${currencyFormat.format(approvedAmount)}"

                                    rejectedLoansText.text =
                                        "❌ Demandes refusées : $rejectedCount"

                                } else {

                                    totalRequestedText.text =
                                        "💰 Total Requested: ${currencyFormat.format(totalRequested)}"

                                    pendingLoansText.text =
                                        "⏳ Pending Requests: $pendingCount"

                                    approvedAmountText.text =
                                        "✅ Approved Amount: ${currencyFormat.format(approvedAmount)}"

                                    rejectedLoansText.text =
                                        "❌ Rejected Requests: $rejectedCount"
                                }
                            }
                    }

                }





    }
        processPaymentReminders()
        processAutoPayments()


    }
    private fun processPaymentReminders() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val now = System.currentTimeMillis()

        val oneDayMillis =
            24L * 60L * 60L * 1000L
        db.collection("loan_requests")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { loans ->


                for (document in loans.documents) {

                    val dueDate =
                        document.getLong("nextPaymentDate") ?: 0L

                    val reminderSent =
                        document.getBoolean("reminderSent") ?: false

                    val remainingBalance =
                        parseMoney(document.getString("remainingBalance") ?: "0")

                    val paymentAmount =
                        document.getString("nextPaymentAmount") ?: "0.00"


                    val shouldSendReminder =
                        dueDate > 0 &&
                                !reminderSent &&
                                remainingBalance > 0 &&
                                dueDate - now <= oneDayMillis &&
                                dueDate > now

                    if (shouldSendReminder) {

                        val notification = hashMapOf(
                            "userId" to userId,
                            "title" to "Payment Reminder",
                            "message" to "Your payment of $$paymentAmount is due tomorrow.",
                            "timestamp" to now,
                            "isRead" to false
                        )

                        db.collection("notifications")
                            .add(notification)
                            .addOnSuccessListener {
                                db.collection("loan_requests")
                                    .document(document.id)
                                    .update("reminderSent", true)
                            }
                    }
                }
            }
    }
    private fun processAutoPayments() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val now = System.currentTimeMillis()

        db.collection("loan_requests")
            .whereEqualTo("userId", userId)
            .whereEqualTo("autoPayEnabled", true)
            .whereEqualTo("autoPayStatus", "scheduled")
            .get()
            .addOnSuccessListener { loans ->

                for (document in loans.documents) {

                    val nextPaymentDate =
                    document.getLong("nextPaymentDate") ?: 0L

                    if (nextPaymentDate > now) {
                        continue
                    }


                    val remainingBalance =
                        parseMoney(document.getString("remainingBalance") ?: "0")

                    val paymentAmount =
                        parseMoney(document.getString("nextPaymentAmount") ?: "0")

                    if (remainingBalance <= 0.0 || paymentAmount <= 0.0) {
                        continue
                    }

                    val actualPayment =
                        if (paymentAmount > remainingBalance) {
                            remainingBalance
                        } else {
                            paymentAmount
                        }

                    val newBalance =
                        remainingBalance - actualPayment

                    val paymentFrequency =
                        document.getString("paymentFrequency") ?: "weekly"

                    val millisecondsPerDay =
                        24L * 60L * 60L * 1000L

                    val nextDate =
                        when (paymentFrequency) {
                            "weekly" -> now + (7L * millisecondsPerDay)
                            "monthly" -> now + (30L * millisecondsPerDay)
                            "one_time" -> 0L
                            else -> now + (7L * millisecondsPerDay)
                        }

                    val updates = hashMapOf<String, Any>(
                        "remainingBalance" to String.format("%.2f", newBalance),
                        "lastAutoPaymentAt" to now
                    )

                    if (newBalance <= 0.0) {
                        updates["remainingBalance"] = "0.00"
                        updates["status"] = "paid"
                        updates["autoPayStatus"] = "completed"
                        updates["nextPaymentDate"] = 0L
                    } else {
                        updates["nextPaymentDate"] = nextDate
                        updates["autoPayStatus"] = "scheduled"
                    }

                    db.collection("loan_requests")
                        .document(document.id)
                        .update(updates)
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Auto Pay failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnSuccessListener {

                            val transaction = hashMapOf(
                                "userId" to userId,
                                "loanId" to document.id,
                                "paymentAmount" to String.format("%.2f", actualPayment),
                                "previousBalance" to String.format("%.2f", remainingBalance),
                                "newBalance" to String.format("%.2f", newBalance.coerceAtLeast(0.0)),
                                "paymentDate" to now,
                                "paymentType" to "auto_pay"
                            )

                            db.collection("transactions")
                                .add(transaction)

                            val notification = hashMapOf(
                                "userId" to userId,
                                "title" to "Auto Pay Processed",
                                "message" to "Your automatic payment of $${String.format("%.2f", actualPayment)} was processed.",
                                "timestamp" to now,
                                "isRead" to false
                            )

                            db.collection("notifications")
                                .add(notification)
                        }
                }
            }
    }
    private fun parseMoney(value: String): Double {
        return value
            .replace("$", "")
            .replace("FCFA", "")
            .replace("F CFA", "")
            .replace("CFA", "")
            .replace(",", ".")
            .trim()
            .toDoubleOrNull() ?: 0.0
    }
}