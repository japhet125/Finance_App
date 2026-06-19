package com.ramdefinance.financeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoanAgreementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_agreement)

        val txtAgreement = findViewById<TextView>(R.id.txtAgreement)
        val agreementCheckBox = findViewById<CheckBox>(R.id.cbAgreement)
        val signatureInput = findViewById<EditText>(R.id.etSignature)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val fullName = document.getString("fullName") ?: ""
                    signatureInput.setText(fullName)
                    signatureInput.isEnabled = false
                }
        }
        val acceptButton = findViewById<Button>(R.id.btnAcceptAgreement)

        txtAgreement.text = """
BAOBAB FINANCE LOAN AGREEMENT

By submitting this loan request, you agree that:

1. The information you provided is accurate.

2. You agree to repay the loan according to the approved payment schedule.

3. Failure to repay may result in account suspension, collection activity, or legal action where permitted by law.

4. Interest and repayment terms are shown in your loan summary.

5. Electronic acceptance of this agreement constitutes your signature.
        """.trimIndent()

        acceptButton.setOnClickListener {
            val signatureName = signatureInput.text.toString().trim()

            if (!agreementCheckBox.isChecked) {
                Toast.makeText(this, "You must accept the agreement.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (signatureName.isBlank()) {
                Toast.makeText(this, "Please type your full legal name.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val resultIntent = Intent()
            resultIntent.putExtra("agreementAccepted", true)
            resultIntent.putExtra("agreementName", signatureName)
            resultIntent.putExtra("agreementAcceptedAt", System.currentTimeMillis())
            resultIntent.putExtra("agreementVersion", "1.0")

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}