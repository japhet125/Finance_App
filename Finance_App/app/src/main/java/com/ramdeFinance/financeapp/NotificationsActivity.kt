package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationList: MutableList<NotificationModel>
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerNotifications)

        notificationList = mutableListOf()
        adapter = NotificationAdapter(notificationList)

        recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            db.collection("notifications")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshots, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    notificationList.clear()

                    if (snapshots != null) {
                        for (document in snapshots.documents) {
                            val notification =
                                document.toObject(NotificationModel::class.java)

                            if (notification != null) {
                                notificationList.add(notification)

                                if (!notification.isRead) {
                                    db.collection("notifications")
                                        .document(document.id)
                                        .update("isRead", true)
                                        .addOnFailureListener { e ->
                                            android.widget.Toast.makeText(
                                                this,
                                                "Read update failed: ${e.message}",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                            }
                        }
                    }

                    adapter.notifyDataSetChanged()
                }
        }
    }
}