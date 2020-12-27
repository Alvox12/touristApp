package com.example.tourapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
            tvname.text = place.placeName
            tvdescription.text = place.placeDescription
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
            }
        }
    }

    override fun getItemCount() = this.listPlace?.size ?: 0

}