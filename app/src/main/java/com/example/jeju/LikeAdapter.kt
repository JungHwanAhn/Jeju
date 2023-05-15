package com.example.jeju

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.json.JSONObject

class LikeAdapter(private val context: Context, private var likeList: List<Item>, private val email: String) :
    RecyclerView.Adapter<LikeAdapter.ViewHolder>() {
    private lateinit var requestQueue: RequestQueue
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.like_list, parent, false)
        requestQueue = Volley.newRequestQueue(context)
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
        private val listTitle: TextView = itemView.findViewById(R.id.like_title_text_view)
        private val listAddress: TextView = itemView.findViewById(R.id.like_address_text_view)
        private val heartButton: ImageButton = itemView.findViewById(R.id.like_image_button)

        fun bind(item: Item) {
            val imageUrl = item.image
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(imageUrl).apply(RequestOptions().centerCrop()).into(listImage)
            }
            listTitle.text = item.title
            listAddress.text = item.address

            if (item.like == "1") {
                heartButton.setImageResource(R.drawable.ic_heart_filled)
                heartButton.tag = "filled"
            }
            else {
                heartButton.setImageResource(R.drawable.ic_heart_empty)
                heartButton.tag = "empty"
            }

            heartButton.setOnClickListener {
                if (heartButton.tag == "empty") {
                    heartButton.setImageResource(R.drawable.ic_heart_filled)
                    heartButton.tag = "filled"
                    val addUrl = "http://49.142.162.247:8050/interest/add"
                    val addData: Map<String, String> = hashMapOf(
                        "email" to email,
                        "tourid" to item.tourId
                    )

                    val requestBody = JSONObject(addData).toString()

                    val addRequest = object : StringRequest(
                        Method.POST, // 요청 방식을 POST로 지정합니다.
                        addUrl, // 요청을 보낼 URL 주소를 지정합니다.
                        Response.Listener<String>
                        { response ->
                            // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                            if (response == "success") {
                                Toast.makeText(context, "찜 등록!", Toast.LENGTH_SHORT).show()
                                Log.e("LikeAdapter", "찜 등록!")
                            }
                            else {
                                Toast.makeText(context, "찜 등록 실패", Toast.LENGTH_SHORT).show()
                                Log.e("LikeAdapter", "찜 등록 실패")
                            }
                        },
                        Response.ErrorListener { error ->
                            // 요청 실패 시 수행되는 코드를 작성합니다.
                            Log.e("LikeAdapter", "error!", error)
                            Toast.makeText(context, "error!", Toast.LENGTH_SHORT).show()
                        }) {
                        override fun getBodyContentType(): String {
                            return "application/json; charset=utf-8"
                        }
                        @Throws(AuthFailureError::class)
                        override fun getBody(): ByteArray {
                            return requestBody.toByteArray(Charsets.UTF_8)
                        }
                    }
                    // 생성한 Request 객체를 RequestQueue에 추가합니다.
                    requestQueue.add(addRequest)
                } else {
                    heartButton.setImageResource(R.drawable.ic_heart_empty)
                    heartButton.tag = "empty"
                    val deleteUrl = "http://49.142.162.247:8050/interest/delete"
                    val deleteData: Map<String, String> = hashMapOf(
                        "email" to email,
                        "tourid" to item.tourId
                    )
                    val requestBody = JSONObject(deleteData).toString()

                    val deleteRequest = object : StringRequest(
                        Method.POST, // 요청 방식을 POST로 지정합니다.
                        deleteUrl, // 요청을 보낼 URL 주소를 지정합니다.
                        Response.Listener<String>
                        { response ->
                            // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                            if (response == "success") {
                                Toast.makeText(context, "찜 삭제!", Toast.LENGTH_SHORT).show()
                                Log.e("LikeAdapter", "찜 삭제!")
                            }
                            else {
                                Toast.makeText(context, "찜 삭제 실패", Toast.LENGTH_SHORT).show()
                                Log.e("LikeAdapter", "찜 삭제 실패")
                            }
                        },
                        Response.ErrorListener { error ->
                            // 요청 실패 시 수행되는 코드를 작성합니다.
                            Log.e("LikeAdapter", "error!", error)
                            Toast.makeText(context, "error!", Toast.LENGTH_SHORT).show()
                        }) {
                        override fun getBodyContentType(): String {
                            return "application/json; charset=utf-8"
                        }
                        @Throws(AuthFailureError::class)
                        override fun getBody(): ByteArray {
                            return requestBody.toByteArray(Charsets.UTF_8)
                        }
                    }
                    // 생성한 Request 객체를 RequestQueue에 추가합니다.
                    requestQueue.add(deleteRequest)
                }
            }
        }
    }
}

