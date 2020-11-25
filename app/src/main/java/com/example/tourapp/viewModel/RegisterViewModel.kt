package com.example.tourapp.viewModel

import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterViewModel : ViewModel() {

    //var registerNotify: MutableLiveData<Boolean> = MutableLiveData()
    private  var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var user: User
    var userClient: MutableLiveData<User> = MutableLiveData()
    var flagCreate: MutableLiveData<Boolean> = MutableLiveData()

    private  var refUser: DatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.USERS)

    /**
     * Fucion para añadir usurarios
     */
    fun addNewUser(
        userName: String,
        userPassword: String,
        userType: String,
        userMail: String){

        createUser(
            userName,
            userPassword,
            userType,
            userMail)


        onSignUp()

    }

    /**
     * Funcion que crea un objeto usuario con los datos
     */
    private fun createUser(
        userName: String,
        userPassword: String,
        userType: String,
        userMail: String) {

        user = User(
            userName = userName,
            userPassword = userPassword,
            userType = userType,
            userMail = userMail
        )
    }

    /**
     * Funcion para autenticar al usuario y añadirlo en DB
     */
    private fun onSignUp() {

        //var userMail = userClient.value!!.userMail
        //var pass = userClient.value!!.userPassword
        //var userPassword = Utils.decodeString(pass)
        var usrPassAux = user.userPassword

        if(user.userMail.isNotEmpty() && user.userPassword.isNotEmpty()) {
            mFirebaseAuth.createUserWithEmailAndPassword(user.userMail, user.userPassword)
                    .addOnCompleteListener {result ->

                        if(result.isSuccessful) {
                            Log.v("FIREBASE_LOGIN", "SUCCESS_LOGIN")

                            user.userPassword =  Base64.encodeToString(user.userPassword.toByteArray(), Base64.DEFAULT)
                            mFirebaseAuth.uid?.let { uid ->

                                user.userId = uid
                                //Damos de alta el usuraio en el nodo User
                                refUser.child(uid).setValue(user).addOnCompleteListener { result ->

                                    if (result.isSuccessful) {
                                        Log.v("FIREBASE_BBDD", "SUCCESS_UPLOAD")
                                        loginAfterSignUp(usrPassAux)
                                    }
                                }
                            }
                        }
                        else {
                            flagCreate.value = false
                            Log.v("FIREBASE_LOGIN", "ERROR, BAD CREDENCIALES")
                        }
                    }

            //mFirebaseAuth.signInWithEmailAndPassword(userMail,userPassword)
        }

    }

    /**
     * Tras registrar al usuario nos entramos en su cuenta.
     */
    private fun loginAfterSignUp(usrPassAux: String) {

        mFirebaseAuth?.signInWithEmailAndPassword(user.userMail, usrPassAux)
                ?.addOnCompleteListener {
                    if(it.isSuccessful) {
                        //loadClient(mFirebaseAuth.uid.toString())
                        Log.v("FIREBASE_LOGIN", "SUCCESS_LOGIN")
                        flagCreate.value = true
                        //loginNotify.value = true
                    }
                    else {
                        //SharedPreferencesManager.setSomeBooleanValues(Constants.SAVELOGIN, false)
                        Log.v("FIREBASE_LOGIN", "ERROR, BAD CREDENCIALES")
                        flagCreate.value = false
                        //loginNotify.value = false
                    }
                }
    }

    /**
     * Funcion para autenticar al usuario y añadirlo en DB
     */
    /*private fun onSignUp(clientName: String){
        var userMail = userClient.value!!.userMail
        var pass = userClient.value!!.userPassword
        var userPassword = Utils.decodeString(pass)

        var client = userClient.value
        if(user.userMail.isNotEmpty() && user.userPassword.isNotEmpty()) {
            mFirebaseAuth.createUserWithEmailAndPassword(user.userMail, user.userPassword)
                .addOnCompleteListener {result ->

                    if(result.isSuccessful) {
                        user.userPassword =  Base64.encodeToString(user.userPassword.toByteArray(), Base64.DEFAULT)
                        mFirebaseAuth.uid?.let {uid ->

                            // para cliente asignamos su uid
                            if(user.userType == Constants.CLIENTE){
                                user.clientId = uid
                            }

                            //Damos de alta el usuraio en el nodo User
                            refUser.child(uid).setValue(user).addOnCompleteListener {result ->

                                if (result.isSuccessful) {
                                    //Añadimos el nuevo contactos a la lista de contactos del cliente

                                    var nodo: String

                                    when(client) {
                                        null -> nodo = user.clientId
                                        else -> nodo = client.clientId
                                    }

                                    /*if(client?.userType == Constants.ADMIN && user.userType != Constants.CLIENTE) {
                                        refContact
                                            .child(user.clientId)
                                            .child(Constants.CONTACTS)
                                            .child(uid)
                                            .setValue(Contact(
                                                userName = user.userName,
                                                userMail = user.userMail,
                                                userPhone = user.userPhone,
                                                proyectCode = user.userProyectCode,
                                                userType = user.userType,
                                                userClient = user.clientName).toAnyObject())
                                    }*/

                                    /*refContact
                                        .child(nodo)
                                        .child(Constants.CONTACTS)
                                        .child(uid)
                                        .setValue(Contact(
                                            userName = user.userName,
                                            userMail = user.userMail,
                                            userPhone = user.userPhone,
                                            proyectCode = user.userProyectCode,
                                            userType = user.userType,
                                            userClient = user.clientName).toAnyObject())
                                        .addOnCompleteListener { result ->
                                            if (result.isSuccessful) {
                                                flagCreate.value = true
                                            }else {
                                                //Si el usuario no se actualiza correctamente en la DB lo eliminamos de authentication y de DB del nodo uders
                                                mFirebaseAuth.currentUser?.delete()
                                                refUser.child(uid).removeValue()
                                                flagCreate.value = false
                                            }
                                        }*/

                                }else{

                                    //Si el usuario no se añade correctamente en la DB lo eliminamos de authentication
                                    mFirebaseAuth.currentUser?.delete()
                                    flagCreate.value = false

                                }
                            }
                        }

                        Log.v("FIREBASE_LOGIN", "SUCCESS_LOGIN")

                    } else {
                        flagCreate.value = false
                        Log.v("FIREBASE_LOGIN", "ERROR, BAD CREDENCIALES")
                    }
                }
            mFirebaseAuth.signInWithEmailAndPassword(userMail,userPassword)
        }

    }*/

}