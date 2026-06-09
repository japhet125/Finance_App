package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class PhoneVerificationActivity : AppCompatActivity() {

    private lateinit var verificationId: String
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verification)

        auth = FirebaseAuth.getInstance()

        val phoneInput = findViewById<EditText>(R.id.etPhoneNumber)
        val otpInput = findViewById<EditText>(R.id.etOtpCode)
        val sendButton = findViewById<Button>(R.id.btnSendOtp)
        val verifyButton = findViewById<Button>(R.id.btnVerifyOtp)
        val backButton = findViewById<Button>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        sendButton.setOnClickListener {
            val phoneNumber = phoneInput.text.toString().trim()

            if (phoneNumber.isBlank()) {
                Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendVerificationCode(phoneNumber)
        }

        verifyButton.setOnClickListener {
            val code = otpInput.text.toString().trim()

            if (code.isBlank()) {
                Toast.makeText(this, "Enter verification code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!::verificationId.isInitialized) {
                Toast.makeText(this, "Send code first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val credential =
                PhoneAuthProvider.getCredential(verificationId, code)

            verifyCode(credential)
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(
                credential: PhoneAuthCredential
            ) {
                verifyCode(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(
                    this@PhoneVerificationActivity,
                    "Verification failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@PhoneVerificationActivity.verificationId = verificationId

                Toast.makeText(
                    this@PhoneVerificationActivity,
                    "Verification code sent",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun verifyCode(credential: PhoneAuthCredential) {
        auth.currentUser
            ?.linkWithCredential(credential)
            ?.addOnSuccessListener {
                markPhoneVerified()
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Verification failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun markPhoneVerified() {
        val userId = auth.currentUser?.uid ?: return

        val updates = mapOf(
            "phoneVerified" to true,
            "phoneVerifiedAt" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Phone verified successfully",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
    }
}