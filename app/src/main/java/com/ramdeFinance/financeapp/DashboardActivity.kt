package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.card.MaterialCardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat


class DashboardActivity : AppCompatActivity() {

    private var userLanguage = "en"
    private var isAdminUser = false

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userLanguage =
            AppCompatDelegate.getApplicationLocales()[0]?.language
                ?: resources.configuration.locales[0].language
        setContentView(R.layout.activity_dashboard)
        bottomNavigation =
            findViewById(R.id.bottomNavigation)

        BottomNavigationHelper.setup(
            activity = this,
            bottomNavigation = bottomNavigation,
            selectedItemId = R.id.navigationHome,
            onHomeReselected = {
                val dashboardScrollView =
                    findViewById<android.widget.ScrollView>(
                        R.id.dashboardScrollView
                    )

                dashboardScrollView.smoothScrollTo(0, 0)
            }
        )


        val userId = auth.currentUser?.uid

        val menuButton = findViewById<ImageButton>(R.id.btnMenu)

        val welcomeText = findViewById<TextView>(R.id.txtWelcome)
        val greetingText = findViewById<TextView>(R.id.txtGreeting)
        val taglineText = findViewById<TextView>(R.id.txtTagline)

        val creditScoreText =
            findViewById<TextView>(R.id.txtCreditScore)

        val creditScoreRatingText =
            findViewById<TextView>(R.id.txtCreditScoreRating)

        val creditScoreProgress =
            findViewById<LinearProgressIndicator>(R.id.progressCreditScore)

        val borrowerLevelText =
            findViewById<TextView>(R.id.txtBorrowerLevel)

        val borrowerProgressTitleText =
            findViewById<TextView>(R.id.txtBorrowerProgressTitle)

        val borrowerLevelProgress =
            findViewById<LinearProgressIndicator>(R.id.progressBorrowerLevel)

        val nextLevelProgressText =
            findViewById<TextView>(R.id.txtNextLevelProgress)
        val requestLoanButton =
            findViewById<MaterialButton>(R.id.btnRequestLoan)


        val loanLimitText = findViewById<TextView>(R.id.txtLoanLimit)
        val identityBadgeText = findViewById<TextView>(R.id.txtIdentityBadge)
        val unreadNotificationsText = findViewById<TextView>(R.id.txtUnreadNotifications)

        val autoPayStatusText = findViewById<TextView>(R.id.txtAutoPayStatus)
        val autoPayNextDateText = findViewById<TextView>(R.id.txtAutoPayNextDate)
        val autoPayAmountText = findViewById<TextView>(R.id.txtAutoPayAmount)

