package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.firestore.FirebaseFirestore

class AdminAnalyticActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var userLanguage = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_analytic)

        userLanguage = resources.configuration.locales[0].language

        val backButton = findViewById<Button>(R.id.btnBack)
        val chartButton = findViewById<Button>(R.id.btnLoanStatusChart)

        val totalUsersText = findViewById<TextView>(R.id.txtTotalUsers)
        val totalLoansText = findViewById<TextView>(R.id.txtTotalLoans)
        val approvedLoansText = findViewById<TextView>(R.id.txtApprovedLoans)
        val overdueLoansText = findViewById<TextView>(R.id.txtOverdueLoans)
        val totalLoanedText = findViewById<TextView>(R.id.txtTotalLoaned)
        val totalPaymentsText = findViewById<TextView>(R.id.txtTotalPayments)
        val averageCreditText = findViewById<TextView>(R.id.txtAverageCredit)
        val verifiedUsersText = findViewById<TextView>(R.id.txtVerifiedUsers)
        val flaggedUsersText = findViewById<TextView>(R.id.txtFlaggedUsers)
        val rejectedLoansText = findViewById<TextView>(R.id.txtRejectedLoans)
        val outstandingBalanceText =
            findViewById<TextView>(R.id.txtOutstandingBalance)

        val pieLoansStatus = findViewById<PieChart>(R.id.pieLoansStatus)
        val barPayments = findViewById<BarChart>(R.id.barPayments)

        backButton.setOnClickListener {
            finish()
        }

        chartButton.setOnClickListener {
            val intent = Intent(
                this,
                LoanStatusChartActivity::class.java
            )
            startActivity(intent)
        }

        loadUserAnalytics(
            totalUsersText = totalUsersText,
            averageCreditText = averageCreditText,
            verifiedUsersText = verifiedUsersText,
            flaggedUsersText = flaggedUsersText
        )

        loadLoanAnalytics(
            totalLoansText = totalLoansText,
            approvedLoansText = approvedLoansText,
            overdueLoansText = overdueLoansText,
            totalLoanedText = totalLoanedText,
            rejectedLoansText = rejectedLoansText,
            outstandingBalanceText = outstandingBalanceText,
            pieLoansStatus = pieLoansStatus
        )

        loadPaymentAnalytics(
            totalPaymentsText = totalPaymentsText,
            barPayments = barPayments
        )
    }

    private fun loadUserAnalytics(
        totalUsersText: TextView,
        averageCreditText: TextView,
        verifiedUsersText: TextView,
        flaggedUsersText: TextView
    ) {
        db.collection("users")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                var creditTotal = 0L
                var userCount = 0
                var verifiedUsers = 0
                var flaggedUsers = 0

                snapshots?.documents?.forEach { document ->
                    userCount++

                    val creditScore =
                        document.getLong("creditScore") ?: 500L

                    val identityVerified =
                        document.getBoolean("identityVerified") ?: false

                    val accountFlagged =
                        document.getBoolean("accountFlagged") ?: false

                    creditTotal += creditScore

                    if (identityVerified) {
                        verifiedUsers++
                    }

                    if (accountFlagged) {
                        flaggedUsers++
                    }
                }

                val averageCredit =
                    if (userCount > 0) {
                        creditTotal / userCount
                    } else {
                        0L
                    }

                totalUsersText.text =
                    getString(
                        R.string.admin_total_users,
                        userCount
                    )

                averageCreditText.text =
                    getString(
                        R.string.admin_average_credit,
                        averageCredit
                    )

                verifiedUsersText.text =
                    getString(
                        R.string.admin_verified_users,
                        verifiedUsers
                    )

                flaggedUsersText.text =
                    getString(
                        R.string.admin_flagged_users,
                        flaggedUsers
                    )
            }
    }

    private fun loadLoanAnalytics(
        totalLoansText: TextView,
        approvedLoansText: TextView,
        overdueLoansText: TextView,
        totalLoanedText: TextView,
        rejectedLoansText: TextView,
        outstandingBalanceText: TextView,
        pieLoansStatus: PieChart
    ) {
        db.collection("loan_requests")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                var totalLoans = 0
                var approvedLoans = 0
                var overdueLoans = 0
                var rejectedLoans = 0

                var totalLoaned = 0.0
                var outstandingBalance = 0.0

                snapshots?.documents?.forEach { document ->
                    totalLoans++

                    val status =
                        document.getString("status") ?: ""

                    val principal =
                        parseMoney(
                            document.get("principalAmount")
                        )

                    val remainingBalance =
                        parseMoney(
                            document.get("remainingBalance")
                        )

                    when (status) {
                        "approved", "paid", "overdue" -> {
                            approvedLoans++
                            totalLoaned += principal
                        }

                        "rejected" -> {
                            rejectedLoans++
                        }
                    }

                    if (
                        status == "approved" ||
                        status == "overdue"
                    ) {
                        outstandingBalance += remainingBalance
                    }

                    if (status == "overdue") {
                        overdueLoans++
                    }
                }

                val formattedTotalLoaned =
                    CurrencyFormatter.format(
                        amount = totalLoaned,
                        currencyCode = "XOF",
                        languageCode = userLanguage
                    )

                val formattedOutstandingBalance =
                    CurrencyFormatter.format(
                        amount = outstandingBalance,
                        currencyCode = "XOF",
                        languageCode = userLanguage
                    )

                totalLoansText.text =
                    getString(
                        R.string.admin_total_loans,
                        totalLoans
                    )

                approvedLoansText.text =
                    getString(
                        R.string.admin_approved_loans,
                        approvedLoans
                    )

                overdueLoansText.text =
                    getString(
                        R.string.admin_overdue_loans,
                        overdueLoans
                    )

                rejectedLoansText.text =
                    getString(
                        R.string.admin_rejected_loans,
                        rejectedLoans
                    )

                totalLoanedText.text =
                    getString(
                        R.string.admin_total_loaned,
                        formattedTotalLoaned
                    )

                outstandingBalanceText.text =
                    getString(
                        R.string.admin_outstanding_balance,
                        formattedOutstandingBalance
                    )

                val pieEntries = listOf(
                    PieEntry(
                        approvedLoans.toFloat(),
                        getString(R.string.chart_approved)
                    ),
                    PieEntry(
                        overdueLoans.toFloat(),
                        getString(R.string.chart_overdue)
                    ),
                    PieEntry(
                        rejectedLoans.toFloat(),
                        getString(R.string.chart_rejected)
                    )
                )

                val pieDataSet = PieDataSet(
                    pieEntries,
                    getString(R.string.chart_loans_by_status)
                )

                val pieData = PieData(pieDataSet)

                pieLoansStatus.data = pieData
                pieLoansStatus.description.isEnabled = false
                pieLoansStatus.invalidate()
            }
    }

    private fun loadPaymentAnalytics(
        totalPaymentsText: TextView,
        barPayments: BarChart
    ) {
        db.collection("transactions")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                var totalPayments = 0.0

                snapshots?.documents?.forEach { document ->
                    totalPayments += parseMoney(
                        document.get("paymentAmount")
                    )
                }

                val formattedTotalPayments =
                    CurrencyFormatter.format(
                        amount = totalPayments,
                        currencyCode = "XOF",
                        languageCode = userLanguage
                    )

                totalPaymentsText.text =
                    getString(
                        R.string.admin_total_payments,
                        formattedTotalPayments
                    )

                val barEntries = listOf(
                    BarEntry(
                        1f,
                        totalPayments.toFloat()
                    )
                )

                val barDataSet = BarDataSet(
                    barEntries,
                    getString(R.string.chart_total_payments)
                )

                val barData = BarData(barDataSet)

                barPayments.data = barData
                barPayments.description.isEnabled = false
                barPayments.invalidate()
            }
    }

    private fun parseMoney(value: Any?): Double {
        return when (value) {
            is Number -> value.toDouble()

            is String -> value
                .replace(",", ".")
                .trim()
                .toDoubleOrNull() ?: 0.0

            else -> 0.0
        }
    }
}