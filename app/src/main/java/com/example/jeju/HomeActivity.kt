package com.example.jeju

import android.content.Intent
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jeju.databinding.ActivityHomeBinding
import com.example.jeju.databinding.NavHeaderBinding
import com.example.jeju.fragment.*
import com.google.android.material.navigation.NavigationView
import org.json.JSONException
import org.json.JSONObject

class HomeActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var backPressedTime : Long = 0
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var binding: ActivityHomeBinding
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private lateinit var requestQueue: RequestQueue
    private var userEmail : String? = null
    private var loginToken : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        requestQueue = Volley.newRequestQueue(this)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.main_layout_toolbar) // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 메뉴 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu) // 메뉴 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 네비게이션 드로어 생성
        drawerLayout = findViewById(R.id.main_drawer_layout)

        // 네비게이션 드로어 내에 있는 화면의 이벤트를 처리하기 위해 생성
        navigationView = findViewById(R.id.main_navigationView)
        navigationView.setNavigationItemSelectedListener(this) //navigation 리스너

        val headerBinding = NavHeaderBinding.bind(navigationView.getHeaderView(0))
        userEmail = intent.getStringExtra("email")
        headerBinding.userEmail.text = userEmail

        loginToken = intent.getStringExtra("login").toString()

        val url = "http://49.142.162.247:8050/main"
        val adapter = ViewPagerAdapter(this)

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                // 서버 응답을 처리하는 코드
                try {
                    // JSONArray에서 title 값과 imageUrl 값을 추출하는 코드
                    val fragmentList = mutableListOf<FragmentData>()
                    for (i in 0 until response.length()) {
                        val title = response.getJSONObject(i).getString("title")
                        val address = response.getJSONObject(i).getString("roadaddress")
                        val imageUrl = response.getJSONObject(i).getString("imagepath")
                        val fragment = when (i) {
                            0 -> FirstFragment()
                            1 -> SecondFragment()
                            2 -> ThirdFragment()
                            3 -> FourthFragment()
                            4 -> FifthFragment()
                            else -> throw IndexOutOfBoundsException("Fragment index out of bounds: $i")
                        }
                        fragmentList.add(FragmentData(fragment, title, address, imageUrl))
                    }

                    adapter.fragmentList = fragmentList
                    binding.viewPager.adapter = adapter
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                // 요청 실패 시 수행되는 코드
                Log.e("MainActivity", "데이터 가져오기 실패!", error)
                Toast.makeText(this@HomeActivity, "데이터 가져오기 실패", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(request)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            var currentState = 0
            var currentPos = 0

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if(currentState == ViewPager2.SCROLL_STATE_DRAGGING && currentPos == position) {
                    if(currentPos == 0) binding.viewPager.currentItem = 5
                    else if(currentPos == 5) binding.viewPager.currentItem = 0
                }
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                currentPos = position
                super.onPageSelected(position)

            }

            override fun onPageScrollStateChanged(state: Int) {
                currentState = state
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    handler.removeCallbacksAndMessages(null) // 모든 runnable, message 제거
                    runnable = object : Runnable {
                        override fun run() {
                            val nextPos = (currentPos + 1) % 5
                            binding.viewPager.currentItem = nextPos
                            handler.postDelayed(this, 3000)
                        }
                    }
                    handler.postDelayed(runnable, 3000) // 3초마다 페이지 전환
                } else if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    handler.removeCallbacksAndMessages(null) // 모든 runnable, message 제거
                }
            }
        })
        // 첫 페이지에서 자동 슬라이드가 동작하도록 호출
        binding.viewPager.postDelayed({
            binding.viewPager.currentItem = 1
        }, 3000)

        binding.searchBtn.setOnClickListener {
            val searchTerm = binding.searchEdit.text.toString()
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("result", searchTerm)
            intent.putExtra("email", userEmail)
            intent.putExtra("login", loginToken)
            startActivity(intent)
        }
    }

    // 툴바 메뉴 버튼이 클릭 됐을 때 실행하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        when(item!!.itemId){
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 드로어 내 아이템 클릭 이벤트 처리하는 함수
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.home-> {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("email", userEmail)
                intent.putExtra("login", intent.getStringExtra("login"))
                startActivity(intent)
                finish()
            }
            R.id.map-> {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("email", userEmail)
                intent.putExtra("login", loginToken)
                startActivity(intent)
            }
            R.id.like-> {
                val intent = Intent(this, LikeActivity::class.java)
                intent.putExtra("email", userEmail)
                intent.putExtra("login", loginToken)
                startActivity(intent)
            }
            R.id.logout-> {
                val url = "http://49.142.162.247:8050/oauth/logout"
                val logoutData: Map<String, String?> = hashMapOf(
                    "logout" to loginToken
                )
                Log.e("MainActivity", loginToken.toString())

                val requestBody = JSONObject(logoutData).toString()

                val logoutRequest = object : StringRequest(
                    Method.POST,
                    url,
                    Response.Listener<String> { response ->
                        // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                        if (response == "success") {
                            // 로그아웃에 성공한 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this@HomeActivity, "로그아웃하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("HomeActivity", "로그아웃 성공!")
                            val intent = Intent(this@HomeActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish() // 현재 액티비티를 종료합니다.
                        } else {
                            // 로그아웃에 실패한 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this@HomeActivity, "로그아웃에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("HomeActivity", "로그아웃 실패!")
                        }
                    },
                    Response.ErrorListener { error ->
                        // 요청 실패 시 수행되는 코드를 작성합니다.
                        Log.e("HomeActivity", "로그아웃 요청 실패!", error)
                        Toast.makeText(this@HomeActivity, "로그아웃 요청이 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    override fun getBodyContentType(): String {
                        return "application/json; charset=utf-8"
                    }

                    override fun getBody(): ByteArray {
                        return requestBody.toByteArray(Charsets.UTF_8)
                    }
                }
                requestQueue.add(logoutRequest)
            }
        }
        return false
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