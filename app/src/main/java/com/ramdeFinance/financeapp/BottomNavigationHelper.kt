package com.ramdefinance.financeapp

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavigationHelper {

    fun setup(
        activity: Activity,
        bottomNavigation: BottomNavigationView,
        selectedItemId: Int,
        onHomeReselected: (() -> Unit)? = null
    ) {
        syncSelection(
            bottomNavigation = bottomNavigation,
            selectedItemId = selectedItemId
        )

        bottomNavigation.setOnItemSelectedListener { item ->

            if (item.itemId == selectedItemId) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.navigationHome -> {
                    openActivity(
                        currentActivity = activity,
                        destination = DashboardActivity::class.java
                    )
                    true
                }

                R.id.navigationLoans -> {
                    openActivity(
                        currentActivity = activity,
                        destination = LoanHistoryActivity::class.java
                    )
                    true
                }

                R.id.navigationNotifications -> {
                    openActivity(
                        currentActivity = activity,
                        destination = NotificationsActivity::class.java
                    )
                    true
                }

                R.id.navigationProfile -> {
                    openActivity(
                        currentActivity = activity,
                        destination = ProfileActivity::class.java
                    )
                    true
                }

                else -> false
            }
        }

        bottomNavigation.setOnItemReselectedListener { item ->
            if (
                item.itemId == R.id.navigationHome &&
                selectedItemId == R.id.navigationHome
            ) {
                onHomeReselected?.invoke()
            }
        }
    }

    fun syncSelection(
        bottomNavigation: BottomNavigationView,
        selectedItemId: Int
    ) {
        bottomNavigation.menu
            .findItem(selectedItemId)
            ?.isChecked = true
    }

    private fun openActivity(
        currentActivity: Activity,
        destination: Class<out Activity>
    ) {
        if (currentActivity::class.java == destination) {
            return
        }

        val intent = Intent(
            currentActivity,
            destination
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }

        currentActivity.startActivity(intent)
        currentActivity.overridePendingTransition(0, 0)
    }
}