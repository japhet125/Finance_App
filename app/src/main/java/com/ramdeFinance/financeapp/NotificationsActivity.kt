package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private lateinit var notificationList:
            MutableList<NotificationModel>

    private lateinit var adapter: NotificationAdapter
    private lateinit var bottomNavigation: BottomNavigationView

    private val db =
        FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        bottomNavigation =
            findViewById(R.id.bottomNavigation)

        BottomNavigationHelper.setup(
            activity = this,
            bottomNavigation = bottomNavigation,
            selectedItemId = R.id.navigationNotifications
        )

        val backButton =
            findViewById<Button>(R.id.btnBack)

        recyclerView =
            findViewById(R.id.recyclerNotifications)

        progressBar =
            findViewById(R.id.progressNotifications)

        emptyText =
            findViewById(R.id.tvNotificationsEmpty)

        backButton.setOnClickListener {
            finish()
        }

        notificationList = mutableListOf()
        adapter = NotificationAdapter(notificationList)

        recyclerView.adapter = adapter

        ListStateHelper.showLoading(
            recyclerView = recyclerView,
            progressBar = progressBar,
            emptyText = emptyText
        )

        val userId =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid

        if (userId == null) {
            ListStateHelper.showError(
                recyclerView = recyclerView,
                progressBar = progressBar,
                emptyText = emptyText,
                message = getString(
                    R.string.user_not_signed_in
                )
            )

            return
        }

        loadUserLanguage(
            userId = userId
        )

        loadNotifications(
            userId = userId
        )
    }

    private fun loadUserLanguage(
        userId: String
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                val language =
                    document.getString("language") ?: "en"

                adapter.updateLanguage(language)
            }
    }

    private fun loadNotifications(
        userId: String
    ) {
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy(
                "timestamp",
                Query.Direction.DESCENDING
            )
            .addSnapshotListener { snapshots, error ->

                if (error != null) {

                    error.printStackTrace()

                    Toast.makeText(
                        this,
                        error.message,
                        Toast.LENGTH_LONG
                    ).show()

                    ListStateHelper.showError(
                        recyclerView = recyclerView,
                        progressBar = progressBar,
                        emptyText = emptyText,
                        message = getString(
                            R.string.notifications_load_failed
                        )
                    )

                    return@addSnapshotListener
                }

                notificationList.clear()

                snapshots?.documents?.forEach { document ->

                    val notification =
                        document.toObject(
                            NotificationModel::class.java
                        )

                    if (notification != null) {
                        notificationList.add(notification)

                        if (!notification.isRead) {
                            markNotificationAsRead(
                                notificationId = document.id
                            )
                        }
                    }
                }

                adapter.notifyDataSetChanged()

                if (notificationList.isEmpty()) {
                    ListStateHelper.showEmpty(
                        recyclerView = recyclerView,
                        progressBar = progressBar,
                        emptyText = emptyText,
                        message = getString(
                            R.string.notifications_empty
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

    private fun markNotificationAsRead(
        notificationId: String
    ) {
        db.collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(
                        R.string.notification_read_update_failed
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onResume() {
        super.onResume()

        if (::bottomNavigation.isInitialized) {
            BottomNavigationHelper.syncSelection(
                bottomNavigation = bottomNavigation,
                selectedItemId = R.id.navigationNotifications
            )
        }
    }
}