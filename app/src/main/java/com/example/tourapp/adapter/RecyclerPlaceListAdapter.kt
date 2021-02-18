package com.example.tourapp.adapter

import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.dataModel.Place

class RecyclerPlaceListAdapter():
    RecyclerView.Adapter<RecyclerPlaceListAdapter.ViewHolder>() {

    private var listPlace: ArrayList<Place>? = null
    private var parent: ViewGroup? = null

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view){

        fun bind(place: Place, parent: ViewGroup){
            val tvname = view.findViewById<TextView>(R.id.tv_place_name)
            val tvdescription = view.findViewById<TextView>(R.id.tv_place_description)
            val tvscore = view.findViewById<TextView>(R.id.tv_place_score)
            tvname.text = place.placeName
            tvdescription.text = place.placeDescription
            tvscore.text = place.placeScore.toString()
        }
    }

    fun setPlaceList(places: ArrayList<Place>) {
        this.listPlace = places
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = LayoutInflater.from(parent.context).inflate(R.layout.place_list_item, parent,false)
        this.parent = parent
        return ViewHolder(vh)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listPlace?.get(position)?.let {
            this.parent?.let { parent ->
                holder.bind(it, parent)

                val item_layout = holder.view.findViewById<LinearLayout>(R.id.ll_place_layout)
                item_layout.setOnClickListener { view->
                    detailIncidence(position, view)
                }
            }
        }
    }

    fun detailIncidence(position: Int, view: View){

        var auxPlace = this.listPlace?.get(position)

        //Creo un bundle para guardar la información y recogerla en el siguiente activity
        val bundle : Bundle = Bundle()
        bundle.putSerializable("Place", auxPlace)

        view.let { Navigation.findNavController(it).navigate(R.id.action_placeListFragment_to_placeDataFragment, bundle)}
    }

    override fun getItemCount() = this.listPlace?.size ?: 0

}