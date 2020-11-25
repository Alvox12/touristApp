package com.example.tourapp.viewModel

import android.util.Base64
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
    private val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
    var userNotify: MutableLiveData<User> = MutableLiveData()
        private set


    fun getUserData() {
        //currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val userRef = FirebaseDatabase.getInstance().getReference("USUARIOS/$currentUser")

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
                    val id = snapshot.child("userId").value as String
                    userNotify.value = User(name, password, type, email, id)
                }
                else Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }

        }

        userRef.addValueEventListener(mListenerUser)
    }
    
    
    fun uploadUserData(user: User) {
        //currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val password =  Base64.encodeToString(user.userPassword.toByteArray(), Base64.DEFAULT)
        val userRef = FirebaseDatabase.getInstance().getReference("USUARIOS/${user.userId}")
        userRef.child("userName").setValue(user.userName)
        userRef.child("userMail").setValue(user.userMail)
        userRef.child("userPassword").setValue(password)
        userRef.child("userType").setValue(user.userType)
    }

    fun deleteUserListener() {
        val userRef = FirebaseDatabase.getInstance().getReference("USUARIOS/$currentUser")
        userRef.removeEventListener(mListenerUser)
    }

}