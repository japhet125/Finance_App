package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaymentActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loanList: MutableList<Pair<String, PaymentLoanModel>>
    private lateinit var adapter: PaymentLoanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerPayments)

        loanList = mutableListOf()
        adapter = PaymentLoanAdapter(loanList)

        recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            db.collection("loan_requests")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "approved")
                .addSnapshotListener { snapshots, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    loanList.clear()

                    val tempList = mutableListOf<Pair<String, PaymentLoanModel>>()

                    if (snapshots != null) {
                        for (document in snapshots.documents) {
                            val loan = document.toObject(PaymentLoanModel::class.java)

                            if (loan != null) {
                                tempList.add(Pair(document.id, loan))
                            }
                        }
                    }

                    tempList.sortWith(
                        compareBy<Pair<String, PaymentLoanModel>> {
                            if (it.second.status == "paid") 1 else 0
                        }.thenByDescending {
                            it.second.createdAt
                        }
                    )

                    loanList.addAll(tempList)

                    adapter.notifyDataSetChanged()
                }
        }
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->

                    val language =
                        document.getString("language") ?: "en"

                    adapter.updateLanguage(language)

                    backButton.text =
                        if (language == "fr") "Retour" else "Back"
                }
        }
    }
}