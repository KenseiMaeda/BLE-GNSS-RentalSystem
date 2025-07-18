package com.example.ble

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class RentalFragment : Fragment() {

    private lateinit var textBeaconStatus: TextView
    private lateinit var textTimer: TextView
    private lateinit var btnLend: Button
    private lateinit var btnReturn: Button
    private lateinit var editBikeNumber: EditText
    private lateinit var btnSearchBike: Button
    private lateinit var textLockNumber: TextView

    private val buttonCheckHandler = Handler(Looper.getMainLooper())
    private val timerHandler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable

    private var rentalStartTime: Long = 0L
    private var isTimerRunning = false
    private var searchedBikeValid = false

    private val bikeData = mapOf(
        "1001" to "001",
        "1002" to "002",
        "1003" to "003"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_rental, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textBeaconStatus = view.findViewById(R.id.textBeaconStatus)
        textTimer = view.findViewById(R.id.textTimer)
        btnLend = view.findViewById(R.id.btnLend)
        btnReturn = view.findViewById(R.id.btnReturn)
        editBikeNumber = view.findViewById(R.id.editBikeNumber)
        btnSearchBike = view.findViewById(R.id.btnSearchBike)
        textLockNumber = view.findViewById(R.id.textLockNumber)

        btnLend.isEnabled = false
        btnReturn.isEnabled = false
        btnSearchBike.isEnabled = false
        textTimer.text = "⏱ 経過時間: 00:00:00"

        val mainActivity = activity as? MainActivity
        val beacon = mainActivity?.latestBeacon

        if (beacon != null) {
            val name = BeaconRegistry.resolveName(
                beacon.id1.toString(),
                beacon.id2.toInt(),
                beacon.id3.toInt()
            )
            textBeaconStatus.text = "📍 検出: $name"
            btnSearchBike.isEnabled = true
        } else {
            textBeaconStatus.text = "⏱️ ビーコンが検出されていません"
            btnSearchBike.isEnabled = false
        }

        btnLend.isEnabled = false
        btnReturn.isEnabled = false

        btnSearchBike.setOnClickListener {
            val input = editBikeNumber.text.toString().trim()
            val lock = bikeData[input]
            if (lock != null) {
                textLockNumber.text = "🔒 ロック番号: $lock"
                searchedBikeValid = true
            } else {
                textLockNumber.text = "🚫 該当する自転車が見つかりません"
                searchedBikeValid = false
            }
            updateLendButtonState()
        }

        btnLend.setOnClickListener {
            rentalStartTime = System.currentTimeMillis()
            startTimer()
            btnLend.isEnabled = false
            btnReturn.isEnabled = true
        }

        btnReturn.setOnClickListener {
            stopTimer()
            btnReturn.isEnabled = false
            textBeaconStatus.text = "✅ 自転車を返却しました"
        }

        startButtonStatusChecker()
    }

    private fun startButtonStatusChecker() {
        buttonCheckHandler.post(object : Runnable {
            override fun run() {
                val mainActivity = activity as? MainActivity
                val beacon = mainActivity?.latestBeacon

                if (beacon != null) {
                    val name = BeaconRegistry.resolveName(
                        beacon.id1.toString(),
                        beacon.id2.toInt(),
                        beacon.id3.toInt()
                    )
                    textBeaconStatus.text = "📍 検出: $name"
                    btnSearchBike.isEnabled = true
                    btnReturn.isEnabled = isTimerRunning
                } else {
                    textBeaconStatus.text = "⏱️ ビーコンが検出されていません"
                    btnSearchBike.isEnabled = false
                    btnReturn.isEnabled = false
                }

                updateLendButtonState()
                buttonCheckHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun updateLendButtonState() {
        val mainActivity = activity as? MainActivity
        val beaconDetected = mainActivity?.latestBeacon != null
        btnLend.isEnabled = searchedBikeValid && beaconDetected && !isTimerRunning
    }

    private fun startTimer() {
        isTimerRunning = true
        timerRunnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - rentalStartTime
                val hours = (elapsedMillis / (1000 * 60 * 60))
                val minutes = (elapsedMillis / (1000 * 60)) % 60
                val seconds = (elapsedMillis / 1000) % 60

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

    override fun onDestroyView() {
        super.onDestroyView()
        buttonCheckHandler.removeCallbacksAndMessages(null)
        timerHandler.removeCallbacksAndMessages(null)
    }
}
