package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView


class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var allLoansList: MutableList<Pair<String, AdminLoanModel>>

    private lateinit var recyclerView: RecyclerView
    private lateinit var loanList: MutableList<Pair<String, AdminLoanModel>>
    private lateinit var adapter: AdminLoanAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)
        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }
        recyclerView = findViewById(R.id.recyclerAdminLoans)

        allLoansList = mutableListOf()
        loanList = mutableListOf()
        adapter = AdminLoanAdapter(loanList)
        var userLanguage = "en"

        recyclerView.adapter = adapter
        val paymentReviewButton = findViewById<Button>(R.id.btnPaymentReview)
        val pendingLoansText = findViewById<TextView>(R.id.txtPendingLoans)
        val pendingIdentityText = findViewById<TextView>(R.id.txtPendingIdentity)
        val pendingPaymentsText = findViewById<TextView>(R.id.txtPendingPayments)

        paymentReviewButton.setOnClickListener {
            val intent = Intent(this, AdminPaymentReviewActivity::class.java)
            startActivity(intent)
        }
        val adminMenuButton = findViewById<ImageButton>(R.id.btnAdminMenu)

        adminMenuButton.setOnClickListener {
            val popupMenu = PopupMenu(this, adminMenuButton)

            val auditLogsText =
                if (userLanguage == "fr") "Journaux d'audit" else "Audit Logs"

            val usersText =
                if (userLanguage == "fr") "Utilisateurs" else "Users"

            val analyticsText =
                if (userLanguage == "fr") "Analytique" else "Analytics"

            val identityText =
                if (userLanguage == "fr") "Vérification d'identité" else "Identity Verification"

            popupMenu.menu.add(auditLogsText)
            popupMenu.menu.add(usersText)
            popupMenu.menu.add(analyticsText)
            popupMenu.menu.add(identityText)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.title.toString()) {
                    auditLogsText -> {
                        startActivity(Intent(this, AuditLogActivity::class.java))
                        true
                    }
                    usersText -> {
                        startActivity(Intent(this, AdminUsersActivity::class.java))
                        true
                    }
                    analyticsText -> {
                        startActivity(Intent(this, AdminAnalyticActivity::class.java))
                        true
                    }
                    identityText -> {
                        startActivity(Intent(this, IdentityVerificationActivity::class.java))
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }

        val searchInput = findViewById<EditText>(R.id.etSearchLoans)

        searchInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {

                val query = s.toString().lowercase().trim()

                loanList.clear()

                if (query.isBlank()) {

                    loanList.addAll(allLoansList)

                } else {

                    val filtered = allLoansList.filter { pair ->

                        val loan = pair.second

                        loan.status.lowercase().contains(query) ||
                                loan.reason.lowercase().contains(query) ||
                                loan.amount.lowercase().contains(query) ||
                                pair.first.lowercase().contains(query)
                    }

                    loanList.addAll(filtered)
                }

                adapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })



        val db = FirebaseFirestore.getInstance()
        val currentUserId =
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->

                    userLanguage =
                        document.getString("language") ?: "en"

                    adapter.updateLanguage(userLanguage)

                    backButton.text =
                        if (userLanguage == "fr") "Retour" else "Back"

                    searchInput.hint =
                        if (userLanguage == "fr") {
                            "Rechercher des prêts"
                        } else {
                            "Search loans"
                        }
                    loadAdminSummaryCards(
                        db,
                        userLanguage,
                        pendingLoansText,
                        pendingIdentityText,
                        pendingPaymentsText
                    )
                }
        } else {
            loadAdminSummaryCards(
                db,
                "en",
                pendingLoansText,
                pendingIdentityText,
                pendingPaymentsText
            )
        }

        db.collection("loan_requests")
            .orderBy(
                "createdAt",
                com.google.firebase.firestore.Query.Direction.DESCENDING
            )
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                allLoansList.clear()
                loanList.clear()

                if (snapshots != null) {
                    for (document in snapshots.documents) {
                        val loan = document.toObject(AdminLoanModel::class.java)

                        if (loan != null) {
                            val pair = Pair(document.id, loan)
                            allLoansList.add(pair)
                            loanList.add(pair)
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }

    }
    private fun loadAdminSummaryCards(
        db: FirebaseFirestore,
        language: String,
        pendingLoansText: TextView,
        pendingIdentityText: TextView,
        pendingPaymentsText: TextView
    ) {
        db.collection("loan_requests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, _ ->
                pendingLoansText.text =
                    if (language == "fr")
                        "Prêts en attente : ${snapshots?.size() ?: 0}"
                    else
                        "Pending Loans: ${snapshots?.size() ?: 0}"
            }

        db.collection("users")
            .whereEqualTo("identityStatus", "pending")
            .addSnapshotListener { snapshots, _ ->
                pendingIdentityText.text =
                    if (language == "fr")
                        "Identités en attente : ${snapshots?.size() ?: 0}"
                    else
                        "Pending IDs: ${snapshots?.size() ?: 0}"
            }

        db.collection("transactions")
            .whereEqualTo("status", "pending")
            .whereEqualTo("paymentType", "loan_repayment")
            .addSnapshotListener { snapshots, _ ->
                pendingPaymentsText.text =
                    if (language == "fr")
                        "Paiements en attente : ${snapshots?.size() ?: 0}"
                    else
                        "Pending Payments: ${snapshots?.size() ?: 0}"
            }
    }

}