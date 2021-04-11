package com.example.tourapp.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.dataModel.Place
import com.example.tourapp.dataModel.User

class RecyclerCustomListsAdapter():
    RecyclerView.Adapter<RecyclerCustomListsAdapter.ViewHolder>() {

    var arrayNames: ArrayList<String> = arrayListOf()
    var arrayElems: ArrayList<Int> = arrayListOf()
    var arrayCodigos: ArrayList<String> = arrayListOf()

    private var parent: ViewGroup? = null

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view){

        fun bind(name: String, num: Int, parent: ViewGroup){
            val tvlistname = view.findViewById<TextView>(R.id.tv_list_name)
            val tvnumelems = view.findViewById<TextView>(R.id.tv_num_elems)
            tvlistname.text = name
            tvnumelems.text = "Num. de lugares: $num"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = LayoutInflater.from(parent.context).inflate(R.layout.userplace_list_item, parent,false)
        this.parent = parent
        return ViewHolder(vh)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        this.parent?.let { parent ->
            holder.bind(arrayNames[position], arrayElems[position], parent)
            val llCard = holder.view.findViewById<LinearLayout>(R.id.ll_list_card)
            llCard.setOnClickListener { view ->
                val bundle = Bundle()
                //bundle.putString("codigo", arrayCodigos[position])
                bundle.putString("nombre", arrayNames[position])
                bundle.putStringArrayList("ListCodes", this.arrayCodigos)
                bundle.putBoolean("CustomList", true)
                view.let { Navigation.findNavController(it).navigate(R.id.action_userListOfListsFragment_to_placeListFragment, bundle)}
            }
        }
    }

    fun setLists(listNames: ArrayList<String>, listCodes: ArrayList<String>, listElems: ArrayList<Int>) {
        arrayNames.clear()
        arrayCodigos.clear()
        arrayElems.clear()

        arrayNames = listNames.clone() as ArrayList<String>
        arrayCodigos = listCodes.clone() as ArrayList<String>
        arrayElems = listElems.clone() as ArrayList<Int>
    }

    override fun getItemCount() = this.arrayNames.size ?: 0
}