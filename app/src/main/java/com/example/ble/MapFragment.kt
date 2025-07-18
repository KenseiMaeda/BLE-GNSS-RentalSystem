package com.example.ble

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import androidx.appcompat.app.AppCompatActivity


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var currentGreenMarker: Marker? = null
    private val handler = Handler(Looper.getMainLooper())

    private val beaconNameToLocation = mapOf(
        "予備" to LatLng(36.10948194674492, 140.09998489081934),
        "筑波病院入口" to LatLng(36.093185892556335, 140.1037914639882),
        "追越学生宿舎前" to LatLng(36.095668888272726, 140.1028445895598),
        "平砂学生宿舎前" to LatLng(36.097919783656984, 140.1020445108298),
        "筑波大学西" to LatLng(36.103511800430105, 140.10153750886695),
        "大学会館前" to LatLng(36.10487541794868, 140.10111729192286),
        "第一エリア前" to LatLng(36.107965400224856, 140.0998112941018),
        "第三エリア前" to LatLng(36.11019180117873, 140.0984365219353),
        "虹の広場" to LatLng(36.11416118595694, 140.0970178451117),
        "農林技術センター" to LatLng(36.11867845255836, 140.09621579653592),
        "一ノ矢学生宿舎前" to LatLng(36.119539646538655, 140.09900459735684),
        "大学植物見本園" to LatLng(36.11624218198861, 140.102200745114),
        "TARAセンター前" to LatLng(36.11307129398093, 140.10235269246826),
        "筑波大学中央" to LatLng(36.111369694255615, 140.10367466093868),
        "大学公園" to LatLng(36.110020652055105, 140.104041523001),
        "松美池" to LatLng(36.10816512507706, 140.10439364641243),
        "合宿所" to LatLng(36.10384602305381, 140.1067827908528),
        "天久保池" to LatLng(36.100705339486716, 140.10607356710554)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()

        val center = LatLng(36.106, 140.101)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15f))

        // すべてのビーコンを赤で描画
        for ((name, location) in beaconNameToLocation) {
            map.addMarker(
                MarkerOptions()
                    .position(location)
                    .title("📍 $name")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }

        // ビーコン更新監視（3秒ごと）
        handler.post(object : Runnable {
            override fun run() {
                if (!isAdded) {
                    handler.postDelayed(this, 3000)
                    return
                }

                val activity = activity as? MainActivity
                val beacon = activity?.latestBeacon
                val beaconName = beacon?.let {
                    BeaconRegistry.resolveName(it.id1.toString(), it.id2.toInt(), it.id3.toInt())
                }

                if (beaconName != null && beaconNameToLocation.containsKey(beaconName)) {
                    val location = beaconNameToLocation[beaconName]
                    currentGreenMarker?.remove()
                    currentGreenMarker = map.addMarker(
                        MarkerOptions()
                            .position(location!!)
                            .title("📡 最新ビーコン: $beaconName")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                }

                handler.postDelayed(this, 3000)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }
}
