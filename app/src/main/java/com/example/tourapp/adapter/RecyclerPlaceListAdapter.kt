package com.example.tourapp.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.dataModel.Place
import com.example.tourapp.viewModel.CustomPlaceListViewModel
import kotlinx.android.synthetic.main.custom_place_list_item.view.*


class RecyclerPlaceListAdapter(customList: Boolean = false):
    RecyclerView.Adapter<RecyclerPlaceListAdapter.ViewHolder>(), Filterable {

    private var listPlace: ArrayList<Place>? = arrayListOf()
    private var listFiltered: ArrayList<Place>? = arrayListOf()
    private var parent: ViewGroup? = null
    private var customList: Boolean = false

    lateinit var model: CustomPlaceListViewModel

    init {
        this.customList = customList
    }

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


    private var myFilter: Filter = object : Filter() {

        //Automatic on background thread
        override fun performFiltering(charSequence: CharSequence): FilterResults {

            val filteredList: ArrayList<Place> = arrayListOf()
            if (charSequence == null || charSequence.isEmpty()) {
                for(place in listPlace!!.iterator()) {
                    filteredList.add(place)
                }
            } else {
                for (place in listPlace!!.iterator()) {
                    if (place.placeName.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        filteredList.add(place)
                    }
                }
            }
            val filterResults = FilterResults()
            filterResults.values = filteredList
            return filterResults
        }

        //Automatic on UI thread
        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            listFiltered?.clear()

            val listRes = filterResults.values as List<Place?>
            for (elem in listRes.iterator()) {
                if (elem != null) {
                    listFiltered?.add(elem)
                }
            }
            notifyDataSetChanged()
        }
    }

    fun setCustomPlaceModel(model: CustomPlaceListViewModel) {
        this.model = model
    }

    fun setPlaceList(places: ArrayList<Place>) {
        this.listPlace = places.clone() as ArrayList<Place>
        this.listFiltered = places.clone() as ArrayList<Place>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh: View
        if(!customList)
            vh = LayoutInflater.from(parent.context).inflate(R.layout.place_list_item, parent, false)
        else
            vh = LayoutInflater.from(parent.context).inflate(R.layout.custom_place_list_item, parent, false)

        this.parent = parent
        return ViewHolder(vh)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listFiltered?.get(position)?.let {
            this.parent?.let { parent ->
                holder.bind(it, parent)

                val item_layout = holder.view.findViewById<LinearLayout>(R.id.ll_place_layout)
                item_layout.setOnClickListener { view->
                    detailIncidence(position, view)
                }

                if(customList) {
                    val popupMenu = PopupMenu(holder.view.context, holder.view.iv_button_opt)
                    popupMenu.inflate(R.menu.custom_list_item_menu)

                    popupMenu.setOnMenuItemClickListener { item->
                        when(item.itemId) {
                            R.id.opt_delete_list_elem -> {
                                popupMenu.dismiss()
                                listPlace?.get(position)?.let { place -> model.deleteListElem(position, place.placeId) }
                                Toast.makeText(parent.context, "Elemento eliminado de lista", Toast.LENGTH_SHORT).show()
                                true
                            }
                            else -> {
                                popupMenu.dismiss();
                                false
                            }
                        }
                    }

                    holder.view.iv_button_opt.setOnClickListener {
                        popupMenu.show()
                    }
                }
            }
        }
    }

    fun detailIncidence(position: Int, view: View){

        var auxPlace = this.listPlace?.get(position)

        //Creo un bundle para guardar la información y recogerla en el siguiente activity
        val bundle : Bundle = Bundle()
        bundle.putSerializable("Place", auxPlace)
        bundle.putString("Previous", "")

        view.let { Navigation.findNavController(it).navigate(R.id.action_placeListFragment_to_placeDataFragment, bundle)}
    }

    override fun getItemCount() = this.listFiltered?.size ?: 0

    override fun getFilter(): Filter {
        return myFilter
    }

}