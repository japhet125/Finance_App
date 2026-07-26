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

    override fun onBindViewHolder(
        holder: AdminUserViewHolder,
        position: Int
    ) {
        val (userId, user) = userList[position]
        val context = holder.itemView.context

        holder.name.text =
            context.getString(
                R.string.admin_user_name,
                user.fullName
            )

        holder.email.text =
            context.getString(
                R.string.admin_user_email,
                user.email
            )

        holder.phone.text =
            context.getString(
                R.string.admin_user_phone,
                user.phone
            )

        holder.role.text =
            context.getString(
                R.string.admin_user_role,
                translateRole(context, user.role)
            )

        holder.credit.text =
            context.getString(
                R.string.admin_user_credit_score,
                user.creditScore
            )

        holder.identity.text =
            if (user.identityVerified) {
                context.getString(
                    R.string.admin_user_identity_verified,
                    translateIdentityStatus(
                        context,
                        user.identityStatus
                    )
                )
            } else {
                context.getString(
                    R.string.admin_user_identity_not_verified,
                    translateIdentityStatus(
                        context,
                        user.identityStatus
                    )
                )
            }

        holder.itemView.setOnClickListener {
            val intent = Intent(
                context,
                AdminUserDetailsActivity::class.java
            )

            intent.putExtra("USER_ID", userId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    private fun translateRole(
        context: android.content.Context,
        role: String
    ): String {
        return when (role.lowercase()) {
            "admin" ->
                context.getString(R.string.user_role_admin)

            "borrower", "user" ->
                context.getString(R.string.user_role_borrower)

            else -> role
        }
    }

    private fun translateIdentityStatus(
        context: android.content.Context,
        status: String
    ): String {
        return when (status.lowercase()) {
            "approved" ->
                context.getString(R.string.identity_status_approved)

            "pending" ->
                context.getString(R.string.identity_status_pending)

            "rejected" ->
                context.getString(R.string.identity_status_rejected)

            else ->
                context.getString(R.string.identity_status_not_submitted)
        }
    }
}