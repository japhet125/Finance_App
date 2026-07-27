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
import android.content.Intent
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView

class LoanHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var loanList: MutableList<LoanModel>
    private lateinit var adapter: LoanAdapter
    private lateinit var allLoans: MutableList<LoanModel>
    private var selectedFilter = FILTER_ALL
    private var searchQuery = ""
    private var selectedSort = SORT_NEWEST

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_history)
        bottomNavigation =
            findViewById(R.id.bottomNavigation)

        BottomNavigationHelper.setup(
            activity = this,
            bottomNavigation = bottomNavigation,
            selectedItemId = R.id.navigationLoans
        )
        val backButton = findViewById<Button>(R.id.btnBack)
        val downloadButton = findViewById<Button>(R.id.btnDownloadStatement)

        downloadButton.setOnClickListener {
            startActivity(Intent(this, LoanStatementActivity::class.java))
        }

        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerLoans)
        progressBar = findViewById(R.id.progressLoanHistory)
        emptyText = findViewById(R.id.tvLoanHistoryEmpty)
        ListStateHelper.showLoading(
            recyclerView = recyclerView,
            progressBar = progressBar,
            emptyText = emptyText
        )

        loanList = mutableListOf()
        allLoans = mutableListOf()
        adapter = LoanAdapter(loanList)

        recyclerView.adapter = adapter

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        val filterSpinner = findViewById<Spinner>(R.id.spinnerLoanFilter)

        val filterLabels = listOf(
            getString(R.string.filter_all),
            getString(R.string.filter_pending),
            getString(R.string.filter_approved),
            getString(R.string.filter_rejected),
            getString(R.string.filter_overdue),
            getString(R.string.filter_paid)
        )

        val filterValues = listOf(
            FILTER_ALL,
            FILTER_PENDING,
            FILTER_APPROVED,
            FILTER_REJECTED,
            FILTER_OVERDUE,
            FILTER_PAID
        )

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filterLabels
        )

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = spinnerAdapter

        val sortSpinner = findViewById<Spinner>(R.id.spinnerLoanSort)

        val sortOptions = listOf(
            getString(R.string.newest_first),
            getString(R.string.oldest_first),
            getString(R.string.highest_amount),
            getString(R.string.lowest_amount)
        )


        val sortAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sortOptions
        )

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter

        sortSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    selectedSort = when (position) {
                        0 -> SORT_NEWEST
                        1 -> SORT_OLDEST
                        2 -> SORT_HIGHEST
                        3 -> SORT_LOWEST
                        else -> SORT_NEWEST
                    }

                    applyLoanFilter()
                }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        filterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedFilter = filterValues[position]
                    applyLoanFilter()
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {

                }
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

                        ListStateHelper.showError(
                            recyclerView = recyclerView,
                            progressBar = progressBar,
                            emptyText = emptyText,
                            message = getString(R.string.loan_history_load_failed)
                        )

                        Toast.makeText(
                            this,
                            getString(R.string.loan_history_load_failed),
                            Toast.LENGTH_SHORT
                        ).show()

                        return@addSnapshotListener
                    }
                    allLoans.clear()

                    if (snapshots != null) {

                        for (document in snapshots.documents) {

                            val loan = document.toObject(LoanModel::class.java)

                            if (loan != null) {
                                val loanWithId =
                                    loan.copy(loanId = document.id)

                                allLoans.add(loanWithId)
                            }

                            if (loan != null) {

                                val remainingBalance =
                                    parseMoney(loan.remainingBalance)

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


                            }
                        }

                        applyLoanFilter()
                    }
                }
        }
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val language = document.getString("language") ?: "en"
                    adapter.updateLanguage(language)
                }
        }
    }
    private fun applyLoanFilter() {
        loanList.clear()

        var filteredLoans = allLoans.toList()

        if (selectedFilter != FILTER_ALL) {
            filteredLoans = filteredLoans.filter {
                it.status.equals(
                    selectedFilter,
                    ignoreCase = true
                )
            }
        }
        if (searchQuery.isNotBlank()) {
            filteredLoans = filteredLoans.filter {
                it.reason.contains(searchQuery, ignoreCase = true)
            }
        }

        filteredLoans = when (selectedSort) {

            SORT_NEWEST ->
                filteredLoans.sortedByDescending {
                    it.createdAt
                }

            SORT_OLDEST ->
                filteredLoans.sortedBy {
                    it.createdAt
                }

            SORT_HIGHEST ->
                filteredLoans.sortedByDescending {
                    parseMoney(it.amount)
                }

            SORT_LOWEST ->
                filteredLoans.sortedBy {
                    parseMoney(it.amount)
                }

            else -> filteredLoans
        }

        loanList.addAll(filteredLoans)
        adapter.notifyDataSetChanged()

        when {
            allLoans.isEmpty() -> {
                ListStateHelper.showEmpty(
                    recyclerView = recyclerView,
                    progressBar = progressBar,
                    emptyText = emptyText,
                    message = getString(R.string.loan_history_empty)
                )
            }

            loanList.isEmpty() -> {
                ListStateHelper.showEmpty(
                    recyclerView = recyclerView,
                    progressBar = progressBar,
                    emptyText = emptyText,
                    message = getString(R.string.loan_history_no_results)
                )
            }

            else -> {
                ListStateHelper.showContent(
                    recyclerView = recyclerView,
                    progressBar = progressBar,
                    emptyText = emptyText
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()

        if (::bottomNavigation.isInitialized) {
            BottomNavigationHelper.syncSelection(
                bottomNavigation = bottomNavigation,
                selectedItemId = R.id.navigationLoans
            )
        }
    }
    private fun parseMoney(value: String): Double {
        return value
            .replace("$", "")
            .replace("F CFA", "")
            .replace("FCFA", "")
            .replace("CFA", "")
            .replace(" ", "")
            .replace(",", ".")
            .trim()
            .toDoubleOrNull() ?: 0.0
    }
    private companion object {

        const val SORT_NEWEST = "newest"
        const val SORT_OLDEST = "oldest"
        const val SORT_HIGHEST = "highest"
        const val SORT_LOWEST = "lowest"

        const val FILTER_ALL = "all"
        const val FILTER_PENDING = "pending"
        const val FILTER_APPROVED = "approved"
        const val FILTER_REJECTED = "rejected"
        const val FILTER_OVERDUE = "overdue"
        const val FILTER_PAID = "paid"
    }

}