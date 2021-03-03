package com.example.tourapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerCommentListAdapter
import com.example.tourapp.adapter.RecyclerPlaceListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.Place
import com.example.tourapp.dataModel.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlaceDataViewModel : ViewModel() {

    lateinit var place:Place
    lateinit var user: User
    private lateinit var mListenerComment : ValueEventListener
    //lateinit var myAdapter: RecyclerCommentListAdapter

    fun getCommentList() {

        place.placeComments.clear()
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(place.placeId).child(Constants.PLACECOMMENTS)
        var comentario: Comment

        mListenerComment = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.forEachIndexed { index, commentSnapshot ->
                    commentSnapshot.children.forEachIndexed { index, dataSnapshot ->

                        if (dataSnapshot.key!= Constants.USERPLACESCORE){

                            val commentId = dataSnapshot.child(Constants.COMMENTID).value as String
                            val commenttxt = dataSnapshot.child(Constants.COMMENTTXT).value as String
                            val commentuserid = dataSnapshot.child(Constants.COMMENTUSERID).value as String
                            val commentusername = dataSnapshot.child(Constants.COMMENTUSERNAME).value as String

                            comentario = Comment(commenttxt, commentuserid, commentusername, commentId)
                            place.placeComments.put(commentId, comentario)
                            //place.placeComments.add(index, comentario)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        placeRef.addValueEventListener(mListenerComment)
    }

    fun uploadScoreUser(score: Float) {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(place.placeId).child(Constants.PLACECOMMENTS).child(user.userId)

        placeRef.child(Constants.USERPLACESCORE).setValue(score).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_ADD_PASSWORD")
            }
            else
                Log.v("FIREBASE_BBDD", "ERROR_ADD_PASSWORD")
        }
    }


    fun deleteCommentListener() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(place.placeId).child(Constants.PLACECOMMENTS)
        placeRef.removeEventListener(mListenerComment)
    }
}