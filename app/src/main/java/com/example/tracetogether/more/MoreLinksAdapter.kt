package com.example.tracetogether.more

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracetogether.R

class MoreLinksAdapter(private val onItemTapped: (MoreLink) -> Unit) :
        ListAdapter<MoreLink, MoreLinksAdapter.MoreViewHolder>(MoreLinksDiffCallback) {

    class MoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_title)

        fun bind(moreLink: MoreLink) {
            title.text = moreLink.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_more_item, parent, false)
        return MoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoreViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemTapped(item) }
    }
}

object MoreLinksDiffCallback : DiffUtil.ItemCallback<MoreLink>() {
    override fun areItemsTheSame(oldItem: MoreLink, newItem: MoreLink): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MoreLink, newItem: MoreLink): Boolean {
        return oldItem.title == newItem.title
    }
}
