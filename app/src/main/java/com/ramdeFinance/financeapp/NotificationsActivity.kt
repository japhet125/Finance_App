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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class NotificationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private lateinit var notificationList:
            MutableList<NotificationModel>

    private lateinit var adapter: NotificationAdapter
    private lateinit var bottomNavigation: BottomNavigationView

    private val auth =
        FirebaseAuth.getInstance()

    private val db =
        FirebaseFirestore.getInstance()

    private var notificationListener:
            ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        setupBottomNavigation()
        setupViews()
        setupRecyclerView()

        val userId =
            auth.currentUser?.uid

        if (userId == null) {
            finish()
            return
        }

        loadUserLanguage(userId)
        loadNotifications(userId)
    }

    private fun setupBottomNavigation() {
        bottomNavigation =
            findViewById(R.id.bottomNavigation)

        BottomNavigationHelper.setup(
            activity = this,
            bottomNavigation = bottomNavigation,
            selectedItemId = R.id.navigationNotifications
        )
    }

    private fun setupViews() {
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
    }

    private fun setupRecyclerView() {
        notificationList = mutableListOf()
        adapter = NotificationAdapter(notificationList)

        recyclerView.adapter = adapter

        ListStateHelper.showLoading(
            recyclerView = recyclerView,
            progressBar = progressBar,
            emptyText = emptyText
        )
    }

    private fun loadUserLanguage(
        userId: String
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                if (auth.currentUser?.uid != userId) {
                    return@addOnSuccessListener
                }

                val language =
                    document.getString("language") ?: "en"

                adapter.updateLanguage(language)
            }
    }

    private fun loadNotifications(
        userId: String
    ) {
        notificationListener?.remove()

        notificationListener =
            db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy(
                    "timestamp",
                    Query.Direction.DESCENDING
                )
                .addSnapshotListener { snapshots, error ->

                    if (error != null) {
                        if (
                            auth.currentUser == null ||
                            isFinishing ||
                            isDestroyed
                        ) {
                            return@addSnapshotListener
                        }

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
                                    notificationId = document.id,
                                    userId = userId
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
        notificationId: String,
        userId: String
    ) {
        if (auth.currentUser?.uid != userId) {
            return
        }

        db.collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .addOnFailureListener {

                if (
                    auth.currentUser == null ||
                    isFinishing ||
                    isDestroyed
                ) {
                    return@addOnFailureListener
                }

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

        val userId =
            auth.currentUser?.uid

        if (
            userId != null &&
            notificationListener == null
        ) {
            loadNotifications(userId)
        }
    }

    override fun onStop() {
        notificationListener?.remove()
        notificationListener = null

        super.onStop()
    }
}