        val totalRequestedText = findViewById<TextView>(R.id.txtTotalRequested)
        val pendingLoansText = findViewById<TextView>(R.id.txtPendingLoans)
        val approvedAmountText = findViewById<TextView>(R.id.txtApprovedAmount)
        val rejectedLoansText = findViewById<TextView>(R.id.txtRejectedLoans)
        val unreadNotificationsCard =
            findViewById<MaterialCardView>(R.id.cardUnreadNotifications)
        unreadNotificationsCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    NotificationsActivity::class.java
                )
            )
        }

        menuButton.setOnClickListener {
            showDashboardMenu(menuButton)
        }

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        requestLoanButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    LoanRequestActivity::class.java
                )
            )
        }

        saveFcmToken(userId)

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    return@addOnSuccessListener
                }

                val savedLanguage =
                    document.getString("language") ?: userLanguage

                if (savedLanguage != userLanguage) {
                    userLanguage = savedLanguage

                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(savedLanguage)
                    )

                    return@addOnSuccessListener
                }


                val fullName = document.getString("fullName") ?: "User"
                val role = document.getString("role") ?: "user"

                isAdminUser = role == "admin"

                greetingText.text = getGreeting(userLanguage)
                taglineText.text = getTagline(userLanguage)
                welcomeText.text = fullName

                val creditScore =
                    (document.getLong("creditScore") ?: 500L).toInt()

                creditScoreText.text = creditScore.toString()

                creditScoreRatingText.text =
                    getCreditScoreRating(creditScore, userLanguage)

                creditScoreProgress.progress =
                    calculateCreditScoreProgress(creditScore)

                val borrowerLevel = document.getString("borrowerLevel") ?: "New"
                borrowerLevelText.text =
                    if (userLanguage == "fr") {
                        "🏅 ${translateBorrowerLevel(borrowerLevel)}"
                    } else {
                        "🏅 $borrowerLevel Borrower"
                    }

                val completedLoans =
                    document.getLong("completedLoans") ?: 0L

                val nextLevel =
                    getNextBorrowerLevel(borrowerLevel)

                val requiredLoans =
                    getRequiredLoansForNextLevel(borrowerLevel)

                val remainingLoans =
                    (requiredLoans - completedLoans).coerceAtLeast(0L)

                val borrowerProgress =
                    calculateBorrowerProgress(
                        completedLoans = completedLoans,
                        requiredLoans = requiredLoans
                    )

                borrowerLevelProgress.progress = borrowerProgress

                if (borrowerLevel == "Platinum") {
                    borrowerProgressTitleText.text =
                        if (userLanguage == "fr") {
                            "Niveau maximum atteint"
                        } else {
                            "Maximum level reached"
                        }

                    nextLevelProgressText.text =
                        if (userLanguage == "fr") {
                            "🏆 Vous avez atteint le niveau le plus élevé"
                        } else {
                            "🏆 You have reached the highest level"
                        }

                    borrowerLevelProgress.progress = 100
                } else {
                    borrowerProgressTitleText.text =
                        if (userLanguage == "fr") {
                            "Progression vers ${translateBorrowerLevel(nextLevel)}"
                        } else {
                            "Progress to $nextLevel"
                        }

                    nextLevelProgressText.text =
                        if (userLanguage == "fr") {
                            if (remainingLoans == 1L) {
                                "1 prêt supplémentaire"
                            } else {
                                "$remainingLoans prêts supplémentaires"
                            }
                        } else {
                            if (remainingLoans == 1L) {
                                "1 loan remaining"
                            } else {
                                "$remainingLoans loans remaining"
                            }
                        }
                }

                val identityVerified = document.getBoolean("identityVerified") ?: false
                requestLoanButton.isEnabled = identityVerified

                requestLoanButton.alpha =
                    if (identityVerified) {
                        1.0f
                    } else {
                        0.5f
                    }

                val maxLoanLimit =
                    if (!identityVerified) {
                        0
                    } else {
                        when (borrowerLevel) {
                            "Platinum" -> 1500
                            "Gold" -> 1000
                            "Silver" -> 750
                            "Bronze" -> 500
                            else -> 250
                        }
                    }

                loanLimitText.text =
                    if (maxLoanLimit == 0) {
                        if (userLanguage == "fr") {
                            "⚠️ Vérifiez votre identité pour débloquer les prêts"
                        } else {
                            "⚠️ Verify your identity to unlock loans"
                        }
                    } else {
                        val formattedLimit =
                            CurrencyFormatter.format(
                                maxLoanLimit.toDouble(),
                                userLanguage
                            )

                        if (userLanguage == "fr") {
                            "💰 Limite de prêt disponible : $formattedLimit"
                        } else {
                            "💰 Available Loan Limit: $formattedLimit"
                        }
                    }

                val identityStatus =
                    document.getString("identityStatus") ?: "not_submitted"

                identityBadgeText.text = getIdentityBadgeText(identityStatus)

                listenForUnreadNotifications(userId, unreadNotificationsText)
                listenForAutoPayStatus(
                    userId,
                    autoPayStatusText,
                    autoPayNextDateText,
                    autoPayAmountText
                )
                listenForLoanStats(
                    userId,
                    totalRequestedText,
                    pendingLoansText,
                    approvedAmountText,
                    rejectedLoansText
                )
            }

        processPaymentReminders()
        processAutoPayments()
    }
    override fun onResume() {
        super.onResume()

        if (::bottomNavigation.isInitialized) {
            BottomNavigationHelper.syncSelection(
                bottomNavigation = bottomNavigation,
                selectedItemId = R.id.navigationHome
            )
        }
    }

    private fun showDashboardMenu(menuButton: ImageButton) {
        val popupMenu = PopupMenu(this, menuButton)

        val transactionsText =
            if (userLanguage == "fr") "Transactions" else "Transactions"

        val requestLoanText =
            if (userLanguage == "fr") "Demander un prêt" else "Request Loan"

        val loanHistoryText =
            if (userLanguage == "fr") "Historique des prêts" else "Loan History"

        val makePaymentText =
            if (userLanguage == "fr") "Effectuer un paiement" else "Make Payment"

        val profileText =
            if (userLanguage == "fr") "Profil" else "Profile"

        val notificationsText =
            if (userLanguage == "fr") "Notifications" else "Notifications"

        val bankAccountText =
            if (userLanguage == "fr") "Compte bancaire" else "Bank Account"

        val mobileMoneyText =
            if (userLanguage == "fr") "Mobile Money" else "Mobile Money"

        val languageSettingsText =
            if (userLanguage == "fr") "Langue" else "Language"

        val adminDashboardText =
            if (userLanguage == "fr") "Tableau Admin" else "Admin Dashboard"

        val logoutText =
            if (userLanguage == "fr") "Déconnexion" else "Logout"

        popupMenu.menu.add(transactionsText)
        popupMenu.menu.add(requestLoanText)
        popupMenu.menu.add(makePaymentText)
        popupMenu.menu.add(bankAccountText)
        popupMenu.menu.add(mobileMoneyText)
        popupMenu.menu.add(languageSettingsText)

        if (isAdminUser) {
            popupMenu.menu.add(adminDashboardText)
        }

        popupMenu.menu.add(logoutText)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                transactionsText -> {
                    startActivity(Intent(this, TransactionHistoryActivity::class.java))
                    true
                }

                requestLoanText -> {
                    startActivity(Intent(this, LoanRequestActivity::class.java))
                    true
                }


                makePaymentText -> {
                    startActivity(Intent(this, PaymentActivity::class.java))
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

                languageSettingsText -> {
                    startActivity(Intent(this, LanguageSettingsActivity::class.java))
                    true
                }

                adminDashboardText -> {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    true
                }

                logoutText -> {
                    auth.signOut()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun saveFcmToken(userId: String) {
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

    private fun listenForUnreadNotifications(
        userId: String,
        unreadNotificationsText: TextView
    ) {
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                val unreadCount = snapshots?.size() ?: 0

                unreadNotificationsText.text =
                    unreadCount.toString()
            }
    }

    private fun listenForAutoPayStatus(
        userId: String,
        autoPayStatusText: TextView,
        autoPayNextDateText: TextView,
        autoPayAmountText: TextView
    ) {
        db.collection("loan_requests")
            .whereEqualTo("userId", userId)
            .whereEqualTo("autoPayEnabled", true)
            .whereEqualTo("autoPayStatus", "scheduled")
            .limit(1)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                val loan = snapshots?.documents?.firstOrNull()

                if (loan != null) {
                    val nextDate = loan.getLong("nextPaymentDate") ?: 0L
                    val amount = loan.getString("nextPaymentAmount") ?: "0.00"

                    val dateText =
                        SimpleDateFormat(
                            "MMM dd, yyyy",
                            Locale.getDefault()
                        ).format(Date(nextDate))

                    if (userLanguage == "fr") {
                        autoPayStatusText.text =
                            "Paiement automatique : planifié"

                        autoPayNextDateText.text =
                            dateText

                        val numericAmount =
                            parseMoney(amount)

                        autoPayAmountText.text =
                            CurrencyFormatter.format(
                                numericAmount,
                                userLanguage
                            )
                    } else {
                        autoPayStatusText.text =
                            "Auto Pay: Scheduled"

                        autoPayNextDateText.text =
                            dateText

                        val numericAmount =
                            parseMoney(amount)

                        autoPayAmountText.text =
                            CurrencyFormatter.format(
                                numericAmount,
                                userLanguage
                            )
                    }
                } else {
                    autoPayStatusText.text =
                        if (userLanguage == "fr") {
                            "Aucun paiement automatique actif"
                        } else {
                            "No active Auto Pay"
                        }

                    autoPayNextDateText.text =
                        if (userLanguage == "fr") {
                            "Non planifié"
                        } else {
                            "Not scheduled"
                        }

                    autoPayAmountText.text =
                        CurrencyFormatter.format(
                            0.0,
                            userLanguage
                        )
                }
            }
    }

    private fun listenForLoanStats(
        userId: String,
        totalRequestedText: TextView,
        pendingLoansText: TextView,
        approvedAmountText: TextView,
        rejectedLoansText: TextView
    ) {


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

                snapshots?.documents?.forEach { document ->
                    val amount = parseMoney(document.getString("amount") ?: "0")
                    val status = document.getString("status") ?: "pending"

                    totalRequested += amount

                    when (status) {
                        "pending" -> pendingCount++
                        "approved" -> approvedAmount += amount
                        "rejected" -> rejectedCount++
                    }
                }

                totalRequestedText.text =
                    CurrencyFormatter.format(
                        amount = totalRequested,
                        languageCode = userLanguage
                    )

                approvedAmountText.text =
                    CurrencyFormatter.format(
                        amount = approvedAmount,
                        languageCode = userLanguage
                    )

                pendingLoansText.text =
                    pendingCount.toString()

                rejectedLoansText.text =
                    rejectedCount.toString()
            }
    }

    private fun processPaymentReminders() {
        val userId = auth.currentUser?.uid ?: return
        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60L * 60L * 1000L

        db.collection("loan_requests")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { loans ->

                for (document in loans.documents) {
                    val dueDate = document.getLong("nextPaymentDate") ?: 0L
                    val reminderSent = document.getBoolean("reminderSent") ?: false
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
                        val notification =
                            if (userLanguage == "fr") {
                                hashMapOf(
                                    "userId" to userId,
                                    "title" to "Rappel de paiement",
                                    "message" to "Votre paiement de $$paymentAmount est dû demain.",
                                    "timestamp" to now,
                                    "isRead" to false
                                )
                            } else {
                                hashMapOf(
                                    "userId" to userId,
                                    "title" to "Payment Reminder",
                                    "message" to "Your payment of $$paymentAmount is due tomorrow.",
                                    "timestamp" to now,
                                    "isRead" to false
                                )
                            }

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
        val userId = auth.currentUser?.uid ?: return
        val now = System.currentTimeMillis()

        db.collection("loan_requests")
            .whereEqualTo("userId", userId)
            .whereEqualTo("autoPayEnabled", true)
            .whereEqualTo("autoPayStatus", "scheduled")
            .get()
            .addOnSuccessListener { loans ->

                for (document in loans.documents) {
                    val nextPaymentDate = document.getLong("nextPaymentDate") ?: 0L

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

                    val newBalance = remainingBalance - actualPayment

                    val paymentFrequency =
                        document.getString("paymentFrequency") ?: "weekly"

                    val nextDate =
                        when (paymentFrequency) {
                            "weekly" -> addDays(now, 7)
                            "monthly" -> addMonths(now, 1)
                            "one_time" -> 0L
                            else -> addDays(now, 7)
                        }

                    val updates = hashMapOf<String, Any>(
                        "remainingBalance" to String.format(Locale.US, "%.2f", newBalance),
                        "lastAutoPaymentAt" to now,
                        "reminderSent" to false
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
                                "paymentAmount" to String.format(Locale.US, "%.2f", actualPayment),
                                "previousBalance" to String.format(Locale.US, "%.2f", remainingBalance),
                                "newBalance" to String.format(
                                    Locale.US,
                                    "%.2f",
                                    newBalance.coerceAtLeast(0.0)
                                ),
                                "paymentDate" to now,
                                "paymentType" to "auto_pay",
                                "status" to "completed"
                            )

                            db.collection("transactions")
                                .add(transaction)

                            val notification =
                                if (userLanguage == "fr") {
                                    hashMapOf(
                                        "userId" to userId,
                                        "title" to "Paiement automatique traité",
                                        "message" to "Votre paiement automatique de $${
                                            String.format(Locale.US, "%.2f", actualPayment)
                                        } a été traité.",
                                        "timestamp" to now,
                                        "isRead" to false
                                    )
                                } else {
                                    hashMapOf(
                                        "userId" to userId,
                                        "title" to "Auto Pay Processed",
                                        "message" to "Your automatic payment of $${
                                            String.format(Locale.US, "%.2f", actualPayment)
                                        } was processed.",
                                        "timestamp" to now,
                                        "isRead" to false
                                    )
                                }

                            db.collection("notifications")
                                .add(notification)
                        }
                }
            }
    }

    private fun getIdentityBadgeText(identityStatus: String): String {
        return if (userLanguage == "fr") {
            when (identityStatus) {
                "approved" -> "🛡️ Identité vérifiée"
                "pending" -> "⏳ Vérification en cours"
                "rejected" -> "❌ Vérification refusée"
                else -> "⚠️ Aucune identité soumise"
            }
        } else {
            when (identityStatus) {
                "approved" -> "🛡️ Identity Verified"
                "pending" -> "⏳ Verification Pending"
                "rejected" -> "❌ Verification Rejected"
                else -> "⚠️ No Identity Submitted"
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

    private fun getGreeting(language: String): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return if (language == "fr") {
            when {
                hour < 12 -> "Bonjour,"
                hour < 18 -> "Bon après-midi,"
                else -> "Bonsoir,"
            }
        } else {
            when {
                hour < 12 -> "Good morning,"
                hour < 18 -> "Good afternoon,"
                else -> "Good evening,"
            }
        }
    }

    private fun getTagline(language: String): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return if (language == "fr") {
            when {
                hour < 12 -> "Une excellente journée pour faire grandir vos finances."
                hour < 18 -> "Restez sur la bonne voie vers vos objectifs financiers."
                else -> "Consultez vos progrès et préparez la suite."
            }
        } else {
            when {
                hour < 12 -> "A great day to grow your finances."
                hour < 18 -> "Stay on track with your financial goals."
                else -> "Review your progress and plan ahead."
            }
        }
    }

    private fun addDays(timeMillis: Long, days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.timeInMillis
    }

    private fun addMonths(timeMillis: Long, months: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        calendar.add(Calendar.MONTH, months)
        return calendar.timeInMillis
    }
    private fun getCreditScoreRating(
        creditScore: Int,
        language: String
    ): String {
        return if (language == "fr") {
            when (creditScore) {
                in 300..579 -> "Faible"
                in 580..669 -> "Moyen"
                in 670..739 -> "Bon"
                in 740..799 -> "Très bon"
                in 800..850 -> "Excellent"
                else -> "Non disponible"
            }
        } else {
            when (creditScore) {
                in 300..579 -> "Poor"
                in 580..669 -> "Fair"
                in 670..739 -> "Good"
                in 740..799 -> "Very Good"
                in 800..850 -> "Excellent"
                else -> "Unavailable"
            }
        }
    }

    private fun calculateCreditScoreProgress(
        creditScore: Int
    ): Int {
        val minimumScore = 300
        val maximumScore = 850

        val safeScore =
            creditScore.coerceIn(minimumScore, maximumScore)

        return (
                (safeScore - minimumScore).toDouble() /
                        (maximumScore - minimumScore).toDouble() *
                        100
                ).toInt()
    }

    private fun getNextBorrowerLevel(
        borrowerLevel: String
    ): String {
        return when (borrowerLevel) {
            "New" -> "Bronze"
            "Bronze" -> "Silver"
            "Silver" -> "Gold"
            "Gold" -> "Platinum"
            else -> "Platinum"
        }
    }

    private fun getRequiredLoansForNextLevel(
        borrowerLevel: String
    ): Long {
        return when (borrowerLevel) {
            "New" -> 3L
            "Bronze" -> 5L
            "Silver" -> 8L
            "Gold" -> 15L
            else -> 15L
        }
    }

    private fun calculateBorrowerProgress(
        completedLoans: Long,
        requiredLoans: Long
    ): Int {
        if (requiredLoans <= 0L) {
            return 0
        }

        return (
                completedLoans.toDouble() /
                        requiredLoans.toDouble() *
                        100
                )
            .toInt()
            .coerceIn(0, 100)
    }

    private fun translateBorrowerLevel(
        borrowerLevel: String
    ): String {
        return when (borrowerLevel) {
            "New" -> "Nouvel emprunteur"
            "Bronze" -> "Emprunteur Bronze"
            "Silver" -> "Emprunteur Argent"
            "Gold" -> "Emprunteur Or"
            "Platinum" -> "Emprunteur Platine"
            else -> borrowerLevel
        }

    }



}