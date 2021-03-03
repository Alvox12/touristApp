package com.example.tourapp.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.Place
import com.example.tourapp.dataModel.User
import com.example.tourapp.viewModel.CommentListViewModel
import kotlinx.android.synthetic.main.fragment_comment_list.*
import kotlinx.android.synthetic.main.fragment_place_list.*


class CommentListFragment : Fragment() {

    companion object {
        fun newInstance() = CommentListFragment()
    }

    private lateinit var viewModel: CommentListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager
    private lateinit var user: User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_comment_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CommentListViewModel::class.java)
        val place = arguments?.get("Comments") as Place
        viewModel.placeId = place.placeId
        viewModel.mapComments = place.placeComments

        this.user = (activity as MainActivity).user
        viewModel.userId = this.user.userId

        setListeners()

        manager = LinearLayoutManager(this.activity)
        viewModel.configAdapter()

        recyclerView = recycler_comment_view.apply {
            layoutManager = manager
            adapter =  viewModel.myAdapter
        }

        viewModel.setCommentList()

        viewModel.loadChildEventListener()
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
            val idComment = Comment().generateId()
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
        (activity as MainActivity).setDrawerEnabled(true)
    }

}