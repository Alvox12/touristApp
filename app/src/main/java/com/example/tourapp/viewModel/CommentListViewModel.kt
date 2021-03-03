package com.example.tourapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerCommentListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Comment
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class CommentListViewModel : ViewModel() {

    lateinit var myAdapter: RecyclerCommentListAdapter
    private var listComments :MutableList<Comment> = mutableListOf()
    private lateinit var childListener : ChildEventListener

    lateinit var placeId: String
    lateinit var userId: String
    var mapComments: MutableMap<String, Comment> = mutableMapOf()

    fun configAdapter() {
        myAdapter = RecyclerCommentListAdapter(this)
    }

    fun setCommentList() {
        listComments.clear()
        listComments.addAll(mapComments.values)

        myAdapter.setCommentList(listComments)
        myAdapter.notifyDataSetChanged()
    }

    private fun getCommentData(snapshot: DataSnapshot): Comment {

        val commenttxt = snapshot.child(Constants.COMMENTTXT).value as String
        val commentuserid = snapshot.child(Constants.COMMENTUSERID).value as String
        val commentusername = snapshot.child(Constants.COMMENTUSERNAME).value as String
        val commentid = snapshot.child(Constants.COMMENTID).value as String

        return Comment(commenttxt, commentuserid, commentusername, commentid)
    }

    fun addComment(comment: Comment) {

        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(placeId).child(Constants.PLACECOMMENTS).child(userId)
        var aux = comment.toAnyObject()

        placeRef.child(comment.commentId).setValue(aux).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_ADD_COMMENT")
            }
            else
                Log.v("FIREBASE_BBDD", "ERROR_ADD_COMMENT")
        }
    }


    fun delComment(commentId: String) {

        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(placeId).child(Constants.PLACECOMMENTS).child(userId)
        placeRef.child(commentId).removeValue().addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_DEL_COMMENT")
            }
            else
                Log.v("FIREBASE_BBDD", "ERROR_DEL_COMMENT")
        }
    }


    fun editComment(commentId: String, commentTxt: String) {

        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(placeId).child(Constants.PLACECOMMENTS).child(userId)
        
        placeRef.child(commentId).child(Constants.COMMENTTXT).setValue(commentTxt).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_EDIT_COMMENT")
            }
            else
                Log.v("FIREBASE_BBDD", "ERROR_EDIT_COMMENT")
        }
    }


    fun loadChildEventListener() {

        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(placeId).child(Constants.PLACECOMMENTS).child(userId)

        childListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.key!= Constants.USERPLACESCORE) {

                    var comment = getCommentData(snapshot)
                    mapComments[comment.commentId] = comment

                    setCommentList()
                    Log.d("childFirebase", "Added Success")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                if (snapshot.key!= Constants.USERPLACESCORE) {
                    val comment = getCommentData(snapshot)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        mapComments.replace(comment.commentId, comment)
                    } else {
                        mapComments.remove(comment.commentId)
                        mapComments[comment.commentId] = comment
                    }

                    setCommentList()
                    Log.d("childFirebase", "Changed Success")
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

                if (snapshot.key!= Constants.USERPLACESCORE) {
                    val comment = getCommentData(snapshot)
                    mapComments.remove(comment.commentId)

                    setCommentList()
                    Log.d("childFirebase", "Removed Success")
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("childFirebase", "Moved Success")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("childFirebase", "CANCELLED")
            }

        }

        placeRef.addChildEventListener(childListener)
    }


    fun removeChildListener() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(placeId).child(Constants.PLACECOMMENTS).child(userId)
        placeRef.removeEventListener(childListener)
    }

}