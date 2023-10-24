package com.g4s.go4_driver.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.g4s.go4_driver.R
import com.g4s.go4_driver.databinding.ActivityTrackingOrderBinding
import com.g4s.go4_driver.model.OrderLogModel
import com.g4s.go4_driver.model.ResponseRoutes
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.webservice.ApiClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.bottomsheet_data_customer.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast

class TrackingOrderActivity : AppCompatActivity(), AnkoLogger, OnMapReadyCallback {
    private lateinit var binding: ActivityTrackingOrderBinding
    private val api = ApiClient.instance()
    private lateinit var sessionManager: SessionManager
    private lateinit var mMap: GoogleMap
    var order: OrderLogModel? = null
    var route: ResponseRoutes? = null
    private val handler = Handler(Looper.getMainLooper())
    var latCurrent: Double? = null
    var longCurrent: Double? = null
    val currentLocation: Location? = null
    private var routePolyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tracking_order)
        binding.lifecycleOwner = this
        sessionManager = SessionManager(this)
        val gson = Gson()
        order = gson.fromJson(intent.getStringExtra("order"), OrderLogModel::class.java)
        route = gson.fromJson(order!!.routes, ResponseRoutes::class.java)
        val supportMapFragment =
            (supportFragmentManager.findFragmentById(R.id.mapview) as SupportMapFragment?)!!
        supportMapFragment.getMapAsync(this)

        binding.bottomSheetLayout.btn_start.visibility = View.VISIBLE
        binding.bottomSheetLayout.btn_waypoint.visibility = View.GONE // Sembunyikan btnWaypoint awalnya
        binding.bottomSheetLayout.btn_waypoint.setOnClickListener {

        }
        binding.bottomSheetLayout.btn_start.setOnClickListener {
            // Register receiver for location updates
            val locationUpdateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == "LOCATION_UPDATE") {
                        val latitude = intent.getDoubleExtra("latitude", 0.0)
                        val longitude = intent.getDoubleExtra("longitude", 0.0)
                        currentLocation?.latitude = latitude
                        currentLocation?.longitude = longitude

                    }
                }
            }

            val filter = IntentFilter("LOCATION_UPDATE")
            this.registerReceiver(locationUpdateReceiver, filter)
            binding.bottomSheetLayout.btn_start.visibility = View.GONE
        }
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
            mMap.addMarker(
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
            mMap.addMarker(
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
            mMap.addMarker(
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        handler.removeCallbacksAndMessages(null)
    }
}
