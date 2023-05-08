package com.example.jeju

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.jeju.databinding.ActivitySignupBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var requestQueue: RequestQueue

    private val url = "http://49.142.162.247:8050/signUp"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        requestQueue = Volley.newRequestQueue(this)
        setContentView(binding.root)


        // Continue 버튼이 클릭되면 수행되는 리스너를 설정합니다.
        binding.continueButton.setOnClickListener {
            // EditText로부터 email과 password 값을 읽어옵니다.
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val checkPassword = binding.checkPasswordEditText.text.toString()

            val requestBody = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("checkPassword", checkPassword)
            }

            // CheckBox의 체크 여부를 확인합니다.
            val isAgree = binding.agreeCheckbox.isChecked

            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "이메일을 정확히 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else if (password.isBlank()) {
                Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else if (checkPassword.isBlank()) {
                Toast.makeText(this, "비밀번호를 다시 한 번 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            else if (password != checkPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
            else if (!isAgree) {
                Toast.makeText(this, "약관에 동의해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 이메일과 비밀번호를 사용하여 로그인 처리
                Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, requestBody,
                    Response.Listener { response ->
                        if (response.getBoolean("success")) {
                            // 회원가입 성공 시 처리
                            Log.d("SignUpActivity", "회원가입 성공: $response")
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // 회원가입 실패 시 처리
                            val message = response.getString("message")
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    Response.ErrorListener { error ->
                        // 회원가입 실패 시 처리
                        Log.e("SignUpActivity", "회원가입 실패: ${error.message}", error)
                        Toast.makeText(this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }) {
                    override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
                        try {
                            val jsonString = String(response.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers)))
                            return Response.success(JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response))
                        } catch (e: UnsupportedEncodingException) {
                            return Response.error(ParseError(e))
                        } catch (je: JSONException) {
                            return Response.error(ParseError(je))
                        }
                    }
                }
                requestQueue.add(jsonObjectRequest)

                finish()
            }
        }
    }
}
