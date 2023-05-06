package com.example.jeju

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jeju.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signinText.setOnClickListener {
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

            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "이메일을 정확히 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else if (password.isBlank()) {
                Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else if (!isAgree) {
                Toast.makeText(this, "약관에 동의해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 이메일과 비밀번호를 사용하여 로그인 처리
                Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
