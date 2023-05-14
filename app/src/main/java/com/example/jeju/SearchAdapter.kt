package com.example.jeju

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class SearchAdapter(private val context: Context, private val results: List<String>, private val email: String) :
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
        private var tourId: Int? = null
        fun bind(result: String) {
            resultTitle.text = result

            val searchUrl = "http://49.142.162.247:8050/search/title"
            val queue = Volley.newRequestQueue(context)
            val jsonRequest = JSONArray().apply {
                put(JSONObject().apply {
                    put("title", result)
                })
            }

            val request = JsonArrayRequest(
                Request.Method.POST, searchUrl, jsonRequest,
                { response ->
                    // 결과 받아서 처리
                    tourId = response.getJSONObject(0).getInt("tourid")
                    Log.d("SearchActivity", "$tourId")
                },
                { error ->
                    // 에러 처리
                    Toast.makeText(context, "검색결과 요청 실패!", Toast.LENGTH_SHORT).show()
                    Log.e("SearchActivity", "검색결과 요청 실패!", error)
                })
            queue.add(request)

            val favoriteUrl = "http://49.142.162.247:8050/search/title"
            val favoriteData: Map<String, String> = hashMapOf(
                "email" to email,
                "tourid" to tourId.toString()
            )
            val requestBody = JSONObject(favoriteData).toString()

            val addRequest = object : StringRequest(
                Method.POST, // 요청 방식을 POST로 지정합니다.
                favoriteUrl, // 요청을 보낼 URL 주소를 지정합니다.
                Response.Listener<String>
                { response ->
                    // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                    if (response == "Success") {
                        heartButton.setImageResource(R.drawable.ic_heart_filled)
                        heartButton.tag = "filled"
                    }
                    else {
                        heartButton.setImageResource(R.drawable.ic_heart_empty)
                        heartButton.tag = "empty"
                    }
                },
                Response.ErrorListener { error ->
                    // 요청 실패 시 수행되는 코드를 작성합니다.
                    Log.e("SearchAdapter", "error!", error)
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
            queue.add(addRequest)

            heartButton.setOnClickListener {
                if (heartButton.tag == "empty") {
                    heartButton.setImageResource(R.drawable.ic_heart_filled)
                    heartButton.tag = "filled"
                    val addUrl = "http://49.142.162.247:8050/interest/add"
                    val addData: Map<String, String> = hashMapOf(
                        "email" to email,
                        "tourid" to tourId.toString()
                    )
                    val requestBody = JSONObject(addData).toString()

                    val addRequest = object : StringRequest(
                        Method.POST, // 요청 방식을 POST로 지정합니다.
                        addUrl, // 요청을 보낼 URL 주소를 지정합니다.
                        Response.Listener<String>
                        { response ->
                            // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                            if (response == "Success") {
                                Toast.makeText(context, "찜 등록!", Toast.LENGTH_SHORT).show()
                                Log.e("SearchAdapter", "찜 등록!")
                            }
                            else {
                                Toast.makeText(context, "찜 등록 실패", Toast.LENGTH_SHORT).show()
                                Log.e("SearchAdapter", "찜 등록 실패")
                            }
                        },
                        Response.ErrorListener { error ->
                            // 요청 실패 시 수행되는 코드를 작성합니다.
                            Log.e("SearchAdapter", "error!", error)
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
                    queue.add(addRequest)
                } else {
                    heartButton.setImageResource(R.drawable.ic_heart_empty)
                    heartButton.tag = "empty"
                    val deleteUrl = "http://49.142.162.247:8050/interest/delete"
                    val deleteData: Map<String, String> = hashMapOf(
                        "email" to email,
                        "tourid" to tourId.toString()
                    )
                    val requestBody = JSONObject(deleteData).toString()

                    val deleteRequest = object : StringRequest(
                        Method.POST, // 요청 방식을 POST로 지정합니다.
                        deleteUrl, // 요청을 보낼 URL 주소를 지정합니다.
                        Response.Listener<String>
                        { response ->
                            // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                            if (response == "Success") {
                                Toast.makeText(context, "찜 삭제!", Toast.LENGTH_SHORT).show()
                                Log.e("SearchAdapter", "찜 삭제!")
                            }
                            else {
                                Toast.makeText(context, "찜 삭제 실패", Toast.LENGTH_SHORT).show()
                                Log.e("SearchAdapter", "찜 삭제 실패")
                            }
                        },
                        Response.ErrorListener { error ->
                            // 요청 실패 시 수행되는 코드를 작성합니다.
                            Log.e("SearchAdapter", "error!", error)
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
                    queue.add(deleteRequest)                }
            }
        }
    }
}