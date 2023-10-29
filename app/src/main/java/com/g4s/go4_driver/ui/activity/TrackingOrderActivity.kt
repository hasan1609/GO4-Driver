package com.g4s.go4_driver.ui.activity

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.g4s.go4_driver.R
import com.g4s.go4_driver.databinding.ActivityTrackingOrderBinding
import com.g4s.go4_driver.model.OrderLogModel
import com.g4s.go4_driver.model.ResponsePostData
import com.g4s.go4_driver.model.ResponseRoutes
import com.g4s.go4_driver.model.TrackingModel
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.webservice.ApiClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.bottomsheet_data_customer.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrackingOrderActivity : AppCompatActivity(), AnkoLogger, OnMapReadyCallback {
    private lateinit var binding: ActivityTrackingOrderBinding
    private val api = ApiClient.instance()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sessionManager: SessionManager
    private lateinit var mMap: GoogleMap
    var order: OrderLogModel? = null
    var route: ResponseRoutes? = null
    var status1: String? = null
    var status2: String? = null
    private val handler = Handler(Looper.getMainLooper())
    var currentLocation: Location? = null
    private var routePolyline: Polyline? = null
    private var marker: Marker? = null
    private var locationUpdateReceiver: BroadcastReceiver? = null
    private var isCheckingWaypoint = true
    private var isCheckingDestination = false

    private val saveLocationRunnable = object : Runnable {
        override fun run() {
            if (currentLocation != null) {
                saveLocationToFirebase(currentLocation!!.latitude, currentLocation!!.longitude)
            }
            handler.postDelayed(this, 5000) // Jadwalkan ulang tugas setiap 5 detik
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tracking_order)
        binding.lifecycleOwner = this
        sessionManager = SessionManager(this)
        progressDialog = ProgressDialog(this)
        val gson = Gson()
        order = gson.fromJson(intent.getStringExtra("order"), OrderLogModel::class.java)
        route = gson.fromJson(order!!.routes, ResponseRoutes::class.java)
        val supportMapFragment =
            (supportFragmentManager.findFragmentById(R.id.mapview) as SupportMapFragment?)!!
        supportMapFragment.getMapAsync(this)
        setupUi()
    }

    private fun setupUi() {
        val type = binding.bottomSheetLayout.type
        when (order!!.kategori) {
            "resto" -> {
                status1 = "Menuju Lokasi Resto"
                status2 = "Sampai Lokasi Resto"
                binding.bottomSheetLayout.btn_start.text = status1
                binding.bottomSheetLayout.btn_waypoint.text = status2
                type.setImageResource(R.drawable.makanan)
            }
            "mobil" -> {
                status1 = "Menuju TItik Jemput"
                status2 = "Sampai Titik Jemput"
                binding.bottomSheetLayout.btn_start.text = status1
                binding.bottomSheetLayout.btn_waypoint.text = status2
                type.setImageResource(R.drawable.mobil)
            }
            else -> {
                status1 = "Menuju TItik Jemput"
                status2 = "Sampai Titik Jemput"
                type.setImageResource(R.drawable.motor)
            }
        }
        val urlImage = this.getString(R.string.urlImage)
        val foto = order!!.detailCustomer!!.foto.toString()
        val ft = binding.bottomSheetLayout.foto
        if (order!!.detailCustomer!!.foto != null) {
            Picasso.get()
                .load(urlImage + foto)
                .into(ft)
        }
        when (order!!.status) {
            "7" -> {
                binding.bottomSheetLayout.btn_start.visibility = View.VISIBLE
            }
            "1" -> {
                binding.bottomSheetLayout.status.text = "Menuju Waypoint"
                isCheckingWaypoint = true
                isCheckingDestination = false
                registerLocationUpdateReceiver()
                startLocationUpdates()
            }
            "2" -> {
                binding.bottomSheetLayout.status.text = "Sampai Waypoint"
                isCheckingWaypoint = false
                isCheckingDestination = false
                binding.bottomSheetLayout.btn_waypoint2.visibility = View.VISIBLE
            }
            "3" -> {
                binding.bottomSheetLayout.status.text = "Menuju Destination"
                isCheckingWaypoint = false
                isCheckingDestination = true
                registerLocationUpdateReceiver()
                startLocationUpdates()
            }
            "4" -> {
                binding.bottomSheetLayout.status.text = "Sampai Tujuan"
                isCheckingWaypoint = false
                isCheckingDestination = false
                binding.bottomSheetLayout.btn_selesai.visibility = View.VISIBLE
            }
        }
        binding.bottomSheetLayout.nama.text = order!!.customer!!.nama
        binding.bottomSheetLayout.btn_start.setOnClickListener {
            registerLocationUpdateReceiver()
            startLocationUpdates()
            updateStatus("1",  status1.toString(), binding.bottomSheetLayout.btn_start,
                checkWaypoint = true,
                checkDestination = false
            )
        }
        binding.bottomSheetLayout.btn_waypoint.setOnClickListener {
            updateStatus("2", status2.toString(), binding.bottomSheetLayout.btn_waypoint,
                checkWaypoint = false,
                checkDestination = false
            )
            handler.removeCallbacksAndMessages(null)
            unregisterReceiver(locationUpdateReceiver)
            binding.bottomSheetLayout.btn_waypoint2.visibility = View.VISIBLE

        }
        binding.bottomSheetLayout.btn_waypoint2.setOnClickListener {
            registerLocationUpdateReceiver()
            startLocationUpdates()
            updateStatus("3", "Menuju Lokasi Tujuan", binding.bottomSheetLayout.btn_waypoint2,
                checkWaypoint = false,
                checkDestination = true
            )
        }
        binding.bottomSheetLayout.btn_sampai.setOnClickListener {
            updateStatus("4", "Sampai Lokasi Tujuan", binding.bottomSheetLayout.btn_sampai,
                checkWaypoint = false,
                checkDestination = false
            )
            binding.bottomSheetLayout.btn_selesai.visibility = View.VISIBLE
        }
        binding.bottomSheetLayout.btn_selesai.setOnClickListener {
        }
    }

    private fun updateMapWithLocation(latitude: Double, longitude: Double) {
        if (marker != null) {
            val newLocation = LatLng(latitude, longitude)
            val startPosition = marker!!.position // Posisi awal marker
            val endPosition = newLocation // Posisi akhir marker (lokasi baru)

            // Hitung arah dan rotasi motor (misalnya, sejajar dengan rute)
            val bearing = SphericalUtil.computeHeading(startPosition, endPosition)

            // Menghitung jarak antara posisi awal dan akhir
            val distance = SphericalUtil.computeDistanceBetween(startPosition, endPosition)

            // Animasi motor berjalan dari posisi awal ke posisi akhir dengan interval waktu tertentu
            val duration = 10000L // Durasi animasi dalam milidetik (2 detik)

            val interpolator = LinearInterpolator()
            val animator = ObjectAnimator.ofObject(marker, "position",
                TypeEvaluator<LatLng> { fraction, startValue, endValue ->
                    return@TypeEvaluator SphericalUtil.interpolate(startValue, endValue,
                        fraction.toDouble()
                    )
                },
                startPosition, endPosition
            )
            animator.duration = duration
            animator.interpolator = interpolator
            animator.start()

            // Rotasi motor (menghadap ke arah rute)
            marker!!.rotation = bearing.toFloat()

            // Anda juga dapat mengatur kamera agar mengikuti perjalanan motor:
            mMap.animateCamera(CameraUpdateFactory.newLatLng(newLocation))
        }
    }
    private fun saveLocationToFirebase(latitude: Double, longitude: Double) {
        // Anda perlu mengganti ini sesuai dengan Firebase Realtime Database atau Firestore Anda.
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("perjalanan_pengemudi").child(sessionManager.getId().toString())
        val locationData = TrackingModel(latitude, longitude)
        // Lakukan pemeriksaan apakah referensi sudah berisi data atau tidak
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Referensi sudah berisi data, maka lakukan update
                    reference.setValue(locationData)
                } else {
                    // Referensi kosong, maka buat entri baru
                    reference.setValue(locationData)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Penanganan kesalahan, jika diperlukan
            }
        })
    }
    private fun registerLocationUpdateReceiver() {
        locationUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "LOCATION_UPDATE") {
                    val latitude = intent.getDoubleExtra("latitude", 0.0)
                    val longitude = intent.getDoubleExtra("longitude", 0.0)
                    currentLocation = Location("")
                    currentLocation?.latitude = latitude
                    currentLocation?.longitude = longitude
                    updateMapWithLocation(latitude, longitude)
                    // Memeriksa jarak ke waypoint dan lokasi tujuan setiap kali menerima pembaruan lokasi
                    if (currentLocation != null) {
                        if (isCheckingWaypoint && !isCheckingDestination)
                        {
                            checkDistanceToWaypoint(currentLocation!!)
                        }else if(!isCheckingWaypoint && isCheckingDestination)
                        {
                            checkDistanceDestination(currentLocation!!)
                        }
                    }
                }
            }
        }
        val filter = IntentFilter("LOCATION_UPDATE")
        this.registerReceiver(locationUpdateReceiver, filter)
    }
    private fun startLocationUpdates() {
        // Mulai jadwal tugas penyimpanan setiap 5 detik
        handler.postDelayed(saveLocationRunnable, 10000) // 5000 milidetik = 5 detik
    }
    private fun checkDistanceToWaypoint(currentLocation: Location) {
        val waypointLocation = Location("")
        waypointLocation.latitude = order!!.latitudeDari.toString().toDouble()
        waypointLocation.longitude = order!!.longitudeDari.toString().toDouble()
        val distanceToWaypoint = currentLocation.distanceTo(waypointLocation)
        if (distanceToWaypoint < 50.0) {
            binding.bottomSheetLayout.btn_waypoint.visibility = View.VISIBLE
        }
    }
    private fun checkDistanceDestination(currentLocation: Location) {
        val destinationLocation = Location("")
        destinationLocation.latitude = order!!.latitudeTujuan.toString().toDouble()
        destinationLocation.longitude = order!!.longitudeTujuan.toString().toDouble()
        val distanceToDestination = currentLocation.distanceTo(destinationLocation)
        if (distanceToDestination < 50.0) {
            binding.bottomSheetLayout.btn_sampai.visibility = View.VISIBLE
        }
    }
    // Fungsi untuk memeriksa apakah BroadcastReceiver aktif
    fun isReceiverEnabled(context: Context, receiver: BroadcastReceiver): Boolean {
        val intent = Intent(context, receiver.javaClass)
        val packageManager = context.packageManager
        val activities = packageManager.queryBroadcastReceivers(intent, 0)
        return activities.size > 0
    }
    private fun updateStatus(status: String, nama: String, button: Button, checkWaypoint: Boolean, checkDestination: Boolean){
        api.updateStatusOrder(order!!.idOrder.toString(), status).enqueue(object :
            Callback<ResponsePostData> {
            override fun onResponse(
                call: Call<ResponsePostData>,
                response: Response<ResponsePostData>
            ) {
                try {
                    if (response.isSuccessful) {
                        loading(false)
                        isCheckingWaypoint = checkWaypoint
                        isCheckingDestination = checkDestination
                        button.visibility = View.GONE
                        binding.bottomSheetLayout.status.text = nama

                    } else {
                        loading(false)
                        toast("gagal mendapatkan response")
                    }
                } catch (e: Exception) {
                    loading(false)
                    info { "hasan ${e.message}" }
                    toast(e.message.toString())
                }
            }
            override fun onFailure(call: Call<ResponsePostData>, t: Throwable) {
                loading(false)
                info { "hasan ${t.message}" }
                toast(t.message.toString())
            }
        })
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val markerTujuan = BitmapDescriptorFactory.fromResource(R.drawable.ic_pinmap)
        val markerDriverMotor = BitmapDescriptorFactory.fromResource(R.drawable.motor)
        val markerDriverMobil = BitmapDescriptorFactory.fromResource(R.drawable.mobil)
        val markerResto = BitmapDescriptorFactory.fromResource(R.drawable.makanan)
        val markerStart = BitmapDescriptorFactory.fromResource(R.drawable.ic_start_order)
        val lat1 = order!!.detailDriver!!.latitude.toString()
        val long1 = order!!.detailDriver!!.longitude.toString()
        val lat2 = order!!.latitudeDari!!.toString()
        val long2 = order!!.longitudeDari!!.toString()
        val lat3 = order!!.latitudeTujuan!!.toString()
        val long3 = order!!.longitudeTujuan!!.toString()
        if (order!!.kategori == "resto") {
            marker = mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat1.toDouble(), long1.toDouble()))
                    .title("Titik Awal").icon(markerDriverMotor))

            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat2.toDouble(), long2.toDouble()))
                    .title("Lokasi Resto").icon(markerResto))
            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat3.toDouble(), long3.toDouble()))
                    .title("Lokasi Tujuan").icon(markerTujuan))
        } else if(order!!.kategori == "mobil") {
            marker = mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat1.toDouble(), long1.toDouble()))
                    .title("Titik Awal").icon(markerDriverMobil))

            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat2.toDouble(), long2.toDouble()))
                    .title("Lokasi Jemput").icon(markerStart))
            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat3.toDouble(), long3.toDouble()))
                    .title("Lokasi Tujuan").icon(markerTujuan))
        } else if(order!!.kategori == "motor_otomatis" || order!!.kategori == "motor_manual"){
            marker = mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat1.toDouble(), long1.toDouble()))
                    .title("Lokasi Jemput").icon(markerDriverMotor))

            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat2.toDouble(), long2.toDouble()))
                    .title("Lokasi Jemput").icon(markerStart))
            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat3.toDouble(), long3.toDouble()))
                    .title("Lokasi Tujuan").icon(markerTujuan))
        }
        mMap.setOnMapLoadedCallback {
            val polyline = route!!.routes?.firstOrNull()?.overviewPolyline?.points
            setRoute(polyline.toString())
        }
    }
    // Fungsi untuk menggambar rute pada peta
    fun setRoute(polyline: String){
        val routeColor = ContextCompat.getColor(this, R.color.primary_color)
        val decodedPath = PolyUtil.decode(polyline)
        if (routePolyline != null) {
            routePolyline!!.remove() // Hapus rute sebelumnya jika ada
        }

        routePolyline = mMap.addPolyline(PolylineOptions()
            .color(routeColor)
            .width(20f)
            .addAll(decodedPath))

        // Menghitung LatLngBounds yang mencakup seluruh rute
        val builder = LatLngBounds.builder()
        for (point in decodedPath) {
            builder.include(LatLng(point.latitude, point.longitude))
        }
        val bounds = builder.build()

        // Set tampilan peta agar fokus pada rute
        val padding = 300
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap.moveCamera(cameraUpdate)
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            progressDialog.setMessage("Tunggu sebentar...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        } else {
            progressDialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        handler.removeCallbacksAndMessages(null)
    }
}
