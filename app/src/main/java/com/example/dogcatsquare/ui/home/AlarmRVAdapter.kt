package com.example.dogcatsquare.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.data.model.home.Alarm
import com.example.dogcatsquare.databinding.ItemAlarmBinding

class AlarmRVAdapter(
    private val alarmList: List<Alarm>,
    private val onItemClick: (Long) -> Unit
) : RecyclerView.Adapter<AlarmRVAdapter.AlarmAdapterViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmAdapterViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmAdapterViewHolder(binding)
    }

    override fun getItemCount(): Int = alarmList.size

    override fun onBindViewHolder(holder: AlarmAdapterViewHolder, position: Int) {
        val alarm = alarmList[position]
        holder.bind(alarm)
    }

    inner class AlarmAdapterViewHolder(val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm : Alarm) {
            binding.alarmNameTv.text = alarm.name
            binding.alarmContentTv.text = alarm.content
            binding.alarmDateTv.text = alarm.date

            binding.root.setOnClickListener {
                onItemClick(alarm.id) // 알림 클릭 시 해당 알림 ID를 Fragment로 전달
                // 클릭 후 읽음 처리 상태를 UI에 반영하는 로직은 Fragment에서 API 성공 후 처리할 수 있습니다.
            }
        }
    }
}