package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var signUpButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        signUpButton = findViewById(R.id.btnSignUp)
        loginButton = findViewById(R.id.btnLogin)

        signUpButton.setOnClickListener {
            Toast.makeText(this, "Sign Up clicked", Toast.LENGTH_SHORT).show()
        }

        loginButton.setOnClickListener {
            Toast.makeText(this, "Login clicked", Toast.LENGTH_SHORT).show()
        }
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

}