package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
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
        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

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
                .addSnapshotListener { snapshots, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    loanList.clear()

                    if (snapshots != null) {

                        for (document in snapshots.documents) {

                            val loan = document.toObject(LoanModel::class.java)

                            if (loan != null) {

                                val remainingBalance =
                                    loan.remainingBalance.toDoubleOrNull() ?: 0.0

                                val isLate =
                                    loan.status == "approved" &&
                                            loan.dueDate > 0 &&
                                            System.currentTimeMillis() > loan.dueDate &&
                                            remainingBalance > 0.0 &&
                                            !loan.overduePenaltyApplied
                                if (isLate) {
                                    val db = FirebaseFirestore.getInstance()

                                    db.collection("loan_requests")
                                        .document(document.id)
                                        .update(
                                            mapOf(
                                                "status" to "overdue",
                                                "overduePenaltyApplied" to true
                                            )
                                        )
                                        .addOnSuccessListener {

                                            val notification = hashMapOf(
                                                "userId" to userId,
                                                "title" to "Loan Overdue",
                                                "message" to "Your loan payment is overdue. Please make a payment as soon as possible.",
                                                "timestamp" to System.currentTimeMillis(),
                                                "isRead" to false
                                            )

                                            db.collection("notifications")
                                                .add(notification)

                                            val userRef = db.collection("users")
                                                .document(userId)

                                            userRef.get()
                                                .addOnSuccessListener { userDocument ->

                                                    val currentScore =
                                                        userDocument.getLong("creditScore") ?: 500

                                                    userRef.update(
                                                        "creditScore",
                                                        currentScore - 25
                                                    )
                                                }
                                        }
                                }

                                loanList.add(loan)
                            }
                        }

                        adapter.notifyDataSetChanged()
                    }
                }
        }
    }

}