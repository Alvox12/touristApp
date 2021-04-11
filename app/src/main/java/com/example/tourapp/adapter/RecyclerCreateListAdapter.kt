package com.example.tourapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.dataModel.Place
import com.example.tourapp.viewModel.PlaceCreateListViewModel
import java.util.*
import kotlin.collections.ArrayList

class RecyclerCreateListAdapter(var model: PlaceCreateListViewModel):
        RecyclerView.Adapter<RecyclerPlaceListAdapter.ViewHolder>()  {

    private var listPlace: ArrayList<Place>? = arrayListOf()
    private var mapPlaceSelected: MutableMap<Int, Place> = mutableMapOf()
    private var parent: ViewGroup? = null

    private var arrayListSelected: ArrayList<Boolean> = arrayListOf()
    //private var arrayImageButton: ArrayList<ImageButton> = arrayListOf()
    var mapImageButton: MutableMap<Int, ImageButton> = mutableMapOf()

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
        this.listPlace?.clear()
        this.listPlace = places.clone() as ArrayList<Place>
        this.arrayListSelected = ArrayList(Collections.nCopies(listPlace!!.size, false))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerPlaceListAdapter.ViewHolder {
        val vh = LayoutInflater.from(parent.context).inflate(R.layout.user_add_list_item, parent,false)
        this.parent = parent
        return RecyclerPlaceListAdapter.ViewHolder(vh)
    }

    override fun onBindViewHolder(holder: RecyclerPlaceListAdapter.ViewHolder, position: Int) {
        listPlace?.get(position)?.let {
            this.parent?.let { parent ->
                holder.bind(it, parent)

                var imageButton = holder.view.findViewById<ImageButton>(R.id.imb_add_list)
                mapImageButton[position] = imageButton

                mapImageButton[position]?.setOnClickListener {
                    arrayListSelected[position] = !arrayListSelected[position]

                    if(!arrayListSelected[position])
                        mapImageButton[position]?.setImageResource(R.drawable.ic_add_list_icon)
                    else
                        mapImageButton[position]?.setImageResource(R.drawable.ic_cancel_icon)

                    addElement(position, arrayListSelected[position])
                    notifyItemChanged(position)
                }

            }
        }
    }


    private fun addElement(index: Int, add: Boolean) {
        if(add) {
            listPlace?.get(index)?.let {mapPlaceSelected.put(index, it)}
            model.listPlacesSelected?.clear()
            mapPlaceSelected.forEach {
                model.listPlacesSelected?.add(it.value)
            }
            //listPlace?.get(index)?.let { model.listPlacesSelected?.add(it) }
        }
        else {
            mapPlaceSelected.remove(index)
            model.listPlacesSelected?.clear()
            mapPlaceSelected.forEach {
                model.listPlacesSelected?.add(it.value)
            }
            //model.listPlacesSelected?.removeAt(index)
        }
    }

    override fun getItemCount() = this.listPlace?.size ?: 0

}