package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userList: MutableList<Pair<String, AdminUserModel>>
    private lateinit var adapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_users)

        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerAdminUsers)

        userList = mutableListOf()
        adapter = AdminUserAdapter(userList)

        recyclerView.adapter = adapter

        FirebaseFirestore.getInstance()
            .collection("users")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                userList.clear()

                if (snapshots != null) {
                    for (document in snapshots.documents) {
                        val user = document.toObject(AdminUserModel::class.java)

                        if (user != null) {
                            userList.add(Pair(document.id, user))
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }
}