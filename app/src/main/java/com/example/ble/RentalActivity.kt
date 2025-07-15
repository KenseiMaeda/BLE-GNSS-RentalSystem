package com.example.ble

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.altbeacon.beacon.*

class RentalActivity : AppCompatActivity(), BeaconConsumer {

    private lateinit var beaconManager: BeaconManager
    private lateinit var textBeaconStatus: TextView
    private lateinit var textTimer: TextView
    private lateinit var btnLend: Button
    private lateinit var btnReturn: Button
    private lateinit var editBikeNumber: EditText
    private lateinit var btnSearchBike: Button
    private lateinit var textLockNumber: TextView

    private var lastBeaconDetectedTime: Long = 0L
    private val buttonCheckHandler = Handler(Looper.getMainLooper())

    private var rentalStartTime: Long = 0L
    private val timerHandler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable
    private var isTimerRunning = false

    private var searchedBikeValid = false
    private var beaconNearby = false

    private val bikeData = mapOf(
        "1001" to "001",
        "1002" to "002",
        "1003" to "003"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rental)

        textBeaconStatus = findViewById(R.id.textBeaconStatus)
        textTimer = findViewById(R.id.textTimer)
        btnLend = findViewById(R.id.btnLend)
        btnReturn = findViewById(R.id.btnReturn)
        editBikeNumber = findViewById(R.id.editBikeNumber)
        btnSearchBike = findViewById(R.id.btnSearchBike)
        textLockNumber = findViewById(R.id.textLockNumber)

        btnLend.isEnabled = false
        btnReturn.isEnabled = false
        textTimer.text = "⏱ 経過時間: 00:00:00"

        btnSearchBike.isEnabled = false
        // 検索処理
        btnSearchBike.setOnClickListener {
            val input = editBikeNumber.text.toString().trim()
            val lock = bikeData[input]

            if (lock != null) {
                textLockNumber.text = "🔒 ロック番号: $lock"
                searchedBikeValid = true
                updateLendButtonState()
            } else {
                textLockNumber.text = "🚫 該当する自転車が見つかりません"
                searchedBikeValid = false
                btnLend.isEnabled = false
            }
        }

        // 貸出
        btnLend.setOnClickListener {
            rentalStartTime = System.currentTimeMillis()
            startTimer()
            btnLend.isEnabled = false
            btnReturn.isEnabled = true
        }

        // 返却
        btnReturn.setOnClickListener {
            stopTimer()
            btnReturn.isEnabled = false
            textBeaconStatus.text = "✅ 自転車を返却しました"
        }

        // Beacon init
        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.clear()
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )
        beaconManager.bind(this)
    }

    override fun onBeaconServiceConnect() {
        val region = Region("all-beacons", null, null, null)

        beaconManager.addRangeNotifier { beacons, _ ->
            if (beacons.isNotEmpty()) {
                for (beacon in beacons) {
                    val uuid = beacon.id1.toString()
                    val major = beacon.id2.toInt()
                    val minor = beacon.id3.toInt()
                    val name = BeaconRegistry.resolveName(uuid, major, minor)

                    runOnUiThread {
                        textBeaconStatus.text = "📍 検出: $name"
                        lastBeaconDetectedTime = System.currentTimeMillis()
                        beaconNearby = true

                        btnSearchBike.isEnabled = true  // ← 検出されたら検索ボタンを有効に
                        updateLendButtonState()
                        btnReturn.isEnabled = isTimerRunning
                    }
                }
            }
        }

        try {
            beaconManager.startRangingBeacons(region)
        } catch (e: RemoteException) {
            Log.e("RentalActivity", "ビーコン開始失敗", e)
        }

        startButtonStatusChecker()
    }

    private fun startButtonStatusChecker() {
        buttonCheckHandler.post(object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - lastBeaconDetectedTime
                if (elapsed > 3000) {
                    beaconNearby = false
                    btnSearchBike.isEnabled = false  // ← 追加
                    btnLend.isEnabled = false
                    btnReturn.isEnabled = false
                    textBeaconStatus.text = "⏱️ ビーコンが検出されていません"
                }
                buttonCheckHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun updateLendButtonState() {
        btnLend.isEnabled = searchedBikeValid && beaconNearby && !isTimerRunning
    }

    private fun startTimer() {
        isTimerRunning = true
        timerRunnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - rentalStartTime
                val seconds = (elapsedMillis / 1000) % 60
                val minutes = (elapsedMillis / (1000 * 60)) % 60
                val hours = (elapsedMillis / (1000 * 60 * 60))

                val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                textTimer.text = "⏱ 経過時間: $formattedTime"
                timerHandler.postDelayed(this, 1000)
            }
        }
        timerHandler.post(timerRunnable)
    }

    private fun stopTimer() {
        isTimerRunning = false
        timerHandler.removeCallbacks(timerRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
        buttonCheckHandler.removeCallbacksAndMessages(null)
        timerHandler.removeCallbacksAndMessages(null)
    }
}
