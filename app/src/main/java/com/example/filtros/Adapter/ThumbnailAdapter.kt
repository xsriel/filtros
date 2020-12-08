package com.example.filtros.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.filtros.Interface.FilterListFragmentListener
import com.example.filtros.R
import com.zomato.photofilters.utils.ThumbnailItem
import kotlinx.android.synthetic.main.thumbnial_list_item.view.*

class ThumbnailAdapter(private val context: Context,
                       private val thumbnailItemList: List<ThumbnailItem>,
                       private val listener: FilterListFragmentListener) : RecyclerView.Adapter<ThumbnailAdapter.MyViewholder>() {

    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewholder {
        val itemView: View = LayoutInflater.from(context).inflate(R.layout.thumbnial_list_item, parent, false)
        return  MyViewholder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewholder, position: Int) {
        val thumbnailItem = thumbnailItemList[position]
        holder.thumbNail.setImageBitmap(thumbnailItem.image)
        holder.thumbNail.setOnClickListener {
            listener.OnFilterSelected(thumbnailItem.filter)
            selectedIndex = position
            notifyDataSetChanged()
        }

        holder.fileterName.text = thumbnailItem.filterName
        if (selectedIndex == position) {
            holder.fileterName.setTextColor(ContextCompat.getColor(context, R.color.filter_label_selected))
        }
        else {
            holder.fileterName.setTextColor(ContextCompat.getColor(context, R.color.filter_label_normal))
        }
    }

    override fun getItemCount(): Int {
        return thumbnailItemList.size
    }

    class MyViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var thumbNail:ImageView
        var fileterName:TextView
        init {
            thumbNail = itemView.thumbnail
            fileterName = itemView.filter_name

        }
    }
}