package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.etLoginEmail)
        val password = findViewById<EditText>(R.id.etLoginPassword)
        val loginButton = findViewById<Button>(R.id.btnLoginNow)

        loginButton.setOnClickListener {

            if (email.text.isBlank() || password.text.isBlank()) {

                Toast.makeText(
                    this,
                    "Please enter email and password",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Login successful",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}