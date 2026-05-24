package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionList: MutableList<TransactionModel>
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerTransactions)

        transactionList = mutableListOf()

        adapter = TransactionAdapter(transactionList)

        recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {

            db.collection("transactions")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshots, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    transactionList.clear()

                    if (snapshots != null) {

                        for (document in snapshots.documents) {

                            val transaction =
                                document.toObject(TransactionModel::class.java)

                            if (transaction != null) {
                                transactionList.add(transaction)
                            }
                        }
                    }

                    adapter.notifyDataSetChanged()
                }
        }
    }
}