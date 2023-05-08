package com.example.jeju

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.jeju.databinding.ActivityMainBinding
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback

class MainActivity : AppCompatActivity() {
    var backPressedTime : Long = 0
    private lateinit var binding: ActivityMainBinding
    val naver_key = BuildConfig.NAVER_CLIENT_SECRET
    val naver_id = BuildConfig.NAVER_CLIENT_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NaverIdLoginSDK.initialize(this, naver_id, naver_key, "제주앱")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
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
                    val tvExpires = NaverIdLoginSDK.getExpiresAt().toString()
                    val tvType = NaverIdLoginSDK.getTokenType()
                    val tvState = NaverIdLoginSDK.getState().toString()
                    Log.d("NaverLogin", "accessToken : $tvAccessToken, refreshToken : $tvRefreshToken, expiresIn : $tvExpires, tokenType : $tvType")
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