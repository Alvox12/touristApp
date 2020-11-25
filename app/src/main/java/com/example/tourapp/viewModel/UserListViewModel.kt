package com.example.tourapp.viewModel

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerUsersAdapter
import com.example.tourapp.dataModel.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserListViewModel : ViewModel() {

    var adapter: RecyclerUsersAdapter = RecyclerUsersAdapter(this)

    var listUsu: ArrayList<User> = ArrayList()
    private val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
    var positionClient : Int = 0
    lateinit var viewUser : View

    private lateinit var mListenerUser : ValueEventListener

    fun setUserList() {
        adapter.setUserList(listUsu)
        adapter.notifyDataSetChanged()
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
                        //val user: User? = snapshot.getValue(User::class.java)
                        val email = user.child("userMail").value as String
                        val name = user.child("userName").value as String
                        val password = user.child("userPassword").value as String
                        val type = user.child("userType").value as String
                        val id = user.child("userId").value as String

                        userAux = User(name, password, type, email, id)
                        listUsu.add(userAux)
                        setUserList()
                    }
                }
            }

        }

        userRef.addValueEventListener(mListenerUser)
    }

}