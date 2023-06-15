package com.example.jeju

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jeju.databinding.ActivitySearchBinding
import com.example.jeju.databinding.NavHeaderBinding
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import org.json.JSONObject

data class Result(
    val title: String,
    val tourId: String,
    val like: String,
    val introduction: String,
    val image: String
)
class SearchActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var binding: ActivitySearchBinding
    private val ACCESS_FINE_LOCATION = 1000 // Request Code

    private lateinit var resultRecyclerView: RecyclerView
    private lateinit var resultAdapter: SearchAdapter
    private lateinit var requestQueue: RequestQueue
    private lateinit var locationManager: LocationManager

    private var searchTerm: String? = null
    private var email: String? = null
    private var loginToken : String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        requestQueue = Volley.newRequestQueue(this)
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

        val headerBinding = NavHeaderBinding.bind(navigationView.getHeaderView(0))
        email = intent.getStringExtra("email").toString()
        headerBinding.userEmail.text = email

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var userLocation: Location = getLatLng()
        if(userLocation != null) {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
        }

        val option = intent.getStringExtra("option")

        when(option) {
            "0" -> binding.optionDefault.isChecked = true
            "1" -> binding.optionTraffic.isChecked = true
            "2" -> binding.optionLocation.isChecked = true
        }

        loginToken = intent.getStringExtra("login")
        searchTerm = intent.getStringExtra("result").toString()
        binding.searchEdit.setText(searchTerm)

        search(searchTerm!!, email!!, option!!, latitude!!, longitude!!)

        binding.searchBtn.setOnClickListener {
            searchTerm = binding.searchEdit.text.toString()
            var option: String? = null
            when (binding.searchOption.checkedRadioButtonId) {
                R.id.option_default -> option = "0"
                R.id.option_traffic -> option = "1"
                R.id.option_location -> option = "2"
            }
            search(searchTerm!!, email!!, option!!, latitude!!, longitude!!)
        }

    }

    // 검색어 가져오기
    private fun search(searchTerm: String, email: String, option: String, latitude: Double, longitude: Double) {
        val results = mutableListOf<Result>()
        checkToken()
        val url = "http://49.142.162.247:8050/search/title"

        val jsonRequest = JSONArray().apply {
            put(JSONObject().apply {
                put("email", email)
                put("title", searchTerm)
                put("option", option)
                put("latitude", latitude)
                put("longitude", longitude)
                put("token", loginToken)
            })
        }

        if (searchTerm.isBlank()) {
            Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
        } else {
            val request = JsonArrayRequest(
                Request.Method.POST, url, jsonRequest,
                { response ->
                    // 결과 받아서 처리
                    if (response.length() > 0) {
                        for (i in 0 until response.length()) {
                            val title = response.getJSONObject(i).getString("title")
                            val tourId = response.getJSONObject(i).getString("tourId")
                            val like = response.getJSONObject(i).getString("interested")
                            val introduction = response.getJSONObject(i).getString("introduction")
                            val image = response.getJSONObject(i).getString("imagepath")

                            val result = Result(title, tourId, like, introduction, image)
                            results.add(result)

                            val noResultsText = findViewById<TextView>(R.id.no_results_text)
                            noResultsText.visibility = View.GONE
                        }
                        resultAdapter.notifyDataSetChanged()
                    } else {
                        val noResultsText = findViewById<TextView>(R.id.no_results_text)
                        noResultsText.visibility = View.VISIBLE
                    }
                },
                { error ->
                    // 에러 처리
                    Toast.makeText(this, "검색결과 요청 실패!", Toast.LENGTH_SHORT).show()
                    Log.e("SearchActivity", "검색결과 요청 실패!", error)
                })
            requestQueue.add(request)

            resultRecyclerView = findViewById(R.id.result_list)
            resultAdapter = SearchAdapter(this, results, intent.getStringExtra("email").toString(), intent.getStringExtra("login").toString())
            resultRecyclerView.adapter = resultAdapter
        }
    }

    private fun getLatLng(): Location {
        var currentLatLng: Location? = null
        var hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        var hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            val locationProvider = LocationManager.GPS_PROVIDER
            currentLatLng = locationManager?.getLastKnownLocation(locationProvider)

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION)
            }
            currentLatLng = getLatLng()
        }
        return currentLatLng!!
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 요청 후 승인됨 (추적 시작)
                Toast.makeText(this, "위치 권한이 승인되었습니다", Toast.LENGTH_SHORT).show()
                getLatLng()
            } else {
                // 권한 요청 후 거절됨 (다시 요청 or 토스트)
                Toast.makeText(this, "위치 권한이 거절되었습니다", Toast.LENGTH_SHORT).show()
            }
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
                intent.putExtra("email", email)
                intent.putExtra("login", loginToken)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
            R.id.map-> {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("email", email)
                intent.putExtra("login", loginToken)
                startActivity(intent)
                finish()
            }
            R.id.like-> {
                val intent = Intent(this, LikeActivity::class.java)
                intent.putExtra("email", email)
                intent.putExtra("login", loginToken)
                startActivity(intent)
                finish()
            }
            R.id.logout-> {
                val url = "http://49.142.162.247:8050/oauth/logout"
                val logoutData: Map<String, String?> = hashMapOf(
                    "logout" to loginToken
                )
                Log.e("SearchActivity", loginToken.toString())

                val requestBody = JSONObject(logoutData).toString()

                val logoutRequest = object : StringRequest(
                    Method.POST,
                    url,
                    Response.Listener<String> { response ->
                        // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                        if (response == "success") {
                            // 로그아웃에 성공한 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this@SearchActivity, "로그아웃하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("SearchActivity", "로그아웃 성공!")
                            val intent = Intent(this@SearchActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish() // 현재 액티비티를 종료합니다.
                        } else {
                            // 로그아웃에 실패한 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this@SearchActivity, "로그아웃에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("SearchActivity", "로그아웃 실패!")
                        }
                    },
                    Response.ErrorListener { error ->
                        // 요청 실패 시 수행되는 코드를 작성합니다.
                        Log.e("SearchActivity", "로그아웃 요청 실패!", error)
                        Toast.makeText(this@SearchActivity, "로그아웃 요청이 실패하였습니다.", Toast.LENGTH_SHORT).show()
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

    private fun checkToken() {
        val url = "http://49.142.162.247:8050/tokenCheck"
        val logoutData: Map<String, String?> = hashMapOf(
            "logout" to loginToken
        )

        val requestBody = JSONObject(logoutData).toString()

        val tokenCheckRequest = object : StringRequest(
            Method.POST,
            url,
            Response.Listener<String> { response ->
                // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                if (response == "fail") {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
            },
            Response.ErrorListener { error ->
                // 요청 실패 시 수행되는 코드를 작성합니다.
                Log.e("SearchActivity", "토큰 체크 실패!", error)
                Toast.makeText(this, "토큰 체크에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charsets.UTF_8)
            }
        }
        requestQueue.add(tokenCheckRequest)
    }
}