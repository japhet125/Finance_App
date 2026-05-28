package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoanStatusChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_status_chart)

        val backButton = findViewById<Button>(R.id.btnBack)

        val pendingText = findViewById<TextView>(R.id.txtPendingChart)
        val approvedText = findViewById<TextView>(R.id.txtApprovedChart)
        val rejectedText = findViewById<TextView>(R.id.txtRejectedChart)
        val overdueText = findViewById<TextView>(R.id.txtOverdueChart)
        val paidText = findViewById<TextView>(R.id.txtPaidChart)

        val pendingBar = findViewById<ProgressBar>(R.id.progressPending)
        val approvedBar = findViewById<ProgressBar>(R.id.progressApproved)
        val rejectedBar = findViewById<ProgressBar>(R.id.progressRejected)
        val overdueBar = findViewById<ProgressBar>(R.id.progressOverdue)
        val paidBar = findViewById<ProgressBar>(R.id.progressPaid)

        backButton.setOnClickListener {
            finish()
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("loan_requests")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                var pending = 0
                var approved = 0
                var rejected = 0
                var overdue = 0
                var paid = 0

                snapshots?.documents?.forEach { document ->
                    when (document.getString("status")) {
                        "pending" -> pending++
                        "approved" -> approved++
                        "rejected" -> rejected++
                        "overdue" -> overdue++
                        "paid" -> paid++
                    }
                }

                val total = pending + approved + rejected + overdue + paid

                fun percentage(value: Int): Int {
                    return if (total > 0) ((value.toDouble() / total) * 100).toInt() else 0
                }

                pendingText.text = "⏳ Pending: $pending (${percentage(pending)}%)"
                approvedText.text = "✅ Approved: $approved (${percentage(approved)}%)"
                rejectedText.text = "❌ Rejected: $rejected (${percentage(rejected)}%)"
                overdueText.text = "⚠️ Overdue: $overdue (${percentage(overdue)}%)"
                paidText.text = "💵 Paid: $paid (${percentage(paid)}%)"

                pendingBar.progress = percentage(pending)
                approvedBar.progress = percentage(approved)
                rejectedBar.progress = percentage(rejected)
                overdueBar.progress = percentage(overdue)
                paidBar.progress = percentage(paid)
            }
    }
}