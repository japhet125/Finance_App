package com.ramdefinance.financeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoanHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loanList: MutableList<LoanModel>
    private lateinit var adapter: LoanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_history)

        recyclerView = findViewById(R.id.recyclerLoans)

        loanList = mutableListOf()
        adapter = LoanAdapter(loanList)

        recyclerView.adapter = adapter

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid

        if (userId != null) {

            db.collection("loan_requests")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->

                    for (document in documents) {

                        val loan = document.toObject(LoanModel::class.java)

                        loanList.add(loan)
                    }

                    adapter.notifyDataSetChanged()
                }
        }
    }
}