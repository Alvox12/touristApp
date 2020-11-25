package com.example.tourapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.dataModel.User
import com.example.tourapp.views.MainActivity


class RecyclerUserListAdapter():
    RecyclerView.Adapter<RecyclerUserListAdapter.ViewHolder>() {

    //private var dataSet: Array<String>? = null
    private var listUser: ArrayList<User>? = null
    private var parent: ViewGroup? = null

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view){

        fun bind(user: User, parent: ViewGroup){
            val tvmail = view.findViewById<TextView>(R.id.tv_user_email)
            val tvname = view.findViewById<TextView>(R.id.tv_user_name)
            val tvtype = view.findViewById<TextView>(R.id.tv_user_type)
            tvmail.text = user.userMail
            tvname.text = user.userName
            tvtype.text = user.userType
        }
    }

   /* fun setListData(myDataSet : Array<String>) {
        dataSet = myDataSet
    }*/

    fun setUserList(users: ArrayList<User>) {
        this.listUser = users
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent,false)
        this.parent = parent
        return ViewHolder(vh)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listUser?.get(position)?.let {
            this.parent?.let { parent ->
                holder.bind(it, parent)

                val btn = holder.view.findViewById<ImageView>(R.id.iv_button)
                btn.setOnClickListener { view ->
                    //creating a popup menu

                    //creating a popup menu
                    val popup = PopupMenu(view.context, btn)
                    popup.inflate(R.menu.user_list_menu)
                    //adding click listener
                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.opt_edituser -> callActivity(parent.context, item, listUser!![position])
                            else -> false
                        }
                    }

                    //displaying the popup
                    popup.show()
                }
            }
        }
    }

    override fun getItemCount() = this.listUser?.size ?: 0

    private fun callActivity(context: Context, item: MenuItem, user: User) : Boolean{
        (context as MainActivity).useredit = user
        (context as MainActivity).onNavigationItemSelected(item)
        return true
    }

}