package com.example.jeju

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jeju.databinding.ActivityMapBinding
import com.example.jeju.databinding.NavHeaderBinding
import com.google.android.material.navigation.NavigationView
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.json.JSONObject

class MapActivity : AppCompatActivity(), MapView.POIItemEventListener,
    MapView.MapViewEventListener, NavigationView.OnNavigationItemSelectedListener {
    val currentLocationMaker: MapPOIItem = MapPOIItem()
    var centerPoint: MapPoint? = null
    private var email: String? = null
    private var loginToken : String? = null
    private val ACCESS_FINE_LOCATION = 1000 // Request Code

    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    private lateinit var requestQueue: RequestQueue
    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        requestQueue = Volley.newRequestQueue(this)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.main_layout_toolbar) // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 메뉴 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu) // 메뉴 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 네비게이션 드로어 생성
        drawerLayout = findViewById(R.id.map_drawer_layout)

        // 네비게이션 드로어 내에 있는 화면의 이벤트를 처리하기 위해 생성
        navigationView = findViewById(R.id.map_navigationView)
        navigationView.setNavigationItemSelectedListener(this) //navigation 리스너

        val headerBinding = NavHeaderBinding.bind(navigationView.getHeaderView(0))
        email = intent.getStringExtra("email").toString()
        headerBinding.userEmail.text = email

        loginToken = intent.getStringExtra("login")

        if (MapView.isMapTilePersistentCacheEnabled()) {
            MapView.setMapTilePersistentCacheEnabled(true)
        }

        binding.btnMyLocation.setOnClickListener {
            startTracking()
            if (centerPoint != null) {
                val point = MapPoint.mapPointWithGeoCoord(
                    centerPoint!!.mapPointGeoCoord.latitude,
                    centerPoint!!.mapPointGeoCoord.longitude
                )
                binding.mapView.setMapCenterPoint(point, false)
            }
        }

        binding.mapView.setMapViewEventListener(this)
        binding.mapView.setPOIItemEventListener(this)


        class CurrentLocationListener : MapView.CurrentLocationEventListener {
            override fun onCurrentLocationUpdateFailed(p0: MapView?) {
                Toast.makeText(applicationContext, "위치정보 로딩 실패", Toast.LENGTH_LONG).show()
            }

            override fun onCurrentLocationUpdate(
                mapView: MapView?,
                point: MapPoint?,
                accuracy: Float
            ) {
                if (point != null) {
                    centerPoint = point
                    currentLocationMaker.moveWithAnimation(point, false)
                    currentLocationMaker.alpha = 1f
                    if (mapView != null) {
                        binding.mapView.setMapCenterPoint(
                            MapPoint.mapPointWithGeoCoord(
                                point.mapPointGeoCoord.latitude,
                                point.mapPointGeoCoord.longitude
                            ), false
                        )
                    }
                }
            }

            override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
                Toast.makeText(applicationContext, "위치 요청 취소", Toast.LENGTH_LONG).show()
            }

            override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
                Toast.makeText(applicationContext, "단말의 각도 값 요청", Toast.LENGTH_LONG).show()
            }
        }

        binding.mapView.setCurrentLocationEventListener(CurrentLocationListener())
    }

    private fun createCustomMaker(mapView: MapView) {
        val customMaker = MapPOIItem()
        customMaker.itemName = "Custom Marker"
        customMaker.tag = 1
        println("eventPoint: $centerPoint")
        customMaker.mapPoint = centerPoint
        customMaker.markerType = MapPOIItem.MarkerType.CustomImage
        customMaker.customImageResourceId = R.drawable.ic_location_mark
        customMaker.isCustomImageAutoscale = false
        customMaker.setCustomImageAnchor(0.5f, 0.5f)
        binding.mapView.addPOIItem(customMaker)
        binding.mapView.selectPOIItem(customMaker, true)
        binding.mapView.setMapCenterPoint(centerPoint, false)
    }

    private fun createStoreMaker(mapView: MapView, storePoint: MapPoint) {
        val customMaker = MapPOIItem()
        customMaker.itemName = "Custom Marker"
        customMaker.tag = 1
        customMaker.mapPoint = storePoint
        customMaker.markerType = MapPOIItem.MarkerType.CustomImage
        customMaker.customImageResourceId = R.drawable.ic_location_mark
        customMaker.isCustomImageAutoscale = false
        currentLocationMaker.setCustomImageAnchor(0.5f, 0.5f)
        binding.mapView.addPOIItem(currentLocationMaker)
        binding.mapView.selectPOIItem(currentLocationMaker, true)
        binding.mapView.setMapCenterPoint(centerPoint, false)
    }

    private fun startTracking() {
        binding.btnMyLocation.setImageResource(R.drawable.ic_gps_fixed)
        val preference = getPreferences(MODE_PRIVATE)
        val isFirstCheck = preference.getBoolean("isFirstPermissionCheck", true)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없는 상태
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // 권한 거절 (다시 한 번 물어봄)
                val builder = AlertDialog.Builder(this)
                builder.setMessage("현재 위치를 확인하시려면 위치 권한을 허용해주세요.")
                builder.setPositiveButton("확인") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                }
                builder.setNegativeButton("취소") { dialog, which ->

                }
                builder.show()
            } else {
                if (isFirstCheck) {
                    // 최초 권한 요청
                    preference.edit().putBoolean("isFirstPermissionCheck", false).apply()
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                } else {
                    // 다시 묻지 않음 클릭 (앱 정보 화면으로 이동)
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("현재 위치를 확인하시려면 설정에서 위치 권한을 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { dialog, which ->
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                    builder.setNegativeButton("취소") { dialog, which ->

                    }
                    builder.show()
                }
            }
        } else {
            // 권한이 있는 상태
            binding.mapView.currentLocationTrackingMode =
                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        }
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {

    }
    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {

    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {

    }

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {

    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewInitialized(p0: MapView?) {
        startTracking()
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
        binding.btnMyLocation.setImageResource(R.drawable.ic_gps_not_fixed)
        binding.mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {

    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

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
                startActivity(intent)
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
                Log.e("MapActivity", loginToken.toString())

                val requestBody = JSONObject(logoutData).toString()

                val logoutRequest = object : StringRequest(
                    Method.POST,
                    url,
                    Response.Listener<String> { response ->
                        // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                        if (response == "success") {
                            // 로그아웃에 성공한 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this@MapActivity, "로그아웃하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("MapActivity", "로그아웃 성공!")
                            val intent = Intent(this@MapActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish() // 현재 액티비티를 종료합니다.
                        } else {
                            // 로그아웃에 실패한 경우 처리할 코드를 작성합니다.
                            Toast.makeText(this@MapActivity, "로그아웃에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("MapActivity", "로그아웃 실패!")
                        }
                    },
                    Response.ErrorListener { error ->
                        // 요청 실패 시 수행되는 코드를 작성합니다.
                        Log.e("MapActivity", "로그아웃 요청 실패!", error)
                        Toast.makeText(this@MapActivity, "로그아웃 요청이 실패하였습니다.", Toast.LENGTH_SHORT).show()
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
}


