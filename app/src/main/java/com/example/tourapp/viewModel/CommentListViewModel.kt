package com.example.tourapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerCommentListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Comment
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class CommentListViewModel : ViewModel() {

    lateinit var myAdapter: RecyclerCommentListAdapter
    var listComments :MutableList<Comment> = mutableListOf()
    private lateinit var childListener : ChildEventListener

    fun configAdapter() {
        myAdapter = RecyclerCommentListAdapter()
    }

    fun setCommentList() {
        myAdapter.setCommentList(listComments)
        myAdapter.notifyDataSetChanged()
    }

    private fun getCommentData(snapshot: DataSnapshot): Comment {

        val commenttxt = snapshot.child(Constants.COMMENTTXT).value as String
        val commentuserid = snapshot.child(Constants.COMMENTUSERID).value as String
        val commentusername = snapshot.child(Constants.COMMENTUSERNAME).value as String

        return Comment(commenttxt, commentuserid, commentusername)
    }

    fun loadChildEventListener() {

        childListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                var comment = getCommentData(snapshot)
                listComments.add(comment)
                Log.d("childFirebase", "Added Success")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("childFirebase", "Changed Success")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                var comment = getCommentData(snapshot)
                val index = listComments.indexOf(comment)
                listComments.removeAt(index)
                Log.d("childFirebase", "Removed Success")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("childFirebase", "Moved Success")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("childFirebase", "CANCELLED")
            }

        }
    }



}