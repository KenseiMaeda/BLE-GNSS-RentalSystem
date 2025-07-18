package com.example.ble

import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.RemoteException
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.altbeacon.beacon.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), BeaconConsumer {

    lateinit var beaconManager: BeaconManager
    var latestBeacon: Beacon? = null
    var isBeaconAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_rental -> switchFragment(RentalFragment())
                R.id.nav_map -> switchFragment(MapFragment())
                R.id.nav_log -> switchFragment(LogFragment())
            }
            true
        }

        switchFragment(RentalFragment())

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.clear()
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )
        beaconManager.bind(this)
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBeaconServiceConnect() {
        val region = Region("all-beacons", null, null, null)

        val notifier = object : RangeNotifier {
            override fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region) {
                if (beacons.isNotEmpty()) {
                    val beacon = beacons.first()
                    latestBeacon = beacon

                    val name = BeaconRegistry.resolveName(
                        beacon.id1.toString(),
                        beacon.id2.toInt(),
                        beacon.id3.toInt()
                    )

                    val logEntry = LogFragment.BeaconLog(
                        time = getCurrentTimeString(),
                        name = name,
                        uuid = beacon.id1.toString(),
                        major = beacon.id2.toInt(),
                        minor = beacon.id3.toInt(),
                        rssi = beacon.rssi
                    )
                    LogFragment.LogStore.add(logEntry)

                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (currentFragment is LogFragment && currentFragment.isAdded) {
                        runOnUiThread {
                            currentFragment.refreshLogs()
                        }
                    }
                }
            }
        }

        beaconManager.addRangeNotifier(notifier)

        try {
            beaconManager.startRangingBeacons(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun getCurrentTimeString(): String {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return format.format(Date())
    }

    // BeaconConsumer に必要なオーバーライド
    override fun getApplicationContext(): android.content.Context {
        return super.getApplicationContext()
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
    }
}
