package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.net.Uri

class IdentityVerificationAdapter(
    private val userList: List<Pair<String, IdentityUserModel>>
) : RecyclerView.Adapter<IdentityVerificationAdapter.IdentityViewHolder>() {

    class IdentityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.txtIdentityName)
        val email: TextView = itemView.findViewById(R.id.txtIdentityEmail)
        val phone: TextView = itemView.findViewById(R.id.txtIdentityPhone)
        val address: TextView = itemView.findViewById(R.id.txtIdentityAddress)
        val status: TextView = itemView.findViewById(R.id.txtIdentityStatus)
        val viewIdButton: Button =
            itemView.findViewById(R.id.btnViewId)
        val verifyButton: Button = itemView.findViewById(R.id.btnVerifyIdentity)
        val rejectButton: Button = itemView.findViewById(R.id.btnRejectIdentity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdentityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.identity_user_item, parent, false)

        return IdentityViewHolder(view)
    }

    override fun onBindViewHolder(holder: IdentityViewHolder, position: Int) {
        val (documentId, user) = userList[position]

        holder.name.text = "Name: ${user.fullName}"
        holder.email.text = "Email: ${user.email}"
        holder.phone.text = "Phone: ${user.phone}"
        holder.address.text =
            "Address: ${user.address}, Apt ${user.apt}, ${user.city}, ${user.state} ${user.zipCode}, ${user.country}"
        if (user.identityDocumentUrl.isBlank()) {
            holder.viewIdButton.visibility = View.GONE
        } else {
            holder.viewIdButton.visibility = View.VISIBLE
        }
        if (user.identityStatus == "approved") {
            holder.status.text = "✓ Identity Verified"
            holder.verifyButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE
        } else if (user.identityStatus == "rejected") {
            holder.status.text = "✗ Identity Rejected"
            holder.verifyButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE
        } else {
            holder.status.text = "Status: Pending"
            holder.verifyButton.visibility = View.VISIBLE
            holder.rejectButton.visibility = View.VISIBLE
        }

        val db = FirebaseFirestore.getInstance()
        holder.viewIdButton.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(user.identityDocumentUrl)
            )

            holder.itemView.context.startActivity(intent)
        }

        holder.verifyButton.setOnClickListener {
            db.collection("users")
                .document(documentId)
                .update(
                    mapOf(
                        "identityVerified" to true,
                        "identityStatus" to "approved"
                    )
                )
                .addOnSuccessListener {

                    holder.status.text = "✓ Identity Verified"
                    holder.verifyButton.visibility = View.GONE
                    holder.rejectButton.visibility = View.GONE
                    holder.viewIdButton.visibility = View.GONE
                    val notification = hashMapOf(
                        "userId" to documentId,
                        "title" to "Identity Verified",
                        "message" to "Your identity has been verified successfully.",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )

                    db.collection("notifications").add(notification)


                    val userLanguage = user.language
                    val subject =
                        if (userLanguage == "fr") {
                            "Vérification d'identité approuvée"
                        } else {
                            "Identity Verification Approved"
                        }

                    val message =
                        if (userLanguage == "fr") {
                            """
        Bonjour ${user.fullName},

        Votre identité a été vérifiée avec succès. Vous pouvez maintenant continuer à utiliser les services Baobab Finance.

        Merci d'avoir choisi Baobab Finance.

        L'équipe Baobab
        """.trimIndent()
                        } else {
                            """
        Hello ${user.fullName},

        Your identity has been successfully verified. You can now continue using Baobab Finance services.

        Thank you for choosing Baobab Finance.

        Baobab Team
        """.trimIndent()
                        }

                    val emailRequest = hashMapOf(
                        "userId" to documentId,
                        "fullName" to user.fullName,
                        "email" to user.email,
                        "type" to "identity_approved",
                        "subject" to subject,
                        "message" to message,
                        "status" to "pending",
                        "createdAt" to System.currentTimeMillis()
                    )

                    db.collection("email_requests").add(emailRequest)
                    val auditLog = hashMapOf(
                        "actorId" to "admin",
                        "action" to "identity_verified",
                        "targetType" to "user",
                        "targetId" to documentId,
                        "message" to "User identity was verified.",
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("audit_logs").add(auditLog)

                    Toast.makeText(
                        holder.itemView.context,
                        "Identity verified",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        holder.itemView.context,
                        "Update failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

        }

        holder.rejectButton.setOnClickListener {
            db.collection("users")
                .document(documentId)
                .update(
                    mapOf(
                        "identityVerified" to false,
                        "identityStatus" to "rejected"
                    )
                )
                .addOnSuccessListener {

                    holder.status.text = "✗ Identity Rejected"
                    holder.verifyButton.visibility = View.GONE
                    holder.rejectButton.visibility = View.GONE
                    holder.viewIdButton.visibility = View.GONE
                    val notification = hashMapOf(
                        "userId" to documentId,
                        "title" to "Identity Rejected",
                        "message" to "Your identity verification was rejected. Please update your information and try again.",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )

                    db.collection("notifications").add(notification)
                    val userLanguage = user.language
                    val subject =
                        if (userLanguage == "fr") {
                            "Vérification d'identité rejetée"
                        } else {
                            "Identity Verification Rejected"
                        }

                    val message =
                        if (userLanguage == "fr") {
                            """
        Bonjour ${user.fullName},

        Votre vérification d'identité a été rejetée. Veuillez vérifier vos informations et soumettre à nouveau vos documents.

        Merci d'avoir choisi Baobab Finance.

        L'équipe Baobab
        """.trimIndent()
                        } else {
                            """
        Hello ${user.fullName},

        Your identity verification was rejected. Please review your information and submit your documents again.

        Thank you for choosing Baobab Finance.

        Baobab Team
        """.trimIndent()
                        }

                    val emailRequest = hashMapOf(
                        "userId" to documentId,
                        "fullName" to user.fullName,
                        "email" to user.email,
                        "type" to "identity_rejected",
                        "subject" to subject,
                        "message" to message,
                        "status" to "pending",
                        "createdAt" to System.currentTimeMillis()
                    )

                    db.collection("email_requests").add(emailRequest)

                    val auditLog = hashMapOf(
                        "actorId" to "admin",
                        "action" to "identity_rejected",
                        "targetType" to "user",
                        "targetId" to documentId,
                        "message" to "User identity was rejected.",
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("audit_logs").add(auditLog)

                    Toast.makeText(
                        holder.itemView.context,
                        "Identity rejected",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        holder.itemView.context,
                        "Update failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}