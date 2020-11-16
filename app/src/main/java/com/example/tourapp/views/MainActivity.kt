package com.example.tourapp.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.example.tourapp.R
import com.example.tourapp.commons.BaseActivity
import com.example.tourapp.commons.Constants
import com.example.tourapp.commons.SharedPreferencesManager
import com.example.tourapp.databinding.ActivityMainBinding
import com.example.tourapp.viewModel.UserViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_logout.view.*


class MainActivity :  BaseActivity<ActivityMainBinding, UserViewModel>(), NavigationView.OnNavigationItemSelectedListener {

    private  var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun getLayoutResource(): Int = R.layout.activity_main
    override fun getViewModel(): Class<UserViewModel> = UserViewModel::class.java

    private val navController by lazy { findNavController(R.id.nav_host_fragment) } //1
    private val appBarConfiguration by lazy {
        AppBarConfiguration(
                setOf(
                        R.id.userDataFragment,
                        R.id.editUserFragment
                ), drawer_layout
        )
    } //2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        /*val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()*/

        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_host) as NavHostFragment
        //val navController = navHostFragment.navController

        initNavigation()
    }

    private fun initNavigation() {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController (this, navController, drawer_layout)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> showDialogLogout(item)
        }
        return true
    }

    /**
     * Método para desconectar la conexión con firebase
     */
    private fun logout(){
        SharedPreferencesManager.setSomeBooleanValues(Constants.SAVELOGIN, false)
        mFirebaseAuth?.signOut()
        Log.v("FIREBASE_LOGOUT", "LOGOUT")
    }


    /**
     * Funcion que infla una modal para desloguear al usuario de la app
     */
    private fun showDialogLogout(itemId: MenuItem) {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val b = dialogBuilder.setView(dialogView).create()

        dialogView.btnCerrar.setOnClickListener{
            itemId.isChecked = false
            b.dismiss()
        }

        dialogView.btnAceptar.setOnClickListener{
            logout()
            startActivity(Intent(this, LoginActivity::class.java))
            //SharedPreferencesManager.setSomeStringValues(Constants.PASSWORD, "") No quito la pass de shared preferences
            b.dismiss()
            finish()
        }

        b.setCancelable(false)
        b.show()
    }

}