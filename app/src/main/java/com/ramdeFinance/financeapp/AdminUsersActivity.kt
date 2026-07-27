package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private lateinit var userList:
            MutableList<Pair<String, AdminUserModel>>

    private lateinit var adapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_users)

        val backButton =
            findViewById<Button>(R.id.btnBack)

        recyclerView =
            findViewById(R.id.recyclerAdminUsers)

        progressBar =
            findViewById(R.id.progressAdminUsers)

        emptyText =
            findViewById(R.id.tvAdminUsersEmpty)

        backButton.setOnClickListener {
            finish()
        }

        userList = mutableListOf()
        adapter = AdminUserAdapter(userList)

        recyclerView.adapter = adapter

        ListStateHelper.showLoading(
            recyclerView = recyclerView,
            progressBar = progressBar,
            emptyText = emptyText
        )

        loadUsers()
    }

    private fun loadUsers() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    ListStateHelper.showError(
                        recyclerView = recyclerView,
                        progressBar = progressBar,
                        emptyText = emptyText,
                        message = getString(
                            R.string.admin_users_load_failed
                        )
                    )

                    return@addSnapshotListener
                }

                userList.clear()

                snapshots?.documents?.forEach { document ->
                    val user =
                        document.toObject(
                            AdminUserModel::class.java
                        )

                    if (user != null) {
                        userList.add(
                            Pair(
                                document.id,
                                user
                            )
                        )
                    }
                }

                adapter.notifyDataSetChanged()

                if (userList.isEmpty()) {
                    ListStateHelper.showEmpty(
                        recyclerView = recyclerView,
                        progressBar = progressBar,
                        emptyText = emptyText,
                        message = getString(
                            R.string.admin_users_empty
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