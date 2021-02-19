package com.example.tourapp.viewModel

import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerCommentListAdapter
import com.example.tourapp.adapter.RecyclerPlaceListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.Place
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlaceDataViewModel : ViewModel() {

    lateinit var place:Place
    private lateinit var mListenerComment : ValueEventListener
    //lateinit var myAdapter: RecyclerCommentListAdapter

    fun getCommentList() {

        place.placeComments.clear()
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(place.placeId).child(Constants.PLACECOMMENTS)
        var comentario: Comment

        mListenerComment = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.forEachIndexed { index, comment ->

                    val commenttxt = comment.child(Constants.COMMENTTXT).value as String
                    val commentuserid = comment.child(Constants.COMMENTUSERID).value as String
                    val commentusername = comment.child(Constants.COMMENTUSERNAME).value as String

                    comentario = Comment(commenttxt, commentuserid, commentusername)
                    place.placeComments.add(index, comentario)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        placeRef.addValueEventListener(mListenerComment)
    }


    fun deleteCommentListener() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(place.placeId).child(Constants.PLACECOMMENTS)
        placeRef.removeEventListener(mListenerComment)
    }
}