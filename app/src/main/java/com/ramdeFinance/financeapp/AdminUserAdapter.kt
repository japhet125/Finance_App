package com.ramdefinance.financeapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminUserAdapter(
    private val userList: List<Pair<String, AdminUserModel>>
) : RecyclerView.Adapter<AdminUserAdapter.AdminUserViewHolder>() {

    class AdminUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.txtAdminUserName)
        val email: TextView = itemView.findViewById(R.id.txtAdminUserEmail)
        val phone: TextView = itemView.findViewById(R.id.txtAdminUserPhone)
        val role: TextView = itemView.findViewById(R.id.txtAdminUserRole)
        val credit: TextView = itemView.findViewById(R.id.txtAdminUserCredit)
        val identity: TextView = itemView.findViewById(R.id.txtAdminUserIdentity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_user_item, parent, false)

        return AdminUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminUserViewHolder, position: Int) {
        val (_, user) = userList[position]

        holder.name.text = "Name: ${user.fullName}"
        holder.email.text = "Email: ${user.email}"
        holder.phone.text = "Phone: ${user.phone}"
        holder.role.text = "Role: ${user.role}"
        holder.credit.text = "Credit Score: ${user.creditScore}"

        val identityText = if (user.identityVerified) {
            "✓ Identity: ${user.identityStatus}"
        } else {
            "✗ Identity: ${user.identityStatus}"
        }

        holder.identity.text = identityText
        holder.itemView.setOnClickListener {

            val intent = Intent(
                holder.itemView.context,
                AdminUserDetailsActivity::class.java
            )

            intent.putExtra("USER_ID", userList[position].first)

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}