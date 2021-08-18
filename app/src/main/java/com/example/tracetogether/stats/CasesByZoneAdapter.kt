package com.example.tracetogether.stats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracetogether.R
import com.example.tracetogether.network.ZoneStats

class CasesByZoneAdapter :
    ListAdapter<ZoneStats, CasesByZoneAdapter.CasesByZoneViewHolder>(CasesByZoneDiffCallback) {
    class CasesByZoneViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.tv_zone_name)
        private val activeCases: TextView = itemView.findViewById(R.id.tv_zone_active_cases_count)

        fun bind(zoneStats: ZoneStats) {
            title.text = zoneStats.zone
            activeCases.text = StatsFormatter.formatWithCommas(zoneStats.activeCases)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CasesByZoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_stats_cases_by_zone_item, parent, false)
        return CasesByZoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: CasesByZoneViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object CasesByZoneDiffCallback : DiffUtil.ItemCallback<ZoneStats>() {
    override fun areItemsTheSame(oldItem: ZoneStats, newItem: ZoneStats): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ZoneStats, newItem: ZoneStats): Boolean {
        return oldItem.zone == newItem.zone
    }
}
