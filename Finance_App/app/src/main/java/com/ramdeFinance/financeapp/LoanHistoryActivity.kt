package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.AdapterView
import android.view.View
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class LoanHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loanList: MutableList<LoanModel>
    private lateinit var adapter: LoanAdapter
    private lateinit var allLoans: MutableList<LoanModel>
    private var selectedFilter = "All"
    private var searchQuery = ""
    private var selectedSort = "Newest First"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_history)
        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerLoans)

        loanList = mutableListOf()
        allLoans = mutableListOf()
        adapter = LoanAdapter(loanList)

        recyclerView.adapter = adapter

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        val filterSpinner = findViewById<Spinner>(R.id.spinnerLoanFilter)

        val filters = listOf("All", "Pending", "Approved", "Rejected", "Overdue", "Paid")

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filters
        )

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = spinnerAdapter

        val sortSpinner = findViewById<Spinner>(R.id.spinnerLoanSort)

        val sortOptions = listOf(
            "Newest First",
            "Oldest First",
            "Highest Amount",
            "Lowest Amount"
        )

        val sortAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sortOptions
        )

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedSort = sortOptions[position]
                applyLoanFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedFilter = filters[position]
                applyLoanFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val searchInput = findViewById<EditText>(R.id.etLoanSearch)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                searchQuery = s.toString()
                applyLoanFilter()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        if (userId != null) {

            db.collection("loan_requests")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshots, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    allLoans.clear()

                    if (snapshots != null) {

                        for (document in snapshots.documents) {

                            val loan = document.toObject(LoanModel::class.java)

                            if (loan != null) {

                                val remainingBalance =
                                    loan.remainingBalance
                                        .replace("$", "")
                                        .replace(",", "")
                                        .trim()
                                        .toDoubleOrNull() ?: 0.0

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
                                                "message" to "Your loan is overdue. Please make a payment as soon as possible.",
                                                "timestamp" to System.currentTimeMillis(),
                                                "isRead" to false
                                            )

                                            db.collection("notifications").add(notification)

                                            val userRef = db.collection("users").document(userId)

                                            userRef.get().addOnSuccessListener { userDocument ->
                                                val currentScore =
                                                    userDocument.getLong("creditScore") ?: 500

                                                userRef.update(
                                                    "creditScore",
                                                    currentScore - 25
                                                )
                                            }

                                            val auditLog = hashMapOf(
                                                "actorId" to "system",
                                                "action" to "loan_overdue",
                                                "targetType" to "loan_request",
                                                "targetId" to document.id,
                                                "message" to "Loan ${document.id} was marked overdue.",
                                                "timestamp" to System.currentTimeMillis()
                                            )

                                            db.collection("audit_logs").add(auditLog)
                                        }
                                }

                                allLoans.add(loan)
                            }
                        }

                        applyLoanFilter()
                    }
                }
        }
    }
    private fun applyLoanFilter() {
        loanList.clear()

        var filteredLoans = allLoans.toList()

        if (selectedFilter != "All") {
            filteredLoans = filteredLoans.filter {
                it.status.equals(selectedFilter, ignoreCase = true)
            }
        }

        if (searchQuery.isNotBlank()) {
            filteredLoans = filteredLoans.filter {
                it.reason.contains(searchQuery, ignoreCase = true)
            }
        }

        filteredLoans = when (selectedSort) {
            "Newest First" -> filteredLoans.sortedByDescending { it.createdAt }
            "Oldest First" -> filteredLoans.sortedBy { it.createdAt }
            "Highest Amount" -> filteredLoans.sortedByDescending {
                it.amount.toDoubleOrNull() ?: 0.0
            }
            "Lowest Amount" -> filteredLoans.sortedBy {
                it.amount.toDoubleOrNull() ?: 0.0
            }
            else -> filteredLoans
        }

        loanList.addAll(filteredLoans)
        adapter.notifyDataSetChanged()
    }

}