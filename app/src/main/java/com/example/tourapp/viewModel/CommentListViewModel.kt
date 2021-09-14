package com.example.tourapp.viewModel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerCommentListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.User
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class CommentListViewModel : ViewModel() {

    lateinit var myAdapter: RecyclerCommentListAdapter
    /*Lista de comentarios descargados de la BBDD*/
    private var listComments :MutableList<Comment> = mutableListOf()
    private lateinit var childListener : ChildEventListener

    /*ID del lugar donde se almacenan los comentarios*/
    lateinit var placeId: String
    /*Asocia el ID unico de cada comentario a el objeto de tipo Comment correspondiente*/
    var mapComments: MutableMap<String, Comment> = mutableMapOf()
    lateinit var user: User

    var commentIndex = 0
    var descargas = 0

    //Guardamos las imagenes de PlaceDataFragment
    var arrayLIstBitmap: ArrayList<Bitmap> = arrayListOf()

    fun configAdapter() {
        myAdapter = RecyclerCommentListAdapter(this)
    }

    fun setCommentList() {
        listComments.clear()
        listComments.addAll(mapComments.values)

        myAdapter.setCommentList(listComments)
        myAdapter.notifyDataSetChanged()
    }

    /**Obtiene los datos del comentario y los convierte en un objeto Comment*/
    private fun getCommentData(snapshot: DataSnapshot): Comment {

        val commenttxt = snapshot.child(Constants.COMMENTTXT).value as String
        val commentuserid = snapshot.child(Constants.COMMENTUSERID).value as String
        val commentusername = snapshot.child(Constants.COMMENTUSERNAME).value as String
        val commentid = snapshot.child(Constants.COMMENTID).value as String

        return Comment(commenttxt, commentuserid, commentusername, commentid)
    }

    /**Sube un comentario nuevo a la base de datos*/
    fun addComment(comment: Comment) {

        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(placeId).child(Constants.PLACECOMMENTS).child(user.userId)
        var aux = comment.toAnyObject()

        placeRef.child(comment.commentId).setValue(aux).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_ADD_COMMENT")
            }
            else
                Log.v("FIREBASE_BBDD", "ERROR_ADD_COMMENT")
        }
    }


    /**Dado el ID unico de un comentario lo elimina de los comentarios en la BBDD*/
    fun delComment(commentId: String) {

        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(placeId).child(Constants.PLACECOMMENTS).child(user.userId)
        placeRef.child(commentId).removeValue().addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_DEL_COMMENT")
            }
            else
                Log.v("FIREBASE_BBDD", "ERROR_DEL_COMMENT")
        }
    }


    /**Dado el ID de un comentario y un string actualiza el mensaje de un comentario en la BBDD*/
    fun editComment(commentId: String, commentTxt: String) {

        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(placeId).child(Constants.PLACECOMMENTS).child(user.userId)
        
        placeRef.child(commentId).child(Constants.COMMENTTXT).setValue(commentTxt).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_EDIT_COMMENT")
            }
            else
                Log.v("FIREBASE_BBDD", "ERROR_EDIT_COMMENT")
        }
    }


    /**Descarga mas comentarios de la base de datos hasta descargar el maximo permitido o descargar todos*/
    fun loadNewData() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(placeId).child(Constants.PLACECOMMENTS).child(user.userId)
        placeRef.removeEventListener(childListener)

        descargas = 0
        loadChildEventListener()
    }

    /**Detecta cambios en una ruta especificada de la base de datos y en funcion del cambio realiza un ajuste en
     * la informacion u otro*/
    fun loadChildEventListener() {

        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(placeId).child(Constants.PLACECOMMENTS).child(user.userId)

        childListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                if (snapshot.key!= Constants.USERPLACESCORE) {

                    if(descargas < Constants.MAX_DATABASE_ITEMS && commentIndex < snapshot.childrenCount) {
                        val comment = getCommentData(snapshot)
                        mapComments[comment.commentId] = comment

                        commentIndex++
                        descargas++

                        setCommentList()
                        Log.d("childFirebase", "Added Success")
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                if (snapshot.key!= Constants.USERPLACESCORE) {
                    val comment = getCommentData(snapshot)
                    commentIndex++
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
                    commentIndex--
                    descargas--

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
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(placeId).child(Constants.PLACECOMMENTS).child(user.userId)
        placeRef.removeEventListener(childListener)
    }

}