package com.example.tourapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.commons.Constants
import com.example.tourapp.viewModel.UserListOfListsViewModel
import kotlinx.android.synthetic.main.fragment_place_list.*
import kotlinx.android.synthetic.main.fragment_user_list_of_lists.*


class UserListOfListsFragment : Fragment() {

    companion object {
        fun newInstance() = UserListOfListsFragment()
    }

    private lateinit var viewModel: UserListOfListsViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setDrawerEnabled(false)
        return inflater.inflate(R.layout.fragment_user_list_of_lists, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(UserListOfListsViewModel::class.java)

        val user = (activity as MainActivity).user
        viewModel.user = user

        manager = LinearLayoutManager(this.activity)
        recyclerView = rv_list_lists.apply {
            layoutManager = manager
            adapter = viewModel.adapter
        }


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fab_add_list.isShown)
                    fab_add_list.hide()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fab_add_list.show()

                if(user.userType == Constants.ADMIN) {
                    //direction integers: -1 for up, 1 for down, 0 will always return false
                    if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                        if(viewModel.descargas >= Constants.MAX_DATABASE_ITEMS) {
                            viewModel.loadNewData()
                        }
                    }
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        fab_add_list.setOnClickListener { btnView ->
            view.let {
                if (it != null) {
                    val bundle = Bundle()
                    bundle.putBoolean("newList", true)
                    bundle.putStringArrayList("listCodes", viewModel.listCodes)
                    bundle.putString("ListCreator", user.userId)
                    Navigation.findNavController(it).navigate(R.id.action_userListOfListsFragment_to_placeCreateListFragment2, bundle)
                }
            }
        }

        if(user.userType == Constants.ADMIN) {
            viewModel.listIndex = 0
            viewModel.descargas = 0
            viewModel.getAllLists()
        }
        else
            viewModel.getLists()

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.deleteListener()
        (activity as MainActivity).setDrawerEnabled(true)
    }

}