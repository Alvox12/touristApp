package com.example.tourapp.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
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
import com.example.tourapp.viewModel.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_delete_account.view.*
import kotlinx.android.synthetic.main.dialog_edit_comment.*
import kotlinx.android.synthetic.main.dialog_edit_comment.view.*
import kotlinx.android.synthetic.main.dialog_edit_comment.view.btn_edit_accept
import kotlinx.android.synthetic.main.dialog_edit_comment.view.btn_edit_cancel
import kotlinx.android.synthetic.main.dialog_edit_list_name.view.*
import kotlinx.android.synthetic.main.dialog_logout.view.*
import kotlinx.android.synthetic.main.dialog_name_list.view.*
import kotlinx.android.synthetic.main.dialog_score_place.view.*
import kotlinx.android.synthetic.main.nav_header.*
import java.io.InputStream


class MainActivity :  BaseActivity<ActivityMainBinding, UserViewModel>(), NavigationView.OnNavigationItemSelectedListener {

    private  var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var user: User
    lateinit var useredit: User
    //Observador del booleano usuario descargado
    lateinit var observerUser: Observer<User>


    //var mapTags: MutableMap<Int, String> = mutableMapOf()
    var arrayListTags: ArrayList<String> = arrayListOf()
    private lateinit var mListenerTags : ValueEventListener
    private var tagsDownloaded: MutableLiveData<Boolean> = MutableLiveData()
    lateinit var observerTags: Observer<Boolean>

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

        //Recibimos datos de usuario recien logueado
        user = intent.getSerializableExtra("MyUser") as User

        tagsDownloaded.value = false
        observerTags = Observer {
            if(tagsDownloaded.value == true)
                deleteListeners()
        }
        tagsDownloaded.observe(this, observerTags)

