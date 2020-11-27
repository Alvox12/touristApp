package com.example.tourapp.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import com.example.tourapp.R
import com.example.tourapp.commons.BaseActivity
import com.example.tourapp.commons.Constants
import com.example.tourapp.commons.SharedPreferencesManager
import com.example.tourapp.dataModel.User
import com.example.tourapp.databinding.ActivityLoginBinding
import com.example.tourapp.viewModel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    //private val executor = Executor {}
    lateinit var email : EditText
    lateinit var password : EditText
    lateinit var linkRegister: TextView

    //Observador del booleano loginNotify
    lateinit var observerLogin: Observer<Boolean>

    //Observador del booleano usuario descargado
    lateinit var observerUser: Observer<User>
    private lateinit var user: User

    override fun getLayoutResource(): Int = R.layout.activity_login
    override fun getViewModel(): Class<LoginViewModel> = LoginViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        email = findViewById(R.id.input_email)
        password = findViewById(R.id.input_password)
        linkRegister = findViewById(R.id.link_register)

        observerLogin = Observer {
            if(model.loginNotify.value!!) {
                //loginFinished(SharedPreferencesManager.getSomeBooleanValues(Constants.SAVELOGIN))
                getUserData(it)
                //loginFinished(it)
            }
        }

        model.loginNotify.observe(this, observerLogin)
    }

    override fun onStart() {
        super.onStart()

        linkRegister.setOnClickListener {
            val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
            finish()
        }
    }


    fun onLoginClick(v: View){

        when {
            email.text.toString().isEmpty() || password.text.toString().isEmpty()  -> {
                if (email.text.toString().isEmpty()) tiEmail.error = getString(R.string.ob_campo) else tiEmail.error = null
                if (password.text.toString().isEmpty()) tiPassword.error = getString(R.string.ob_campo) else tiPassword.error = null
            } else -> {
            //cl_loading_user.visibility = View.VISIBLE
            v.hideKeyboard()
            tiEmail.error = null
            tiPassword.error = null
            model.loginData.email = email.text.toString() //Guardo el valor introducido (email) en el viewmodel
            model.loginData.password = password.text.toString() //Guardo el valor introducido (password) en el viewmodel
            model.onLoginClick()
        }
        }
    }

    private fun loginFinished(loginSuccess : Boolean) {

        if (loginSuccess){//Si hace login correctamente

            val intent = Intent(this, MainActivity::class.java)//Entramos a pantallan principal
            intent.putExtra("MyUser", user)
            //intent.putExtra(Constants.USERS, model.user)
            startActivity(intent)
            finish()
            //Cerramos la actividad del login

        } else {//Si no hace login

            val dialogBuilder = AlertDialog.Builder(this)//Mostramos alerta de error en los datos introducidos
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_error_login, null)
            dialogBuilder.setView(dialogView)
            val b = dialogBuilder.create()
            b.show()
            b.setCancelable(false)
            val b_cerrar = dialogView.findViewById<View>(R.id.btnAceptarDialog)

            b_cerrar.setOnClickListener(){
                b.dismiss()
                //cl_loading_user.visibility = View.GONE
            }

        }
    }

    private fun getUserData(loginSuccess: Boolean) {
        observerUser = Observer {user->
            this.user = user
            loginFinished(loginSuccess)
        }
        model.userNotify.observe(this, observerUser)
        model.getUserData()
    }

    //Ocultar teclado
    private fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }


    //Eliminamos observer de loginNotify y lo ponemos a false para evitar probelmas en login
    override fun onStop() {
        super.onStop()
        model.loginNotify.removeObserver(observerLogin)
        model.deleteUserListener()

        if(this::observerUser.isInitialized)
            model.userNotify.removeObserver(observerUser)

        model.loginNotify.value = false
    }

}