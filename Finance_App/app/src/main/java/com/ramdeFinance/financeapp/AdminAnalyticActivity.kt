package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData

class AdminAnalyticActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val currencyFormat = NumberFormat.getCurrencyInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_analytic)

        val backButton = findViewById<Button>(R.id.btnBack)
        val totalUsersText = findViewById<TextView>(R.id.txtTotalUsers)
        val totalLoansText = findViewById<TextView>(R.id.txtTotalLoans)
        val approvedLoansText = findViewById<TextView>(R.id.txtApprovedLoans)
        val overdueLoansText = findViewById<TextView>(R.id.txtOverdueLoans)
        val totalLoanedText = findViewById<TextView>(R.id.txtTotalLoaned)
        val totalPaymentsText = findViewById<TextView>(R.id.txtTotalPayments)
        val averageCreditText = findViewById<TextView>(R.id.txtAverageCredit)
        val pieLoansStatus = findViewById<PieChart>(R.id.pieLoansStatus)
        val barPayments = findViewById<BarChart>(R.id.barPayments)

        backButton.setOnClickListener {
            finish()
        }

        db.collection("users")
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                var creditTotal = 0L
                var userCount = 0

                snapshots?.documents?.forEach { document ->
                    userCount++

                    val creditScore = document.getLong("creditScore") ?: 500
                    creditTotal += creditScore
                }

                val averageCredit =
                    if (userCount > 0) creditTotal / userCount else 0

                totalUsersText.text = "\uD83D\uDC65 Total Users: $userCount"
                averageCreditText.text = "⭐ Average Credit Score: $averageCredit"
            }

        db.collection("loan_requests")
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                var totalLoans = 0
                var approvedLoans = 0
                var overdueLoans = 0
                var totalLoaned = 0.0

                snapshots?.documents?.forEach { document ->
                    totalLoans++

                    val status = document.getString("status") ?: ""
                    val principal =
                        document.getString("principalAmount")
                            ?.toDoubleOrNull() ?: 0.0

                    when (status) {
                        "approved", "paid", "overdue" -> {
                            approvedLoans++
                            totalLoaned += principal
                        }

                        "overdue" -> overdueLoans++
                    }
                }

                totalLoansText.text = "\uD83D\uDCC4 Total Loans: $totalLoans"
                approvedLoansText.text = "✅ Approved Loans: $approvedLoans"
                overdueLoansText.text = "⚠\uFE0F Overdue Loans: $overdueLoans"
                totalLoanedText.text =
                    "\uD83D\uDCB0 Total Money Loaned: ${currencyFormat.format(totalLoaned)}"
                val pieEntries = listOf(
                    PieEntry(approvedLoans.toFloat(), "Approved"),
                    PieEntry(overdueLoans.toFloat(), "Overdue"),
                    PieEntry(totalLoans.toFloat(), "Total")
                )

                val pieDataSet = PieDataSet(pieEntries, "Loans by Status")
                val pieData = PieData(pieDataSet)

                pieLoansStatus.data = pieData
                pieLoansStatus.invalidate()
            }

        db.collection("transactions")
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                var totalPayments = 0.0

                snapshots?.documents?.forEach { document ->
                    val payment =
                        document.getString("paymentAmount")
                            ?.toDoubleOrNull() ?: 0.0

                    totalPayments += payment
                }

                totalPaymentsText.text =
                    "\uD83D\uDCB5 Total Payments Received: ${currencyFormat.format(totalPayments)}"
                val barEntries = listOf(
                    BarEntry(1f, totalPayments.toFloat())
                )

                val barDataSet = BarDataSet(barEntries, "Total Payments")
                val barData = BarData(barDataSet)

                barPayments.data = barData
                barPayments.invalidate()
            }
        val chartButton = findViewById<Button>(R.id.btnLoanStatusChart)

        chartButton.setOnClickListener {
            val intent = Intent(this, LoanStatusChartActivity::class.java)
            startActivity(intent)
        }
    }
}