package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent


class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loanList: MutableList<Pair<String, AdminLoanModel>>
    private lateinit var adapter: AdminLoanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)
        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }
        recyclerView = findViewById(R.id.recyclerAdminLoans)

        loanList = mutableListOf()

        adapter = AdminLoanAdapter(loanList)

        recyclerView.adapter = adapter

        val db = FirebaseFirestore.getInstance()

        db.collection("loan_requests")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                loanList.clear()

                if (snapshots != null) {

                    for (document in snapshots.documents) {

                        val loan = document.toObject(AdminLoanModel::class.java)

                        if (loan != null) {
                            loanList.add(Pair(document.id, loan))
                        }
                    }

                    adapter.notifyDataSetChanged()
                }
            }
        val analyticsButton = findViewById<Button>(R.id.btnAdminAnalytics)

        analyticsButton.setOnClickListener {
            val intent = Intent(this, AdminAnalyticActivity::class.java)
            startActivity(intent)
        }
        val auditButton = findViewById<Button>(R.id.btnAuditLogs)

        auditButton.setOnClickListener {
            val intent = Intent(this, AuditLogActivity::class.java)
            startActivity(intent)
        }
    }

}