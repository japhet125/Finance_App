package com.ramdefinance.financeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loanList: MutableList<Pair<String, AdminLoanModel>>
    private lateinit var adapter: AdminLoanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        recyclerView = findViewById(R.id.recyclerAdminLoans)

        loanList = mutableListOf()

        adapter = AdminLoanAdapter(loanList)

        recyclerView.adapter = adapter

        val db = FirebaseFirestore.getInstance()

        db.collection("loan_requests")
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {

                    val loan = document.toObject(AdminLoanModel::class.java)

                    loanList.add(Pair(document.id, loan))
                }

                adapter.notifyDataSetChanged()
            }
    }
}