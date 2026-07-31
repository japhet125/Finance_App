package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoanHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private lateinit var loanList: MutableList<LoanModel>
    private lateinit var allLoans: MutableList<LoanModel>
    private lateinit var adapter: LoanAdapter

    private lateinit var bottomNavigation: BottomNavigationView

    private var selectedFilter = FILTER_ALL
    private var selectedSort = SORT_NEWEST
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_history)

        setupBottomNavigation()
        setupButtons()
        setupRecyclerView()
        setupSpinners()
        setupSearch()
        loadLoans()
        loadUserLanguage()
    }

    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation)

        BottomNavigationHelper.setup(
            activity = this,
            bottomNavigation = bottomNavigation,
            selectedItemId = R.id.navigationLoans
        )
    }

    private fun setupButtons() {
        val backButton =
            findViewById<MaterialButton>(R.id.btnBack)

        val downloadButton =
            findViewById<MaterialButton>(R.id.btnDownloadStatement)

        backButton.setOnClickListener {
            finish()
        }

        downloadButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    LoanStatementActivity::class.java
                )
            )
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerLoans)
        progressBar = findViewById(R.id.progressLoanHistory)
        emptyText = findViewById(R.id.tvLoanHistoryEmpty)

        loanList = mutableListOf()
        allLoans = mutableListOf()

        adapter = LoanAdapter(loanList)
        recyclerView.adapter = adapter

        ListStateHelper.showLoading(
            recyclerView = recyclerView,
            progressBar = progressBar,
            emptyText = emptyText
        )
    }

    private fun setupSpinners() {
        val filterSpinner =
            findViewById<Spinner>(R.id.spinnerLoanFilter)

        val sortSpinner =
            findViewById<Spinner>(R.id.spinnerLoanSort)

        setupFilterSpinner(filterSpinner)
        setupSortSpinner(sortSpinner)
    }

    private fun setupFilterSpinner(filterSpinner: Spinner) {
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

        val filterAdapter = ArrayAdapter(
            this,
            R.layout.item_spinner_selected,
            filterLabels
        )

        filterAdapter.setDropDownViewResource(
            R.layout.item_spinner_dropdown
        )

        filterSpinner.adapter = filterAdapter

        filterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedFilter =
                        filterValues.getOrElse(position) {
                            FILTER_ALL
                        }

                    applyLoanFilter()
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    // No action needed.
                }
            }
    }

    private fun setupSortSpinner(sortSpinner: Spinner) {
        val sortOptions = listOf(
            getString(R.string.newest_first),
            getString(R.string.oldest_first),
            getString(R.string.highest_amount),
            getString(R.string.lowest_amount)
        )

        val sortAdapter = ArrayAdapter(
            this,
            R.layout.item_spinner_selected,
            sortOptions
        )

        sortAdapter.setDropDownViewResource(
            R.layout.item_spinner_dropdown
        )

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

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    // No action needed.
                }
            }
    }

    private fun setupSearch() {
        val searchInput =
            findViewById<EditText>(R.id.etLoanSearch)

        searchInput.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    text: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // No action needed.
                }

                override fun onTextChanged(
                    text: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    searchQuery = text?.toString().orEmpty()
                    applyLoanFilter()
                }

                override fun afterTextChanged(
                    editable: Editable?
                ) {
                    // No action needed.
                }
            }
        )
    }

    private fun loadLoans() {
        val userId =
            FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            ListStateHelper.showError(
                recyclerView = recyclerView,
                progressBar = progressBar,
                emptyText = emptyText,
                message = getString(
                    R.string.loan_history_load_failed
                )
            )

            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("loan_requests")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    ListStateHelper.showError(
                        recyclerView = recyclerView,
                        progressBar = progressBar,
                        emptyText = emptyText,
                        message = getString(
                            R.string.loan_history_load_failed
                        )
                    )

                    Toast.makeText(
                        this,
                        getString(
                            R.string.loan_history_load_failed
                        ),
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addSnapshotListener
                }

                allLoans.clear()

                snapshots?.documents?.forEach { document ->
                    val loan =
                        document.toObject(LoanModel::class.java)

                    if (loan != null) {
                        val loanWithId =
                            loan.copy(
                                loanId = document.id
                            )

                        allLoans.add(loanWithId)

                        checkAndMarkLoanOverdue(
                            db = db,
                            userId = userId,
                            documentId = document.id,
                            loan = loan
                        )
                    }
                }

                applyLoanFilter()
            }
    }

    private fun checkAndMarkLoanOverdue(
        db: FirebaseFirestore,
        userId: String,
        documentId: String,
        loan: LoanModel
    ) {
        val remainingBalance =
            parseMoney(loan.remainingBalance)

        val isLate =
            loan.status == FILTER_APPROVED &&
                    loan.dueDate > 0 &&
                    System.currentTimeMillis() > loan.dueDate &&
                    remainingBalance > 0.0 &&
                    !loan.overduePenaltyApplied

        if (!isLate) {
            return
        }

        db.collection("loan_requests")
            .document(documentId)
            .update(
                mapOf(
                    "status" to FILTER_OVERDUE,
                    "overduePenaltyApplied" to true
                )
            )
            .addOnSuccessListener {
                createOverdueNotification(
                    db = db,
                    userId = userId
                )

                reduceUserCreditScore(
                    db = db,
                    userId = userId
                )

                createOverdueAuditLog(
                    db = db,
                    documentId = documentId
                )
            }
    }

    private fun createOverdueNotification(
        db: FirebaseFirestore,
        userId: String
    ) {
        val notification = hashMapOf(
            "userId" to userId,
            "title" to "Loan Overdue",
            "message" to
                    "Your loan is overdue. Please make a payment as soon as possible.",
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false
        )

        db.collection("notifications")
            .add(notification)
    }

    private fun reduceUserCreditScore(
        db: FirebaseFirestore,
        userId: String
    ) {
        val userReference =
            db.collection("users").document(userId)

        userReference.get()
            .addOnSuccessListener { userDocument ->
                val currentScore =
                    userDocument.getLong("creditScore")
                        ?: DEFAULT_CREDIT_SCORE

                val updatedScore =
                    (currentScore - OVERDUE_SCORE_PENALTY)
                        .coerceAtLeast(0)

                userReference.update(
                    "creditScore",
                    updatedScore
                )
            }
    }

    private fun createOverdueAuditLog(
        db: FirebaseFirestore,
        documentId: String
    ) {
        val auditLog = hashMapOf(
            "actorId" to "system",
            "action" to "loan_overdue",
            "targetType" to "loan_request",
            "targetId" to documentId,
            "message" to
                    "Loan $documentId was marked overdue.",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("audit_logs")
            .add(auditLog)
    }

    private fun loadUserLanguage() {
        val userId =
            FirebaseAuth.getInstance().currentUser?.uid
                ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val language =
                    document.getString("language") ?: "en"

                adapter.updateLanguage(language)
            }
    }

    private fun applyLoanFilter() {
        loanList.clear()

        var filteredLoans =
            allLoans.toList()

        if (selectedFilter != FILTER_ALL) {
            filteredLoans =
                filteredLoans.filter { loan ->
                    loan.status.equals(
                        selectedFilter,
                        ignoreCase = true
                    )
                }
        }

        if (searchQuery.isNotBlank()) {
            filteredLoans =
                filteredLoans.filter { loan ->
                    loan.reason.contains(
                        searchQuery,
                        ignoreCase = true
                    )
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

            else ->
                filteredLoans
        }

        loanList.addAll(filteredLoans)
        adapter.notifyDataSetChanged()

        updateListState()
    }

    private fun updateListState() {
        when {
            allLoans.isEmpty() -> {
                ListStateHelper.showEmpty(
                    recyclerView = recyclerView,
                    progressBar = progressBar,
                    emptyText = emptyText,
                    message = getString(
                        R.string.loan_history_empty
                    )
                )
            }

            loanList.isEmpty() -> {
                ListStateHelper.showEmpty(
                    recyclerView = recyclerView,
                    progressBar = progressBar,
                    emptyText = emptyText,
                    message = getString(
                        R.string.loan_history_no_results
                    )
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
            .toDoubleOrNull()
            ?: 0.0
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

        const val DEFAULT_CREDIT_SCORE = 500L
        const val OVERDUE_SCORE_PENALTY = 25L
    }
}