package com.example.dogcatsquare.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.data.model.home.Alarm
import com.example.dogcatsquare.databinding.ItemAlarmBinding

class AlarmRVAdapter(private val alarmList: List<Alarm>) : RecyclerView.Adapter<AlarmRVAdapter.AlarmAdapterViewHolder>()  {
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
        }
    }
}