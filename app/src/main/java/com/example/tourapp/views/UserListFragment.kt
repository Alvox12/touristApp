package com.example.tourapp.views

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.viewModel.UserListViewModel
import kotlinx.android.synthetic.main.fragment_user_list.*

class UserListFragment : Fragment() {

    companion object {
        fun newInstance() = UserListFragment()
    }



    private lateinit var viewModelUser: UserListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager
    //private lateinit var myAdapter: RecyclerView.Adapter<*>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).setDrawerEnabled(false)
        return inflater.inflate(R.layout.fragment_user_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModelUser = ViewModelProvider(this).get(UserListViewModel::class.java)

        manager = LinearLayoutManager(this.activity)
        viewModelUser.configAdapter()

        recyclerView = recycler_view.apply {
            layoutManager = manager
            adapter =  viewModelUser.myAdapter
        }

        viewModelUser.getUserList()
    }

    /**Eliminamos observer*/
    override fun onDestroyView() {
        super.onDestroyView()
        viewModelUser.deleteUserListener()
        (activity as MainActivity).setDrawerEnabled(true)
    }

}