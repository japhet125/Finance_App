package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminPaymentReviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private lateinit var paymentList:
            MutableList<Pair<String, AdminPaymentModel>>

    private lateinit var adapter: AdminPaymentAdapter

    private val db =
        FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_admin_payment_review
        )

        val backButton =
            findViewById<Button>(R.id.btnBack)

        recyclerView =
            findViewById(R.id.recyclerAdminPayments)

        progressBar =
            findViewById(R.id.progressAdminPayments)

        emptyText =
            findViewById(R.id.tvAdminPaymentsEmpty)

        backButton.setOnClickListener {
            finish()
        }

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        paymentList = mutableListOf()
        adapter = AdminPaymentAdapter(paymentList)

        recyclerView.adapter = adapter

        ListStateHelper.showLoading(
            recyclerView = recyclerView,
            progressBar = progressBar,
            emptyText = emptyText
        )

        loadPendingPayments()
    }

    private fun loadPendingPayments() {
        db.collection("transactions")
            .whereEqualTo(
                "paymentType",
                "loan_repayment"
            )
            .whereEqualTo(
                "status",
                "pending"
            )
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    ListStateHelper.showError(
                        recyclerView = recyclerView,
                        progressBar = progressBar,
                        emptyText = emptyText,
                        message = getString(
                            R.string.admin_payments_load_failed
                        )
                    )

                    return@addSnapshotListener
                }

                paymentList.clear()

                snapshots
                    ?.documents
                    ?.forEach { document ->

                        val payment =
                            document.toObject(
                                AdminPaymentModel::class.java
                            )

                        if (payment != null) {
                            paymentList.add(
                                Pair(
                                    document.id,
                                    payment
                                )
                            )
                        }
                    }

                adapter.notifyDataSetChanged()

                if (paymentList.isEmpty()) {
                    ListStateHelper.showEmpty(
                        recyclerView = recyclerView,
                        progressBar = progressBar,
                        emptyText = emptyText,
                        message = getString(
                            R.string.admin_payments_empty
                        )
                    )
                } else {
                    ListStateHelper.showContent(
                        recyclerView = recyclerView,
                        progressBar = progressBar,
                        emptyText = emptyText
                    )
                }
            }
    }
}