package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        val fullName = findViewById<EditText>(R.id.etFullName)
        val email = findViewById<EditText>(R.id.etEmail)
        val phone = findViewById<EditText>(R.id.etPhone)
        val password = findViewById<EditText>(R.id.etPassword)
        val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val createButton = findViewById<Button>(R.id.btnCreateAccount)

        createButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val confirmPasswordText = confirmPassword.text.toString().trim()

            if (
                fullName.text.isBlank() ||
                emailText.isBlank() ||
                phone.text.isBlank() ||
                passwordText.isBlank() ||
                confirmPasswordText.isBlank()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (passwordText != confirmPasswordText) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, DashboardActivity::class.java)
                            startActivity(intent)

                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}