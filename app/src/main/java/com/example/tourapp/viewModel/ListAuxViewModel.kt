package com.example.tourapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerAuxAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListAuxViewModel : ViewModel() {

    lateinit var myAdapter: RecyclerAuxAdapter

    var  values = arrayOf("one", "two" , "three", "four", "five" , "six", "seven", "eight", "nine", "ten")
    var listUsu: ArrayList<User> = ArrayList()
    private lateinit var mListenerUser : ValueEventListener
    private  var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()

    fun configAdapter() {
        myAdapter = RecyclerAuxAdapter()
    }

    fun getUserList() {

        listUsu.clear()
        val userRef = FirebaseDatabase.getInstance().getReference("USUARIOS")
        var userAux: User

        mListenerUser = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.forEachIndexed { index, user ->
                    if(user.key != currentUser) {
                        Log.v("FIREBASE_BBDD_USER", "EXITO AL DESCARGAR INFO")

                        val type = user.child("userType").value as String
                        if(type != Constants.ADMIN) {
                            //val user: User? = snapshot.getValue(User::class.java)
                            val email = user.child("userMail").value as String
                            val name = user.child("userName").value as String
                            val password = user.child("userPassword").value as String
                            val id = user.child("userId").value as String

                            userAux = User(name, password, type, email, id)
                            listUsu.add(userAux)
                            setUserList()
                        }
                    }
                }
            }

        }

        userRef.addValueEventListener(mListenerUser)
    }

    fun setUserList() {
        myAdapter.setUserList(listUsu)
        myAdapter.notifyDataSetChanged()
    }

    fun setListData() {
        //myAdapter.setListData(values)
        myAdapter.notifyDataSetChanged()
    }

    fun addValue(aux: String) {
        values.set(1, aux)
        setListData()
    }
}