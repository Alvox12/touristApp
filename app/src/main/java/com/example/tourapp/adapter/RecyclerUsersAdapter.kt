package com.example.tourapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.databinding.UserItemLayoutBinding
import com.example.tourapp.viewModel.UserListViewModel
import androidx.databinding.library.baseAdapters.BR
import com.example.tourapp.R
import com.example.tourapp.dataModel.User

class RecyclerUsersAdapter(var model: UserListViewModel): RecyclerView.Adapter<RecyclerUsersAdapter.ViewHolder>()  {

    private var listUser: List<User>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val incidenceBinding : UserItemLayoutBinding = DataBindingUtil.inflate(layoutInflater, viewType, parent, false)

        return ViewHolder(incidenceBinding)
    }


    override fun getItemCount() = this.listUser?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)

        holder.itemView.findViewById<TextView>(R.id.tv_user_name).text = listUser!![position].userName
        holder.itemView.findViewById<TextView>(R.id.tv_user_email).text = listUser!![position].userMail
        holder.itemView.findViewById<TextView>(R.id.tv_user_type).text = listUser!![position].userType
    }

    fun setUserList(users: List<User>) {
        this.listUser = users
    }


    inner class ViewHolder(var incidenceBinding: UserItemLayoutBinding)
        : RecyclerView.ViewHolder(incidenceBinding.root) {

        fun bind(position: Int) {
            incidenceBinding.setVariable(BR.model, model)
            incidenceBinding.setVariable(BR.position, position)
            incidenceBinding.executePendingBindings()
        }
    }
}