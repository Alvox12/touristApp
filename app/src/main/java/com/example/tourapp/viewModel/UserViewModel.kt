package com.example.tourapp.viewModel

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class UserViewModel : ViewModel() {

    /*Escucha a ver si hay o se dan cambios en los datos de la ruta especificada de la BBDD*/
    private lateinit var mListenerUser : ValueEventListener

    /*ID del usuario logueado*/
    private val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()

    /*Avisa de que los datos del uusario han sido editados o modificados correctamente*/
    var userEdited: MutableLiveData<Boolean> = MutableLiveData()

    /*Avisa de que los datos del usuario han sido descargados*/
    var userNotify: MutableLiveData<User> = MutableLiveData()
        private set

    private val mFirebaseAuth = FirebaseAuth.getInstance()



    /**Descarga de los datos de usuario de la BBDD*/
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

    /**Actualiza los datos del usuario especificado*/
    fun uploadUserData(newuser: User, oldPssw: String, currentuser: User) {

        var data = Base64.decode(oldPssw, Base64.DEFAULT)
        var psswd = String(data)
        val credential = EmailAuthProvider.getCredential(newuser.userMail, psswd)

        /*Sale de la cuenta actual, entra con los datos del nuevo usuario y actualiza la información correspondiente*/
        mFirebaseAuth.signOut()
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful) {
                mFirebaseAuth.currentUser?.updatePassword(newuser.userPassword)?.addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        //currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
                        val password =  Base64.encodeToString(newuser.userPassword.toByteArray(), Base64.DEFAULT)
                        val userRef = FirebaseDatabase.getInstance().getReference("USUARIOS/${newuser.userId}")
                        userRef.child("userName").setValue(newuser.userName)
                        userRef.child("userMail").setValue(newuser.userMail)
                        userRef.child("userPassword").setValue(password)
                        userRef.child("userType").setValue(newuser.userType).addOnCompleteListener {

                            FirebaseAuth.getInstance().signOut()
                            data = Base64.decode(currentuser.userPassword, Base64.DEFAULT)
                            psswd = String(data)
                            /*Tras actualizar la informacion vuelve a entrar con su usuario y contraseña normal (ADMIN)*/
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(currentuser.userMail, psswd).addOnCompleteListener {
                                userEdited.value = true
                            }
                        }
                    }
                }
            }
        }

    }

    /**Elimina al usuario de la base de datos y elimina tambien su información*/
    fun deleteUserData(userDel: User, oldPssw: String, currentuser: User) {

        var data = Base64.decode(oldPssw, Base64.DEFAULT)
        var psswd = String(data)
        val credential = EmailAuthProvider.getCredential(userDel.userMail, psswd)

        mFirebaseAuth.signOut()
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful) {
                val userRef = FirebaseDatabase.getInstance().getReference("USUARIOS/${userDel.userId}")
                userRef.removeValue().addOnCompleteListener { it2 ->
                    if(it2.isSuccessful) {
                        FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener { task ->
                            if(task.isSuccessful) {
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(currentuser.userMail, psswd).addOnCompleteListener {
                                    userEdited.value = true
                                }
                            }
                        }

                    }
                }
            }
        }
    }


    fun deleteUserListener() {
        val userRef = FirebaseDatabase.getInstance().getReference("USUARIOS/$currentUser")
        //val tagsRef = FirebaseDatabase.getInstance().getReference(Constants.ETIQUETAS)

        userRef.removeEventListener(mListenerUser)
        //tagsRef.removeEventListener(mListenerTags)
    }

}