package com.example.tourapp.views

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
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
    private lateinit var user: User
    private  lateinit var place: Place

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_comment_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CommentListViewModel::class.java)
        this.place = arguments?.get("Comments") as Place
        viewModel.arrayLIstBitmap = arguments?.get("ImagesMap") as ArrayList<Bitmap>

        viewModel.placeId = place.placeId
        viewModel.mapComments = place.placeComments

        this.user = (activity as MainActivity).user
        viewModel.user = this.user

        // This callback will only be called when MyFragment is at least Started.
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

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //direction integers: -1 for up, 1 for down, 0 will always return false
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if(viewModel.descargas >= Constants.MAX_DATABASE_ITEMS) {
                        viewModel.loadNewData()
                    }
                    Toast.makeText((activity as MainActivity), "endOfScroll", Toast.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.setCommentList()

        viewModel.commentIndex = 0
        viewModel.descargas = 0
        
        viewModel.loadChildEventListener()
    }

    private fun prepareCallback() {

        val bundle : Bundle = Bundle()
        val arrayBitmap: ArrayList<Bitmap> = viewModel.arrayLIstBitmap

        bundle.putParcelableArrayList("ImagesMap", arrayBitmap)
        bundle.putSerializable("Place", this.place)
        bundle.putString("Previous", "Comments")

        /*val manager: FragmentManager = (activity as MainActivity).supportFragmentManager
        val trans: FragmentTransaction = manager.beginTransaction()

       val fragment: Fragment = PlaceDataFragment()
        fragment.arguments = bundle
        manager.popBackStack()*/


        //manager.popBackStack(R.id.commentListFragment, 0)
       // manager.popBackStackImmediate()

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