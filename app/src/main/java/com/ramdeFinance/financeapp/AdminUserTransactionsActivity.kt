package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminUserTransactionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionList: MutableList<TransactionModel>
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user_transactions)

        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerUserTransactions)

        transactionList = mutableListOf()
        adapter = TransactionAdapter(transactionList)

        recyclerView.adapter = adapter

        val userId = intent.getStringExtra("USER_ID")

        if (userId == null) {
            finish()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("transactions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                transactionList.clear()

                snapshots?.documents?.forEach { document ->
                    val transaction =
                        document.toObject(TransactionModel::class.java)

                    if (transaction != null) {
                        transactionList.add(transaction)
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }
}