        getTags()
        initNavigation()
    }


    /**Establece el emnu lateral y la cabecera del mismo*/
    private fun initNavigation() {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawer_layout)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
        nav_view.setNavigationItemSelectedListener(this)

        val viewHeader = nav_view.getHeaderView(0)
        val tvName = viewHeader.findViewById<TextView>(R.id.tv_name)
        val tvEmail = viewHeader.findViewById<TextView>(R.id.tv_email)

        //Ponemos la primera letra del nombre a mayuscula
        editMenuName(user.userName)

        if(user.userType != Constants.ADMIN){
            nav_view.menu.findItem(R.id.nav_userlist).isEnabled = false
            nav_view.menu.findItem(R.id.nav_userlist).isVisible = false
        }

    }

    /**Establece el nombre del menu lateral de la cabecera*/
    fun editMenuName(nameStr: String) {
        val viewHeader = nav_view.getHeaderView(0)
        val tvName = viewHeader.findViewById<TextView>(R.id.tv_name)
        val tvEmail = viewHeader.findViewById<TextView>(R.id.tv_email)

        val nameAux = nameStr.substring(0, 1).toUpperCase() + nameStr.substring(1)

        //Cambiamos el tamaño de la fuente. Puedes cambiar 2f como desees.
        //Change font size of the first character. You can change 2f as you want
        val spannableString = SpannableString(nameAux)
        spannableString.setSpan(RelativeSizeSpan(2f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvName.text = spannableString
        tvEmail.text = user.userMail
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

    /**Cierra el teclado virtual*/
    fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    /**Ventana flotante para modificar la puntuación de un lugar y actualizarla*/
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
            //ratingBar es un objeto que con un numero de estrellas predeterminado puedes establecer una puntuación
            dialogView.ratingBar.isEnabled = false
            val rating = dialogView.ratingBar.rating

            place_model.uploadScoreUser(rating)

            b.dismiss()
        }

        b.setCancelable(false)
        b.show()
    }


    /**Ventana flotante para editar el nombre de una de las listas del usuario*/
    fun addCustomListName(list_model: PlaceCreateListViewModel) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_name_list, null)
        val b = dialogBuilder.setView(dialogView).create()

        dialogView.btn_name_list_cancel.setOnClickListener {
            dialogView.btn_name_list_accept.isEnabled = false
            b.dismiss()
        }

        dialogView.btn_name_list_accept.setOnClickListener {
            dialogView.btn_name_list_cancel.isEnabled = false
            dialogView.et_name_list.isEnabled = false
            val name: String = dialogView.et_name_list.text.toString()

            if(name.isNotBlank() && list_model.listPlacesSelected?.isNotEmpty()!!) {
                list_model.uploadListPlace(name)
            }
            else {
                Toast.makeText(this, "No hay ningún nombre escrito", Toast.LENGTH_SHORT).show()
            }

            b.dismiss()
        }

        b.setCancelable(false)
        b.show()
    }

    /**Ventana flotante en la cual se puede editar un comentario de la lista de comentarios*/
    fun editCommentPopup(comment: Comment, comment_model: CommentListViewModel) {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_comment, null)
        val b = dialogBuilder.setView(dialogView).create()

        dialogView.et_edit_comment.setText(comment.commentTxt)

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

    fun editListNamePopup(listId: String, position: Int, oldName: String, model: UserListOfListsViewModel) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_list_name, null)
        val b = dialogBuilder.setView(dialogView).create()

        dialogView.et_edit_list_name.setText(oldName)

        dialogView.btn_edit_cancel.setOnClickListener {
            dialogView.btn_edit_accept.isEnabled = false
            b.dismiss()
        }

        dialogView.btn_edit_accept.setOnClickListener {
            dialogView.btn_edit_cancel.isEnabled = false
            val textName = dialogView.et_edit_list_name.text.toString()

            if(!textName.isBlank())
                model.changeListName(listId, textName, position)

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
                    Navigation.findNavController(view).navigate(R.id.action_userDataFragment_to_userOptionsFragment)
                }
            }
            R.id.nav_userlist -> {
                nav_host_fragment.view?.let { view ->
                    Navigation.findNavController(view).navigate(R.id.action_userDataFragment_to_listAuxFragment)
                }
            }
            R.id.nav_placelist -> {
                nav_host_fragment.view?.let { view ->
                    val bundle = Bundle()
                    bundle.putBoolean("CustomList", false) //Descargamos toda la lista de lugares
                    Navigation.findNavController(view).navigate(R.id.action_userDataFragment_to_placeListFragment, bundle)
                }
            }
            R.id.opt_edituser -> {
                nav_host_fragment.view?.let { view ->
                    Navigation.findNavController(view).navigate(R.id.action_listAuxFragment_to_editUserFragment)
                }
            }
            R.id.opt_deleteuser -> {
                deleteAccountPopupAdmin()
            }
            R.id.nav_user_custom_lists -> {
                nav_host_fragment.view?.let { view ->
                    Navigation.findNavController(view).navigate(R.id.action_userDataFragment_to_userListOfListsFragment)
                }
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
     * Funcion que muestra una ventana flotante para cerrar sesion de la aplicacion
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


    /**Descargamos todas las etiquetas o tags disponibles*/
    private fun getTags() {
        arrayListTags.clear()
        arrayListTags.add("Selecciona etiqueta")
        //Ruta de la base de datos donde se encuentran las etiquetas
        val tagsRef = FirebaseDatabase.getInstance().getReference(Constants.ETIQUETAS)

        mListenerTags = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_TAGS", "ERROR AL DESCARGAR INFO")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.v("FIREBASE_BBDD_TAGS", "EXITO AL DESCARGAR INFO")

                snapshot.children.forEach {
                    //arrayListTags[it.key!!.toInt()] = it.value.toString()
                    arrayListTags.add(it.value.toString())
                }

                tagsDownloaded.value = true
            }

        }

        tagsRef.addValueEventListener(mListenerTags)
    }

    /**Una vez los tags han sido descargados eliminamos las funciones de escucha ya que no son necesarios*/
    private fun deleteListeners() {
        val tagsRef = FirebaseDatabase.getInstance().getReference(Constants.ETIQUETAS)
        tagsRef.removeEventListener(mListenerTags)
        tagsDownloaded.removeObserver(observerTags)
    }


    /**
     * Comprueba si la imagen del icono tiene las medidas correctas*/
    fun validPicture(uri: Uri?): Boolean {

        var valido = false

        val input: InputStream? = uri?.let { contentResolver?.openInputStream(it) }
        val bitmap = BitmapFactory.decodeStream(input)

        if(bitmap.height <= Constants.ICON_MAX_SIZE4 && bitmap.width <= Constants.ICON_MAX_SIZE4)
            valido = true

        input?.close()

        return valido
    }

    /**Ventana flotante que pregunta al usuario si desea eliminar o dar de baja su cuenta en Firebase*/
    fun deleteAccountPopup() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_account, null)
        val b = dialogBuilder.setView(dialogView).create()


        dialogView.btn_delete_cancel.setOnClickListener {
            dialogView.btn_delete_accept.isEnabled = false
            b.dismiss()
        }

        dialogView.btn_delete_accept.setOnClickListener {
            dialogView.btn_delete_cancel.isEnabled = false
            darDeBaja()

            b.dismiss()
        }

        b.setCancelable(false)
        b.show()
    }

    /**
     * Ventana flotante al eliminar usuario de la lista de usuarios
     */
    private fun deleteAccountPopupAdmin() {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_user_fromlist, null)
        val b = dialogBuilder.setView(dialogView).create()

        dialogView.btn_delete_cancel.setOnClickListener {
            dialogView.btn_delete_accept.isEnabled = false
            b.dismiss()
        }

        dialogView.btn_delete_accept.setOnClickListener {
            dialogView.btn_delete_cancel.isEnabled = false
            model.deleteUserData(useredit, useredit.userPassword, user)

            b.dismiss()
        }

        b.setCancelable(false)
        b.show()
    }

    /**
     * Ventana flotante al eliminar elemento de lista personalizada usuario
     */
    fun deleteUserListElement(listmodel: UserListOfListsViewModel, nombre:String, listId:String, position: Int) {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_userlist_elem, null)
        val b = dialogBuilder.setView(dialogView).create()

        dialogView.btn_delete_cancel.setOnClickListener {
            dialogView.btn_delete_accept.isEnabled = false
            b.dismiss()
        }

        dialogView.btn_delete_accept.setOnClickListener {
            dialogView.btn_delete_cancel.isEnabled = false

            listmodel.deleteList(listId, position)
            Toast.makeText(this, "Lista $nombre eliminada", Toast.LENGTH_SHORT).show()

            b.dismiss()
        }

        b.setCancelable(false)
        b.show()
    }

    /**Función que elimina la cuenta de los servicios de firebase*/
    private fun darDeBaja() {
        val userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS)
        mFirebaseAuth.currentUser?.let { fuser ->
            //Se eliminan los datos del usuario de la base de datos
            userRef.child(fuser.uid).removeValue().addOnCompleteListener { it1->
                if(it1.isSuccessful) {
                    //Se da de baja al usuario
                    mFirebaseAuth.currentUser!!.delete().addOnSuccessListener {
                        //Se devuelve al usuario a la pantalla de login
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
        } }
    }


}