package com.example.jeju

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jeju.databinding.ActivityForgotBinding
import org.json.JSONObject

class ForgotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotBinding
    private lateinit var requestQueue: RequestQueue
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotBinding.inflate(layoutInflater)
        requestQueue = Volley.newRequestQueue(this)
        setContentView(binding.root)

        binding.signinText.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.sendButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val url = "http://49.142.162.247:8050/find"
            val forgotdata: Map<String, String> = hashMapOf(
                "email" to email
            )
            val requestBody = JSONObject(forgotdata).toString()

            if (email.isNullOrBlank()){
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val forgotRequest = object : StringRequest(
                    Method.POST, // 요청 방식을 POST로 지정합니다.
                    url, // 요청을 보낼 URL 주소를 지정합니다.
                    Response.Listener<String>
                    { response ->
                        // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                        if (response == "success") {
                            Toast.makeText(this, "메일로 임시 비밀번호를 발급하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("ForgotActivity", "메일 전송!")
                            val intent = Intent(this, ModifyActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else if (response == "fail1"){
                            Toast.makeText(this, "가입되지 않은 이메일입니다.", Toast.LENGTH_SHORT).show()
                            Log.e("ForgotActivity", "가입되지 않은 이메일")
                        }
                        else if (response == "fail2"){
                            Toast.makeText(this, "메일이 이미 발송되었습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("ForgotActivity", "중복 요청")
                        }
                        else {
                            Toast.makeText(this, "메일 요청 실패", Toast.LENGTH_SHORT).show()
                            Log.e("ForgotActivity", "메일 요청 실패")
                        }
                    },
                    Response.ErrorListener { error ->
                        // 요청 실패 시 수행되는 코드를 작성합니다.
                        Log.e("ForgotActivity", "error!", error)
                        Toast.makeText(this, "error!", Toast.LENGTH_SHORT).show()
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
                requestQueue.add(forgotRequest)
            }
        }
    }
}
