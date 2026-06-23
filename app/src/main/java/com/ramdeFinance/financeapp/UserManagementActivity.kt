package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class UserManagementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userList: MutableList<UserModel>
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        val backButton = findViewById<Button>(R.id.btnBack)
        backButton.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerUsers)

        userList = mutableListOf()
        adapter = UserAdapter(userList)
        recyclerView.adapter = adapter

        FirebaseFirestore.getInstance()
            .collection("users")
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                userList.clear()

                snapshots?.documents?.forEach { document ->
                    val user = document.toObject(UserModel::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }
}