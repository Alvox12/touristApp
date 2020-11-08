package com.example.tourapp.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tourapp.R
import com.example.tourapp.commons.SharedPreferencesManager

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LoginFragment.newInstance())
                .commitNow()
        }

        
    }
}