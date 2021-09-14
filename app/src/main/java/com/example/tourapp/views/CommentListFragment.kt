package com.example.tourapp.views

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.Place
import com.example.tourapp.dataModel.User
import com.example.tourapp.viewModel.CommentListViewModel
import kotlinx.android.synthetic.main.fragment_comment_list.*
import kotlinx.android.synthetic.main.fragment_place_list.*


class CommentListFragment : Fragment()  {

    companion object {
        fun newInstance() = CommentListFragment()
    }

    private lateinit var viewModel: CommentListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager

    /*Datos usuario logueado*/
    private lateinit var user: User
    /*Lugar al que se encuentra asociado a lista de comentarios*/
    private  lateinit var place: Place

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_comment_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CommentListViewModel::class.java)
        /*Se guarda la informacion del lugar para devolverla mas tarde y asi no tener que descargarla de nuevo*/
        this.place = arguments?.get("Comments") as Place
        viewModel.arrayLIstBitmap = arguments?.get("ImagesMap") as ArrayList<Bitmap>

        viewModel.placeId = place.placeId
        /*Se hace una copia de los comentarios asignados al lugar*/
        viewModel.mapComments = place.placeComments

        this.user = (activity as MainActivity).user
        viewModel.user = this.user

        // Preparamos vuelta a la vista anterior personalizada
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            prepareCallback()
        }
        callback.isEnabled = true

        setListeners()

        manager = LinearLayoutManager(this.activity)
        viewModel.configAdapter()

        recyclerView = recycler_comment_view.apply {
            layoutManager = manager
            adapter =  viewModel.myAdapter
        }

        /*Los comentarios pueden ser descargados de MAX_DATABSE_ITEMS en MAX_DATABSE_ITEMS, para descargar
        * nuevos comentarios el usuario ha de bajar a la parte inferior de la vista*/
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //direction integers: -1 for up, 1 for down, 0 will always return false
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if(viewModel.descargas >= Constants.MAX_DATABASE_ITEMS) {
                        viewModel.loadNewData()
                    }
                }
            }
        })

        viewModel.setCommentList()

        viewModel.commentIndex = 0
        viewModel.descargas = 0
        
        viewModel.loadChildEventListener()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { // ActionBar back/parent button is pressed
            prepareCallback()
            return true
        }
        return true
    }

    /**Antes de volver a la pantalla anterior enviamos unos objetos para no tener que
     * descargarlos de la BBDD*/
    private fun prepareCallback() {

        val bundle : Bundle = Bundle()

        view.let {
            if (it != null) {
                //Navigation.findNavController(it).navigate(R.id.action_commentListFragment_to_placeDataFragment, bundle)
                val navController = Navigation.findNavController(it)
                navController.previousBackStackEntry?.savedStateHandle?.set("key", bundle)
                navController.popBackStack()
            }
        }
    }

    private fun setListeners() {
        bt_send_comment.setOnClickListener {
            val comment = getComment()

            if (comment != null) {
                viewModel.addComment(comment)
            }
        }
    }

    /**Obtiene el mensaje escrito por el usuario y lo convierte en un objeto de tipo Comment*/
    private fun getComment(): Comment? {
        val txt = et_message_comment.text.toString()

        if(!txt.isBlank()) {
            et_message_comment.setText("")
            var idComment = Comment().generateId()
            while (viewModel.mapComments.containsKey(idComment)) {
                idComment = Comment().generateId()
            }
            val idUser = this.user.userId
            val nameUser = this.user.userName

            (activity as MainActivity).closeKeyboard()

            return Comment(txt, idUser, nameUser, idComment)
        }

        return null
    }



    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeChildListener()
    }

}