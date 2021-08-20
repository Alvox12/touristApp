package com.example.tourapp.viewModel

import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerRegisterTagsAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterViewModel : ViewModel() {

    //var registerNotify: MutableLiveData<Boolean> = MutableLiveData()
    private  var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var user: User
    var userClient: MutableLiveData<User> = MutableLiveData()
    var flagCreate: MutableLiveData<Boolean> = MutableLiveData()
    var tagLiveData: MutableLiveData<Boolean> = MutableLiveData()

    var arrayTags: ArrayList<String> = arrayListOf()

    lateinit var myAdapter: RecyclerRegisterTagsAdapter

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

    fun configAdapter() {
        myAdapter = RecyclerRegisterTagsAdapter(arrayTags)
        myAdapter.notifyDataSetChanged()
    }

    /**
     * Funcion para autenticar al usuario y añadirlo en DB
     */
    private fun onSignUp() {

        if(user.userMail.isNotEmpty() && user.userPassword.isNotEmpty()) {
            mFirebaseAuth.signInAnonymously().addOnCompleteListener { result ->
                if(result.isSuccessful) {
                    tagLiveData.value = true
                    Log.v("FIREBASE_LOGIN", "EXITO, REGISTRO ANONIMO")
                }
                else {
                    tagLiveData.value = false
                    Log.v("FIREBASE_LOGIN", "ERROR, REGISTRO ANONIMO")
                }
            }
            /*mFirebaseAuth.createUserWithEmailAndPassword(user.userMail, user.userPassword)
                    .addOnCompleteListener {result ->

                        if(result.isSuccessful) {
                            Log.v("FIREBASE_LOGIN", "SUCCESS_LOGIN")
                            //uploadUserData(usrPassAux)
                            loginAfterSignUp(usrPassAux)
                        }
                        else {
                            flagCreate.value = false
                            Log.v("FIREBASE_LOGIN", "ERROR, BAD CREDENCIALES")
                        }
                    }*/

            //mFirebaseAuth.signInWithEmailAndPassword(userMail,userPassword)
        }

    }

    fun createUser(userPass: String) {

        val usrPassAux = user.userPassword
        mFirebaseAuth.signOut()

        mFirebaseAuth.createUserWithEmailAndPassword(user.userMail, user.userPassword)
            .addOnCompleteListener {result ->

                if(result.isSuccessful) {
                    Log.v("FIREBASE_LOGIN", "SUCCESS_LOGIN")
                    loginAfterSignUp(usrPassAux)
                }
                else {
                    flagCreate.value = false
                    Log.v("FIREBASE_LOGIN", "ERROR, BAD CREDENCIALES")
                }
            }
    }

    /**
     * Tras registrar al usuario nos entramos en su cuenta.
     */
    private fun loginAfterSignUp(usrPassAux: String) {

        mFirebaseAuth?.signInWithEmailAndPassword(user.userMail, usrPassAux)
            ?.addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.v("FIREBASE_LOGIN", "SUCCESS_LOGIN")
                    uploadUserData(usrPassAux)
                }
                else {
                    Log.v("FIREBASE_LOGIN", "ERROR, BAD CREDENCIALES")
                    flagCreate.value = false
                }
            }
    }

    fun uploadUserData(userPass: String) {
        user.userPassword =  Base64.encodeToString(user.userPassword.toByteArray(), Base64.DEFAULT)
        mFirebaseAuth.uid?.let { uid ->

            user.userId = uid
            //Damos de alta el usuraio en el nodo User
            refUser.child(uid).setValue(user).addOnCompleteListener { result ->

                if (result.isSuccessful) {
                    Log.v("FIREBASE_BBDD", "SUCCESS_UPLOAD")
                    uploadUserPrefs(userPass)
                }
            }
        }
    }

    private fun uploadUserPrefs(userPass: String) {
        val prefRef = FirebaseDatabase.getInstance().getReference(Constants.USERS).child(user.userId).child(Constants.USERPREFS)

        val msg = getMsg()
        user.userPrefs = msg
        user.arrayPrefs = getPrefs(msg)

        prefRef.setValue(msg).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_TAGS_UPLOAD")
                flagCreate.value = true
            }
        }
    }

    private fun getPrefs(prefs: String): ArrayList<Int> {
        val listInt: ArrayList<Int> = arrayListOf()

        if(prefs != "") {
            val list: List<String> = prefs.split(",")
            list.forEach { str ->
                listInt.add(Integer.parseInt(str))
            }
        }
        return listInt
    }

    private fun getMsg(): String {
        val selected = myAdapter.getTagsSelected()
        var msg = ""
        var firstTag = true
        for((index, elem) in arrayTags.withIndex()) {
            if(selected?.get(index) == true) {
                if(firstTag) {
                    firstTag = false
                    msg += index
                }
                else {
                    msg += ",${index}"
                }
            }
        }

        return msg
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