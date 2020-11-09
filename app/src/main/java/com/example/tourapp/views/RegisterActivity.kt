package com.example.tourapp.views

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
import com.example.tourapp.databinding.ActivityRegisterBinding
import com.example.tourapp.viewModel.LoginViewModel

class RegisterActivity : BaseActivity<ActivityRegisterBinding, LoginViewModel>() {

    lateinit var email : EditText
    lateinit var password : EditText
    lateinit var nameuser: EditText
    lateinit var backLogin: ImageView

    //Observador del booleano registerNotify
    lateinit var observerRegister: Observer<Boolean>

    override fun getLayoutResource(): Int = R.layout.activity_register
    override fun getViewModel(): Class<LoginViewModel> = LoginViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = findViewById(R.id.input_email)
        password = findViewById(R.id.input_password)
        nameuser = findViewById(R.id.input_name)
        backLogin = findViewById(R.id.backToLogin)


        observerRegister = Observer {
            if(model.registerNotify.value!!)
            //loginFinished(SharedPreferencesManager.getSomeBooleanValues(Constants.SAVELOGIN))
                registerFinished(it)
        }

        model.registerNotify.observe(this, observerRegister)
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

    }

    private fun registerFinished(registerSuccess: Boolean) {

    }

    //Ocultar teclado
    private fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
}