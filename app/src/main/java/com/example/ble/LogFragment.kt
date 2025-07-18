package com.example.ble

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LogFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BeaconLogAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerLog)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = BeaconLogAdapter(LogStore.logs)
        recyclerView.adapter = adapter
    }

    fun refreshLogs() {
        adapter.notifyDataSetChanged()
    }

    // ビーコンログの1件分のデータ構造
    data class BeaconLog(
        val time: String,
        val name: String,
        val uuid: String,
        val major: Int,
        val minor: Int,
        val rssi: Int
    )

    // ビーコンログの保存場所（共通で使える）
    object LogStore {
        val logs = mutableListOf<BeaconLog>()

        fun add(log: BeaconLog) {
            logs.add(0, log) // 最新が上に来るように
        }
    }

    // 表示用のRecyclerViewアダプター
    inner class BeaconLogAdapter(private val data: List<BeaconLog>) :
        RecyclerView.Adapter<BeaconLogAdapter.LogViewHolder>() {

        inner class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textLine: TextView = view.findViewById(R.id.textLogLine)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_beacon_log, parent, false)
            return LogViewHolder(view)
        }

        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            val log = data[position]
            holder.textLine.text = "${log.time}  ${log.name}  ${log.uuid.take(8)}...  (${log.major},${log.minor})  RSSI:${log.rssi}"
        }

        override fun getItemCount(): Int = data.size
    }
}
