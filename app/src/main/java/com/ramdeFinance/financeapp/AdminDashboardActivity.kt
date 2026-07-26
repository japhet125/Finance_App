package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var allLoansList:
            MutableList<Pair<String, AdminLoanModel>>

    private lateinit var loanList:
            MutableList<Pair<String, AdminLoanModel>>

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminLoanAdapter

    private var userLanguage = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val backButton =
            findViewById<Button>(R.id.btnBack)

        val paymentReviewButton =
            findViewById<Button>(R.id.btnPaymentReview)

        val adminMenuButton =
            findViewById<ImageButton>(R.id.btnAdminMenu)

        val searchInput =
            findViewById<EditText>(R.id.etSearchLoans)

        val pendingLoansText =
            findViewById<TextView>(R.id.txtPendingLoans)

        val pendingIdentityText =
            findViewById<TextView>(R.id.txtPendingIdentity)

        val pendingPaymentsText =
            findViewById<TextView>(R.id.txtPendingPayments)

        recyclerView =
            findViewById(R.id.recyclerAdminLoans)

        allLoansList = mutableListOf()
        loanList = mutableListOf()

        adapter = AdminLoanAdapter(loanList)
        recyclerView.adapter = adapter

        backButton.setOnClickListener {
            finish()
        }

        paymentReviewButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AdminPaymentReviewActivity::class.java
                )
            )
        }

        adminMenuButton.setOnClickListener {
            showAdminMenu(adminMenuButton)
        }

        setupSearch(searchInput)

        val db = FirebaseFirestore.getInstance()

        loadAdminLanguage(
            db = db,
            pendingLoansText = pendingLoansText,
            pendingIdentityText = pendingIdentityText,
            pendingPaymentsText = pendingPaymentsText
        )

        loadLoans(db)
    }

    private fun showAdminMenu(
        adminMenuButton: ImageButton
    ) {
        val popupMenu =
            PopupMenu(this, adminMenuButton)

        popupMenu.menu.add(
            0,
            MENU_AUDIT_LOGS,
            0,
            getString(R.string.audit_logs_title)
        )

        popupMenu.menu.add(
            0,
            MENU_USERS,
            1,
            getString(R.string.admin_users_title)
        )

        popupMenu.menu.add(
            0,
            MENU_ANALYTICS,
            2,
            getString(R.string.admin_analytics_title)
        )

        popupMenu.menu.add(
            0,
            MENU_IDENTITY,
            3,
            getString(R.string.identity_verification_title)
        )

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_AUDIT_LOGS -> {
                    startActivity(
                        Intent(
                            this,
                            AuditLogActivity::class.java
                        )
                    )
                    true
                }

                MENU_USERS -> {
                    startActivity(
                        Intent(
                            this,
                            AdminUsersActivity::class.java
                        )
                    )
                    true
                }

                MENU_ANALYTICS -> {
                    startActivity(
                        Intent(
                            this,
                            AdminAnalyticActivity::class.java
                        )
                    )
                    true
                }

                MENU_IDENTITY -> {
                    startActivity(
                        Intent(
                            this,
                            IdentityVerificationActivity::class.java
                        )
                    )
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun setupSearch(
        searchInput: EditText
    ) {
        searchInput.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    text: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // No action required.
                }

                override fun onTextChanged(
                    text: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    filterLoans(
                        text
                            ?.toString()
                            .orEmpty()
                            .trim()
                            .lowercase()
                    )
                }

                override fun afterTextChanged(
                    text: Editable?
                ) {
                    // No action required.
                }
            }
        )
    }

    private fun filterLoans(query: String) {
        loanList.clear()

        if (query.isBlank()) {
            loanList.addAll(allLoansList)
        } else {
            val filteredLoans =
                allLoansList.filter { pair ->
                    val loan = pair.second

                    loan.status
                        .lowercase()
                        .contains(query) ||
                            loan.reason
                                .lowercase()
                                .contains(query) ||
                            loan.amount
                                .lowercase()
                                .contains(query) ||
                            pair.first
                                .lowercase()
                                .contains(query)
                }

            loanList.addAll(filteredLoans)
        }

        adapter.notifyDataSetChanged()
    }

    private fun loadAdminLanguage(
        db: FirebaseFirestore,
        pendingLoansText: TextView,
        pendingIdentityText: TextView,
        pendingPaymentsText: TextView
    ) {
        val currentUserId =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid

        if (currentUserId == null) {
            adapter.updateLanguage(userLanguage)

            loadAdminSummaryCards(
                db = db,
                pendingLoansText = pendingLoansText,
                pendingIdentityText = pendingIdentityText,
                pendingPaymentsText = pendingPaymentsText
            )

            return
        }

        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                userLanguage =
                    document.getString("language") ?: "en"

                adapter.updateLanguage(userLanguage)

                loadAdminSummaryCards(
                    db = db,
                    pendingLoansText = pendingLoansText,
                    pendingIdentityText = pendingIdentityText,
                    pendingPaymentsText = pendingPaymentsText
                )
            }
            .addOnFailureListener {
                adapter.updateLanguage(userLanguage)

                loadAdminSummaryCards(
                    db = db,
                    pendingLoansText = pendingLoansText,
                    pendingIdentityText = pendingIdentityText,
                    pendingPaymentsText = pendingPaymentsText
                )
            }
    }

    private fun loadAdminSummaryCards(
        db: FirebaseFirestore,
        pendingLoansText: TextView,
        pendingIdentityText: TextView,
        pendingPaymentsText: TextView
    ) {
        db.collection("loan_requests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                pendingLoansText.text =
                    getString(
                        R.string.pending_loans_count,
                        snapshots?.size() ?: 0
                    )
            }

        db.collection("users")
            .whereEqualTo("identityStatus", "pending")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                pendingIdentityText.text =
                    getString(
                        R.string.pending_identities_count,
                        snapshots?.size() ?: 0
                    )
            }

        db.collection("transactions")
            .whereEqualTo("status", "pending")
            .whereEqualTo(
                "paymentType",
                "loan_repayment"
            )
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                pendingPaymentsText.text =
                    getString(
                        R.string.pending_payments_count,
                        snapshots?.size() ?: 0
                    )
            }
    }

    private fun loadLoans(
        db: FirebaseFirestore
    ) {
        db.collection("loan_requests")
            .orderBy(
                "createdAt",
                Query.Direction.DESCENDING
            )
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                allLoansList.clear()
                loanList.clear()

                snapshots?.documents?.forEach { document ->
                    val loan =
                        document.toObject(
                            AdminLoanModel::class.java
                        )

                    if (loan != null) {
                        val loanPair =
                            Pair(document.id, loan)

                        allLoansList.add(loanPair)
                        loanList.add(loanPair)
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }

    companion object {
        private const val MENU_AUDIT_LOGS = 1
        private const val MENU_USERS = 2
        private const val MENU_ANALYTICS = 3
        private const val MENU_IDENTITY = 4
    }
}