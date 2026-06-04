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

        recyclerView.adapter = adapter
        val adminMenuButton = findViewById<ImageButton>(R.id.btnAdminMenu)

        adminMenuButton.setOnClickListener {
            val popupMenu = PopupMenu(this, adminMenuButton)

            popupMenu.menu.add("Audit Logs")
            popupMenu.menu.add("Users")
            popupMenu.menu.add("Analytics")
            popupMenu.menu.add("Identity Verification")

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.title.toString()) {
                    "Audit Logs" -> {
                        startActivity(Intent(this, AuditLogActivity::class.java))
                        true
                    }
                    "Users" -> {
                        startActivity(Intent(this, AdminUsersActivity::class.java))
                        true
                    }

                    "Analytics" -> {
                        startActivity(Intent(this, AdminAnalyticActivity::class.java))
                        true
                    }

                    "Identity Verification" -> {
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

}