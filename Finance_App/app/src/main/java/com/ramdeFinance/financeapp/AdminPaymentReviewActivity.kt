package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.LinearLayoutManager

class AdminPaymentReviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var paymentList: MutableList<Pair<String, AdminPaymentModel>>
    private lateinit var adapter: AdminPaymentAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_payment_review)

        val backButton = findViewById<Button>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerAdminPayments)
        recyclerView.layoutManager = LinearLayoutManager(this)

        paymentList = mutableListOf()
        adapter = AdminPaymentAdapter(paymentList)

        recyclerView.adapter = adapter

        db.collection("transactions")
            .whereEqualTo("paymentType", "loan_repayment")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                paymentList.clear()

                snapshots?.documents?.forEach { document ->
                    val payment = document.toObject(AdminPaymentModel::class.java)

                    if (payment != null) {
                        paymentList.add(Pair(document.id, payment))
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }
}