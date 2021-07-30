package com.example.tracetogether.widgets

import android.view.View
import android.widget.RemoteViews
import com.example.tracetogether.R
import com.example.tracetogether.stats.Trend

object WidgetHelper {
    fun updateTrendImage(views: RemoteViews, viewId: Int, trend: Trend, blue: Boolean = true) {
        when (trend) {
            Trend.TRENDING_UP -> {
                views.setViewVisibility(viewId, View.VISIBLE)
                views.setImageViewResource(viewId, if (blue) R.drawable.ic_trending_up else R.drawable.ic_trending_up_grey)
            }
            Trend.TRENDING_DOWN -> {
                views.setViewVisibility(viewId, View.VISIBLE)
                views.setImageViewResource(viewId, if (blue) R.drawable.ic_trending_down else R.drawable.ic_trending_down_grey)
            }
            Trend.TRENDING_NONE -> {
                views.setViewVisibility(viewId, View.GONE)
            }
        }
    }
}