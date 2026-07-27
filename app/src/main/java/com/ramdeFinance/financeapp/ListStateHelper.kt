package com.ramdefinance.financeapp

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

object ListStateHelper {

    fun showLoading(
        recyclerView: RecyclerView,
        progressBar: ProgressBar,
        emptyText: TextView
    ) {
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE
    }

    fun showContent(
        recyclerView: RecyclerView,
        progressBar: ProgressBar,
        emptyText: TextView
    ) {
        recyclerView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        emptyText.visibility = View.GONE
    }

    fun showEmpty(
        recyclerView: RecyclerView,
        progressBar: ProgressBar,
        emptyText: TextView,
        message: String
    ) {
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        emptyText.visibility = View.VISIBLE
        emptyText.text = message
    }

    fun showError(
        recyclerView: RecyclerView,
        progressBar: ProgressBar,
        emptyText: TextView,
        message: String
    ) {
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        emptyText.visibility = View.VISIBLE
        emptyText.text = message
    }
}