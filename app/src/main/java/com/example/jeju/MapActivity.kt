package com.example.jeju

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jeju.databinding.ActivityMapBinding
import com.example.jeju.databinding.NavHeaderBinding
import com.google.android.material.navigation.NavigationView
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MapDialog(context: Context) : Dialog(context) {
    private var loginToken: String? = null
    private var image: String? = null
    private var title: String? = null
    private var like: String? = null
    private var tourId: String? = null
    private var email: String? = null
    private var phone: String? = null
    private var introduction: String? = null

    private lateinit var requestQueue: RequestQueue

    fun setLoginToken(loginToken: String) {
        this.loginToken = loginToken
    }
    fun setImageResId(image: String) {
        this.image = image
    }

    fun setTitle(title: String) {
        this.title = title
    }
    fun setIntroduction(introduction: String) {
        this.introduction = introduction
    }

    fun setLike(like: String) {
        this.like = like
    }

    fun setTourId(tourId: String) {
        this.tourId = tourId
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun setPhone(phone: String) {
        this.phone = phone
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val inflater = LayoutInflater.from(context)
        requestQueue = Volley.newRequestQueue(context)
        val dialogView: View = inflater.inflate(R.layout.map_dialog, null)
        setContentView(dialogView)

        val titleTextView: TextView = dialogView.findViewById(R.id.map_dialog_title)
        val introduceTextView: TextView = dialogView.findViewById(R.id.map_dialog_introduce)
        val imageView: ImageView = dialogView.findViewById(R.id.map_dialog_image)
        val phoneTextView: TextView = dialogView.findViewById(R.id.map_tel)
        val heartButton: ImageButton = dialogView.findViewById(R.id.map_heart_button)
        val imageUrl = image
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(dialogView.context).load(imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        }

        if (like == "1") {
            heartButton.setImageResource(R.drawable.ic_heart_filled)
            heartButton.tag = "filled"
        }
        else {
            heartButton.setImageResource(R.drawable.ic_heart_empty)
            heartButton.tag = "empty"
        }

        phoneTextView.setOnClickListener {
            val phoneUri = Uri.parse("tel:${phone}")
            val intent = Intent(Intent.ACTION_DIAL, phoneUri)
            context.startActivity(intent)
        }

        heartButton.setOnClickListener {
            checkToken(context)
            if (heartButton.tag == "empty") {
                heartButton.setImageResource(R.drawable.ic_heart_filled)
                heartButton.tag = "filled"
                val addUrl = "http://49.142.162.247:8050/interest/add"
                val addData: Map<String, String> = hashMapOf(
                    "email" to email.toString(),
                    "tourid" to tourId.toString(),
                    "token" to loginToken.toString()
                )

                val requestBody = JSONObject(addData).toString()

                val addRequest = object : StringRequest(
                    Method.POST, // 요청 방식을 POST로 지정합니다.
                    addUrl, // 요청을 보낼 URL 주소를 지정합니다.
                    Response.Listener<String>
                    { response ->
                        // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                        if (response == "success") {
                            Toast.makeText(context, "찜 등록!", Toast.LENGTH_SHORT).show()
                            Log.e("MapActivity", "찜 등록!")
                        }
                        else {
                            Toast.makeText(context, "찜 등록 실패", Toast.LENGTH_SHORT).show()
                            Log.e("MapActivity", "찜 등록 실패")
                        }
                    },
                    Response.ErrorListener { error ->
                        // 요청 실패 시 수행되는 코드를 작성합니다.
                        Log.e("MapActivity", "error!", error)
                        Toast.makeText(context, "error!", Toast.LENGTH_SHORT).show()
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
                requestQueue.add(addRequest)
            } else {
                checkToken(context)
                heartButton.setImageResource(R.drawable.ic_heart_empty)
                heartButton.tag = "empty"
                val deleteUrl = "http://49.142.162.247:8050/interest/delete"
                val deleteData: Map<String, String> = hashMapOf(
                    "email" to email.toString(),
                    "tourid" to tourId.toString(),
                    "token" to loginToken.toString()
                )
                val requestBody = JSONObject(deleteData).toString()

                val deleteRequest = object : StringRequest(
                    Method.POST, // 요청 방식을 POST로 지정합니다.
                    deleteUrl, // 요청을 보낼 URL 주소를 지정합니다.
                    Response.Listener<String>
                    { response ->
                        // 서버로부터 응답을 받았을 때 수행되는 코드를 작성합니다.
                        if (response == "success") {
                            Toast.makeText(context, "찜 삭제!", Toast.LENGTH_SHORT).show()
                            Log.e("MapActivity", "찜 삭제!")
                        }
                        else {
                            Toast.makeText(context, "찜 삭제 실패", Toast.LENGTH_SHORT).show()
                            Log.e("MapActivity", "찜 삭제 실패")
                        }
                    },
                    Response.ErrorListener { error ->
                        // 요청 실패 시 수행되는 코드를 작성합니다.
                        Log.e("MapActivity", "error!", error)
                        Toast.makeText(context, "error!", Toast.LENGTH_SHORT).show()
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
                requestQueue.add(deleteRequest)
            }
        }

        titleTextView.text = title
        introduceTextView.text = introduction
    }
    private fun checkToken(context: Context) {
        val requestQueue = Volley.newRequestQueue(context)
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
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                }
            },
            Response.ErrorListener { error ->
                // 요청 실패 시 수행되는 코드를 작성합니다.
                Log.e("MapActivity", "토큰 체크 실패!", error)
                Toast.makeText(context, "토큰 체크에 실패하였습니다.", Toast.LENGTH_SHORT).show()
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
class MapActivity : AppCompatActivity(), MapView.POIItemEventListener,
    MapView.MapViewEventListener, NavigationView.OnNavigationItemSelectedListener {
    val currentLocationMaker: MapPOIItem = MapPOIItem()
    var centerPoint: MapPoint? = null

    private var email: String? = null
    private var loginToken : String? = null
    private var tourId: String? = null
    private var title: String? = null
    private var like: String? = null
    private var introduction: String? = null
    private var address: String? = null
    private var image: String? = null
    private var phone: String? = null
    private var chargingPlace: String? = null
    private var chargingAddress: String? = null

    private var checkSmooth: Boolean = true
    private var checkNormal: Boolean = false
    private var checkCongestion: Boolean = false

    private val ACCESS_FINE_LOCATION = 1000 // Request Code

    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    private lateinit var requestQueue: RequestQueue
    private lateinit var binding: ActivityMapBinding
    private lateinit var locationManager: LocationManager

    private val current = LocalDateTime.now()
    private val formatDate = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val formatHour = DateTimeFormatter.ofPattern("HH")
    private var baseDate = current.format(formatDate)
    private var baseHour = current.format(formatHour)

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

        loginToken = intent.getStringExtra("login").toString()

        if (MapView.isMapTilePersistentCacheEnabled()) {
            MapView.setMapTilePersistentCacheEnabled(true)
        }

        binding.optionDrawer.setOnClickListener {
            if (binding.mapOption.tag == "open") {
                binding.optionDrawer.setImageResource(R.drawable.ic_more)
                binding.mapOption.visibility = View.GONE
                binding.dateOption.visibility = View.GONE
                binding.chargeOptionBtn.visibility = View.GONE
                binding.mapOption.tag = "close"
            } else {
                binding.optionDrawer.setImageResource(R.drawable.ic_less)
                binding.mapOption.visibility = View.VISIBLE
                binding.dateOption.visibility = View.VISIBLE
                if (binding.chargeMap.tag == "true") {
                    binding.dateOption.visibility = View.GONE
                    binding.chargeOptionBtn.visibility = View.VISIBLE
                }
                binding.mapOption.tag = "open"
            }
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var userLocation: Location? = getLatLng()

        binding.chargeNearBtn.setOnClickListener {
            binding.chargeNearBtn.setBackgroundColor(Color.parseColor("#CCCCCC"))
            binding.chargeAllBtn.setBackgroundColor(Color.WHITE)
            binding.chargeNearBtn.tag = "clicked"
            binding.chargeAllBtn.tag = "nonClicked"
            binding.mapView.removeAllPOIItems()
            chargeMarking(userLocation!!, "2")
        }

        binding.chargeAllBtn.setOnClickListener {
            binding.chargeNearBtn.setBackgroundColor(Color.WHITE)
            binding.chargeAllBtn.setBackgroundColor(Color.parseColor("#CCCCCC"))
            binding.chargeNearBtn.tag = "nonClicked"
            binding.chargeAllBtn.tag = "clicked"
            binding.mapView.removeAllPOIItems()
            chargeMarking(userLocation!!, "1")
        }

        binding.check1.setOnCheckedChangeListener { _, isChecked ->
            checkSmooth = isChecked
        }
        binding.check2.setOnCheckedChangeListener { _, isChecked ->
            checkNormal = isChecked
        }
        binding.check3.setOnCheckedChangeListener { _, isChecked ->
            checkCongestion = isChecked
        }

        val spinner: Spinner = findViewById(R.id.time)
        ArrayAdapter.createFromResource(
            this,
            R.array.times,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.setSelection(baseHour.toInt())

        binding.tourOptionBtn.setOnClickListener {
            if (checkSmooth || checkNormal || checkCongestion) {
                binding.mapView.removeAllPOIItems()
                tourMarking(this, spinner)
            } else {
             Toast.makeText(this, "혼잡도 옵션을 최소 1개 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.chargeOptionBtn.setOnClickListener {
            if (checkSmooth || checkNormal || checkCongestion) {
                binding.mapView.removeAllPOIItems()
                if (binding.chargeNearBtn.tag == "clicked") {
                    chargeMarking(userLocation!!, "2")
                } else {
                    chargeMarking(userLocation!!, "1")
                }

            } else {
                Toast.makeText(this, "혼잡도 옵션을 최소 1개 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.chargeBtn.setOnClickListener {
            if (binding.mapOption.tag == "close") {
                binding.mapOption.tag = "open"
            }
            binding.mapOption.visibility = View.VISIBLE
            if (binding.chargeMap.tag == "false") {
                binding.dateOption.visibility = View.GONE
                binding.chargeOptionBtn.visibility = View.VISIBLE
                binding.tourSearch.visibility = View.GONE
                binding.tourOptionBtn.visibility = View.GONE
                binding.chargeMap.tag = "true"
                binding.chargeNearBtn.setBackgroundColor(Color.WHITE)
                binding.chargeAllBtn.setBackgroundColor(Color.parseColor("#CCCCCC"))
                binding.chargeNearBtn.tag = "clicked"
                binding.chargeAllBtn.tag = "nonClicked"
                binding.chargePrint.visibility = View.VISIBLE
                binding.mapView.removeAllPOIItems()
                chargeMarking(userLocation!!, "2")
            } else {
                binding.dateOption.visibility = View.VISIBLE
                binding.chargeOptionBtn.visibility = View.GONE
                binding.tourSearch.visibility = View.VISIBLE
                binding.tourOptionBtn.visibility = View.VISIBLE
                binding.chargeMap.tag = "false"
                binding.chargePrint.visibility = View.GONE
                binding.mapView.removeAllPOIItems()
                tourMarking(this, spinner)
            }
        }

        binding.mapView.setCalloutBalloonAdapter(CustomBalloonAdapter(this, layoutInflater))

        binding.btnMyLocation.setOnClickListener {
            permissionCheck()
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

        tourMarking(this, spinner)

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

    private fun tourMarking(context: Context, spinner: Spinner) {
        binding.dateText.text = baseDate
        val searchTerm = binding.tourEditText.text.toString()
        println(searchTerm)
        binding.calendar.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val maxDate = calendar.clone() as Calendar
            maxDate.add(Calendar.DAY_OF_MONTH, 6)

            DatePickerDialog(context, { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)

                if (selectedDate.after(maxDate)) {
                    var builder = AlertDialog.Builder(context)
                    val dialogView = layoutInflater.inflate(R.layout.date_dialog, null)

                    builder.setView(dialogView)
                        .setPositiveButton("확인") { _, _ ->

                        }
                        .setNegativeButton("취소") { _, _ ->

                        }.show()
                }

                run {
                    baseDate = if (month + 1 < 10) {
                        if (day < 10) {
                            "${year}0${month + 1}0${day}"
                        } else {
                            "${year}0${month + 1}${day}"
                        }
                    } else {
                        if (day < 10) {
                            "${year}${month + 1}0${day}"
                        } else{
                            "${year}${month + 1}${day}"
                        }
                    }
                    binding.dateText.text = baseDate
                }
            }, year, month, day).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }

        baseHour = spinner.selectedItem.toString()

        checkToken(this)

        val url = "http://49.142.162.247:8050/recommand2"

        val jsonRequest = JSONArray().apply {
            put(JSONObject().apply {
                put("title", searchTerm)
                put("baseDate", baseDate)
                put("baseHour", baseHour)
            })
        }

        val request = JsonArrayRequest(
            Request.Method.POST, url, jsonRequest,
            { response ->
                // 결과 받아서 처리
                for (i in 0 until response.length()) {
                    title = response.getJSONObject(i).getString("title")
                    address = response.getJSONObject(i).getString("roadaddress")
                    val latitude = response.getJSONObject(i).getDouble("latitude")
                    val longitude = response.getJSONObject(i).getDouble("longitude")
                    val traffic = response.getJSONObject(i).getString("traffic")

                    val point = MapPoint.mapPointWithGeoCoord(
                        latitude,
                        longitude
                    )
                    if (checkSmooth && traffic == "0") {
                        createSmoothMaker(point)
                    } else if(checkNormal && traffic == "1") {
                        createNormalMaker(point)
                    } else if(checkCongestion && traffic == "2") {
                        createCongestionMaker(point)
                    }
                }
            },
            { error ->
                // 에러 처리
                Toast.makeText(this, "관광지 데이터 요청 실패!", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "관광지 데이터 요청 실패!", error)
            })
        requestQueue.add(request)
    }

    private fun chargeMarking(userLocation: Location, option: String) {
        checkToken(this)

        val url = "http://49.142.162.247:8050/charge/coordinate"

        val jsonRequest = JSONArray().apply {
            put(JSONObject().apply {
                put("latitude", userLocation.latitude)
                put("longitude", userLocation.longitude)
                put("option", option)
            })
        }

        val request = JsonArrayRequest(
            Request.Method.POST, url, jsonRequest,
            { response ->
                // 결과 받아서 처리
                for (i in 0 until response.length()) {
                    chargingPlace = response.getJSONObject(i).getString("chargingplace")
                    chargingAddress = response.getJSONObject(i).getString("address")
                    val latitude = response.getJSONObject(i).getDouble("latitude")
                    val longitude = response.getJSONObject(i).getDouble("longitude")
                    val traffic = response.getJSONObject(i).getString("traffic")

                    val point = MapPoint.mapPointWithGeoCoord(
                        latitude,
                        longitude
                    )

                    if (checkSmooth && traffic == "0") {
                        createSmoothMaker(point)
                    } else if(checkNormal && traffic == "1") {
                        createNormalMaker(point)
                    } else if(checkCongestion && traffic == "2") {
                        createCongestionMaker(point)
                    }
                }
            },
            { error ->
                // 에러 처리
                Toast.makeText(this, "충전소 데이터 요청 실패!", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "충전소 데이터 요청 실패!", error)
            })
        requestQueue.add(request)
    }

    inner class CustomBalloonAdapter(val context: Context, inflater: LayoutInflater): CalloutBalloonAdapter {
        private val mCalloutBalloon: View = inflater.inflate(R.layout.balloon_layout, null)
        private val nameTextView: TextView = mCalloutBalloon.findViewById(R.id.ball_tv_name)
        private val addressTextView: TextView = mCalloutBalloon.findViewById(R.id.ball_tv_address)

        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
            if (binding.chargeMap.tag == "false") {
                checkToken(context)
                val url = "http://49.142.162.247:8050/search/detail"
                val jsonRequest = JSONArray().apply {
                    put(JSONObject().apply {
                        put("email", email)
                        put("title", poiItem?.itemName)
                    })
                }

                val request = JsonArrayRequest(
                    Request.Method.POST, url, jsonRequest,
                    { response ->
                        // 결과 받아서 처리
                        for (i in 0 until response.length()) {
                            title = response.getJSONObject(i).getString("title")
                            tourId = response.getJSONObject(i).getString("tourId")
                            like = response.getJSONObject(i).getString("interested")
                            introduction = response.getJSONObject(i).getString("introduction")
                            address = response.getJSONObject(i).getString("roadaddress")
                            image = response.getJSONObject(i).getString("imagepath")
                            phone = response.getJSONObject(i).getString("phoneno")
                        }

                    },
                    { error ->
                        // 에러 처리
                        Toast.makeText(context, "검색결과 요청 실패!", Toast.LENGTH_SHORT).show()
                        Log.e("MapActivity", "검색결과 요청 실패!", error)
                    })
                requestQueue.add(request)
                addressTextView.text = address
            } else {
                addressTextView.text = chargingAddress
            }

            nameTextView.text = poiItem?.itemName
            return mCalloutBalloon
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View {
            return mCalloutBalloon
        }
    }
    private fun checkToken(context: Context) {
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
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
            },
            Response.ErrorListener { error ->
                // 요청 실패 시 수행되는 코드를 작성합니다.
                Log.e("HomeActivity", "토큰 체크 실패!", error)
                Toast.makeText(context, "토큰 체크에 실패하였습니다.", Toast.LENGTH_SHORT).show()
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

    private fun createSmoothMaker(point: MapPoint) {
        val customMaker = MapPOIItem()
        if (binding.chargeMap.tag == "true") {
            customMaker.itemName = chargingPlace
        } else {
            customMaker.itemName = title
        }
        customMaker.mapPoint = point
        customMaker.markerType = MapPOIItem.MarkerType.BluePin
        customMaker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
        customMaker.isCustomImageAutoscale = false
        customMaker.setCustomImageAnchor(0.5f, 0.5f)
        binding.mapView.addPOIItem(customMaker)
    }

    private fun createNormalMaker(point: MapPoint) {
        val customMaker = MapPOIItem()
        if (binding.chargeMap.tag == "true") {
            customMaker.itemName = chargingPlace
        } else {
            customMaker.itemName = title
        }
        customMaker.mapPoint = point
        customMaker.markerType = MapPOIItem.MarkerType.YellowPin
        customMaker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
        customMaker.isCustomImageAutoscale = false
        customMaker.setCustomImageAnchor(0.5f, 0.5f)
        binding.mapView.addPOIItem(customMaker)
    }

    private fun createCongestionMaker(point: MapPoint) {
        val customMaker = MapPOIItem()
        if (binding.chargeMap.tag == "true") {
            customMaker.itemName = chargingPlace
        } else {
            customMaker.itemName = title
        }
        customMaker.mapPoint = point
        customMaker.markerType = MapPOIItem.MarkerType.RedPin
        customMaker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
        customMaker.isCustomImageAutoscale = false
        customMaker.setCustomImageAnchor(0.5f, 0.5f)
        binding.mapView.addPOIItem(customMaker)
    }

    private fun permissionCheck() {
        binding.btnMyLocation.setImageResource(R.drawable.ic_gps_fixed)
        val preference = getPreferences(MODE_PRIVATE)
        val isFirstCheck = preference.getBoolean("isFirstPermissionCheck", true)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 상태
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // 권한 거절 (다시 한 번 물어봄)
                val builder = AlertDialog.Builder(this)
                builder.setMessage("현재 위치를 확인하시려면 위치 권한을 허용해주세요.")
                Log.e("Map", "maperorre")
                builder.setPositiveButton("확인") { dialog, which ->
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION)
                }
                builder.setNegativeButton("취소") { dialog, which ->

                }
                builder.show()
            } else {
                if (isFirstCheck) {
                    // 최초 권한 요청
                    preference.edit().putBoolean("isFirstPermissionCheck", false).apply()
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION)
                } else {
                    // 다시 묻지 않음 클릭 (앱 정보 화면으로 이동)
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("현재 위치를 확인하시려면 설정에서 위치 권한을 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { dialog, which ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
                        startActivity(intent)
                    }
                    builder.setNegativeButton("취소") { dialog, which ->

                    }
                    builder.show()
                }
            }
        } else {
            // 권한이 있는 상태
            startTracking()
        }
    }

    // 권한 요청 후 행동
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 요청 후 승인됨 (추적 시작)
                Toast.makeText(this, "위치 권한이 승인되었습니다", Toast.LENGTH_SHORT).show()
                startTracking()
            } else {
                // 권한 요청 후 거절됨 (다시 요청 or 토스트)
                Toast.makeText(this, "위치 권한이 거절되었습니다", Toast.LENGTH_SHORT).show()
                permissionCheck()
            }
        }
    }

    private fun startTracking() {
        binding.mapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
    }

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {

    }
    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {

    }
    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {
        if (binding.chargeMap.tag == "false") {
            val mapDialog = MapDialog(this@MapActivity)
            mapDialog.setLoginToken(loginToken!!)
            mapDialog.setImageResId(image!!)
            mapDialog.setTitle(title!!)
            mapDialog.setIntroduction(introduction!!)
            mapDialog.setLike(like!!)
            mapDialog.setTourId(tourId!!)
            mapDialog.setEmail(email!!)
            mapDialog.setPhone(phone!!)
            mapDialog.show()
        }
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {

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
                drawerLayout.closeDrawer(GravityCompat.START)
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


