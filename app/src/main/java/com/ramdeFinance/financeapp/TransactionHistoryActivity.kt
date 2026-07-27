package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private lateinit var transactionList: MutableList<TransactionModel>
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        val backButton =
            findViewById<Button>(R.id.btnBack)

        recyclerView =
            findViewById(R.id.recyclerTransactions)

        progressBar =
            findViewById(R.id.progressTransactions)

        emptyText =
            findViewById(R.id.tvTransactionEmpty)

        backButton.setOnClickListener {
            finish()
        }

        transactionList = mutableListOf()
        adapter = TransactionAdapter(transactionList)

        recyclerView.adapter = adapter

        ListStateHelper.showLoading(
            recyclerView = recyclerView,
            progressBar = progressBar,
            emptyText = emptyText
        )

        val userId =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid

        val db =
            FirebaseFirestore.getInstance()

        if (userId == null) {
            ListStateHelper.showError(
                recyclerView = recyclerView,
                progressBar = progressBar,
                emptyText = emptyText,
                message = getString(R.string.user_not_signed_in)
            )

            return
        }

        loadUserLanguage(
            db = db,
            userId = userId
        )

        loadTransactions(
            db = db,
            userId = userId
        )
    }

    private fun loadUserLanguage(
        db: FirebaseFirestore,
        userId: String
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                val language =
                    document.getString("language") ?: "en"

                adapter.updateLanguage(language)
            }
    }

    private fun loadTransactions(
        db: FirebaseFirestore,
        userId: String
    ) {
        db.collection("transactions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    ListStateHelper.showError(
                        recyclerView = recyclerView,
                        progressBar = progressBar,
                        emptyText = emptyText,
                        message = getString(
                            R.string.transaction_history_load_failed
                        )
                    )

                    return@addSnapshotListener
                }

                val temporaryList =
                    mutableListOf<TransactionModel>()

                snapshots?.documents?.forEach { document ->

                    val transaction =
                        document.toObject(
                            TransactionModel::class.java
                        )

                    if (transaction != null) {
                        temporaryList.add(transaction)
                    }
                }

                transactionList.clear()
                transactionList.addAll(temporaryList)

                adapter.notifyDataSetChanged()

                if (transactionList.isEmpty()) {
                    ListStateHelper.showEmpty(
                        recyclerView = recyclerView,
                        progressBar = progressBar,
                        emptyText = emptyText,
                        message = getString(
                            R.string.transaction_history_empty
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