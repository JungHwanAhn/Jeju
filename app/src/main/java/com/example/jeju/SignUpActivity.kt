package com.example.jeju

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.jeju.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val signinText = findViewById<TextView>(R.id.signinText)
        signinText.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Continue 버튼이 클릭되면 수행되는 리스너를 설정합니다.
        binding.continueButton.setOnClickListener {
            // EditText로부터 email과 password 값을 읽어옵니다.
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            // CheckBox의 체크 여부를 확인합니다.
            val isAgree = binding.agreeCheckbox.isChecked

            // TODO: 이메일 주소와 비밀번호, 개인정보 취급방침 동의 여부 등을 이용하여
            // 새로운 사용자를 등록하거나 로그인 처리를 수행합니다.

            // 화면 전환을 수행합니다.
            // 다음 화면으로 전환할 때는 Intent를 사용합니다.
            // 예시: val intent = Intent(this, MainActivity::class.java)
            // startActivity(intent)

            // 현재 화면을 종료합니다.
            finish()
        }

        // Login 링크가 클릭되면 수행되는 리스너를 설정합니다.
        binding.signinText.setOnClickListener {
            // 화면 전환을 수행합니다.
            // 다음 화면으로 전환할 때는 Intent를 사용합니다.
            // 예시: val intent = Intent(this, LoginActivity::class.java)
            // startActivity(intent)

            // 현재 화면을 종료합니다.
            finish()
        }
    }
}
