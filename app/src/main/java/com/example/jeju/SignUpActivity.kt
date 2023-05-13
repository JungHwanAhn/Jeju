package com.example.jeju

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jeju.databinding.ActivitySignupBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var requestQueue: RequestQueue

    private val url = "http://49.142.162.247:8050/join"
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

            val signupData: Map<String, String> = hashMapOf(
                "email" to email,
                "password" to password,
                "checkPassword" to checkPassword
            )

            val requestBody = JSONObject(signupData).toString()

            // CheckBox의 체크 여부를 확인합니다.
            val isAgree = binding.agreeCheckbox.isChecked

            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "이메일을 정확히 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else if (password.isBlank()) {
                Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else if (checkPassword.isBlank()) {
                Toast.makeText(this, "비밀번호를 다시 한 번 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else if (password != checkPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            } else if (!isAgree) {
                Toast.makeText(this, "약관에 동의해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 이메일과 비밀번호를 사용하여 로그인 처리
                val signUpRequest = object : StringRequest(
                    Method.POST, // 요청 방식을 POST로 지정합니다.
                    url, // 요청을 보낼 URL 주소를 지정합니다.
                    Response.Listener<String>
                    { response ->
                        // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                        if (response == "success") {
                            // 회원가입에 성공한 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("SignUpActivity", "회원가입 성공!")
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish() // 현재 액티비티를 종료합니다.
                        } else {
                            // 회원가입에 실패한 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("SignUpActivity", "회원가입 실패!")
                        }
                    },
                    Response.ErrorListener { error ->
                        // 요청 실패 시 수행되는 코드를 작성합니다.
                        Log.e("SignUpActivity", "회원가입 요청 실패!", error)
                        Toast.makeText(this, "회원가입 요청이 실패하였습니다.", Toast.LENGTH_SHORT).show()
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
                requestQueue.add(signUpRequest)
            }
        }
    }
}