package com.example.tourapp.views

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.example.tourapp.commons.Constants.Companion.ERROR_DIALOG_REQUEST
import com.example.tourapp.commons.Constants.Companion.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.example.tourapp.commons.Constants.Companion.PERMISSIONS_REQUEST_ENABLE_GPS
import com.example.tourapp.commons.Constants.Companion.SERVICE_TAG
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.User
import com.example.tourapp.databinding.ActivityMainBinding
import com.example.tourapp.viewModel.CommentListViewModel
import com.example.tourapp.viewModel.PlaceDataViewModel
import com.example.tourapp.viewModel.UserViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_edit_comment.*
import kotlinx.android.synthetic.main.dialog_edit_comment.view.*
import kotlinx.android.synthetic.main.dialog_logout.view.*
import kotlinx.android.synthetic.main.dialog_score_place.view.*
import kotlinx.android.synthetic.main.nav_header.*


class MainActivity :  BaseActivity<ActivityMainBinding, UserViewModel>(), NavigationView.OnNavigationItemSelectedListener {

    private  var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var user: User
    lateinit var useredit: User
    //Observador del booleano usuario descargado
    lateinit var observerUser: Observer<User>

    //Booleano que indica si hay permiso localizacion
    var mLocationPermissionGranted: Boolean = false

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

        initNavigation()
    }


    private fun initNavigation() {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawer_layout)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
        nav_view.setNavigationItemSelectedListener(this)

        val viewHeader = nav_view.getHeaderView(0)
        val tvName = viewHeader.findViewById<TextView>(R.id.tv_name)
        val tvEmail = viewHeader.findViewById<TextView>(R.id.tv_email)

        tvName.text = user.userName
        tvEmail.text = user.userMail

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
        mFirebaseAuth.signOut()
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


    fun checkMapServices(): Boolean {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true
            }
        }
        return false
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
                }
        val alert = builder.create()
        alert.show()
    }

    fun isMapsEnabled(): Boolean {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
            //TODO INVOCAR FUNCION GOOGLE MAPS
            //getChatrooms()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    fun isServicesOK(): Boolean {
        Log.d(SERVICE_TAG, "isServicesOK: checking google services version")
        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MainActivity)
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(SERVICE_TAG, "isServicesOK: Google Play Services is working")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(SERVICE_TAG, "isServicesOK: an error occured but we can fix it")
            val dialog: Dialog? = GoogleApiAvailability.getInstance().getErrorDialog(this@MainActivity, available, ERROR_DIALOG_REQUEST)
            dialog?.show()
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String?>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(SERVICE_TAG, "onActivityResult: called.")
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                if (mLocationPermissionGranted) {
                    //TODO INVOCAR FUNCION GOOGLE MAPS
                    //getChatrooms()
                } else {
                    getLocationPermission()
                }
            }
        }
    }

}