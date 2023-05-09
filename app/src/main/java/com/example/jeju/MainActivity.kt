package com.example.jeju

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jeju.databinding.ActivityMainBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import org.json.JSONObject
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.common.model.ClientError

private const val kakao_key = BuildConfig.KAKAO_LOGIN_KEY
class MainActivity : AppCompatActivity() {
    var backPressedTime : Long = 0

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestQueue: RequestQueue

    val naver_key = BuildConfig.NAVER_CLIENT_SECRET
    val naver_id = BuildConfig.NAVER_CLIENT_ID


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NaverIdLoginSDK.initialize(this, naver_id, naver_key, "제주앱")

        binding = ActivityMainBinding.inflate(layoutInflater)
        requestQueue = Volley.newRequestQueue(this)
        setContentView(binding.root)

        binding.signInButton.setOnClickListener {
            // EditText로부터 email과 password 값을 읽어옵니다.
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            val url = "http://49.142.162.247:8050/login"

            val signinData: Map<String, String> = hashMapOf(
                "email" to email,
                "password" to password
            )

            val requestBody = JSONObject(signinData).toString()

            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "이메일을 정확히 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else if (password.isBlank()) {
                Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 이메일과 비밀번호를 사용하여 로그인 처리
                val signInRequest = object : StringRequest(
                    Method.POST, // 요청 방식을 POST로 지정합니다.
                    url, // 요청을 보낼 URL 주소를 지정합니다.
                    Response.Listener<String>
                    { response ->
                        // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                        if (response == "success") {
                            // 회원가입에 성공한 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("MainActivity", "로그인 성공!")
                            val intent = Intent(this, HomeActivity::class.java)
                            intent.putExtra("login", "login")
                            startActivity(intent)
                            finish() // 현재 액티비티를 종료합니다.
                        } else {
                            // 회원가입에 실패한 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("MainActivity", "로그인 실패!")
                        }
                    },
                    Response.ErrorListener { error ->
                        // 요청 실패 시 수행되는 코드를 작성합니다.
                        Log.e("MainActivity", "로그인 요청 실패!", error)
                        Toast.makeText(this, "로그인 요청이 실패하였습니다.", Toast.LENGTH_SHORT).show()
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
                requestQueue.add(signInRequest)
            }
        }

        binding.signUpTextView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this, ForgotActivity::class.java)
            startActivity(intent)
        }

        binding.naverButton.setOnClickListener {
            val oauthLoginCallback = object : OAuthLoginCallback {
                override fun onSuccess() {
                    // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
                    val tvAccessToken = NaverIdLoginSDK.getAccessToken()
                    val tvRefreshToken = NaverIdLoginSDK.getRefreshToken()
                    val url = "http://49.142.162.247:8050/oauth/naver"

                    Log.d("NaverLogin", "accessToken : $tvAccessToken, refreshToken : $tvRefreshToken")

                    val naverData: Map<String, String?> = hashMapOf(
                        "accessToken" to tvAccessToken
                    )

                    val requestBody = JSONObject(naverData).toString()

                    val naverRequest = object : StringRequest(
                        Method.POST,
                        url,
                        Response.Listener<String> { response ->
                            // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                            if (response == "success") {
                                // 로그인에 성공한 경우 처리할 코드를 작성합니다.
                                Toast.makeText(this@MainActivity, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                                Log.e("MainActivity", "로그인 성공!")
                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                intent.putExtra("login", "naver")
                                startActivity(intent)
                                finish() // 현재 액티비티를 종료합니다.
                            } else {
                                // 로그인에 실패한 경우 처리할 코드를 작성합니다.
                                Toast.makeText(this@MainActivity, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                                Log.e("MainActivity", "로그인 실패!")
                            }
                        },
                        Response.ErrorListener { error ->
                            // 요청 실패 시 수행되는 코드를 작성합니다.
                            Log.e("MainActivity", "로그인 요청 실패!", error)
                            Toast.makeText(this@MainActivity, "로그인 요청이 실패하였습니다.", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        override fun getBodyContentType(): String {
                            return "application/json; charset=utf-8"
                        }

                        override fun getBody(): ByteArray {
                            return requestBody.toByteArray(Charsets.UTF_8)
                        }
                    }
                    requestQueue.add(naverRequest)
                }
                override fun onFailure(httpStatus: Int, message: String) {
                    val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                    val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                    Log.e("NaverLogin", "errorCode : $errorCode, errorDesc : $errorDescription")
                }
                override fun onError(errorCode: Int, message: String) {
                    onFailure(errorCode, message)
                }
            }
            NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
        }

        binding.kakaoButton.setOnClickListener {
            // 카카오계정으로 로그인 공통 callback 구성
            // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨

            val url = "http://49.142.162.247:8050/oauth/kakao"

            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e("LOGIN", "카카오계정으로 로그인 실패", error)
                } else if (token != null) {
                    Log.i("LOGIN", "카카오계정으로 로그인 성공 ${token.accessToken}")
                }
            }

            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e("LOGIN", "카카오톡으로 로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    } else if (token != null) {
                        Log.i("LOGIN", "카카오톡으로 로그인 성공 ${token.accessToken}")
                        val kakaoData: Map<String, String> = hashMapOf(
                            "accessToken" to token.accessToken
                        )

                        val requestBody = JSONObject(kakaoData).toString()

                        val kakaoRequest = object : StringRequest(
                            Method.POST,
                            url,
                            Response.Listener<String> { response ->
                                // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                                if (response == "success") {
                                    // 로그인에 성공한 경우 처리할 코드를 작성합니다.
                                    Toast.makeText(this@MainActivity, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                                    Log.e("MainActivity", "로그인 성공!")
                                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                    intent.putExtra("login", "kakao")
                                    startActivity(intent)
                                    finish() // 현재 액티비티를 종료합니다.
                                } else {
                                    // 로그인에 실패한 경우 처리할 코드를 작성합니다.
                                    Toast.makeText(this@MainActivity, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                                    Log.e("MainActivity", "로그인 실패!")
                                }
                            },
                            Response.ErrorListener { error ->
                                // 요청 실패 시 수행되는 코드를 작성합니다.
                                Log.e("MainActivity", "로그인 요청 실패!", error)
                                Toast.makeText(this@MainActivity, "로그인 요청이 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            override fun getBodyContentType(): String {
                                return "application/json; charset=utf-8"
                            }

                            override fun getBody(): ByteArray {
                                return requestBody.toByteArray(Charsets.UTF_8)
                            }
                        }
                        requestQueue.add(kakaoRequest)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

    }

    override fun onBackPressed() {
        // 뒤로가기 버튼 클릭
        if(System.currentTimeMillis() - backPressedTime < 2000 ) {
            finish()
            return
        }
        Toast.makeText(this, "'뒤로'버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
        backPressedTime = System.currentTimeMillis()
    }
}

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Kakao SDK 초기화
        KakaoSdk.init(this, kakao_key)
    }
}