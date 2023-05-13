package com.example.jeju

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchAdapter(private val context: Context, private val results: List<String>) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int {
        return results.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val resultTitle: TextView = itemView.findViewById(R.id.result_title)
        private val heartButton: ImageButton = itemView.findViewById(R.id.heart_button)
        fun bind(result: String) {
            resultTitle.text = result

            heartButton.setOnClickListener {
                if (heartButton.tag == "empty") {
                    heartButton.setImageResource(R.drawable.ic_heart_filled)
                    heartButton.tag = "filled"
                    // TODO: 해당 항목을 찜한 데이터를 서버로 보내는 로직을 작성해주세요.
                } else {
                    heartButton.setImageResource(R.drawable.ic_heart_empty)
                    heartButton.tag = "empty"
                    // TODO: 해당 항목을 찜한 데이터를 서버에서 삭제하는 로직을 작성해주세요.
                }
            }
        }
    }
}