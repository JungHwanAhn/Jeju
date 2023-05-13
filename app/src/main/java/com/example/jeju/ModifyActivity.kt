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
import com.example.jeju.databinding.ActivityModifyBinding
import org.json.JSONObject


class ModifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModifyBinding
    private lateinit var requestQueue: RequestQueue

    private val url = "http://49.142.162.247:8050/change"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModifyBinding.inflate(layoutInflater)
        requestQueue = Volley.newRequestQueue(this)
        setContentView(binding.root)

        binding.signinText.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.continueButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val newPassword = binding.newPasswordEditText.text.toString()
            val checkPassword = binding.checkPasswordEditText.text.toString()

            val modifyData: Map<String, String> = hashMapOf(
                "email" to email,
                "password" to password,
                "newPassword" to newPassword,
                "checkPassword" to checkPassword
            )
            val requestBody = JSONObject(modifyData).toString()

            if (email.isNullOrBlank() || password.isNullOrBlank() || newPassword.isNullOrBlank() || checkPassword.isNullOrBlank()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else if (newPassword != checkPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val modifyRequest = object : StringRequest(
                    Method.POST, // 요청 방식을 POST로 지정합니다.
                    url, // 요청을 보낼 URL 주소를 지정합니다.
                    Response.Listener<String>
                    { response ->
                        // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                        if (response == "success") {
                            // 비밀번호 변경에 성공한 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this, "비밀번호 변경에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("ModifyActivity", "비밀번호 변경 성공!")
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish() // 현재 액티비티를 종료합니다.
                        } else if (response == "fail1"){
                            // 가입된 이메일이 아닐 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this, "이메일을 확인해주세요.", Toast.LENGTH_SHORT).show()
                            Log.e("ModifyActivity", "가입되지 않은 이메일")
                        }
                        else if (response == "fail2"){
                            // 현재 비밀번호가 일치하지 않을 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this, "현재 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
                            Log.e("ModifyActivity", "현재 비밀번호 불일치")
                        }
                        else {
                            Toast.makeText(this, "비밀번호 변경 오류", Toast.LENGTH_SHORT).show()
                            Log.e("ModifyActivity", response)
                        }
                    },
                    Response.ErrorListener { error ->
                        // 요청 실패 시 수행되는 코드를 작성합니다.
                        Log.e("ModifyActivity", "error!", error)
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
                requestQueue.add(modifyRequest)
            }
        }
    }
}
