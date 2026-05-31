package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class IdentityVerificationAdapter(
    private val userList: List<Pair<String, IdentityUserModel>>
) : RecyclerView.Adapter<IdentityVerificationAdapter.IdentityViewHolder>() {

    class IdentityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.txtIdentityName)
        val email: TextView = itemView.findViewById(R.id.txtIdentityEmail)
        val phone: TextView = itemView.findViewById(R.id.txtIdentityPhone)
        val address: TextView = itemView.findViewById(R.id.txtIdentityAddress)
        val status: TextView = itemView.findViewById(R.id.txtIdentityStatus)
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