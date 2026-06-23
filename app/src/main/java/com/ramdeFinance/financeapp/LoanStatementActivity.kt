package com.ramdefinance.financeapp

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import androidx.core.content.FileProvider

class LoanStatementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId =
            FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->

                val language =
                    userDoc.getString("language") ?: "en"

                loadLoans(userId, language)
            }
            .addOnFailureListener {
                loadLoans(userId, "en")
            }
    }

    private fun createPdf(
        loans: List<DocumentSnapshot>,
        language: String
    ) {
        val pdfDocument = PdfDocument()
        val paint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText(
            if (language == "fr")
                "Relevé de prêt Baobab"
            else
                "Baobab Loan Statement",
            50f,
            60f,
            paint
        )
        paint.textSize = 14f
        paint.isFakeBoldText = false

        val today = SimpleDateFormat(
            "MMM dd, yyyy",
            Locale.getDefault()
        ).format(Date())

        var y = 100f

        canvas.drawText(
            if (language == "fr")
                "Date du relevé : $today"
            else
                "Statement Date: $today",
            50f,
            y,
            paint
        )
        y += 35f
        canvas.drawText(
            if (language == "fr")
                "Nombre total de prêts : ${loans.size}"
            else
                "Total Loans: ${loans.size}",
            50f,
            y,
            paint
        )
        y += 45f

        var pageNumber = 1

        for (loan in loans) {

            if (y > 760f) {
                pdfDocument.finishPage(page)

                pageNumber++

                val newPageInfo =
                    PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()

                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                y = 60f
            }

            val amount = loan.getString("amount") ?: "0.00"
            val reason = loan.getString("reason") ?: "N/A"
            val status = loan.getString("status") ?: "N/A"
            val balance = loan.getString("remainingBalance") ?: "0.00"
            val paymentFrequency =
                loan.getString("paymentFrequency") ?: "N/A"
            val paymentAmount =
                loan.getString("paymentAmount") ?: "0.00"
            val autoPayStatus =
                loan.getString("autoPayStatus") ?: "disabled"

            paint.isFakeBoldText = true
            canvas.drawText("Loan ID: ${loan.id}", 50f, y, paint)
            paint.isFakeBoldText = false

            y += 25f

            if (language == "fr") {

                canvas.drawText("Montant : $amount", 70f, y, paint)
                y += 22f

                canvas.drawText("Raison : $reason", 70f, y, paint)
                y += 22f

                canvas.drawText("Statut : $status", 70f, y, paint)
                y += 22f

                canvas.drawText("Solde restant : $balance", 70f, y, paint)
                y += 22f

                canvas.drawText("Plan de paiement : $paymentFrequency", 70f, y, paint)
                y += 22f

                canvas.drawText("Montant du paiement : $paymentAmount", 70f, y, paint)
                y += 22f

                canvas.drawText("Paiement automatique : $autoPayStatus", 70f, y, paint)
                y += 35f

            } else {

                canvas.drawText("Amount: $$amount", 70f, y, paint)
                y += 22f

                canvas.drawText("Reason: $reason", 70f, y, paint)
                y += 22f

                canvas.drawText("Status: $status", 70f, y, paint)
                y += 22f

                canvas.drawText("Remaining Balance: $$balance", 70f, y, paint)
                y += 22f

                canvas.drawText("Payment Plan: $paymentFrequency", 70f, y, paint)
                y += 22f

                canvas.drawText("Payment Amount: $$paymentAmount", 70f, y, paint)
                y += 22f

                canvas.drawText("Auto Pay Status: $autoPayStatus", 70f, y, paint)
                y += 35f

            }
        }

        pdfDocument.finishPage(page)

        val fileName =
            "Baobab_All_Loan_Statement_${System.currentTimeMillis()}.pdf"

        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            fileName
        )

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Toast.makeText(
                this,
                "Statement saved",
                Toast.LENGTH_SHORT
            ).show()

            sharePdf(file)

            finish()

        } catch (e: Exception) {
            pdfDocument.close()

            Toast.makeText(
                this,
                "PDF failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }
    private fun sharePdf(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(
            Intent.createChooser(
                intent,
                "Share Baobab Statement"
            )
        )

        finish()
    }
    private fun loadLoans(
        userId: String,
        language: String
    ) {
        FirebaseFirestore.getInstance()
            .collection("loan_requests")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { loans ->

                if (loans.isEmpty) {
                    Toast.makeText(
                        this,
                        if (language == "fr")
                            "Aucun prêt trouvé"
                        else
                            "No loans found",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                    return@addOnSuccessListener
                }

                createPdf(loans.documents, language)
            }
    }
}