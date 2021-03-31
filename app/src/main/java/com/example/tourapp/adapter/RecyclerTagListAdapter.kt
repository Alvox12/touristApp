package com.example.tourapp.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.viewModel.PlaceAddViewModel
import com.example.tourapp.viewModel.PlaceListViewModel
import java.util.*
import kotlin.collections.ArrayList

class RecyclerTagListAdapter(val list: ArrayList<String>): RecyclerView.Adapter<RecyclerTagListAdapter.ViewHolder>() {

    private var listTags: ArrayList<String>? = arrayListOf()
    private var listTagsSelected: ArrayList<Boolean>? = arrayListOf()
    private var parent: ViewGroup? = null

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view){

        fun bind(tag: String, parent: ViewGroup){
            val radioButton = view.findViewById<RadioButton>(R.id.radioButton)
            radioButton.text = tag
            /*val tvname = view.findViewById<TextView>(R.id.tv_place_name)
            val tvdescription = view.findViewById<TextView>(R.id.tv_place_description)
            val tvscore = view.findViewById<TextView>(R.id.tv_place_score)
            tvname.text = place.placeName
            tvdescription.text = place.placeDescription
            tvscore.text = place.placeScore.toString()*/
        }
    }

    init {
        this.listTags = list.clone() as ArrayList<String>
        this.listTagsSelected = ArrayList(Collections.nCopies(list.size, false))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = LayoutInflater.from(parent.context).inflate(R.layout.tags_item_list, parent,false)
        this.parent = parent
        return ViewHolder(vh)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        listTags?.get(position)?.let {
            this.parent?.let { parent ->
                holder.bind(it, parent)

                val radioBtn = holder.view.findViewById<RadioButton>(R.id.radioButton)

                radioBtn.setOnClickListener {
                    listTagsSelected!![position] = !listTagsSelected?.get(position)!!
                }
            }
        }
    }


    fun setTagsList(list_aux: ArrayList<String>) {
        this.listTags?.clear()
        this.listTags = list_aux.clone() as ArrayList<String>

        //this.listTagsSelected = ArrayList(Collections.nCopies(list_aux.size, false))
    }

    fun getTagsSelected(): ArrayList<Boolean>? {
        return this.listTagsSelected
    }

    override fun getItemCount() = this.listTags?.size ?: 0

}