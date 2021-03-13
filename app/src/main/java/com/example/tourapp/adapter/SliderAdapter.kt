package com.example.tourapp.adapter


import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.dataModel.Place
import com.smarteist.autoimageslider.SliderViewAdapter
import kotlinx.android.synthetic.main.fragment_place_data.view.*


class SliderAdapter:
        SliderViewAdapter<SliderAdapter.SliderAdapterViewHolder>() {

    private var myBitmapPlaceImg: MutableMap<Int, Bitmap> = mutableMapOf()
    private var parent: ViewGroup? = null

    class SliderAdapterViewHolder(itemView: View?): SliderViewAdapter.ViewHolder(itemView) {
        var imageViewBackground: ImageView? = null

        fun bind(bitmap: Bitmap){
            val ivslider = itemView.findViewById<ImageView>(R.id.myimage_slider)
            ivslider.setImageBitmap(bitmap)
        }
    }

    fun setMutableMap(myBitmap: MutableMap<Int, Bitmap>) {
        this.myBitmapPlaceImg.clear()
        this.myBitmapPlaceImg = myBitmap
    }


    override fun getCount(): Int = this.myBitmapPlaceImg.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup?): SliderAdapterViewHolder {
        val vh = LayoutInflater.from(parent?.context).inflate(R.layout.slider_layout, parent, false)
        this.parent = parent
        return SliderAdapterViewHolder(vh)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterViewHolder?, position: Int) {
        if(this.myBitmapPlaceImg[position]?.byteCount != null) {
            myBitmapPlaceImg[position]?.let {
                viewHolder?.bind(it)
            }
        }
    }

}