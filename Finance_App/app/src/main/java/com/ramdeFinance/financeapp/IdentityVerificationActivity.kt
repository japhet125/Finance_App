package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class IdentityVerificationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userList: MutableList<Pair<String, IdentityUserModel>>
    private lateinit var adapter: IdentityVerificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_verification)

        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerIdentityUsers)

        userList = mutableListOf()
        adapter = IdentityVerificationAdapter(userList)

        recyclerView.adapter = adapter

        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("identityStatus", "pending")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                userList.clear()

                if (snapshots != null) {
                    for (document in snapshots.documents) {
                        val user = document.toObject(IdentityUserModel::class.java)

                        if (user != null) {
                            userList.add(Pair(document.id, user))
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }
}