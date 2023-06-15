package com.example.jeju

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
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

class SearchDialog(context: Context) : Dialog(context) {
    private var image: String? = null
    private var title: String? = null
    private var introduction: String? = null
    fun setImageResId(image: String) {
        this.image = image
    }

    fun setTitle(title: String) {
        this.title = title
    }
    fun setIntroduction(introduction: String) {
        this.introduction = introduction
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val inflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(R.layout.custom_dialog, null)
        setContentView(dialogView)

        val titleTextView: TextView = dialogView.findViewById(R.id.dialog_title)
        val introduceTextView: TextView = dialogView.findViewById(R.id.dialog_introduce)
        val imageView: ImageView = dialogView.findViewById(R.id.dialog_image)
        val imageUrl = image
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(dialogView.context).load(imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        }

        titleTextView.text = title
        introduceTextView.text = introduction
    }
}
class SearchAdapter(private val context: Context, private val results: List<Result>, private val email: String, private val loginToken: String) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    private lateinit var requestQueue: RequestQueue

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_result, parent, false)
        requestQueue = Volley.newRequestQueue(context)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchAdapter.ViewHolder, position: Int) {
        holder.bind(results[position], context)
    }

    override fun getItemCount(): Int {
        return results.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val resultTitle: TextView = itemView.findViewById(R.id.result_title)
        private val heartButton: ImageButton = itemView.findViewById(R.id.heart_button)

        fun bind(result: Result, context: Context) {
            resultTitle.text = result.title

            resultTitle.setOnClickListener {
                val customDialog = SearchDialog(context)
                customDialog.setImageResId(result.image)
                customDialog.setTitle(result.title)
                customDialog.setIntroduction(result.introduction)
                customDialog.show()

            }
            if (result.like == "1") {
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
                    fillHeartBtn(result, context)
                } else {
                    heartButton.setImageResource(R.drawable.ic_heart_empty)
                    heartButton.tag = "empty"
                    emptyHeartBtn(result, context)
                }
            }
        }
    }

    private fun fillHeartBtn(result: Result, context: Context) {
        checkToken(context)
        val addUrl = "http://49.142.162.247:8050/interest/add"
        val addData: Map<String, String> = hashMapOf(
            "email" to email,
            "tourid" to result.tourId,
            "token" to loginToken
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
        requestQueue.add(addRequest)
    }

    private fun emptyHeartBtn(result: Result, context: Context) {
        checkToken(context)
        val deleteUrl = "http://49.142.162.247:8050/interest/delete"
        val deleteData: Map<String, String> = hashMapOf(
            "email" to email,
            "tourid" to result.tourId,
            "token" to loginToken
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
        requestQueue.add(deleteRequest)
    }

    private fun checkToken(context: Context) {
        val url = "http://49.142.162.247:8050/tokenCheck"
        val logoutData: Map<String, String?> = hashMapOf(
            "logout" to loginToken
        )

        val requestBody = JSONObject(logoutData).toString()

        val tokenCheckRequest = object : StringRequest(
            Method.POST,
            url,
            Response.Listener<String> { response ->
                // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                if (response == "fail") {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                }
            },
            Response.ErrorListener { error ->
                // 요청 실패 시 수행되는 코드를 작성합니다.
                Log.e("SearchAdapter", "토큰 체크 실패!", error)
                Toast.makeText(context, "토큰 체크에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charsets.UTF_8)
            }
        }
        requestQueue.add(tokenCheckRequest)
    }
}

