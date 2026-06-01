package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminUserLoansActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loanList: MutableList<Pair<String, AdminLoanModel>>
    private lateinit var adapter: AdminUserLoanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin_user_loans)

        val userId =
            intent.getStringExtra("USER_ID")

        val backButton =
            findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        recyclerView =
            findViewById(R.id.recyclerUserLoans)

        loanList = mutableListOf()

        adapter =
            AdminUserLoanAdapter(loanList)

        recyclerView.adapter = adapter

        FirebaseFirestore.getInstance()
            .collection("loan_requests")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                loanList.clear()

                snapshots?.documents?.forEach { doc ->

                    val loan =
                        doc.toObject(AdminLoanModel::class.java)

                    if (loan != null) {
                        loanList.add(
                            Pair(doc.id, loan)
                        )
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }
}