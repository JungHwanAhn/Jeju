package com.example.jeju

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.jeju.databinding.ActivityLikeBinding
import com.example.jeju.databinding.ActivitySearchBinding
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import org.json.JSONObject

data class Item(
    val image: String,
    val title: String,
    val address: String
)

class LikeActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var binding: ActivityLikeBinding
    private lateinit var likeRecyclerView: RecyclerView
    private lateinit var likeAdapter: LikeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.main_layout_toolbar) // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 메뉴 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu) // 메뉴 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 네비게이션 드로어 생성
        drawerLayout = findViewById(R.id.result_drawer_layout)

        // 네비게이션 드로어 내에 있는 화면의 이벤트를 처리하기 위해 생성
        navigationView = findViewById(R.id.result_navigationView)
        navigationView.setNavigationItemSelectedListener(this) //navigation 리스너

        val url = "http://49.142.162.247:8050/interest/return"
        val queue = Volley.newRequestQueue(this)

        val jsonRequest = JSONArray().apply {
            put(JSONObject().apply {
                put("email", intent.getStringExtra("email"))
            })
        }

        val itemList = mutableListOf<Item>()
        val request = JsonArrayRequest(
            Request.Method.POST, url, jsonRequest,
            { response ->
                // 결과 받아서 처리
                for (i in 0 until response.length()) {
                    val image = response.getJSONObject(i).getString("imageurl")
                    val title = response.getJSONObject(i).getString("title")
                    val address = response.getJSONObject(i).getString("roadaddress")
                    Log.d("LikeActivity", title)
                    Log.d("LikeActivity", address)
                    val item = Item(image, title, address)
                    itemList.add(item)
                }
                likeAdapter.notifyDataSetChanged()
            },
            { error ->
                // 에러 처리
                Toast.makeText(this, "검색결과 요청 실패!", Toast.LENGTH_SHORT).show()
                Log.e("SearchActivity", "검색결과 요청 실패!", error)
            })
        queue.add(request)

        likeRecyclerView = findViewById(R.id.like_list)
        likeAdapter = LikeAdapter(itemList)
        likeRecyclerView.adapter = likeAdapter
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
                finish()
            }
            R.id.map-> {
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
            }
            R.id.like-> {
                val intent = Intent(this, LikeActivity::class.java)
                startActivity(intent)
            }
            R.id.logout-> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return false
    }
}