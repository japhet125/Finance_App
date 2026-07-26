package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaymentActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loanList:
            MutableList<Pair<String, PaymentLoanModel>>

    private lateinit var adapter: PaymentLoanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val backButton =
            findViewById<Button>(R.id.btnBack)

        recyclerView =
            findViewById(R.id.recyclerPayments)

        backButton.setOnClickListener {
            finish()
        }

        loanList = mutableListOf()
        adapter = PaymentLoanAdapter(loanList)

        recyclerView.adapter = adapter

        val userId =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid

        val db =
            FirebaseFirestore.getInstance()

        if (userId == null) {
            return
        }

        loadUserLanguage(
            db = db,
            userId = userId
        )

        loadApprovedLoans(
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

    private fun loadApprovedLoans(
        db: FirebaseFirestore,
        userId: String
    ) {
        db.collection("loan_requests")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                val temporaryList =
                    mutableListOf<
                            Pair<String, PaymentLoanModel>
                            >()

                snapshots?.documents?.forEach { document ->

                    val loan =
                        document.toObject(
                            PaymentLoanModel::class.java
                        )

                    if (loan != null) {
                        temporaryList.add(
                            Pair(document.id, loan)
                        )
                    }
                }

                temporaryList.sortWith(
                    compareBy<
                            Pair<String, PaymentLoanModel>
                            > {
                        if (it.second.status == "paid") {
                            1
                        } else {
                            0
                        }
                    }.thenByDescending {
                        it.second.createdAt
                    }
                )

                loanList.clear()
                loanList.addAll(temporaryList)

                adapter.notifyDataSetChanged()
            }
    }
}