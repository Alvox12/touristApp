package com.example.tourapp.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tourapp.R
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.example.tourapp.viewModel.RegisterViewModel
import kotlinx.android.synthetic.main.dialog_error_register.view.*
import kotlinx.android.synthetic.main.fragment_register_user.*

class RegisterActivity2 : AppCompatActivity() {

    lateinit var viewModel: RegisterViewModel

    /*Lista completa de preferencias de usuarios*/
    var arrayListTags: ArrayList<String> = arrayListOf()

    //Observador del booleano registerNotify
    lateinit var observerRegister: Observer<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity2)

        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, RegisterUserFragment.newInstance())
                .commitNow()
        }
    }

    override fun onStart() {
        super.onStart()

        /*Se activa cuando varia el valor de flagCreate (tras registrar al usuario en la BBDD)*/
        observerRegister = Observer {
            if(viewModel.flagCreate.value!!) {
                //loginFinished(SharedPreferencesManager.getSomeBooleanValues(Constants.SAVELOGIN))
                registerFinished(it, viewModel.user)
            }
            else {
                /*Muestra mensaje error*/
                dialogErrorMsg()
            }
        }

        viewModel.flagCreate = MutableLiveData()
        viewModel.flagCreate.observe(this, observerRegister)
    }

    /*Función registrar usuario en base de datos*/
    fun onRegisterClick() {

        viewModel.addNewUser(
            userName = input_name.text.toString(),
            userType = Constants.NORMAL,
            userMail = input_email.text.toString(),
            userPassword = input_password.text.toString()
        )

    }

    /*Registro finalizado*/
    fun registerFinished(registerSuccess: Boolean, user: User) {

        if (registerSuccess){//Si hace login correctamente entra aplicación

            val intent = Intent(this, MainActivity::class.java)//Entramos a pantallan principal
            intent.putExtra("MyUser", user)
            //intent.putExtra(Constants.USERS, model.user)
            startActivity(intent)
            finish()
            //Cerramos la actividad del login

        } else {//Si no hace login mensaje error

            dialogErrorMsg()
        }

    }

    fun showTagFragment() {
        hideSoftKeyboard()
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, RegisterTagsFragment.newInstance(), "tags_fragment")
            .addToBackStack(null)
            .commit()
    }

    fun hideSoftKeyboard() {
        //this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        val view = this.currentFocus
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun dialogErrorMsg() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_error_register, null)
        val b = dialogBuilder.setView(dialogView).create()

        dialogView.btnAceptarDialog.setOnClickListener {
            b.dismiss()
            onBackPressed()
        }

        b.setCancelable(false)
        b.show()
    }

    fun backToLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.flagCreate.removeObserver(observerRegister)
        viewModel.flagCreate.value = false
    }

}