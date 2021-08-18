package com.example.tracetogether.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracetogether.R
import com.example.tracetogether.network.HomeStats
import com.google.android.material.card.MaterialCardView

class HomeStatsAdapter(
        private val onItemTapped: (HomeStats) -> Unit,
        private var tintColor: Int
) :
        ListAdapter<HomeStats, HomeStatsAdapter.HomeStatsViewHolder>(HomeStatsDiffCallback) {

    class HomeStatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_home_stats_title)
        private val value: TextView = itemView.findViewById(R.id.tv_home_stats_value)

        fun bind(homeStats: HomeStats) {
            title.text = homeStats.title
            value.text = homeStats.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeStatsViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_home_stats_item, parent, false)

        view.findViewById<TextView>(R.id.tv_home_stats_title).setTextColor(tintColor)
        view.findViewById<TextView>(R.id.tv_home_stats_value).setTextColor(tintColor)
        (view as? MaterialCardView)?.setStrokeColor(tintColor)

        return HomeStatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeStatsViewHolder, position: Int) {
        val item = getItem(holder.absoluteAdapterPosition)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemTapped(item) }
    }

    fun setColor(color: Int) {
        this.tintColor = color
    }
}

object HomeStatsDiffCallback : DiffUtil.ItemCallback<HomeStats>() {
    override fun areItemsTheSame(oldItem: HomeStats, newItem: HomeStats): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: HomeStats, newItem: HomeStats): Boolean {
        return oldItem.title == newItem.title && oldItem.value == newItem.value
    }
}
