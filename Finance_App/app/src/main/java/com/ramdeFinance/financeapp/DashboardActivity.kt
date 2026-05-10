package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val logoutButton = findViewById<Button>(R.id.btnLogout)

        logoutButton.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            finish()
        }
    }
}