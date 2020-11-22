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
                if(snapshot.key != currentUser) {
                    Log.v("FIREBASE_BBDD_USER", "EXITO AL DESCARGAR INFO")
                    //val user: User? = snapshot.getValue(User::class.java)
                    val email = snapshot.child("userMail").value as String
                    val name = snapshot.child("userName").value as String
                    val password = snapshot.child("userPassword").value as String
                    val type = snapshot.child("userType").value as String

                    userAux = User(name, password, type, email)
                    listUsu.add(userAux)
                    setUserList()
                }
            }

        }
    }

}