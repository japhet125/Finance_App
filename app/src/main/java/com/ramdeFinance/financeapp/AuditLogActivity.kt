package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AuditLogActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var auditLogList: MutableList<AuditLogModel>
    private lateinit var adapter: AuditLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audit_log)

        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerAuditLogs)

        auditLogList = mutableListOf()
        adapter = AuditLogAdapter(auditLogList)

        recyclerView.adapter = adapter

        val db = FirebaseFirestore.getInstance()

        db.collection("audit_logs")
            .orderBy(
                "timestamp",
                com.google.firebase.firestore.Query.Direction.DESCENDING
            )
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                auditLogList.clear()

                if (snapshots != null) {
                    for (document in snapshots.documents) {
                        val log = document.toObject(AuditLogModel::class.java)

                        if (log != null) {
                            auditLogList.add(log)
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }
}