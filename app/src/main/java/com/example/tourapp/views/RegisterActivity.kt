package com.example.tourapp.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.Observer
import com.example.tourapp.R
import com.example.tourapp.commons.BaseActivity
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.example.tourapp.databinding.ActivityRegisterBinding
import com.example.tourapp.viewModel.LoginViewModel
import com.example.tourapp.viewModel.RegisterViewModel
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity<ActivityRegisterBinding, RegisterViewModel>() {

    lateinit var email : EditText
    lateinit var password : EditText
    lateinit var nameuser: EditText
    lateinit var backLogin: ImageView

    private lateinit var user: User

    //Observador del booleano registerNotify
    lateinit var observerRegister: Observer<Boolean>

    override fun getLayoutResource(): Int = R.layout.activity_register
    override fun getViewModel(): Class<RegisterViewModel> = RegisterViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = findViewById(R.id.input_email)
        password = findViewById(R.id.input_password)
        nameuser = findViewById(R.id.input_name)
        backLogin = findViewById(R.id.backToLogin)


        observerRegister = Observer {
            if(model.flagCreate.value!!)
            //loginFinished(SharedPreferencesManager.getSomeBooleanValues(Constants.SAVELOGIN))
                registerFinished(it)
        }

        model.flagCreate.observe(this, observerRegister)
    }

    override fun onStart() {
        super.onStart()

        backLogin.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
    }

    fun onRegisterClick(v: View) {

        model.addNewUser(
                userName = input_name.text.toString(),
                userType = Constants.NORMAL,
                userMail = input_email.text.toString(),
                userPassword = input_password.text.toString()
        )

    }

    private fun registerFinished(registerSuccess: Boolean) {

        if (registerSuccess){//Si hace login correctamente

            val intent = Intent(this, MainActivity::class.java)//Entramos a pantallan principal
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

    //Ocultar teclado
    private fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
}