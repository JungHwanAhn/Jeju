package com.example.jeju

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class LikeAdapter(private val likeList: List<Item>) :
    RecyclerView.Adapter<LikeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.like_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: LikeAdapter.ViewHolder, position: Int) {
        holder.bind(likeList[position])
    }

    override fun getItemCount(): Int {
        return likeList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val listImage: ImageView = itemView.findViewById(R.id.like_image_view)
        private val listTitle: TextView = itemView.findViewById(R.id.like_list_text_view)
        private val listAddress: TextView = itemView.findViewById(R.id.like_address_text_view)

        fun bind(item: Item) {
            val imageUrl = item.image
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imageUrl).apply(RequestOptions().centerCrop()).into(listImage)
            }
            listTitle.text = item.title
            listAddress.text = item.address
        }
    }
}

