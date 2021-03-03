package com.example.tourapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.viewModel.CommentListViewModel
import com.example.tourapp.views.MainActivity
import kotlinx.android.synthetic.main.comment_list_item.view.*

class RecyclerCommentListAdapter(var model: CommentListViewModel):
    RecyclerView.Adapter<RecyclerCommentListAdapter.ViewHolder>() {

    private var listComments: MutableList<Comment> = mutableListOf()
    private var parent: ViewGroup? = null

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view){

        fun bind(comment: Comment, parent: ViewGroup){

            val tvname = view.findViewById<TextView>(R.id.tv_comment_user)
            val tvComment = view.findViewById<TextView>(R.id.tv_comment_text)

            tvname.text = comment.commentUserName
            tvComment.text = comment.commentTxt
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
        listComments[position].let {

            val popupMenu = PopupMenu(holder.view.context, holder.view.iv_button_comment)
            popupMenu.inflate(R.menu.comment_itemlist_menu)

            popupMenu.setOnMenuItemClickListener { item->
               when(item.itemId) {
                   R.id.opt_deletecomment -> {
                       popupMenu.dismiss();
                       model.delComment(listComments[position].commentId);
                       true
                   }
                   R.id.opt_editcomment -> {
                       popupMenu.dismiss();
                       (parent?.context as MainActivity).editCommentPopup(listComments[position] ,model);
                       true
                   }
                   else -> {
                       popupMenu.dismiss();
                       false
                   }
               }
            }

            this.parent?.let { parent ->
                holder.bind(it, parent)
            }

            holder.view.iv_button_comment.setOnClickListener {
                popupMenu.show()
            }
            //popupMenu.show()
        }
    }

    fun setCommentList(listAux: MutableList<Comment>) {
        this.listComments.clear()
        this.listComments.addAll(listAux)
    }

    override fun getItemCount() = this.listComments?.size ?: 0

}