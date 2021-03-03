package com.example.tourapp.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.tourapp.R
import com.example.tourapp.commons.BaseActivity
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.User
import com.example.tourapp.databinding.ActivityMainBinding
import com.example.tourapp.viewModel.CommentListViewModel
import com.example.tourapp.viewModel.PlaceDataViewModel
import com.example.tourapp.viewModel.UserViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_logout.view.*
import kotlinx.android.synthetic.main.dialog_edit_comment.*
import kotlinx.android.synthetic.main.dialog_edit_comment.view.*
import kotlinx.android.synthetic.main.dialog_edit_comment.view.btn_edit_cancel
import kotlinx.android.synthetic.main.dialog_score_place.view.*


class MainActivity :  BaseActivity<ActivityMainBinding, UserViewModel>(), NavigationView.OnNavigationItemSelectedListener {

    private  var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var user: User
    lateinit var useredit: User
    //Observador del booleano usuario descargado
    lateinit var observerUser: Observer<User>

    override fun getLayoutResource(): Int = R.layout.activity_main
    override fun getViewModel(): Class<UserViewModel> = UserViewModel::class.java

    private val navController by lazy { findNavController(R.id.nav_host_fragment) } //1
    private val appBarConfiguration by lazy {
        AppBarConfiguration(
                setOf(
                        R.id.userDataFragment
                ), drawer_layout
        )
    } //2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        user = intent.getSerializableExtra("MyUser") as User
        /*val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()*/

        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_host) as NavHostFragment
        //val navController = navHostFragment.navController

        /*observerUser = Observer {user->
            this.user = user
        }
        model.userNotify.observe(this, observerUser)
        model.getUserData()*/

        initNavigation()
    }


    private fun initNavigation() {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawer_layout)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
        nav_view.setNavigationItemSelectedListener(this)

        if(user.userType != Constants.ADMIN){
            nav_view.menu.findItem(R.id.nav_userlist).isEnabled = false
            nav_view.menu.findItem(R.id.nav_userlist).isVisible = false
        }
    }

    /**
     *Activa o descativa la presencia del menu lateral y muestra el boton de la barra superior
     */
    fun setDrawerEnabled(enabled: Boolean) {
        val lockMode = if (enabled)
            DrawerLayout.LOCK_MODE_UNLOCKED
        else
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        drawer_layout.setDrawerLockMode(lockMode)
    }

    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    fun ratePlace(place_model: PlaceDataViewModel) {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_score_place, null)
        val b = dialogBuilder.setView(dialogView).create()

        dialogView.btn_score_cancel.setOnClickListener {
            dialogView.btn_score_accept.isEnabled = false
            b.dismiss()
        }

        dialogView.btn_score_accept.setOnClickListener {
            dialogView.btn_score_cancel.isEnabled = false
            dialogView.ratingBar.isEnabled = false
            val rating = dialogView.ratingBar.rating

            place_model.uploadScoreUser(rating)

            b.dismiss()
        }

        b.setCancelable(false)
        b.show()
    }


    fun editCommentPopup(comment: Comment, comment_model: CommentListViewModel) {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_comment, null)
        val b = dialogBuilder.setView(dialogView).create()

        dialogView.btn_edit_cancel.setOnClickListener {
            dialogView.btn_edit_accept.isEnabled = false
            b.dismiss()
        }

        dialogView.btn_edit_accept.setOnClickListener {
            dialogView.btn_edit_cancel.isEnabled = false
            val textComment = dialogView.et_edit_comment.text.toString()

            if(!textComment.isBlank())
                comment_model.editComment(comment.commentId, textComment)

            b.dismiss()
        }

        b.setCancelable(false)
        b.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_edituser -> {
                useredit = user
                nav_host_fragment.view?.let { view ->
                    Navigation.findNavController(view).navigate(R.id.action_userDataFragment_to_editUserFragment)
                }
            }
            R.id.nav_userlist -> {
                nav_host_fragment.view?.let { view ->
                    Navigation.findNavController(view).navigate(R.id.action_userDataFragment_to_listAuxFragment)
                }
            }
            R.id.nav_placelist -> {
                nav_host_fragment.view?.let { view ->
                    Navigation.findNavController(view).navigate(R.id.action_userDataFragment_to_placeListFragment)
                }
            }
            R.id.opt_edituser -> {
                nav_host_fragment.view?.let { view ->
                    Navigation.findNavController(view).navigate(R.id.action_listAuxFragment_to_editUserFragment)
                }
            }
            R.id.opt_deleteuser -> {
                model.deleteUserData(useredit, useredit.userPassword, user)
            }
            R.id.nav_logout -> showDialogLogout(item)
        }
        return true
    }

    /**
     * Método para desconectar la conexión con firebase
     */
    private fun logout(){
        //SharedPreferencesManager.setSomeBooleanValues(Constants.SAVELOGIN, false)
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

    //Eliminamos observer
   /* override fun onStop() {
        super.onStop()
        model.deleteUserListener()
        model.userNotify.removeObserver(observerUser)
    }*/

}