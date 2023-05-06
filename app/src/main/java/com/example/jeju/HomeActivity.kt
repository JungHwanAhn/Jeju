package com.example.jeju

import android.content.Intent
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.jeju.databinding.ActivityHomeBinding
import com.example.jeju.fragment.*
import com.google.android.material.navigation.NavigationView

class HomeActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var backPressedTime : Long = 0
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var binding: ActivityHomeBinding
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
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

        var fragmentList = listOf(
            HanraFragment(), OllehFragment(), CheonjeyeonFragment(),
            CheonjiyeonFragment(), JusangFragment(), SeongsanFragment())

        val adapter = ViewPagerAdapter(this)
        adapter.fragmentList = fragmentList
        binding.viewPager.adapter = adapter

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
                            val nextPos = (currentPos + 1) % fragmentList.size
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
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
        }
    }

    // 툴바 메뉴 버튼이 클릭 됐을 때 실행하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        when(item!!.itemId){
            android.R.id.home->{
                // 햄버거 버튼 클릭시 네비게이션 드로어 열기
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
                startActivity(intent)
            }
            R.id.map-> Toast.makeText(this,"menu_item2 실행",Toast.LENGTH_SHORT).show()
            R.id.like-> Toast.makeText(this,"menu_item3 실행",Toast.LENGTH_SHORT).show()
            R.id.logout-> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
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