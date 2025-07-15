package com.example.ble

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import org.altbeacon.beacon.*
import android.content.pm.PackageManager
import android.os.RemoteException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.widget.Button

class MainActivity : AppCompatActivity(), BeaconConsumer {

    private lateinit var beaconManager: BeaconManager
    private lateinit var textStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        textStatus = findViewById(R.id.textStatus)

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.clear()
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )

        beaconManager.bind(this)
        checkPermissions()

        // マップ画面へ遷移するボタンの処理
        findViewById<Button>(R.id.btnOpenMap).setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        // レンタル画面へ遷移するボタンの処理
        findViewById<Button>(R.id.btnOpenRental).setOnClickListener {
            val intent = Intent(this, RentalActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 1)
        }
    }

    override fun onBeaconServiceConnect() {
        val region = Region("all-beacons", null, null, null)
        beaconManager.addRangeNotifier { beacons, _ ->
            runOnUiThread {
                if (beacons.isEmpty()) {
                    textStatus.text = "ビーコンは見つかりませんでした"
                }
            }

            if (beacons.isNotEmpty()) {
                val builder = StringBuilder()
                val database = FirebaseDatabase.getInstance("https://beaconmanager-405e2-default-rtdb.firebaseio.com/")
                val logRef = database.getReference("beacon_logs/receiver_01").limitToLast(1)

                for (beacon in beacons) {
                    val uuid = beacon.id1.toString()
                    val major = beacon.id2.toString().toIntOrNull()
                    val minor = beacon.id3.toString().toIntOrNull()
                    val name = BeaconRegistry.resolveName(uuid, major, minor)

                    val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val log = "[$timeStr] $name - RSSI: ${beacon.rssi}"
                    builder.appendLine(log)
                    Log.d("Beacon", log)
                }

                runOnUiThread {
                    textStatus.append(builder.toString())
                }
            }
        }

        try {
            beaconManager.stopRangingBeacons(region)
            beaconManager.startRangingBeacons(region)
        } catch (e: RemoteException) {
            Log.e("Beacon", "ビーコンの検出に失敗しました", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
    }
}
