package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val userList: List<UserModel>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.txtUserName)
        val email: TextView = itemView.findViewById(R.id.txtUserEmail)
        val phone: TextView = itemView.findViewById(R.id.txtUserPhone)
        val role: TextView = itemView.findViewById(R.id.txtUserRole)
        val credit: TextView = itemView.findViewById(R.id.txtUserCredit)
        val risk: TextView = itemView.findViewById(R.id.txtUserRisk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)

        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.name.text = "Name: ${user.fullName}"
        holder.email.text = "Email: ${user.email}"
        holder.phone.text = "Phone: ${user.phone}"
        holder.role.text = "Role: ${user.role}"
        holder.credit.text = "Credit Score: ${user.creditScore}"

        val riskLabel = when {
            user.creditScore >= 700 -> "🟢 Low Risk"
            user.creditScore >= 550 -> "🟡 Medium Risk"
            else -> "🔴 High Risk"
        }

        holder.risk.text = "Risk: $riskLabel"
    }

    override fun getItemCount(): Int = userList.size
}