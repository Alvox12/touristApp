package com.example.tourapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.children
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import java.util.*
import kotlin.collections.ArrayList

class RecyclerRegisterTagsAdapter(val list: ArrayList<String>): RecyclerView.Adapter<RecyclerRegisterTagsAdapter.ViewHolder>() {

    private var listTags: ArrayList<String>? = arrayListOf()
    var listTagsSelected: ArrayList<Boolean>? = arrayListOf()
    private var parent: ViewGroup? = null
    private lateinit var radioGroup: RadioGroup

    private var arrayRadioBtn: ArrayList<RadioButton> = arrayListOf()
    private var arrayRadioGroup: ArrayList<RadioGroup> = arrayListOf()

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view){

        fun bind(tag: String, select: Boolean, parent: ViewGroup){
            val radioButton = view.findViewById<RadioButton>(R.id.radioButton)
            radioButton.text = tag
            radioButton.isChecked = select
            /*val tvname = view.findViewById<TextView>(R.id.tv_place_name)
            val tvdescription = view.findViewById<TextView>(R.id.tv_place_description)
            val tvscore = view.findViewById<TextView>(R.id.tv_place_score)
            tvname.text = place.placeName
            tvdescription.text = place.placeDescription
            tvscore.text = place.placeScore.toString()*/
        }

        fun getRadioBtn(): RadioButton {
            return view.findViewById<RadioButton>(R.id.radioButton)
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

                val select = listTagsSelected?.get(position) == true
                holder.bind(it, select, parent)
                //var radioGroup = holder.view.findViewById<RadioGroup>(R.id.radioGroup)
                //var radioBtn = holder.view.findViewById<RadioButton>(R.id.radioButton)
                //radioBtn.isChecked = listTagsSelected?.get(position) == true
                //arrayRadioGroup.add(radioGroup)
               // var radioBtn = radioGroup.get(0) as RadioButton
                //if(listTagsSelected?.get(position) == true)
                  //  arrayRadioGroup[position].check(R.id.radioButton)
                //arrayRadioBtn.add(radioBtn)

                val radioBtn = holder.getRadioBtn()
                radioBtn.setOnClickListener {
                    listTagsSelected!![position] = !listTagsSelected?.get(position)!!
                    Log.d("TAGS_CLICKED", "Ha sido pulsado ${radioBtn.text}")
                    arrayRadioBtn[position].isChecked = listTagsSelected?.get(position) == true

                    notifyItemChanged(position)
                }

                arrayRadioBtn.add(radioBtn)
            }
        }
    }

    override fun getItemCount() = this.listTags?.size ?: 0

    fun uploadSetListSelected(selected: ArrayList<Boolean>) {
        listTagsSelected = selected.clone() as ArrayList<Boolean>
    }

    fun getTagsSelected(): ArrayList<Boolean>? {
        return listTagsSelected
    }
}