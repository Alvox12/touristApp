package com.example.tourapp.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.Place

class RecyclerCommentListAdapter():
    RecyclerView.Adapter<RecyclerCommentListAdapter.ViewHolder>() {

    private var listComments: MutableList<Comment> = mutableListOf()
    private var parent: ViewGroup? = null

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view){

        fun bind(comment: Comment, parent: ViewGroup){

            val tvname = view.findViewById<TextView>(R.id.tv_comment_user)
            val tvComment = view.findViewById<TextView>(R.id.tv_comment_text)

            tvname.text = comment.nameUser
            tvComment.text = comment.comment
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val vh = LayoutInflater.from(parent.context).inflate(R.layout.comment_list_item, parent,false)
        this.parent = parent
        return ViewHolder(vh)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listComments?.get(position)?.let {
            this.parent?.let { parent ->
                holder.bind(it, parent)
            }
        }
    }

    fun setCommentList(listAux: MutableList<Comment>) {
        listComments.clear()
        listComments = listAux
    }

    override fun getItemCount() = this.listComments?.size ?: 0

}