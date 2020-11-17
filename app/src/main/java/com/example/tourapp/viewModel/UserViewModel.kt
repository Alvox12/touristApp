package com.example.tourapp.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.dataModel.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class UserViewModel : ViewModel() {

    private lateinit var mListenerUser : ValueEventListener
    var user: MutableLiveData<User> = MutableLiveData()
        private set

    fun getUsersData() {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        val user_reference = FirebaseDatabase.getInstance().getReference("USUARIOS/$currentUser")

        mListenerUser = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.key == currentUser) {
                    Log.v("FIREBASE_BBDD_USER", "EXITO AL DESCARGAR INFO")
                    //val user: User? = snapshot.getValue(User::class.java)
                    val email = snapshot.child("userMail").value as String
                    val name = snapshot.child("userName").value as String
                    val password = snapshot.child("userPassword").value as String
                    val type = snapshot.child("userType").value as String
                    user.value = User(name, password, type, email)
                }
                else Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }

        }

        user_reference.addValueEventListener(mListenerUser)
    }
}